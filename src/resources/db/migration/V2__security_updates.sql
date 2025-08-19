-- V2__security_updates.sql
-- Add security-related updates to the existing schema for MariaDB

-- Users table already has proper password column from V1
-- No need to alter it since MariaDB was designed with proper VARCHAR(255) for passwords

-- Insert a default admin user (password: admin123)
-- BCrypt hash for 'admin123' - $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
INSERT IGNORE INTO users (username, password, email, role, active) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@church.com', 'ADMIN', true);

-- Add some sample users for testing
INSERT IGNORE INTO users (username, password, email, role, active) 
VALUES 
    ('pastor', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'pastor@church.com', 'PASTOR', true),
    ('staff', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'staff@church.com', 'STAFF', true),
    ('volunteer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'volunteer@church.com', 'VOLUNTEER', true);
