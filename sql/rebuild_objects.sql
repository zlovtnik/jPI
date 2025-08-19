-- Utility SQL to rebuild indexes and analyze tables in MariaDB safely.
-- This creates a stored procedure `rebuild_schema()` which loops all base tables
-- in the current database and runs OPTIMIZE TABLE and ANALYZE TABLE on each.
-- This avoids GROUP_CONCAT/INTO OUTFILE and is safer in restricted environments.

/*
Usage:
	CALL rebuild_schema();
*/

DELIMITER $$

DROP PROCEDURE IF EXISTS rebuild_schema$$
CREATE PROCEDURE rebuild_schema()
BEGIN
	DECLARE done INT DEFAULT FALSE;
	DECLARE tbl VARCHAR(255);
	DECLARE cur CURSOR FOR
		SELECT TABLE_NAME FROM information_schema.tables
		WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE';
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	OPEN cur;
	read_loop: LOOP
		FETCH cur INTO tbl;
		IF done THEN
			LEAVE read_loop;
		END IF;

		SET @s = CONCAT('OPTIMIZE TABLE `', tbl, '`');
		PREPARE stmt FROM @s;
		EXECUTE stmt;
		DEALLOCATE PREPARE stmt;

		SET @s = CONCAT('ANALYZE TABLE `', tbl, '`');
		PREPARE stmt2 FROM @s;
		EXECUTE stmt2;
		DEALLOCATE PREPARE stmt2;
	END LOOP;

	CLOSE cur;
END$$

DELIMITER ;

-- Note: Running OPTIMIZE/ANALYZE on large tables can be I/O intensive; run during maintenance windows.
