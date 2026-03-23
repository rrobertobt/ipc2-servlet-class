CREATE DATABASE IF NOT EXISTS todo_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE todo_db;

-- Tabla de tareas
CREATE TABLE IF NOT EXISTS todos (
     id          INT AUTO_INCREMENT PRIMARY KEY,
     titulo      VARCHAR(255)    NOT NULL,
    completado  TINYINT(1)      NOT NULL DEFAULT 0,
    creado_en   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Datos de ejemplo
INSERT INTO todos (titulo, completado) VALUES
('Estudiar Jakarta EE Servlets', 0),
('Crear proyecto Angular', 0),
('Conectar frontend con backend', 0);
