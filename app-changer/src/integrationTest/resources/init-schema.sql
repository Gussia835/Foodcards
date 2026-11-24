CREATE SCHEMA IF NOT EXISTS acc_app_changer;

CREATE OR REPLACE FUNCTION acc_app_changer.sp_process_balance(
    p_system_account VARCHAR,
    p_operation      VARCHAR,
    p_amount         NUMERIC,
    p_currency       VARCHAR
) RETURNS TABLE(
    out_account   VARCHAR,
    out_old_tbal  VARCHAR,
    out_new_tbal  VARCHAR,
    out_operation VARCHAR,
    out_code      INT,
    out_message   VARCHAR
) AS $$
DECLARE
    v_curbal NUMERIC := 1000.00; -- Начальный баланс для теста
BEGIN
    out_account   := p_system_account;
    out_operation := p_operation;
    out_code      := 0;
    out_message   := NULL;
    out_old_tbal  := v_curbal::VARCHAR;

    CASE p_operation
        WHEN 'CR' THEN out_new_tbal := (v_curbal + p_amount)::VARCHAR;
        WHEN 'DR' THEN out_new_tbal := (v_curbal - p_amount)::VARCHAR;
        WHEN 'ZR' THEN out_new_tbal := '0';
        ELSE
            out_new_tbal := v_curbal::VARCHAR;
            out_code := -2;
            out_message := 'Unknown operation: ' || COALESCE(p_operation, 'NULL');
    END CASE;

    RETURN NEXT;
EXCEPTION
    WHEN OTHERS THEN
        out_code := -1;
        out_message := 'Error: ' || SQLERRM;
        RETURN NEXT;
END;
$$ LANGUAGE plpgsql;