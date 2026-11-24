package ru.mealcard.factory.dto;

import jakarta.enterprise.context.ApplicationScoped;
import ru.mealcard.dto.entity.TransactionUpdateDTO;
import ru.mealcard.dto.internal.IncomingMessageCommand;
import ru.mealcard.dto.kafka.GruIncomingDTO;

import static ru.mealcard.utils.Constants.*;


@ApplicationScoped
public class TransDtoFactory {
    public TransactionUpdateDTO buildTransactionUpdate(Long transId, String responseData) {
        return TransactionUpdateDTO.builder()
                .transId(transId)
                .status(STATUS_DONE)
                .respData(responseData)
                .respCode(RESPONSE_CODE)
                .respDesc(null)
                .build();
    }

    public IncomingMessageCommand buildIncomingMessage(String rawJson, GruIncomingDTO incomingDto) {
        return IncomingMessageCommand.builder()
                .protocolType(KAFKA_PROTOCOL)
                .rawJson(rawJson)
                .payload(incomingDto)
                .build();
    }
}
