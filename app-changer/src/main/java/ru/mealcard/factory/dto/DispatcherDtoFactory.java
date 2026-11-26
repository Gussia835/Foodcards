package ru.mealcard.factory.dto;

import ru.mealcard.dto.dispatcher.request.DispatcherRequestDTO;
import ru.mealcard.dto.dispatcher.request.RequestData;
import ru.mealcard.dto.kafka.GruEventDTO;
import ru.mealcard.dto.kafka.GruIncomingDTO;

import java.util.List;
import java.util.UUID;

public class DispatcherDtoFactory {

    public DispatcherRequestDTO buildDispatcherRequestDTO(GruIncomingDTO incoming) {
        List<String> accounts = incoming.getEvents().stream()
                .map(GruEventDTO::getSystemAccount)
                .distinct()
                .toList();

        RequestData requestData = new RequestData();
        requestData.setSystemId(incoming.getSystemId());
        requestData.setRequestId(incoming.getRequestId());
        requestData.setAccounts(accounts);

        // Формируем DispatcherRequestDTO
        DispatcherRequestDTO requestDTO = new DispatcherRequestDTO();
        requestDTO.setData(requestData);

        return requestDTO;

    }
}
