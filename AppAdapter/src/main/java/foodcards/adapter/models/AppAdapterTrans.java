package foodcards.adapter.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppAdapterTrans {
    private Long id;
    private String systemId;
    private String requestId;
    private String eventType;
    private String data;
    private String status;
    private String respCode;
    private String respDesc;
    private LocalDateTime insTs;
    private LocalDateTime updTs;
}
