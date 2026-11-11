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

-- ======================= AFI / PSS (Liannelis): municipios, prestadores y afiliados =======================

-- Municipios: cabecera de cada provincia y algunos municipios principales.
INSERT IGNORE INTO municipio (provincia_id, nombre)
SELECT p.id, m.nombre
FROM provincia p
JOIN (
    SELECT 'Distrito Nacional' AS provincia, 'Santo Domingo de Guzmán' AS nombre
    UNION ALL SELECT 'Santo Domingo', 'Santo Domingo Este'
    UNION ALL SELECT 'Santo Domingo', 'Santo Domingo Norte'
    UNION ALL SELECT 'Santo Domingo', 'Santo Domingo Oeste'
    UNION ALL SELECT 'Santo Domingo', 'Boca Chica'
    UNION ALL SELECT 'Santo Domingo', 'Los Alcarrizos'
    UNION ALL SELECT 'Santo Domingo', 'Pedro Brand'
    UNION ALL SELECT 'Santo Domingo', 'San Antonio de Guerra'
    UNION ALL SELECT 'Santiago', 'Santiago'
    UNION ALL SELECT 'Santiago', 'Tamboril'
    UNION ALL SELECT 'Santiago', 'Villa González'
    UNION ALL SELECT 'La Altagracia', 'Higüey'
    UNION ALL SELECT 'La Altagracia', 'San Rafael del Yuma'
    UNION ALL SELECT 'La Vega', 'La Vega'
    UNION ALL SELECT 'La Vega', 'Jarabacoa'
    UNION ALL SELECT 'La Vega', 'Constanza'
    UNION ALL SELECT 'San Cristóbal', 'San Cristóbal'
    UNION ALL SELECT 'San Cristóbal', 'Villa Altagracia'
    UNION ALL SELECT 'San Cristóbal', 'Haina'
    UNION ALL SELECT 'Puerto Plata', 'Puerto Plata'
    UNION ALL SELECT 'Puerto Plata', 'Sosúa'
    UNION ALL SELECT 'Duarte', 'San Francisco de Macorís'
    UNION ALL SELECT 'San Pedro de Macorís', 'San Pedro de Macorís'
    UNION ALL SELECT 'La Romana', 'La Romana'
    UNION ALL SELECT 'Azua', 'Azua de Compostela'
    UNION ALL SELECT 'Bahoruco', 'Neiba'
    UNION ALL SELECT 'Barahona', 'Barahona'
    UNION ALL SELECT 'Dajabón', 'Dajabón'
    UNION ALL SELECT 'El Seibo', 'El Seibo'
    UNION ALL SELECT 'Elías Piña', 'Comendador'
    UNION ALL SELECT 'Espaillat', 'Moca'
    UNION ALL SELECT 'Hato Mayor', 'Hato Mayor del Rey'
    UNION ALL SELECT 'Hermanas Mirabal', 'Salcedo'
    UNION ALL SELECT 'Independencia', 'Jimaní'
    UNION ALL SELECT 'Maria Trinidad Sánchez', 'Nagua'
    UNION ALL SELECT 'Monseñor Nouel', 'Bonao'
    UNION ALL SELECT 'Monte Cristi', 'Monte Cristi'
    UNION ALL SELECT 'Monte Plata', 'Monte Plata'
    UNION ALL SELECT 'Pedernales', 'Pedernales'
    UNION ALL SELECT 'Peravia', 'Baní'
    UNION ALL SELECT 'Samaná', 'Samaná'
    UNION ALL SELECT 'San José de Ocoa', 'San José de Ocoa'
    UNION ALL SELECT 'San Juan', 'San Juan de la Maguana'
    UNION ALL SELECT 'Sánchez Ramírez', 'Cotuí'
    UNION ALL SELECT 'Santiago Rodríguez', 'San Ignacio de Sabaneta'
    UNION ALL SELECT 'Valverde', 'Mao'
) m ON m.provincia = p.nombre;

