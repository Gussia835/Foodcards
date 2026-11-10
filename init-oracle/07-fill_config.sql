ALTER SESSION SET CONTAINER = XEPDB1;

DECLARE
  v_event    VARCHAR2(50);
  v_entity   VARCHAR2(50);
  v_topic_in VARCHAR2(100);
  v_topic_out VARCHAR2(100);
  v_status   NUMBER(1);
BEGIN
  FOR i IN 1..50 LOOP
    IF MOD(i, 2) = 0 THEN
      v_event := 'BALANCE';
      v_entity := 'ACCOUNT';
    ELSE
      v_event := 'FOOD';
      v_entity := 'CARD';
    END IF;

    v_topic_in  := 'gru.' || LOWER(v_event) || '.in';
    v_topic_out := 'gru.' || LOWER(v_event) || '.out';

    IF MOD(i, 7) = 0 THEN
      v_status := 0;
    ELSE
      v_status := 1;
    END IF;

    INSERT INTO ACC_APP_ADAPTER.APP_ADAPTER_CONFIG (
      ID, SYSTEMID, EVENT_TYPE, ENTITY_TYPE, TOPIC_IN, STATUS_IN, TOPIC_OUT, STATUS_OUT
    ) VALUES (
      i,
      'GRU',
      v_event,
      v_entity,
      v_topic_in,
      v_status,
      v_topic_out,
      v_status
    );
  END LOOP;

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Успешно добавлено 50 записей в ACC_APP_ADAPTER.APP_ADAPTER_CONFIG');
END;
/