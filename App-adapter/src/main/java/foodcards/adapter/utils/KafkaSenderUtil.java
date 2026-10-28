package foodcards.adapter.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import foodcards.adapter.dto.KafkaReceiveDTO;
import foodcards.adapter.dto.KafkaSendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSenderUtil {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public String toJson(KafkaSendDTO payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Ошибка сериализации JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public KafkaReceiveDTO getReceiveFromJson(String jsonMessage) {
        try {
            return objectMapper.readValue(jsonMessage, KafkaReceiveDTO.class);
        } catch (Exception e) {
            log.error("Ошибка чтения jsonMessage как dto");
            throw new RuntimeException("json read failed", e);
        }
    }

    public void send(String topic, String requestId, String jsonMessage) {
        kafkaTemplate.send(topic, requestId, jsonMessage);
        log.info("Отправлено в kafka: {} в топик {}", requestId, topic);
    }
}