-- Prestadores (12 más, de distintos tipos y estados de contrato).
INSERT INTO prestador (rnc_cedula, nombre, tipo, direccion, provincia_id, telefono, correo, estado_contrato) VALUES
    ('130000011', 'Hospital Ejemplo Metropolitano', 'HOSPITAL', 'Av. Ficticia 200',
        (SELECT id FROM provincia WHERE nombre = 'Distrito Nacional'), '(809) 555-0111', 'info@hospitalmetro.example', 'ACTIVO'),
    ('130000022', 'Clínica Ejemplo Santiago', 'CLINICA', 'Calle Imaginaria 45',
        (SELECT id FROM provincia WHERE nombre = 'Santiago'), '(809) 555-0112', 'info@clinicastgo.example', 'ACTIVO'),
    ('130000033', 'Laboratorio Ejemplo Norte', 'LABORATORIO', 'Av. Ejemplo 78',
        (SELECT id FROM provincia WHERE nombre = 'Santo Domingo'), '(809) 555-0113', 'info@labnorte.example', 'ACTIVO'),
    ('130000044', 'Centro de Diagnóstico Ejemplo', 'CENTRO_DIAGNOSTICO', 'Calle Ficticia 12',
        (SELECT id FROM provincia WHERE nombre = 'La Vega'), '(809) 555-0114', 'info@diagnostico.example', 'ACTIVO'),
    ('130000055', 'Farmacia Ejemplo Central', 'FARMACIA', 'Av. Central 301',
        (SELECT id FROM provincia WHERE nombre = 'San Cristóbal'), '(809) 555-0115', 'info@farmaciacentral.example', 'ACTIVO'),
    ('130000066', 'Clínica Ejemplo del Este', 'CLINICA', 'Calle Principal 9',
        (SELECT id FROM provincia WHERE nombre = 'La Altagracia'), '(809) 555-0116', 'info@clinicaeste.example', 'ACTIVO'),
    ('130000077', 'Laboratorio Ejemplo Sur', 'LABORATORIO', 'Calle Duarte 55',
        (SELECT id FROM provincia WHERE nombre = 'Barahona'), '(809) 555-0117', 'info@labsur.example', 'ACTIVO'),
    ('130000088', 'Hospital Ejemplo del Cibao', 'HOSPITAL', 'Av. Libertad 120',
        (SELECT id FROM provincia WHERE nombre = 'Puerto Plata'), '(809) 555-0118', 'info@hospitalcibao.example', 'ACTIVO'),
    ('130000099', 'Centro de Diagnóstico Ejemplo Este', 'CENTRO_DIAGNOSTICO', 'Calle Mella 14',
        (SELECT id FROM provincia WHERE nombre = 'La Romana'), '(809) 555-0119', 'info@diagnosticoeste.example', 'ACTIVO'),
    ('130000110', 'Clínica Ejemplo Duarte', 'CLINICA', 'Av. Duarte 210',
        (SELECT id FROM provincia WHERE nombre = 'Duarte'), '(809) 555-0120', 'info@clinicaduarte.example', 'SUSPENDIDO'),
    ('130000121', 'Farmacia Ejemplo Popular', 'FARMACIA', 'Calle Sánchez 33',
        (SELECT id FROM provincia WHERE nombre = 'Santo Domingo'), '(809) 555-0121', 'info@farmaciapopular.example', 'TERMINADO'),
    ('001-0000099-9', 'Dr. Ejemplo Tres', 'MEDICO_INDEPENDIENTE', 'Consultorio 4, Torre Ficticia',
        (SELECT id FROM provincia WHERE nombre = 'Distrito Nacional'), '(809) 555-0122', 'drejemplo3@correo.example', 'ACTIVO');

-- Afiliados: variables con los ids que se usan abajo.
SET @prov_dn   := (SELECT id FROM provincia WHERE nombre = 'Distrito Nacional');
SET @prov_sd   := (SELECT id FROM provincia WHERE nombre = 'Santo Domingo');
SET @prov_stgo := (SELECT id FROM provincia WHERE nombre = 'Santiago');
SET @prov_lv   := (SELECT id FROM provincia WHERE nombre = 'La Vega');

SET @mun_dn   := (SELECT id FROM municipio WHERE nombre = 'Santo Domingo de Guzmán');
SET @mun_sde  := (SELECT id FROM municipio WHERE nombre = 'Santo Domingo Este');
SET @mun_sdn  := (SELECT id FROM municipio WHERE nombre = 'Santo Domingo Norte');
SET @mun_sdo  := (SELECT id FROM municipio WHERE nombre = 'Santo Domingo Oeste');
SET @mun_stgo := (SELECT id FROM municipio WHERE nombre = 'Santiago');
SET @mun_lv   := (SELECT id FROM municipio WHERE nombre = 'La Vega');

