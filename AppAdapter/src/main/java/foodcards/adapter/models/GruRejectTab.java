package foodcards.adapter.models;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GruRejectTab {
    private Long id;
    private String systemAccount;
    private Long vistaTabId;
    private Long uterrario;
    private BigDecimal oldTbal;
    private BigDecimal newTbal;
    private LocalDateTime frontTimestamp;
    private String frontStatus;
    private String rejectDesc;
    private String checkStatus;
    private String checkDesc;
    private String checkUser;
    private LocalDateTime checkTimestamp;
    private String svfeLoadId;
}
