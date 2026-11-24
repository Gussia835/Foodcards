CREATE INDEX idx_trans_status_updts ON acc_app_changer.app_changer_trans (status, upd_ts);

CREATE INDEX idx_trans_request_id ON acc_app_changer.app_changer_trans (request_id);