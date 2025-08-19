-- Flyway migration to add record_tithe_from_member helper which reads salary from members.salary
-- Only add this if your members table has a salary column.

-- Guard: create only if salary column exists will be handled by DBA; migration assumes column exists.

DELIMITER $$

DROP PROCEDURE IF EXISTS record_tithe_from_member$$
CREATE PROCEDURE record_tithe_from_member(
    IN p_member_id BIGINT,
    IN p_payment_method VARCHAR(64)
)
BEGIN
    DECLARE v_salary DECIMAL(13,2);

    SELECT salary INTO v_salary FROM members WHERE id = p_member_id;
    IF v_salary IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Member salary not found';
    END IF;

    CALL record_tithe(p_member_id, v_salary, p_payment_method);
END$$

DELIMITER ;
