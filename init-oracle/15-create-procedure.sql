ALTER SESSION SET CONTAINER = XEPDB1;

CREATE OR REPLACE TYPE GRU.BALANCE_RECORD AS OBJECT (
  system_account VARCHAR2(255),
  operation      VARCHAR2(10),
  amount         NUMBER,
  currency       VARCHAR2(10)
);
/

CREATE OR REPLACE TYPE GRU.T_BALANCE_TABLE AS TABLE OF BALANCE_RECORD;
/

CREATE OR REPLACE PROCEDURE GRU.SP_PROCESS_BALANCE (
  p_input_data  IN GRU.T_BALANCE_TABLE,
  p_out_cursor  OUT SYS_REFCURSOR,
  p_code        OUT NUMBER,
  p_message     OUT VARCHAR2
) AS
BEGIN
  p_code    := 0;
  p_message := NULL;

  OPEN p_out_cursor FOR
    SELECT
      rec.system_account AS account,
      TO_CHAR(NVL(v.curbal, 0)) AS old_tbal,
      TO_CHAR(
        CASE
          WHEN rec.operation = 'CR' THEN NVL(v.curbal, 0) + rec.amount
          WHEN rec.operation = 'DR' THEN NVL(v.curbal, 0) - rec.amount
          WHEN rec.operation = 'ZR' THEN 0
          ELSE NVL(v.curbal, 0)
        END
      ) AS new_tbal,
      rec.operation
    FROM TABLE(p_input_data) rec
    LEFT JOIN GRU.GRU_VISTA_TAB v ON rec.system_account = v.systemaccount;

EXCEPTION
  WHEN OTHERS THEN
    p_code    := -1;
    p_message := 'Error: ' || SQLERRM;

    OPEN p_out_cursor FOR
      SELECT NULL AS account, NULL AS old_tbal, NULL AS new_tbal, NULL AS operation
      FROM DUAL WHERE 1 = 0;
END;
/