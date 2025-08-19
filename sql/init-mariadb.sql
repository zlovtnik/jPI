-- Create database and user (these are handled by docker environment variables)
-- USE churchdb;

-- Members table (core entity for people)
CREATE TABLE members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    birth_date DATE,
    baptism_date DATE,
    membership_date DATE DEFAULT (CURRENT_DATE),
    family_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Families table (group members together)
CREATE TABLE families (
    id INT AUTO_INCREMENT PRIMARY KEY,
    family_name VARCHAR(200) NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Foreign key for members to families
ALTER TABLE members ADD FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE SET NULL;

-- Events table (for services, classes, etc.)
CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Event registrations
CREATE TABLE event_registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    member_id INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_member (event_id, member_id)
);

-- Attendance tracking
CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    member_id INT NOT NULL,
    check_in_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_member_attendance (event_id, member_id)
);

-- Donations table
CREATE TABLE donations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    donation_date DATE DEFAULT (CURRENT_DATE),
    type ENUM('tithe', 'offering', 'pledge', 'special') NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL
);

-- Groups table (small groups, ministries)
CREATE TABLE groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    leader_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES members(id) ON DELETE SET NULL
);

-- Group members (many-to-many)
CREATE TABLE group_members (
    group_id INT NOT NULL,
    member_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, member_id),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- Volunteers (assignments to events/groups)
CREATE TABLE volunteers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    event_id INT,
    group_id INT,
    role VARCHAR(100),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL
);

-- Users table (for authentication, linked to members)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- Use BCrypt or similar
    role ENUM('admin', 'member', 'volunteer') NOT NULL,
    member_id INT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_events_start_time ON events(start_time);
CREATE INDEX idx_donations_member_id ON donations(member_id);
CREATE INDEX idx_donations_date ON donations(donation_date);
CREATE INDEX idx_attendance_event ON attendance(event_id);
CREATE INDEX idx_volunteers_member ON volunteers(member_id);

-- Insert some sample data
INSERT INTO families (family_name, address) VALUES 
('Smith Family', '123 Main St, Anytown, USA'),
('Johnson Family', '456 Oak Ave, Anytown, USA');

INSERT INTO members (first_name, last_name, email, phone, family_id) VALUES 
('John', 'Smith', 'john.smith@email.com', '555-0101', 1),
('Jane', 'Smith', 'jane.smith@email.com', '555-0102', 1),
('Bob', 'Johnson', 'bob.johnson@email.com', '555-0201', 2),
('Alice', 'Johnson', 'alice.johnson@email.com', '555-0202', 2);

INSERT INTO users (username, password_hash, role, member_id) VALUES 
('admin', '$2a$10$example.hash.here', 'admin', 1),
('jsmith', '$2a$10$example.hash.here', 'member', 1),
('bjohnson', '$2a$10$example.hash.here', 'member', 3);

INSERT INTO events (name, description, start_time, location) VALUES 
('Sunday Service', 'Weekly worship service', '2024-01-07 10:00:00', 'Main Sanctuary'),
('Bible Study', 'Weekly Bible study group', '2024-01-03 19:00:00', 'Fellowship Hall'),
('Youth Group', 'Weekly youth meeting', '2024-01-05 18:00:00', 'Youth Room');

INSERT INTO groups (name, description, leader_id) VALUES 
('Men''s Ministry', 'Fellowship and study for men', 1),
('Women''s Ministry', 'Fellowship and study for women', 2),
('Youth Ministry', 'Ministry for teenagers', 3);
