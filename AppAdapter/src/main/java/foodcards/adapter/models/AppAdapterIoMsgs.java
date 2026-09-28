package foodcards.adapter.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppAdapterIoMsgs {
    private Long id;
    private Long transId;
    private String msgType;
    private String dir;
    private String msg;
    private LocalDateTime insTs;
    private String nodeId;
}
