package ru.mealcard.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name="APP_DISPATCHER_PERMISSION", schema = "ACC_APP_DISPATCHER")
public class DispatcherPermission extends PanacheEntityBase {
    @Id
    @Column(name = "ID")
    private UUID id;

    @Column(name = "login")
    private String login;

    @Column(name = "pass_bcrypt")
    private String passBcrypt;

    @Column(name = "permission")
    private Integer permission;
}
