package ru.mealcard.factory.entity;

import jakarta.enterprise.context.ApplicationScoped;
import ru.mealcard.dto.internal.IncomingMessageCommand;
import ru.mealcard.dto.kafka.GruIncomingDTO;
import ru.mealcard.models.AppChangerTransEntity;

import java.time.LocalDateTime;

import static ru.mealcard.utils.Constants.STATUS_NEW;

@ApplicationScoped
public class TransEntityFactory {
    public AppChangerTransEntity buildTrans(IncomingMessageCommand command) {
        GruIncomingDTO dto = command.getPayload();

        return AppChangerTransEntity.builder()
                .protocolType(command.getProtocolType())
                .systemId(dto.getSystemId())
                .requestId(dto.getRequestId())
                .eventType(dto.getEventType())
                .reqData(command.getRawJson())
                .respData(null)
                .status(STATUS_NEW)
                .respCode(null)
                .respDesc(null)
                .insTs(LocalDateTime.now())
                .updTs(LocalDateTime.now())
                .build();
    }
}
