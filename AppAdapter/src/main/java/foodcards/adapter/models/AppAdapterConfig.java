package foodcards.adapter.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppAdapterConfig {
    private Long id;
    private String systemId;
    private String eventType;
    private String entityType;
    private String topicIn;
    private Integer statusIn;
    private String topicOut;
    private Integer statusOut;
    private LocalDateTime insTs;
    private LocalDateTime updTs;
}
