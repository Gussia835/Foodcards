package ru.mealcard.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "account_ref_database_info", schema = "ACC_APP_DISPATCHER")
public class AccountRefDbInfo extends PanacheEntityBase {
    @Id
    @Column(name = "ID")
    private UUID id;

    @Column(name = "ACCOUNT")
    private String account;

    @Column(name = "JDBC_URL")
    private String jdbcUrl;

    @Column(name = "JDBC_TYPE")
    private String jdbcType;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "INS_TS")
    private LocalDateTime insTs;

    @Column(name = "UPD_TS")
    private LocalDateTime updTs;
}
