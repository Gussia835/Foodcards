CREATE TABLE acc_app_changer.app_changer_io_msgs (
    id            BIGSERIAL PRIMARY KEY,
    trans_id      BIGINT,
    protocol_type VARCHAR(64),
    msg_type      VARCHAR(64),
    dir           VARCHAR(16),
    msg           TEXT,
    ins_ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    node_id       VARCHAR(8)
);