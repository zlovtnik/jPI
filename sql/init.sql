-- Create schema
CREATE SCHEMA church_app;

-- Enums for common types
CREATE TYPE donation_type AS ENUM ('tithe', 'offering', 'pledge', 'special');
CREATE TYPE role_type AS ENUM ('admin', 'member', 'volunteer');

-- Members table (core entity for people)
CREATE TABLE church_app.members (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    birth_date DATE,
    baptism_date DATE,
    membership_date DATE DEFAULT CURRENT_DATE,
    family_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Families table (group members together)
CREATE TABLE church_app.families (
    id SERIAL PRIMARY KEY,
    family_name VARCHAR(200) NOT NULL,
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Foreign key for members to families
ALTER TABLE church_app.members ADD FOREIGN KEY (family_id) REFERENCES church_app.families(id) ON DELETE SET NULL;

-- Events table (for services, classes, etc.)
CREATE TABLE church_app.events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    location VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Event registrations
CREATE TABLE church_app.event_registrations (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES church_app.events(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE CASCADE,
    UNIQUE (event_id, member_id)
);

-- Attendance tracking
CREATE TABLE church_app.attendance (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    check_in_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES church_app.events(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE CASCADE,
    UNIQUE (event_id, member_id)
);

-- Donations table
CREATE TABLE church_app.donations (
    id SERIAL PRIMARY KEY,
    member_id INTEGER,
    amount DECIMAL(10, 2) NOT NULL,
    donation_date DATE DEFAULT CURRENT_DATE,
    type donation_type NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE SET NULL
);

-- Groups table (small groups, ministries)
CREATE TABLE church_app.groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    leader_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (leader_id) REFERENCES church_app.members(id) ON DELETE SET NULL
);

-- Group members (many-to-many)
CREATE TABLE church_app.group_members (
    group_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, member_id),
    FOREIGN KEY (group_id) REFERENCES church_app.groups(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE CASCADE
);

-- Volunteers (assignments to events/groups)
CREATE TABLE church_app.volunteers (
    id SERIAL PRIMARY KEY,
    member_id INTEGER NOT NULL,
    event_id INTEGER,
    group_id INTEGER,
    role VARCHAR(100),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES church_app.events(id) ON DELETE SET NULL,
    FOREIGN KEY (group_id) REFERENCES church_app.groups(id) ON DELETE SET NULL
);

-- Users table (for authentication, linked to members)
CREATE TABLE church_app.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- Use BCrypt or similar
    role role_type NOT NULL,
    member_id INTEGER UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES church_app.members(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_members_email ON church_app.members(email);
CREATE INDEX idx_events_start_time ON church_app.events(start_time);
CREATE INDEX idx_donations_member_id ON church_app.donations(member_id);