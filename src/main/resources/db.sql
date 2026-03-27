CREATE DATABASE IF NOT EXISTS todo_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE todo_db;

-- ─────────────────────────────────────────
-- Tabla de usuarios
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,          -- se almacena el hash SHA-256
    creado_en    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- Tabla de tareas (relacionada a usuarios)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS todos (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT          NOT NULL,
    titulo       VARCHAR(255) NOT NULL,
    completado   TINYINT(1)   NOT NULL DEFAULT 0,
    creado_en    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────
-- Datos de ejemplo
-- ─────────────────────────────────────────

-- Contrasena "password123" hasheada con SHA-256
-- Hash: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT INTO usuarios (username, password) VALUES
('admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('estudiante', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

INSERT INTO todos (usuario_id, titulo, completado) VALUES
(1, 'Estudiar Jakarta EE Servlets', 0),
(1, 'Crear proyecto Angular', 0),
(2, 'Conectar frontend con backend', 0);
