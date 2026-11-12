CREATE TABLE acc_app_changer.app_changer_config (
    id            BIGSERIAL PRIMARY KEY,
    system_id     VARCHAR(255),
    event_type    VARCHAR(255),
    entity_type   VARCHAR(255),
    protocol_type VARCHAR(255),
    protocol_in   VARCHAR(255),
    status_in     SMALLINT DEFAULT 1,
    protocol_out  VARCHAR(255),
    status_out    SMALLINT DEFAULT 1,
    ins_ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upd_ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);