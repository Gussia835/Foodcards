package foodcards.adapter.service.entityProcessor;

import foodcards.adapter.builder.EntityBuilder;
import foodcards.adapter.dao.AdapterDao;
import foodcards.adapter.dto.KafkaReceiveDTO;
import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.models.AppAdapterConfigEntity;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import foodcards.adapter.service.consumer.UpdateStatusService;
import foodcards.adapter.utils.KafkaSenderUtil;
import foodcards.adapter.service.producer.TaskGenerator.GruTaskGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static foodcards.adapter.utils.Constants.*;

@RequiredArgsConstructor
@Component("gruProcessor")
@Slf4j
public class GruProcessor implements EntityProcessor {

    private final AdapterDao dao;
    private final GruTaskGeneratorService taskGeneratorService;
    private final EntityBuilder entityBuilder;
    private final KafkaSenderUtil producerService;
    private final UpdateStatusService updateService;


    @Override
    public String getEntityType() {
        return "GRU";
    }

    @Override
    @Transactional
    public boolean processOutboundBatch(AppAdapterConfigEntity config) {
        KafkaSendDTO sendDTO = taskGeneratorService.generateDTO(getEntityType(), config.getEventType());

        if (sendDTO == null) {
            return false;
        }

        String json = producerService.toJson(sendDTO);

        /*
        Формируем сущности для записи
         */
        AppAdapterTransEntity trans = entityBuilder.buildTrans(
                sendDTO.getRequestId(), config.getEventType(), json);
        trans.setStatus(STATUS_PROGRESS);

        AppAdapterIoMsgsEntity ioMsg = entityBuilder.buildIoMsg(
                null, config.getEventType(), DIR_OUT, json);

        /*
        Записываем в trans и IoMsgs таблицы информацию
         */
        dao.saveOutboundAudit(trans, ioMsg);

        /*
        Отправляем в kafka
         */
        producerService.send(config.getTopicOut(), sendDTO.getRequestId(), json);
        dao.markTransactionAsSentToKafka(sendDTO.getRequestId());

        log.info("GRU: Успешно отправлен батч, requestId={}", sendDTO.getRequestId());
        return true;
    }

    @Override
    @Transactional
    public void processInboundBatch(List<String> jsonMessages) {
        for (String jsonMessage : jsonMessages) {
            KafkaReceiveDTO receiveDTO = producerService.getReceiveFromJson(jsonMessage);
            String requestId = receiveDTO.getRequestId();

            AppAdapterTransEntity trans = dao.findTransByRequestId(requestId);
            if (trans == null) {
                log.warn("Неизвестный requestId: {}. Игнорируем", requestId);
                continue;
            }

            AppAdapterIoMsgsEntity ioMsg = entityBuilder.buildIoMsg(trans.getId(), trans.getEventType(), DIR_IN, jsonMessage);
            dao.saveInboundAudit(ioMsg);

            if (STATUS_ERROR.equals(receiveDTO.getStatus()) || receiveDTO.getEvents() == null) {
                updateService.processErrorEvents(receiveDTO.getEvents());
            } else {
                updateService.processSuccessEvents(receiveDTO.getEvents());
            }

            dao.markTransactionAsProcessed(requestId);
            log.info("Статус транзакции обновлен на PROCESSED. requestId: {}", requestId);
        }
    }

}
