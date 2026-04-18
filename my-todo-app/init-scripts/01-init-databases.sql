-- Initialize databases for My Todo App

-- Nacos database
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Auth service database
CREATE DATABASE IF NOT EXISTS my_todo_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- User service database
CREATE DATABASE IF NOT EXISTS my_todo_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Permission service database
CREATE DATABASE IF NOT EXISTS my_todo_permission DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Dict service database
CREATE DATABASE IF NOT EXISTS my_todo_dict DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ERP service database
CREATE DATABASE IF NOT EXISTS my_todo_erp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Finance service database
CREATE DATABASE IF NOT EXISTS my_todo_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON nacos.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_auth.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_user.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_permission.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_dict.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_erp.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON my_todo_finance.* TO 'root'@'%';

FLUSH PRIVILEGES;
