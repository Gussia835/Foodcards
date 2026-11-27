package ru.mealcard.service.processor.impl;

import io.quarkus.logging.Log;
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
import ru.mealcard.service.processor.MessageProcessor;
import ru.mealcard.service.processing.ProcedureService;
import ru.mealcard.utils.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.mealcard.utils.Constants.CODE_ERROR_PROCEDURE;
import static ru.mealcard.utils.Constants.STATUS_SUCCESS;

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

        // Формируем запрос и отправляем в dispatcher
        DispatcherRequestDTO requestDTO = dispatcherDtoFactory.buildDispatcherRequestDTO(incoming);
        DispatcherResponseDTO response;

        try {
            response = dispatcherClient.getDatasources(requestDTO);
        } catch (Exception e) {
            Log.errorf(e, "Failed to call dispatcher for request %s", incoming.getRequestId());
            return getErrorEvents(incoming.getEvents(), "Dispatcher unavailable: " + e.getMessage());
        }

        // Проверяем ответ
        if (!STATUS_SUCCESS.equals(response.getStatus())) {
            String errorMsg = response.getError() != null ? response.getError().getMessage() : "Unknown error";
            Log.infof("status unsuccessful %s", errorMsg);

            return getErrorEvents(incoming.getEvents(), "Dispatcher rejected: " + errorMsg);
        }

        // события группируем
        Map<String, String> accountUrlMap = getJdbcUrlForAccounts(response);

        // вызываем процедуры

        List<SmartEventDTO> events = new ArrayList<>();
        for (GruEventDTO event : incoming.getEvents()) {
            String jdbcUrl = accountUrlMap.get(event.getSystemAccount());
            events.add(processEvent(event, jdbcUrl));
        }

        return events;
    }

    private SmartEventDTO processEvent(GruEventDTO event, String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty() || "NULL_URL".equals(jdbcUrl)) {
            Log.infof("null jdbcUrl %s", event.getId());

            return smartDtoFactory.buildSmartEvent(event,
                    procedureDtoFactory.buildProcedureResult(
                            CODE_ERROR_PROCEDURE,
                            "JDBC URL not found for account",
                            List.of()
                    ));
        }

        // Вызываем процедуру
        try {
            ProcedureParamsDTO params = procedureDtoFactory.buildProcedureParams(event, jdbcUrl);
            ProcedureResult result = procedureService.call(params);
            return smartDtoFactory.buildSmartEvent(event, result);
        } catch (Exception e) {
            Log.infof("error of calling procedure %s", event.getId());

            return smartDtoFactory.buildSmartEvent(event,
                    procedureDtoFactory.buildProcedureResult(
                            CODE_ERROR_PROCEDURE,
                            "Database connection failed: " + e.getMessage(),
                            List.of()
                    ));
        }
    }

    private Map<String, String> getJdbcUrlForAccounts(DispatcherResponseDTO response) {
        Map<String, String> map = new HashMap<>();
        for (DatasourceGroup group : response.getData().getDatasource()) {
            for (String account : group.getAccount()) {
                map.put(account, group.getJdbcUrl());
            }
        }
        return map;
    }

    private List<SmartEventDTO> getErrorEvents(List<GruEventDTO> events, String message) {
        return events.stream()
                .map(event -> smartDtoFactory.buildSmartEvent(event,
                        procedureDtoFactory.buildProcedureResult(
                                CODE_ERROR_PROCEDURE,
                                message,
                                List.of()
                        )))
                .collect(Collectors.toList());
    }
}
