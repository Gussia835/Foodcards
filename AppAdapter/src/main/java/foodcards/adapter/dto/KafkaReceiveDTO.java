package foodcards.adapter.dto;

import foodcards.adapter.utils.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KafkaReceiveDTO {
    private String actualTimestamp;
    private String systemId;
    private String requestId;
    private String eventType;
    private String entityType;
    private Status status;
    private List<EventReceiveDTO> events;
    private DataErrorDTO error;
}
