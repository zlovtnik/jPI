-- Example data and procedure calls for testing
-- Run these after applying the migration files
-- Set the database context
USE church_management_system;
-- First, let's add some sample data
INSERT INTO families (family_name, address, city, state, zip_code, phone) VALUES
('Smith Family', '123 Oak Street', 'Springfield', 'IL', '62701', '217-555-0001'),
('Johnson Family', '456 Elm Avenue', 'Springfield', 'IL', '62702', '217-555-0002'),
('Williams Family', '789 Pine Road', 'Springfield', 'IL', '62703', '217-555-0003');

INSERT INTO members (first_name, last_name, email, phone, date_of_birth, gender, marital_status, membership_date, family_id) VALUES
('John', 'Smith', 'john.smith@email.com', '217-555-1001', '1980-05-15', 'Male', 'Married', '2020-01-15', 1),
('Jane', 'Smith', 'jane.smith@email.com', '217-555-1002', '1982-08-22', 'Female', 'Married', '2020-01-15', 1),
('Michael', 'Johnson', 'michael.johnson@email.com', '217-555-2001', '1975-12-03', 'Male', 'Single', '2019-06-10', 2),
('Sarah', 'Williams', 'sarah.williams@email.com', '217-555-3001', '1990-03-18', 'Female', 'Single', '2021-03-20', 3);

INSERT INTO events (name, description, start_date, end_date, location, max_capacity, registration_deadline, cost) VALUES
('Sunday Service', 'Weekly worship service', '2024-12-22 10:00:00', '2024-12-22 11:30:00', 'Main Sanctuary', 200, '2024-12-21 23:59:59', 0.00),
('Christmas Concert', 'Annual Christmas celebration', '2024-12-24 19:00:00', '2024-12-24 21:00:00', 'Main Sanctuary', 150, '2024-12-20 23:59:59', 10.00),
('Youth Retreat', 'Weekend retreat for youth', '2025-01-15 18:00:00', '2025-01-17 12:00:00', 'Camp Wilderness', 30, '2025-01-10 23:59:59', 75.00);

INSERT INTO groups (name, description, meeting_day, meeting_time, location, leader_name, max_members, active) VALUES
('Bible Study Group', 'Weekly Bible study and discussion', 'Wednesday', '7:00 PM', 'Room 101', 'Pastor Johnson', 15, TRUE),
('Youth Group', 'Activities and fellowship for teens', 'Friday', '6:30 PM', 'Youth Hall', 'Sarah Williams', 25, TRUE),
('Choir', 'Church choir practice and performances', 'Thursday', '7:30 PM', 'Music Room', 'John Smith', NULL, TRUE);

INSERT INTO donations (member_id, amount, donation_type, donation_date, payment_method, tax_deductible) VALUES
(1, 500.00, 'TITHE', '2024-01-15', 'Check', TRUE),
(1, 100.00, 'OFFERING', '2024-02-15', 'Cash', TRUE),
(2, 250.00, 'TITHE', '2024-01-20', 'Online', TRUE),
(3, 150.00, 'SPECIAL_GIFT', '2024-03-10', 'Check', TRUE),
(4, 75.00, 'OFFERING', '2024-01-25', 'Cash', TRUE);

-- Now test the procedures

-- 1. Test member management
CALL GetMemberDetails(1);

-- 2. Test adding a new member
CALL AddMemberToFamily(
    'David',
    'Brown',
    'david.brown@email.com',
    '217-555-4001',
    '1988-07-12',
    'Male',
    'Married',
    'Brown Family',
    '321 Maple Street',
    'Springfield',
    'IL',
    '62704',
    '217-555-4000'
);

-- 3. Test donation procedures
CALL GetMemberDonationSummary(1, 2024);
CALL GenerateGivingStatement(1, 2024);
CALL GetTopDonors('2024-01-01', '2024-12-31', 5);

-- 4. Test event management
CALL RegisterMemberForEvent(1, 1, 'Regular attendee');
CALL RegisterMemberForEvent(2, 2, 'Vegetarian meal preference');

-- Test capacity checking (this should work)
CALL RegisterMemberForEvent(3, 3, 'First time retreat participant');

-- 5. Test group management
CALL AddMemberToGroup(1, 1);
CALL AddMemberToGroup(2, 2);
CALL GetGroupMembership(1);

-- 6. Test reporting
CALL GenerateMonthlyReport(2024, 1);
CALL SearchMembers('Smith', 'name');
CALL SearchMembers('gmail', 'email');
CALL SearchMembers('555', 'phone');
CALL SearchMembers('John', 'all');

-- 7. Test views
SELECT * FROM active_members LIMIT 5;
SELECT * FROM current_year_donations;
SELECT * FROM upcoming_events;

-- 8. Test some error conditions (these should fail gracefully)

-- Try to register for non-existent event
-- CALL RegisterMemberForEvent(999, 1, 'This should fail');

-- Try to add member to non-existent group
-- CALL AddMemberToGroup(999, 1);

-- Try to register same member twice for same event
-- CALL RegisterMemberForEvent(1, 1, 'Duplicate registration');

-- Test attendance tracking
INSERT INTO attendance (event_id, member_id, attendance_date, present) VALUES
(1, 1, '2024-12-22 10:00:00', TRUE),
(1, 2, '2024-12-22 10:00:00', TRUE),
(2, 1, '2024-12-24 19:00:00', FALSE);

CALL GetEventAttendanceStats(1);

-- Test volunteer tracking
INSERT INTO volunteers (member_id, service_area, role, start_date, active, training_completed) VALUES
(1, 'Children Ministry', 'Teacher', '2024-01-01', TRUE, TRUE),
(2, 'Music Ministry', 'Choir Member', '2024-02-01', TRUE, TRUE),
(3, 'Hospitality', 'Greeter', '2024-03-01', TRUE, FALSE);

SELECT * FROM active_volunteers;
