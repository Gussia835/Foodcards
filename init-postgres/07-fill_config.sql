INSERT INTO acc_app_changer.app_changer_config (
    system_id, event_type, entity_type, protocol_type, protocol_in, protocol_out, status_in, status_out
)
SELECT
    'GRU' AS system_id,

    CASE WHEN i % 2 = 0 THEN 'BALANCE' ELSE 'FOOD' END AS event_type,
    CASE WHEN i % 2 = 0 THEN 'ACCOUNT' ELSE 'CARD' END AS entity_type,

    'KAFKA' AS protocol_type,

    'gru.' || LOWER(CASE WHEN i % 2 = 0 THEN 'BALANCE' ELSE 'FOOD' END) || '.in' AS protocol_in,
    'gru.' || LOWER(CASE WHEN i % 2 = 0 THEN 'BALANCE' ELSE 'FOOD' END) || '.out' AS protocol_out,

    CASE WHEN i % 7 = 0 THEN 0 ELSE 1 END AS status_in,
    CASE WHEN i % 7 = 0 THEN 0 ELSE 1 END AS status_out

FROM generate_series(1, 50) AS i;

SELECT
    system_id,
    event_type,
    entity_type,
    status_in,
    COUNT(*) as count
FROM acc_app_changer.app_changer_config
GROUP BY system_id, event_type, entity_type, status_in
ORDER BY event_type, status_in;