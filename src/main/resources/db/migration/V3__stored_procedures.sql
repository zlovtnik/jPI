-- V3__stored_procedures.sql
-- Useful stored procedures for the church management system

DELIMITER //

-- 1. Member Management Procedures

-- Get complete member information with family details
DROP PROCEDURE IF EXISTS GetMemberDetails //
CREATE PROCEDURE GetMemberDetails(IN p_member_id INT)
BEGIN
    SELECT 
        m.id,
        m.first_name,
        m.last_name,
        m.email,
        m.phone,
        m.date_of_birth,
        m.gender,
        m.marital_status,
        m.membership_date,
        m.baptism_date,
        m.occupation,
        m.emergency_contact,
        m.notes,
        f.family_name,
        f.address,
        f.city,
        f.state,
        f.zip_code,
        f.phone as family_phone
    FROM members m
    LEFT JOIN families f ON m.family_id = f.id
    WHERE m.id = p_member_id;
END //

-- Add new member to existing family or create new family
DROP PROCEDURE IF EXISTS AddMemberToFamily //
CREATE PROCEDURE AddMemberToFamily(
    IN p_first_name VARCHAR(100),
    IN p_last_name VARCHAR(100),
    IN p_email VARCHAR(255),
    IN p_phone VARCHAR(20),
    IN p_date_of_birth DATE,
    IN p_gender VARCHAR(20),
    IN p_marital_status VARCHAR(50),
    IN p_family_name VARCHAR(255),
    IN p_family_address TEXT,
    IN p_family_city VARCHAR(100),
    IN p_family_state VARCHAR(50),
    IN p_family_zip VARCHAR(20),
    IN p_family_phone VARCHAR(20)
)
BEGIN
    DECLARE v_family_id INT DEFAULT NULL;
    DECLARE v_member_id INT;
    
    START TRANSACTION;
    
    -- Check if family exists
    SELECT id INTO v_family_id 
    FROM families 
    WHERE family_name = p_family_name 
    LIMIT 1;
    
    -- Create family if it doesn't exist
    IF v_family_id IS NULL THEN
        INSERT INTO families (family_name, address, city, state, zip_code, phone)
        VALUES (p_family_name, p_family_address, p_family_city, p_family_state, p_family_zip, p_family_phone);
        SET v_family_id = LAST_INSERT_ID();
    END IF;
    
    -- Add member to family
    INSERT INTO members (first_name, last_name, email, phone, date_of_birth, gender, marital_status, membership_date, family_id)
    VALUES (p_first_name, p_last_name, p_email, p_phone, p_date_of_birth, p_gender, p_marital_status, CURDATE(), v_family_id);
    
    SET v_member_id = LAST_INSERT_ID();
    
    COMMIT;
    
    -- Return the new member details
    CALL GetMemberDetails(v_member_id);
END //

-- 2. Donation Management Procedures

-- Get donation summary for a member
DROP PROCEDURE IF EXISTS GetMemberDonationSummary //
CREATE PROCEDURE GetMemberDonationSummary(
    IN p_member_id INT,
    IN p_year INT
)
BEGIN
    SELECT 
        donation_type,
        COUNT(*) as donation_count,
        SUM(amount) as total_amount,
        AVG(amount) as average_amount,
        MIN(donation_date) as first_donation,
        MAX(donation_date) as last_donation
    FROM donations 
    WHERE member_id = p_member_id 
    AND YEAR(donation_date) = p_year
    AND tax_deductible = TRUE
    GROUP BY donation_type
    ORDER BY total_amount DESC;
END //

-- Generate annual giving statement
DROP PROCEDURE IF EXISTS GenerateGivingStatement //
CREATE PROCEDURE GenerateGivingStatement(
    IN p_member_id INT,
    IN p_year INT
)
BEGIN
    SELECT 
        m.first_name,
        m.last_name,
        m.email,
        f.family_name,
        f.address,
        f.city,
        f.state,
        f.zip_code,
        SUM(d.amount) as total_tax_deductible,
        COUNT(d.id) as total_donations,
        MIN(d.donation_date) as first_donation_date,
        MAX(d.donation_date) as last_donation_date
    FROM members m
    LEFT JOIN families f ON m.family_id = f.id
    LEFT JOIN donations d ON m.id = d.member_id 
        AND YEAR(d.donation_date) = p_year 
        AND d.tax_deductible = TRUE
    WHERE m.id = p_member_id
    GROUP BY
      m.id, m.first_name, m.last_name, m.email,
      f.family_name, f.address, f.city, f.state, f.zip_code;
END //

-- Get top donors for a period
DROP PROCEDURE IF EXISTS GetTopDonors //
CREATE PROCEDURE GetTopDonors(
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_limit INT
)
BEGIN
    SELECT 
        m.id,
        CONCAT(m.first_name, ' ', m.last_name) as full_name,
        m.email,
        COUNT(d.id) as donation_count,
        SUM(d.amount) as total_donated,
        AVG(d.amount) as average_donation
    FROM members m
    INNER JOIN donations d ON m.id = d.member_id
    WHERE d.donation_date BETWEEN p_start_date AND p_end_date
    GROUP BY m.id
    ORDER BY total_donated DESC
    LIMIT p_limit;
