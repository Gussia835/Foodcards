-- Очищаем таблицу перед заполнением (чтобы не дублировать данные при повторных запусках)
TRUNCATE TABLE GRU.GRU_VISTA_TAB;

INSERT /*+ APPEND */ INTO GRU.GRU_VISTA_TAB (
    ID,
    SYSTEMACCOUNT,
    CURRENCY,
    XALFA,
    OPERATION,
    TIME_STAMP,
    POM_ID,
    UTERRARIO,
    OLDTBAL,
    NEWTBAL,
    ADD_INFO,
    FILE_ID,
    FOC_STATUS,
    FOC_TS,
    FOC_TYPE
)
SELECT
    GRU.GRU_VISTA_TAB_SEQ.NEXTVAL,
    LPAD(TRUNC(DBMS_RANDOM.VALUE(100000000, 999999999)), 9, '0'), 
    '222',                                                        
    ROUND(DBMS_RANDOM.VALUE(100, 100000), 2),                  
    CASE MOD(ROWNUM, 3) WHEN 0 THEN 'ZR' WHEN 1 THEN 'CR' ELSE 'DR' END,
    SYSDATE,
    MOD(ROWNUM, 100) + 1,                                          
    TRUNC(DBMS_RANDOM.VALUE(100000000000000000, 999999999999999999)), 
    NULL,
    NULL,
    CASE MOD(ROWNUM, 3) WHEN 0 THEN 'Обнуление счёта' WHEN 1 THEN 'Списание счёта' ELSE 'Зачисление насчёт' END,
    MOD(ROWNUM, 50) + 1,                                       
    'WAIT',
    CASE WHEN MOD(ROWNUM, 5) = 0 THEN SYSDATE - DBMS_RANDOM.VALUE(0, 30) ELSE NULL END,
    CASE WHEN MOD(ROWNUM, 5) = 0 THEN 'IN-TIME' ELSE 'IMMEDIATE' END
FROM
    (SELECT 1 FROM DUAL CONNECT BY LEVEL <= 1000) t1,
    (SELECT 1 FROM DUAL CONNECT BY LEVEL <= 1000) t2;

COMMIT;

SELECT COUNT(*) AS TOTAL, FOC_STATUS
FROM GRU.GRU_VISTA_TAB
GROUP BY FOC_STATUS;

EXIT;
