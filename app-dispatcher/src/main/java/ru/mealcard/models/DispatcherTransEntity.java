package ru.mealcard.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "APP_DISPATCHER_TRANS", schema = "ACC_APP_DISPATCHER")
public class DispatcherTrans extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    private UUID id;

    @Column(name = "PROTOCOL_TYPE")
    private String protocolType;

    @Column(name = "SYSTEM_ID")
    private String systemId;

    @Column(name = "req_DATA", columnDefinition = "TEXT")
    private String reqData;

    @Column(name = "resp_DATA", columnDefinition = "TEXT")
    private String respData;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "RESP_CODE")
    private String respCode;

    @Column(name = "RESP_DESC")
    private String respDesc;

    @Column(name = "INS_TS")
    private LocalDateTime insTs;

    @Column(name = "UPD_TS")
    private LocalDateTime updTs;
}