END //

-- 3. Event Management Procedures

-- Register member for event with capacity check
DROP PROCEDURE IF EXISTS RegisterMemberForEvent //
CREATE PROCEDURE RegisterMemberForEvent(
    IN p_event_id INT,
    IN p_member_id INT,
    IN p_notes TEXT
)
BEGIN
    DECLARE v_current_count INT DEFAULT 0;
    DECLARE v_max_capacity INT DEFAULT NULL;
    DECLARE v_registration_deadline TIMESTAMP DEFAULT NULL;
    DECLARE v_event_exists INT DEFAULT 0;
    DECLARE v_already_registered INT DEFAULT 0;
    
    -- Check if event exists and get details
    SELECT COUNT(*), max_capacity, registration_deadline
    INTO v_event_exists, v_max_capacity, v_registration_deadline
    FROM events 
    WHERE id = p_event_id;
    
    IF v_event_exists = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Event not found';
    END IF;
    
    -- Check registration deadline
    IF v_registration_deadline IS NOT NULL AND NOW() > v_registration_deadline THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Registration deadline has passed';
    END IF;
    
    -- Check if already registered
    SELECT COUNT(*) INTO v_already_registered
    FROM event_registrations
    WHERE event_id = p_event_id AND member_id = p_member_id;
    
    IF v_already_registered > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Member already registered for this event';
    END IF;
    
    -- Check capacity
    IF v_max_capacity IS NOT NULL THEN
        SELECT COUNT(*) INTO v_current_count
        FROM event_registrations
        WHERE event_id = p_event_id;
        
        IF v_current_count >= v_max_capacity THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Event is at full capacity';
        END IF;
    END IF;
    
    -- Register the member
    INSERT INTO event_registrations (event_id, member_id, notes)
    VALUES (p_event_id, p_member_id, p_notes);
    
    SELECT 'Registration successful' as result, LAST_INSERT_ID() as registration_id;
END //

-- Get event attendance statistics
DROP PROCEDURE IF EXISTS GetEventAttendanceStats //
CREATE PROCEDURE GetEventAttendanceStats(IN p_event_id INT)
BEGIN
    SELECT 
        e.name as event_name,
        e.start_date,
        e.end_date,
        COUNT(er.id) as total_registered,
        COUNT(CASE WHEN a.present = TRUE THEN 1 END) as total_attended,
        COUNT(CASE WHEN a.present = FALSE THEN 1 END) as total_absent,
        ROUND(
          (COUNT(CASE WHEN a.present = TRUE THEN 1 END) / NULLIF(COUNT(er.id), 0)) * 100,
          2
        ) as attendance_percentage
    FROM events e
    LEFT JOIN event_registrations er ON e.id = er.event_id
    LEFT JOIN attendance a ON e.id = a.event_id AND er.member_id = a.member_id
    WHERE e.id = p_event_id
    GROUP BY e.id, e.name, e.start_date, e.end_date;
END //

-- 4. Group Management Procedures

-- Add member to group with capacity check
DROP PROCEDURE IF EXISTS AddMemberToGroup //
CREATE PROCEDURE AddMemberToGroup(
    IN p_group_id INT,
    IN p_member_id INT
)
BEGIN
    DECLARE v_current_count INT DEFAULT 0;
    DECLARE v_max_members INT DEFAULT NULL;
    DECLARE v_group_active BOOLEAN DEFAULT FALSE;
    DECLARE v_already_member INT DEFAULT 0;
    
    -- Check if group exists and is active
    SELECT is_active, max_members INTO v_group_active, v_max_members
    FROM groups 
    WHERE id = p_group_id;
    
    IF v_group_active = FALSE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Group is not active';
    END IF;
    
    -- Check if already a member
    SELECT COUNT(*) INTO v_already_member
    FROM group_members
    WHERE group_id = p_group_id AND member_id = p_member_id;
    
    IF v_already_member > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Member is already in this group';
    END IF;
    
    -- Check capacity
    IF v_max_members IS NOT NULL THEN
        SELECT COUNT(*) INTO v_current_count
        FROM group_members
        WHERE group_id = p_group_id;
        
        IF v_current_count >= v_max_members THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Group is at full capacity';
        END IF;
    END IF;
    
    -- Add member to group
    INSERT INTO group_members (group_id, member_id)
    VALUES (p_group_id, p_member_id);
    
    SELECT 'Member added to group successfully' as result;
END //

