package foodcards.adapter.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAdapterIoMsgs {
    private Long id;
    private Long transId;
    private String msgType;
    private String dir;
    private String msg;
    private LocalDateTime insTs;
    private String nodeId;
}
