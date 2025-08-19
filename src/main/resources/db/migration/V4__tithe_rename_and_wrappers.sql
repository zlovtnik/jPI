-- Flyway migration to add explicit namespace-prefixed wrapper functions/procedures for tithe routines
-- This provides tithe_calculate and tithe_record as explicit names.

DELIMITER $$

-- Wrapper function
DROP FUNCTION IF EXISTS tithe_calculate$$
CREATE FUNCTION tithe_calculate(p_salary DECIMAL(13,2))
RETURNS DECIMAL(13,2)
DETERMINISTIC
BEGIN
    RETURN calculate_tithe(p_salary);
END$$

-- Wrapper procedure
DROP PROCEDURE IF EXISTS tithe_record$$
CREATE PROCEDURE tithe_record(
    IN p_member_id BIGINT,
    IN p_salary DECIMAL(13,2),
    IN p_payment_method VARCHAR(64)
)
BEGIN
    CALL record_tithe(p_member_id, p_salary, p_payment_method);
END$$

DELIMITER ;
