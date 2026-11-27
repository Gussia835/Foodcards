package ru.mealcard.service.processor;

import ru.mealcard.dto.entity.TransactionUpdateDTO;

public interface MessageProcessor {
    String getSystemId();

    TransactionUpdateDTO process(String rawJson, Long transId);
}
