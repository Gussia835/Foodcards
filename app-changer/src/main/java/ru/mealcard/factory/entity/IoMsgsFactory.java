package ru.mealcard.factory.entity;

import jakarta.enterprise.context.ApplicationScoped;
import ru.mealcard.dto.entity.IoLogDTO;
import ru.mealcard.models.AppChangerIoMsgsEntity;

import java.time.LocalDateTime;

@ApplicationScoped
public class IoMsgsFactory {
    public AppChangerIoMsgsEntity buildIoMsg(IoLogDTO logDto) {
        return AppChangerIoMsgsEntity.builder()
                .transId(logDto.getTransId())
                .protocolType(logDto.getProtocolType())
                .msgType(logDto.getMsgType())
                .dir(logDto.getDir())
                .msg(logDto.getMsg())
                .insTs(LocalDateTime.now())
                .nodeId(logDto.getNodeId())
                .build();
    }
}
