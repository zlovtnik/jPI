-- Flyway migration to create tithe calculation function and record_tithe procedure
-- This migration runs against the datasource configured in application.yml (no USE statement).

/* flyway:delimiter=$$ */

-- 1) Function: calculate_tithe
DROP FUNCTION IF EXISTS calculate_tithe$$
CREATE FUNCTION calculate_tithe(p_salary DECIMAL(13,2))
RETURNS DECIMAL(13,2)
DETERMINISTIC
BEGIN
    IF p_salary IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN ROUND(p_salary * 0.30, 2);
END$$

-- 2) Procedure: record_tithe
DROP PROCEDURE IF EXISTS record_tithe$$
CREATE PROCEDURE record_tithe(
    IN p_member_id BIGINT,
    IN p_salary DECIMAL(13,2),
    IN p_payment_method VARCHAR(64)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_tithe DECIMAL(13,2);
    DECLARE v_method VARCHAR(64);

    -- Ensure member exists (adjust column/type as needed)
    SELECT COUNT(1) INTO v_exists FROM members WHERE id = p_member_id;
    IF v_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Member not found';
    END IF;

    -- Calculate the tithe
    SET v_tithe = calculate_tithe(p_salary);

    IF p_payment_method IS NULL OR p_payment_method = '' THEN
        SET v_method = 'AUTOMATED';
    ELSE
        SET v_method = p_payment_method;
    END IF;

    -- Insert donation record
    INSERT INTO donations (member_id, amount, donation_type, donation_date, payment_method, tax_deductible)
    VALUES (p_member_id, v_tithe, 'TITHE', CURRENT_DATE(), v_method, TRUE);

    -- Return the inserted id and amount
    SELECT LAST_INSERT_ID() AS donation_id, v_tithe AS tithe_amount;
END$$
