-- Admin 계정 추가
-- Username: admin
-- Password: admin
-- Role: ADMIN
INSERT INTO users (username, password_hash, name, email, role, created_at)
VALUES (
    'admin',
    '$2a$10$WXpHkQX//kb9c2GQHTY.dewHlFbsaFHK9sYzvSiWAgQQqcN/sUwIq',  -- password: admin
    '관리자',
    'admin@example.com',
    'ADMIN',
    CURRENT_TIMESTAMP
);