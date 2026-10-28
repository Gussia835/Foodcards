package foodcards.adapter.service.producer.TaskGenerator;

import foodcards.adapter.dto.KafkaSendDTO;

public interface TaskGenerator {
    public KafkaSendDTO generateDTO(String entityType, String eventType);
}
