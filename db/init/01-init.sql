-- RGBradford Database Initialization Script
-- This script runs automatically when the MySQL container starts for the first time

-- Grant all privileges to the user on the database
GRANT ALL PRIVILEGES ON rgbradford_db.* TO 'rgbradford_user'@'%';
FLUSH PRIVILEGES;

-- Ensure we're using the correct database
USE rgbradford_db;

-- Create tables if they don't exist (Spring Boot will handle this with JPA DDL auto)
-- This file is here for any custom initialization you may need

-- Optional: Add default/seed data here
-- Example:
-- INSERT INTO users (username, email, created_at) VALUES ('admin', 'admin@example.com', NOW());

-- You can add more initialization SQL here as needed