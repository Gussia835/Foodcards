package ru.mealcard.processor.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mealcard.client.DispatcherClient;
import ru.mealcard.dao.AppChangerDao;
import ru.mealcard.dto.dispatcher.request.DispatcherRequestDTO;
import ru.mealcard.dto.dispatcher.response.DatasourceGroup;
import ru.mealcard.dto.dispatcher.response.DispatcherResponseDTO;
import ru.mealcard.dto.entity.TransactionUpdateDTO;
import ru.mealcard.dto.internal.ProcedureParamsDTO;
import ru.mealcard.dto.internal.ProcedureResult;
import ru.mealcard.dto.kafka.GruEventDTO;
import ru.mealcard.dto.kafka.GruIncomingDTO;
import ru.mealcard.dto.kafka.SmartEventDTO;
import ru.mealcard.factory.dto.DispatcherDtoFactory;
import ru.mealcard.factory.dto.ProcedureDtoFactory;
import ru.mealcard.factory.dto.SmartDtoFactory;
import ru.mealcard.processor.MessageProcessor;
import ru.mealcard.service.processing.ProcedureService;
import ru.mealcard.utils.Constants;
import ru.mealcard.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class GruMessageProcessor implements MessageProcessor {
    @Inject
    JsonUtil jsonUtil;

    @Inject
    DispatcherClient dispatcherClient;

    @Inject
    ProcedureService procedureService;

    @Inject
    ProcedureDtoFactory procedureDtoFactory;

    @Inject
    SmartDtoFactory smartDtoFactory;

    @Inject
    AppChangerDao dao;

    @Inject
    DispatcherDtoFactory dispatcherDtoFactory;

    @Override
    public String getSystemId() {
        return "GRU";
    }

    @Override
    public TransactionUpdateDTO process(String json, Long transId) {
        GruIncomingDTO dto = jsonUtil.fromJson(json, GruIncomingDTO.class);

        List<SmartEventDTO> events = processEvents(dto);

        TransactionUpdateDTO transactionUpdateDTO = procedureDtoFactory.buildTransUpdateForResponse(transId, dto, events);

        return transactionUpdateDTO;
    }

    private List<SmartEventDTO> processEvents(GruIncomingDTO incoming) {
        List<SmartEventDTO> events = new ArrayList<>();

        // Формируем запрос и отправляем в dispatcher
        DispatcherRequestDTO requestDTO = dispatcherDtoFactory.buildDispatcherRequestDTO(incoming);
        DispatcherResponseDTO response;
        try {
            response = dispatcherClient.getDatasources(requestDTO);
        } catch (Exception e) {
            for (GruEventDTO event : incoming.getEvents()) {
                events.add(smartDtoFactory.buildSmartEvent(event,
                                                        procedureDtoFactory.buildProcedureResult(
                                                                Constants.CODE_ERROR_PROCEDURE,
                                                                "Dispatcher unavailable: " + e.getMessage(),
                                                                List.of()
                                                        )));
            }
            return events;
        }

        // Проверяем ответ
        if (!"Success".equals(response.getStatus())) {
            String errorMsg = response.getError() != null ? response.getError().getMessage() : "Unknown error";

            // Помечаем как ошибочные
            for (GruEventDTO event : incoming.getEvents()) {
                events.add(smartDtoFactory.buildSmartEvent(event,
                                                    procedureDtoFactory.buildProcedureResult(
                                                            Constants.CODE_ERROR_PROCEDURE,
                                                            "Dispatcher rejected: " + errorMsg,
                                                            List.of()
                                                    )));
            }
            return events;
        }

        // события группируем
        Map<String, List<GruEventDTO>> eventsByJdbcUrl = new HashMap<>();

        for (DatasourceGroup group : response.getData().getDatasource()) {
            String jdbcUrl = group.getJdbcUrl();

            // все события по счетам
            List<GruEventDTO> groupEvents = incoming.getEvents().stream()
                    .filter(event -> group.getAccount().contains(event.getSystemAccount()))
                    .collect(Collectors.toList());

            eventsByJdbcUrl.put(jdbcUrl, groupEvents);
        }

        // вызываем процедуры
        for (Map.Entry<String, List<GruEventDTO>> entry : eventsByJdbcUrl.entrySet()) {
            String jdbcUrl = entry.getKey();
            List<GruEventDTO> groupEvents = entry.getValue();

            if (jdbcUrl == null || jdbcUrl.trim().isEmpty() || "NULL_URL".equals(jdbcUrl)) {

                for (GruEventDTO event : groupEvents) {
                    events.add(smartDtoFactory.buildSmartEvent(event,
                                procedureDtoFactory.buildProcedureResult(
                                        Constants.CODE_ERROR_PROCEDURE,
                                        "JDBC URL not found for account",
                                        List.of()
                                )));
                }
                continue;
            }

            // Вызываем процедуры для каждого счёта в группе
            try {
                for (GruEventDTO event : groupEvents) {
                    ProcedureParamsDTO params = procedureDtoFactory.buildProcedureParams(event, jdbcUrl);
                    ProcedureResult result = procedureService.call(params);
                    events.add(smartDtoFactory.buildSmartEvent(event, result));
                }
            } catch (Exception e) {
                for (GruEventDTO event : groupEvents) {
                    events.add(smartDtoFactory.buildSmartEvent(event,
                                                                procedureDtoFactory.buildProcedureResult(
                                                                        Constants.CODE_ERROR_PROCEDURE,
                                                                        "Database connection failed: " + e.getMessage(),
                                                                        List.of()
                                                                )));
                }
            }
        }

        return events;
    }
}
