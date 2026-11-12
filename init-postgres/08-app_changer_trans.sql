CREATE TABLE acc_app_changer.app_changer_trans (
    id            BIGSERIAL PRIMARY KEY,
    protocol_type VARCHAR(256),
    system_id     VARCHAR(256),
    request_id    VARCHAR(256),
    event_type    VARCHAR(256),
    req_data      TEXT,
    resp_data     TEXT,
    status        VARCHAR(256) DEFAULT 'NEW',
    resp_code     VARCHAR(16),
    resp_desc     VARCHAR(256),
    ins_ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    upd_ts        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);