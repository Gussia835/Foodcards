package foodcards.adapter.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GruVistaTab {
    private Long id;
    private String systemAccount;
    private String currency;
    private BigDecimal xalfa;
    private String operation;
    private LocalDateTime timeStamp;
    private Long pomId;
    private Long uterrario;
    private BigDecimal oldTbal;
    private BigDecimal newTbal;
    private String addInfo;
    private Long fileId;
    private String focStatus;
    private LocalDateTime focTs;
    private String focType;
    private String svfeLoadId;
}
