# MariaDB Stored Procedures Documentation

This document provides examples and usage instructions for the stored procedures created for the church management system.

## 1. Member Management Procedures

### GetMemberDetails
Get complete information about a member including family details.

```sql
-- Get details for member with ID 1
CALL GetMemberDetails(1);
```

### AddMemberToFamily
Add a new member to an existing family or create a new family.

```sql
-- Add new member to existing family or create new family
CALL AddMemberToFamily(
    'John',                    -- first_name
    'Doe',                     -- last_name
    'john.doe@email.com',      -- email
    '555-1234',                -- phone
    '1985-06-15',              -- date_of_birth
    'Male',                    -- gender
    'Married',                 -- marital_status
    'Doe Family',              -- family_name
    '123 Main St',             -- family_address
    'Springfield',             -- family_city
    'IL',                      -- family_state
    '62701',                   -- family_zip
    '555-5678'                 -- family_phone
);
```

## 2. Donation Management Procedures

### GetMemberDonationSummary
Get a summary of donations by type for a specific member and year.

```sql
-- Get donation summary for member 1 in 2024
CALL GetMemberDonationSummary(1, 2024);
```

### GenerateGivingStatement
Generate an annual giving statement for tax purposes.

```sql
-- Generate giving statement for member 1 for year 2024
CALL GenerateGivingStatement(1, 2024);
```

### GetTopDonors
Get the top donors for a specific time period.

```sql
-- Get top 10 donors between Jan 1, 2024 and Dec 31, 2024
CALL GetTopDonors('2024-01-01', '2024-12-31', 10);
```

## 3. Event Management Procedures

### RegisterMemberForEvent
Register a member for an event with automatic capacity and deadline checking.

```sql
-- Register member 1 for event 5 with optional notes
CALL RegisterMemberForEvent(5, 1, 'Vegetarian meal preference');
```

### GetEventAttendanceStats
Get attendance statistics for a specific event.

```sql
-- Get attendance stats for event 1
CALL GetEventAttendanceStats(1);
```

## 4. Group Management Procedures

### AddMemberToGroup
Add a member to a group with capacity checking.

```sql
-- Add member 1 to group 2
CALL AddMemberToGroup(2, 1);
```

### GetGroupMembership
Get detailed information about a group and its members.

```sql
-- Get membership details for group 1
CALL GetGroupMembership(1);
```

## 5. Reporting Procedures

### GenerateMonthlyReport
Generate a comprehensive monthly activity report.

```sql
-- Generate report for January 2024
CALL GenerateMonthlyReport(2024, 1);
```

### SearchMembers
Search for members using various criteria.

```sql
-- Search by name
CALL SearchMembers('John', 'name');

-- Search by email
CALL SearchMembers('gmail.com', 'email');

-- Search by phone
CALL SearchMembers('555', 'phone');

-- Search all fields
CALL SearchMembers('John', 'all');
```

## Helpful Views

The following views are also created for easier data access:

### active_members
```sql
SELECT * FROM active_members WHERE city = 'Springfield';
```

### current_year_donations
```sql
SELECT * FROM current_year_donations WHERE donation_type = 'TITHE';
```

### upcoming_events
```sql
SELECT * FROM upcoming_events WHERE available_spots != 'Unlimited' AND CAST(available_spots AS UNSIGNED) < 5;
```

### active_volunteers
```sql
SELECT * FROM active_volunteers WHERE service_area = 'Children Ministry';
```

## Error Handling

The procedures include error handling for common scenarios:

- **Event Registration**: Checks for capacity limits, registration deadlines, and duplicate registrations
- **Group Membership**: Validates group capacity and prevents duplicate memberships
- **Family Management**: Automatically creates families if they don't exist

## Performance Notes

- All procedures use appropriate indexes defined in the schema
- Complex queries are optimized for performance
- Views provide pre-aggregated data for common reporting needs

## Security Considerations

- Procedures validate input parameters
- Use proper SQL techniques to prevent injection attacks
- Include appropriate error messages without exposing sensitive information

## Usage Examples

Here are some common usage patterns:

```sql
-- Weekly routine: Check upcoming events capacity
SELECT name, start_date, available_spots 
FROM upcoming_events 
WHERE start_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
AND available_spots != 'Unlimited' 
AND CAST(available_spots AS UNSIGNED) < 10;

-- Monthly routine: Generate giving statements for all members
SELECT member_id FROM (
    SELECT DISTINCT member_id 
    FROM donations 
    WHERE YEAR(donation_date) = YEAR(CURDATE())
) AS donors;
-- Then call GenerateGivingStatement for each member_id

-- Find members who haven't donated this year
SELECT m.id, m.first_name, m.last_name, m.email
FROM members m
LEFT JOIN donations d ON m.id = d.member_id AND YEAR(d.donation_date) = YEAR(CURDATE())
WHERE d.id IS NULL;
```
