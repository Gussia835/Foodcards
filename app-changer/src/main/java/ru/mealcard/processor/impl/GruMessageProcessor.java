package ru.mealcard.processor.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.mealcard.dao.AppChangerDao;
import ru.mealcard.dto.entity.TransactionUpdateDTO;
import ru.mealcard.dto.internal.ProcedureParamsDTO;
import ru.mealcard.dto.internal.ProcedureResult;
import ru.mealcard.dto.internal.RoutingInfo;
import ru.mealcard.dto.kafka.GruEventDTO;
import ru.mealcard.dto.kafka.GruIncomingDTO;
import ru.mealcard.dto.kafka.SmartEventDTO;
import ru.mealcard.factory.dto.ProcedureDtoFactory;
import ru.mealcard.factory.dto.SmartDtoFactory;
import ru.mealcard.processor.MessageProcessor;
import ru.mealcard.service.processing.ProcedureService;
import ru.mealcard.controller.ControllerClient;
import ru.mealcard.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GruMessageProcessor implements MessageProcessor {
    @Inject
    JsonUtil jsonUtil;

    @Inject
    ControllerClient appController;

    @Inject
    ProcedureService procedureService;

    @Inject
    ProcedureDtoFactory procedureDtoFactory;

    @Inject
    SmartDtoFactory smartDtoFactory;

    @Inject
    AppChangerDao dao;

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

        for (GruEventDTO event : incoming.getEvents()) {
            RoutingInfo routing = appController.getRouting(event.getSystemAccount());
            ProcedureParamsDTO params = procedureDtoFactory.buildProcedureParams(event, routing);

            ProcedureResult result = procedureService.call(params);

            events.add(smartDtoFactory.buildSmartEvent(event, result));

        }

        return events;

    }
}