SET @plan_pdss  := (SELECT id FROM plan WHERE codigo = 'PDSS');
SET @plan_plata := (SELECT id FROM plan WHERE codigo = 'PLATA');
SET @plan_oro   := (SELECT id FROM plan WHERE codigo = 'ORO');

-- Titulares (9).
INSERT INTO afiliado (tipo_documento, numero_documento, nss, nombres, apellidos, fecha_nacimiento, sexo,
                      estado_civil, nacionalidad, tipo_telefono, telefono, correo, provincia_id, municipio_id,
                      sector, direccion, plan_id, tipo_afiliado, inicio_cobertura, estado, motivo_estado) VALUES
    ('CEDULA', '001-0000001-1', '10000000001', 'Juan Carlos', 'Pérez Rodríguez', '1985-03-14', 'M',
        'CASADO', 'Dominicana', 'CELULAR', '(809) 555-0201', 'juan.perez@correo.example', @prov_dn, @mun_dn,
        'Gazcue', 'Calle Ficticia 10', @plan_pdss, 'TITULAR', '2025-01-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000002-2', '10000000002', 'María Elena', 'Gómez Santos', '1990-07-22', 'F',
        'CASADO', 'Dominicana', 'CELULAR', '(809) 555-0202', 'maria.gomez@correo.example', @prov_sd, @mun_sde,
        'Los Mameyes', 'Calle Imaginaria 22', @plan_plata, 'TITULAR', '2025-02-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000003-3', '10000000003', 'Luis Alberto', 'Martínez Peña', '1978-11-05', 'M',
        'DIVORCIADO', 'Dominicana', 'RESIDENCIAL', '(809) 555-0203', 'luis.martinez@correo.example', @prov_stgo, @mun_stgo,
        'Los Jardines', 'Av. Ejemplo 55', @plan_oro, 'TITULAR', '2024-06-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000004-4', '10000000004', 'Ana Carolina', 'Fernández Díaz', '1995-02-18', 'F',
        'SOLTERO', 'Dominicana', 'CELULAR', '(809) 555-0204', 'ana.fernandez@correo.example', @prov_lv, @mun_lv,
        'Centro', 'Calle Duarte 8', @plan_pdss, 'TITULAR', '2025-03-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000005-5', '10000000005', 'Pedro Antonio', 'Reyes Cabrera', '1982-09-30', 'M',
        'CASADO', 'Dominicana', 'CELULAR', '(809) 555-0205', 'pedro.reyes@correo.example', @prov_sd, @mun_sdn,
        'Villa Mella', 'Calle Sánchez 40', @plan_plata, 'TITULAR', '2024-09-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000006-6', '10000000006', 'Rosa Amelia', 'Núñez Valdez', '1970-12-01', 'F',
        'VIUDO', 'Dominicana', 'RESIDENCIAL', '(809) 555-0206', 'rosa.nunez@correo.example', @prov_dn, @mun_dn,
        'Naco', 'Av. Tiradentes 100', @plan_pdss, 'TITULAR', '2023-05-01', 'SUSPENDIDO', 'Falta de pago'),
    ('CEDULA', '001-0000007-7', '10000000007', 'Carlos Manuel', 'Rodríguez Lora', '1988-05-09', 'M',
        'UNION_LIBRE', 'Dominicana', 'CELULAR', '(809) 555-0207', 'carlos.rodriguez@correo.example', @prov_stgo, @mun_stgo,
        'Cerros de Gurabo', 'Calle Principal 3', @plan_oro, 'TITULAR', '2025-04-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000008-8', '10000000008', 'Yaneris Altagracia', 'Almonte Mejía', '1993-08-27', 'F',
        'SOLTERO', 'Dominicana', 'CELULAR', '(809) 555-0208', 'yaneris.almonte@correo.example', @prov_sd, @mun_sdo,
        'Herrera', 'Calle Mella 17', @plan_pdss, 'TITULAR', '2025-05-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000009-9', '10000000009', 'Miguel Ángel', 'Santana Ruiz', '1965-04-12', 'M',
        'CASADO', 'Dominicana', 'RESIDENCIAL', '(809) 555-0209', 'miguel.santana@correo.example', @prov_sd, @mun_sde,
        'Alma Rosa', 'Av. Ficticia 60', @plan_plata, 'TITULAR', '2022-01-01', 'CANCELADO', 'Cancelación voluntaria');

