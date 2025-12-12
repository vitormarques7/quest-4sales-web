INSERT INTO tb_roles (id, name, description, created_at) VALUES 
('d2fc3433-5b82-416b-ba10-85dc02111817', 'ROLE_ADMIN', 'Acesso total ao sistema', CURRENT_TIMESTAMP);

INSERT INTO tb_roles (id, name, description, created_at) VALUES 
('c790858e-4363-4428-a2d3-9844f23b7b25', 'ROLE_MANAGER', 'Gerencia competições e usuários', CURRENT_TIMESTAMP);

INSERT INTO users (
    id,
    username,
    email,
    password, 
    first_name,
    last_name,
    active,
    created_at
) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    'admin',
    'admin@quest4sale.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7qa8q.Fv.', 
    'Admin',
    'System',
    true,
    CURRENT_TIMESTAMP
);

INSERT INTO user_role (user_id, role_id) VALUES 
('123e4567-e89b-12d3-a456-426614174000', 'd2fc3433-5b82-416b-ba10-85dc02111817');