package foodcards.adapter.service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.builder.EntityBuilder;
import foodcards.adapter.dao.AppAdapterDAO;
import foodcards.adapter.dto.KafkaReceiveDTO;
import foodcards.adapter.models.AppAdapterIoMsgsEntity;
import foodcards.adapter.models.AppAdapterTransEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import static foodcards.adapter.utils.Constants.DIR_IN;
import static foodcards.adapter.utils.Constants.STATUS_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProcessor {
    private final ObjectMapper objectMapper;
    private final AppAdapterDAO dao;
    private final EntityBuilder entityBuilder;
    private final UpdateStatusService updateService;

    public void processMessages(List<String> jsonMessages) throws IOException {
        for (String jsonMessage : jsonMessages) {
            process(jsonMessage);
        }
    }

    private void process(String jsonMessage) throws IOException {

        KafkaReceiveDTO receiveDTO = objectMapper.readValue(jsonMessage, KafkaReceiveDTO.class);
        String requestId = receiveDTO.getRequestId();

        AppAdapterTransEntity trans = dao.findTransByRequestId(requestId);
        if (trans == null) {
            log.warn("Неизвестный requestId: {}. Игнорируем", requestId);
            return;
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