SET @t_juan  := (SELECT id FROM afiliado WHERE nss = '10000000001');
SET @t_maria := (SELECT id FROM afiliado WHERE nss = '10000000002');
SET @t_luis  := (SELECT id FROM afiliado WHERE nss = '10000000003');
SET @t_pedro := (SELECT id FROM afiliado WHERE nss = '10000000005');
SET @t_rosa  := (SELECT id FROM afiliado WHERE nss = '10000000006');

-- Dependientes (7): cónyuges e hijos; los de Rosa quedan suspendidos como su titular (RN-03).
INSERT INTO afiliado (tipo_documento, numero_documento, nss, nombres, apellidos, fecha_nacimiento, sexo,
                      estado_civil, nacionalidad, provincia_id, municipio_id, plan_id, tipo_afiliado,
                      titular_id, parentesco, es_estudiante, inicio_cobertura, estado, motivo_estado) VALUES
    ('CEDULA', '001-0000010-0', '10000000010', 'Laura Beatriz', 'Ramírez de Pérez', '1987-06-03', 'F',
        'CASADO', 'Dominicana', @prov_dn, @mun_dn, @plan_pdss, 'DEPENDIENTE',
        @t_juan, 'CONYUGE', FALSE, '2025-01-01', 'ACTIVO', NULL),
    ('ACTA_NACIMIENTO', 'ACTA-2015-0001', '10000000011', 'Daniel', 'Pérez Ramírez', '2015-09-10', 'M',
        'SOLTERO', 'Dominicana', @prov_dn, @mun_dn, @plan_pdss, 'DEPENDIENTE',
        @t_juan, 'HIJO', FALSE, '2025-01-01', 'ACTIVO', NULL),
    ('ACTA_NACIMIENTO', 'ACTA-2012-0002', '10000000012', 'Mateo Andrés', 'Rosario Gómez', '2012-03-21', 'M',
        'SOLTERO', 'Dominicana', @prov_sd, @mun_sde, @plan_plata, 'DEPENDIENTE',
        @t_maria, 'HIJO', FALSE, '2025-02-01', 'ACTIVO', NULL),
    ('ACTA_NACIMIENTO', 'ACTA-2018-0003', '10000000013', 'Isabella', 'Martínez Cruz', '2018-12-02', 'F',
        'SOLTERO', 'Dominicana', @prov_stgo, @mun_stgo, @plan_oro, 'DEPENDIENTE',
        @t_luis, 'HIJO', FALSE, '2024-06-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000011-1', '10000000014', 'Elena Marisol', 'Taveras de Reyes', '1984-10-15', 'F',
        'CASADO', 'Dominicana', @prov_sd, @mun_sdn, @plan_plata, 'DEPENDIENTE',
        @t_pedro, 'CONYUGE', FALSE, '2024-09-01', 'ACTIVO', NULL),
    ('CEDULA', '001-0000012-2', '10000000015', 'Kevin', 'Reyes Taveras', '2005-04-25', 'M',
        'SOLTERO', 'Dominicana', @prov_sd, @mun_sdn, @plan_plata, 'DEPENDIENTE',
        @t_pedro, 'HIJO', TRUE, '2024-09-01', 'ACTIVO', NULL),
    ('ACTA_NACIMIENTO', 'ACTA-2010-0004', '10000000016', 'Camila Isabel', 'Gómez Núñez', '2010-01-30', 'F',
        'SOLTERO', 'Dominicana', @prov_dn, @mun_dn, @plan_pdss, 'DEPENDIENTE',
        @t_rosa, 'HIJO', FALSE, '2023-05-01', 'SUSPENDIDO', 'Titular suspendido');

-- Número de afiliado con el mismo formato que genera el DAO (AF-000001).
UPDATE afiliado
SET numero_afiliado = CONCAT('AF-', LPAD(id, 6, '0'))
WHERE numero_afiliado IS NULL;
