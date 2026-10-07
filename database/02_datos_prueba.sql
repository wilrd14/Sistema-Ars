-- ARS VitaSalud — datos de prueba (TODOS FICTICIOS, PRD 4.2).
-- Ejecutar después de 01_esquema.sql:  mysql -u root -p vitasalud < database/02_datos_prueba.sql
-- Cada integrante agrega aquí al menos 10 registros de su módulo (PRD 10.1), dentro de su sección.
USE vitasalud;

-- ======================= SEG (Williams) =======================
INSERT INTO rol (codigo, nombre) VALUES
    ('PRESTADOR', 'Prestador'),
    ('ANALISTA_AUTORIZACIONES', 'Analista de autorizaciones'),
    ('AUDITOR_MEDICO', 'Auditor médico'),
    ('AGENTE_AFILIACION', 'Agente de afiliación'),
    ('ADMINISTRADOR', 'Administrador');

-- ======================= AFI / PSS (Liannelis) =======================
INSERT INTO provincia (nombre) VALUES
    ('Distrito Nacional'), ('Santo Domingo'), ('Santiago'), ('La Altagracia'), ('La Vega'),
    ('San Cristóbal'), ('Puerto Plata'), ('Duarte'), ('San Pedro de Macorís'), ('La Romana'),
    ('Azua'), ('Bahoruco'), ('Barahona'), ('Dajabón'), ('El Seibo'), ('Elías Piña'),
    ('Espaillat'), ('Hato Mayor'), ('Hermanas Mirabal'), ('Independencia'), ('Maria Trinidad Sánchez'),
    ('Monseñor Nouel'), ('Monte Cristi'), ('Monte Plata'), ('Pedernales'), ('Peravia'),
    ('Samaná'), ('San José de Ocoa'), ('San Juan'), ('Sánchez Ramírez'), ('Santiago Rodríguez'),
    ('Valverde');

-- TODO (Liannelis): cargar todos los municipios. Muestra mínima para probar la lista dependiente:
INSERT INTO municipio (provincia_id, nombre)
    SELECT id, 'Santo Domingo de Guzmán' FROM provincia WHERE nombre = 'Distrito Nacional';
INSERT INTO municipio (provincia_id, nombre)
    SELECT id, m.nombre FROM provincia
    JOIN (SELECT 'Santo Domingo Este' AS nombre UNION SELECT 'Santo Domingo Norte' UNION SELECT 'Santo Domingo Oeste') m
    WHERE provincia.nombre = 'Santo Domingo';

INSERT INTO prestador (rnc_cedula, nombre, tipo, direccion, provincia_id, telefono, correo, estado_contrato) VALUES
    ('101000011', 'Clínica Ejemplo Uno', 'CLINICA', 'Av. Ficticia 100', 1, '(809) 555-0101', 'contacto@clinica1.example', 'ACTIVO'),
    ('101000022', 'Laboratorio Ejemplo Dos', 'LABORATORIO', 'Calle Imaginaria 25', 2, '(809) 555-0102', 'contacto@lab2.example', 'ACTIVO');

-- ======================= PLA (Luis Miguel) =======================
INSERT INTO plan (codigo, nombre, descripcion, prima_mensual) VALUES
    ('PDSS', 'Básico PDSS', 'Plan básico de salud', 1500.00),
    ('PLATA', 'Complementario Plata', 'Plan complementario nivel plata', 3200.00),
    ('ORO', 'Complementario Oro', 'Plan complementario nivel oro', 5400.00);

INSERT INTO procedimiento (codigo, descripcion, categoria, precio_referencia) VALUES
    ('CON001', 'Consulta médica general', 'CONSULTA', 1200.00),
    ('LAB001', 'Hemograma completo', 'LABORATORIO', 450.00),
    ('IMG001', 'Resonancia magnética', 'IMAGENES', 14500.00),
    ('IMG002', 'Radiografía de tórax', 'IMAGENES', 1100.00),
    ('TER001', 'Sesión de fisioterapia', 'TERAPIA', 800.00);

INSERT INTO cobertura (plan_id, categoria, porcentaje, copago_fijo, tope_anual, dias_carencia, requiere_precertificacion)
    SELECT p.id, c.categoria, c.porcentaje, NULL, c.tope, c.carencia, c.precert
    FROM plan p
    JOIN (SELECT 'CONSULTA' AS categoria, 80.00 AS porcentaje, 30000.00 AS tope, 0 AS carencia, FALSE AS precert
          UNION ALL SELECT 'LABORATORIO', 80.00, 40000.00, 0, FALSE
          UNION ALL SELECT 'IMAGENES', 70.00, 60000.00, 30, TRUE) c
    WHERE p.codigo = 'PDSS';

-- ======================= SEG: usuarios de prueba =======================
-- Contraseña de TODOS los usuarios de prueba: Vita2026!   (hash BCrypt)
-- Dos usuarios prestador de centros distintos, para probar el aislamiento de datos (RN-07).
SET @hash = '$2a$10$odlehAkOu/rEZ6G3JphsZO18yH4AjDzcd9mJmoe8.bPwES1NIbqmu';

INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Administrador del Sistema', 'admin', @hash, 'INTERNO', r.id, NULL, 'Administrador' FROM rol r WHERE r.codigo = 'ADMINISTRADOR';
INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Ana Analista', 'analista', @hash, 'INTERNO', r.id, NULL, 'Analista de autorizaciones' FROM rol r WHERE r.codigo = 'ANALISTA_AUTORIZACIONES';
INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Mario Auditor', 'auditor', @hash, 'INTERNO', r.id, NULL, 'Auditor médico' FROM rol r WHERE r.codigo = 'AUDITOR_MEDICO';
INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Alicia Agente', 'agente', @hash, 'INTERNO', r.id, NULL, 'Agente de afiliación' FROM rol r WHERE r.codigo = 'AGENTE_AFILIACION';
INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Clínica Ejemplo Uno', 'clinica1', @hash, 'PRESTADOR', r.id, p.id, 'Prestador'
FROM rol r JOIN prestador p ON p.rnc_cedula = '101000011' WHERE r.codigo = 'PRESTADOR';
INSERT INTO usuario (nombre, username, password_hash, perfil, rol_id, prestador_id, cargo)
SELECT 'Laboratorio Ejemplo Dos', 'laboratorio2', @hash, 'PRESTADOR', r.id, p.id, 'Prestador'
FROM rol r JOIN prestador p ON p.rnc_cedula = '101000022' WHERE r.codigo = 'PRESTADOR';
