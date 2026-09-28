package foodcards.adapter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.builder.KafkaBuilder;
import foodcards.adapter.dto.KafkaSendDTO;
import foodcards.adapter.mapper.AppAdapterIoMsgsMapper;
import foodcards.adapter.mapper.AppAdapterTransMapper;
import foodcards.adapter.mapper.GruVistaTabMapper;
import foodcards.adapter.models.AppAdapterIoMsgs;
import foodcards.adapter.models.AppAdapterTrans;
import foodcards.adapter.models.GruVistaTab;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterSenderService {

    private final GruVistaTabMapper gruVistaTabMapper;
    private final AppAdapterTransMapper transMapper;
    private final AppAdapterIoMsgsMapper ioMsgsMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaBuilder kafkaBuilder;

    private final ObjectMapper objectMapper;


    @Value("${app.kafka.batch-size}")
    private int batchSize;

    @Value("${app.kafka.topicOutBalance}")
    private String topicOutBalance;

    @Transactional
    public void processBatch() {

        log.info("Начинаем обработку пачки записей. Размер: {}", batchSize);

        /*
        Получаем записи оидающие обработки
         */
        List<GruVistaTab> waitingRecords = gruVistaTabMapper.selectWaitingRecords(batchSize);

        if (waitingRecords == null || waitingRecords.isEmpty()) {
            log.info("Записей со статусом WAIT не найдено");
            return;
        }

        /*
        * Обновляем статусы на "в процессе"
         */
        List<Long> ids = waitingRecords.stream().map(GruVistaTab::getId).collect(Collectors.toList());
        gruVistaTabMapper.updateStatusToProgress(ids);
        log.info("Статус ожидающих записей обновлен на 'В прцоессе'");

        KafkaSendDTO sendDTO = kafkaBuilder.buildKafkaSend(waitingRecords, "BALANCE", "ACCOUNT");
        String requestId = sendDTO.getRequestId();

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(sendDTO);
        } catch (Exception e) {
            log.error("Критическая ошибка сериализации JSON для requestId={}", requestId, e);
            throw new RuntimeException("Ошибка сериализации", e);
        }

        AppAdapterTrans trans = new AppAdapterTrans();
        trans.setSystemId("GRU");
        trans.setRequestId(requestId);
        trans.setEventType("BALANCE");
        trans.setData(jsonMessage);
        trans.setStatus("WAIT");

        AppAdapterIoMsgs ioMsg = new AppAdapterIoMsgs();
        ioMsg.setTransId(trans.getId());
        ioMsg.setMsgType("BALANCE");
        ioMsg.setDir("OUT");
        ioMsg.setMsg(jsonMessage);
        ioMsgsMapper.insert(ioMsg);

        log.info("Отправка в Kafka: topic={}, requestId={}", topicOutBalance, requestId);
        kafkaTemplate.send(topicOutBalance, requestId, jsonMessage);

        transMapper.updateStatus(trans.getId(), "SUCCESS");

        log.info("Пачка успешно обработана и отправлена. requestId={}", requestId);
    }
}
