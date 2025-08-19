-- MariaDB stored routines for tithe calculation and recording
-- NOTE: MariaDB / MySQL do not support Oracle-style "packages". Instead we provide
-- a related set of stored FUNCTIONS/PROCEDURES in one SQL file that act like a package.

-- Use the application's database (adjust if your DB name differs)
USE churchdb;

DELIMITER $$

-- 1) Function: calculate_tithe
-- Returns 30% of the provided salary, rounded to 2 decimal places.
DROP FUNCTION IF EXISTS `churchdb`.calculate_tithe$$
CREATE FUNCTION `churchdb`.calculate_tithe(p_salary DECIMAL(13,2))
RETURNS DECIMAL(13,2)
DETERMINISTIC
BEGIN
    IF p_salary IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN ROUND(p_salary * 0.30, 2);
END$$

-- 2) Procedure: record_tithe
-- Inserts a TITHE donation for the given member using the provided salary.
-- Parameters:
--   p_member_id    : member's id (must exist in members table)
--   p_salary       : salary amount used to calculate tithe
--   p_payment_method: optional payment method (defaults to 'AUTOMATED')
-- Returns: the new donation id and the tithe amount as a result set.
DROP PROCEDURE IF EXISTS `churchdb`.record_tithe$$
CREATE PROCEDURE `churchdb`.record_tithe(
    IN p_member_id BIGINT,
    IN p_salary DECIMAL(13,2),
    IN p_payment_method VARCHAR(64)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_tithe DECIMAL(13,2);
    DECLARE v_method VARCHAR(64);

    -- Ensure member exists (adjust column/type as needed)
    SELECT COUNT(1) INTO v_exists FROM `churchdb`.members WHERE id = p_member_id;
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
    INSERT INTO `churchdb`.donations (member_id, amount, donation_type, donation_date, payment_method, tax_deductible)
    VALUES (p_member_id, v_tithe, 'TITHE', CURRENT_DATE(), v_method, TRUE);

    -- Return the inserted id and amount
    SELECT LAST_INSERT_ID() AS donation_id, v_tithe AS tithe_amount;
END$$

DELIMITER ;

-- Usage examples:
-- SELECT calculate_tithe(5000.00);
-- CALL record_tithe(1, 5000.00, 'BANK_TRANSFER');

-- Optional helper: procedure that reads salary from a members.salary column if your schema includes it
-- Uncomment and adapt if your `members` table actually has a salary column.
--
-- DROP PROCEDURE IF EXISTS record_tithe_from_member$$
-- CREATE PROCEDURE record_tithe_from_member(
--     IN p_member_id BIGINT,
--     IN p_payment_method VARCHAR(64)
-- )
-- BEGIN
--     DECLARE v_salary DECIMAL(13,2);
--     SELECT salary INTO v_salary FROM members WHERE id = p_member_id;
--     IF v_salary IS NULL THEN
--         SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Member salary not found';
--     END IF;
--     CALL record_tithe(p_member_id, v_salary, p_payment_method);
-- END$$
-- DELIMITER ;