-- Get group membership details
DROP PROCEDURE IF EXISTS GetGroupMembership //
CREATE PROCEDURE GetGroupMembership(IN p_group_id INT)
BEGIN
    SELECT 
        g.name as group_name,
        g.description,
        g.meeting_day,
        g.meeting_time,
        g.location,
        CONCAT(l.first_name, ' ', l.last_name) as leader_name,
        g.max_members,
        COUNT(gm.member_id) as current_members,
        CASE 
            WHEN g.max_members IS NULL THEN 'Unlimited'
            ELSE CAST((g.max_members - COUNT(gm.member_id)) AS CHAR)
        END as available_spots
    FROM groups g
    LEFT JOIN members l ON g.leader_id = l.id
    LEFT JOIN group_members gm ON g.id = gm.group_id
    WHERE g.id = p_group_id
    GROUP BY g.id;
    
    -- Also return member list
    SELECT 
        m.id,
        CONCAT(m.first_name, ' ', m.last_name) as full_name,
        m.email,
        m.phone,
        gm.joined_date
    FROM group_members gm
    INNER JOIN members m ON gm.member_id = m.id
    WHERE gm.group_id = p_group_id
    ORDER BY gm.joined_date;
END //

-- 5. Reporting Procedures

-- Generate monthly activity report
DROP PROCEDURE IF EXISTS GenerateMonthlyReport //
CREATE PROCEDURE GenerateMonthlyReport(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    DECLARE v_start_date DATE;
    DECLARE v_end_date DATE;
    
    SET v_start_date = DATE(CONCAT(p_year, '-', LPAD(p_month, 2, '0'), '-01'));
    SET v_end_date = LAST_DAY(v_start_date);
    
    -- New members this month
    SELECT COUNT(*) as new_members_count
    FROM members 
    WHERE membership_date BETWEEN v_start_date AND v_end_date;
    
    -- Donations this month
    SELECT 
        donation_type,
        COUNT(*) as donation_count,
        SUM(amount) as total_amount
    FROM donations 
    WHERE donation_date BETWEEN v_start_date AND v_end_date
    GROUP BY donation_type;
    
    -- Events this month
    SELECT COUNT(*) as events_count
    FROM events 
    WHERE DATE(start_date) BETWEEN v_start_date AND v_end_date;
    
    -- Active volunteers
    SELECT COUNT(*) as active_volunteers_count
    FROM volunteers 
    WHERE active = TRUE 
    AND (end_date IS NULL OR end_date > v_end_date);
END //

-- Search members by various criteria
DROP PROCEDURE IF EXISTS SearchMembers //
CREATE PROCEDURE SearchMembers(
    IN p_search_term VARCHAR(255),
    IN p_search_type ENUM('name', 'email', 'phone', 'all')
)
BEGIN
    IF p_search_type = 'name' THEN
        SELECT id, first_name, last_name, email, phone, membership_date
        FROM members 
        WHERE CONCAT(first_name, ' ', last_name) LIKE CONCAT('%', p_search_term, '%')
        ORDER BY last_name, first_name;
    ELSEIF p_search_type = 'email' THEN
        SELECT id, first_name, last_name, email, phone, membership_date
        FROM members 
        WHERE email LIKE CONCAT('%', p_search_term, '%')
        ORDER BY last_name, first_name;
    ELSEIF p_search_type = 'phone' THEN
        SELECT id, first_name, last_name, email, phone, membership_date
        FROM members 
        WHERE phone LIKE CONCAT('%', p_search_term, '%')
        ORDER BY last_name, first_name;
    ELSE -- 'all'
        SELECT id, first_name, last_name, email, phone, membership_date
        FROM members 
        WHERE CONCAT(first_name, ' ', last_name) LIKE CONCAT('%', p_search_term, '%')
           OR email LIKE CONCAT('%', p_search_term, '%')
           OR phone LIKE CONCAT('%', p_search_term, '%')
        ORDER BY last_name, first_name;
    END IF;
END //

DELIMITER ;

-- Add some helpful views as well
CREATE OR REPLACE VIEW active_members AS
SELECT 
    m.*,
    f.family_name,
    f.address,
    f.city,
    f.state,
    f.zip_code
FROM members m
LEFT JOIN families f ON m.family_id = f.id
WHERE m.id IS NOT NULL;

CREATE OR REPLACE VIEW current_year_donations AS
SELECT 
    m.id as member_id,
    CONCAT(m.first_name, ' ', m.last_name) as member_name,
    d.donation_type,
    d.amount,
    d.donation_date,
    d.payment_method
FROM donations d
INNER JOIN members m ON d.member_id = m.id
WHERE YEAR(d.donation_date) = YEAR(CURDATE());

CREATE OR REPLACE VIEW upcoming_events AS
SELECT 
    e.*,
    COUNT(er.id) as registered_count,
    CASE 
        WHEN e.max_capacity IS NULL THEN 'Unlimited'
        ELSE CAST((e.max_capacity - COUNT(er.id)) AS CHAR)
    END as available_spots
FROM events e
LEFT JOIN event_registrations er ON e.id = er.event_id
WHERE e.start_date > NOW()
GROUP BY e.id
ORDER BY e.start_date;

CREATE OR REPLACE VIEW active_volunteers AS
SELECT 
    v.*,
    CONCAT(m.first_name, ' ', m.last_name) as volunteer_name,
    m.email,
    m.phone
FROM volunteers v
INNER JOIN members m ON v.member_id = m.id
WHERE v.active = TRUE 
AND (v.end_date IS NULL OR v.end_date > CURDATE());
