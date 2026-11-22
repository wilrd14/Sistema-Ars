-- Migración para bases ya cargadas con el seed antiguo (sin cambiar datos existentes):
-- agrega municipios, prestadores y afiliados de prueba (AFI/PSS) y los casos de Joel (AUT + REC).
-- Requiere 01_esquema.sql (con 03_migracion_autorizacion.sql si la base es anterior) y las secciones SEG/PLA del 02.
-- Ejecutar una sola vez:  mysql -u root -p vitasalud < database/04_migracion_datos_afi_aut_rec.sql
USE vitasalud;

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


-- ARS VitaSalud | Datos de prueba de Joel (AUT + REC)
-- Pegar al final de 02_datos_prueba.sql, después de los otros módulos.
-- Reutiliza afiliados, prestadores, usuarios, planes y tarifas existentes.

-- 1. Preparar 10 casos de autorización.
DROP TEMPORARY TABLE IF EXISTS tmp_aut_joel;

CREATE TEMPORARY TABLE tmp_aut_joel (
    numero VARCHAR(12) PRIMARY KEY,
    nss VARCHAR(20) NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    procedimiento VARCHAR(20) NOT NULL,
    estado VARCHAR(12) NOT NULL,
    dias_atras INT NOT NULL
);

INSERT INTO tmp_aut_joel VALUES
    ('AUT-990001','10000000001','clinica1','CON001','PENDIENTE',1),
    ('AUT-990002','10000000001','laboratorio2','LAB001','APROBADA',2),
    ('AUT-990003','10000000002','clinica1','CON001','PENDIENTE',3),
    ('AUT-990004','10000000001','clinica1','CON001','APROBADA',4),
    ('AUT-990005','10000000003','clinica1','CON001','NEGADA',5),
    ('AUT-990006','10000000005','laboratorio2','LAB001','EN_REVISION',6),
    ('AUT-990007','10000000007','clinica1','CON001','PENDIENTE',7),
    ('AUT-990008','10000000008','laboratorio2','LAB001','PENDIENTE',8),
    ('AUT-990009','10000000002','clinica1','CON001','PENDIENTE',9),
    ('AUT-990010','10000000003','laboratorio2','LAB001','PENDIENTE',10);

-- 2. Guardar autorizaciones sin duplicar números.
-- Los importes aprobados se calculan con la cobertura de PLA.
INSERT INTO autorizacion (
    numero, afiliado_id, prestador_id, usuario_solicita_id,
    usuario_revisa_id, fecha_solicitud, fecha_decision,
    tipo_servicio, fecha_servicio, tipo_telefono,
    medico_prescriptor, telefono_contacto,
    diagnostico_cie10, diagnostico_desc,
    monto_solicitado, monto_cubierto, copago,
    estado, nota_revision, fecha_vencimiento
)
SELECT
    d.numero, af.id, u.prestador_id, u.id,
    CASE WHEN d.estado IN ('APROBADA','NEGADA')
         THEN analista.id ELSE NULL END,
    DATE_SUB(NOW(), INTERVAL d.dias_atras DAY),
    CASE WHEN d.estado IN ('APROBADA','NEGADA')
         THEN NOW() ELSE NULL END,
    pr.categoria, CURDATE(), 'CELULAR',
    'Doctor Demo', '809-555-0199',
    'Z00.0', 'Caso ficticio para pruebas',
    pr.precio_referencia,
    CASE WHEN d.estado = 'APROBADA' THEN
        ROUND(
            LEAST(pr.precio_referencia,
                  COALESCE(t.monto, pr.precio_referencia))
            * cob.porcentaje / 100, 2
        )
    ELSE NULL END,
    CASE WHEN d.estado = 'APROBADA' THEN
        COALESCE(
            cob.copago_fijo,
            GREATEST(0,
                pr.precio_referencia -
                ROUND(
                    LEAST(pr.precio_referencia,
                          COALESCE(t.monto, pr.precio_referencia))
                    * cob.porcentaje / 100, 2
                )
            )
        )
    ELSE NULL END,
    d.estado,
    CASE d.estado
        WHEN 'NEGADA' THEN 'Caso ficticio: solicitud negada'
        WHEN 'EN_REVISION' THEN 'Caso ficticio: información adicional'
        ELSE NULL
    END,
    CASE WHEN d.estado = 'APROBADA'
         THEN DATE_ADD(CURDATE(), INTERVAL 30 DAY)
         ELSE NULL END
FROM tmp_aut_joel d
JOIN afiliado af
    ON af.nss = d.nss AND af.estado = 'ACTIVO'
JOIN usuario u
    ON u.username = d.usuario AND u.estado = 'ACTIVO'
JOIN prestador ps
    ON ps.id = u.prestador_id
    AND ps.estado_contrato = 'ACTIVO'
JOIN procedimiento pr
    ON pr.codigo = d.procedimiento
LEFT JOIN cobertura cob
    ON cob.plan_id = af.plan_id AND cob.categoria = pr.categoria
LEFT JOIN tarifa t
    ON t.prestador_id = ps.id AND t.procedimiento_id = pr.id
LEFT JOIN usuario analista
    ON analista.username = 'analista'
WHERE (
    d.estado <> 'APROBADA'
    OR (cob.id IS NOT NULL AND analista.id IS NOT NULL)
)
AND NOT EXISTS (
    SELECT 1 FROM autorizacion a WHERE a.numero = d.numero
);

-- 3. Vincular procedimientos a las autorizaciones.
INSERT INTO detalle_autorizacion (
    autorizacion_id, procedimiento_id, cantidad,
    monto_solicitado, monto_cubierto
)
SELECT a.id, pr.id, 1, a.monto_solicitado, a.monto_cubierto
FROM tmp_aut_joel d
JOIN autorizacion a ON a.numero = d.numero
JOIN procedimiento pr ON pr.codigo = d.procedimiento
WHERE NOT EXISTS (
    SELECT 1 FROM detalle_autorizacion x
    WHERE x.autorizacion_id = a.id
      AND x.procedimiento_id = pr.id
);

-- 4. Registrar 2 reclamaciones sobre autorizaciones aprobadas.
INSERT INTO reclamacion (
    numero, autorizacion_id, prestador_id, factura_prestador,
    fecha_servicio, monto_reclamado, monto_glosado,
    monto_pagar, estado
)
SELECT d.numero, a.id, a.prestador_id, d.factura,
       a.fecha_servicio, d.monto, 0.00, d.monto, 'RECIBIDA'
FROM (
    SELECT 'REC-990001' numero,
           'AUT-990002' aut_numero,
           'FACT-JOEL-001' factura,
           200.00 monto
    UNION ALL
    SELECT 'REC-990002', 'AUT-990004',
           'FACT-JOEL-002', 300.00
) d
JOIN autorizacion a ON a.numero = d.aut_numero
WHERE a.estado = 'APROBADA'
  AND a.monto_cubierto >= d.monto
  AND NOT EXISTS (
      SELECT 1 FROM reclamacion r WHERE r.numero = d.numero
  )
  AND NOT EXISTS (
      SELECT 1 FROM reclamacion r WHERE r.autorizacion_id = a.id
  );

-- 5. Verificar los resultados.
SELECT
    (SELECT COUNT(*) FROM autorizacion
     WHERE numero LIKE 'AUT-99%') AS aut_joel,
    (SELECT COUNT(*) FROM reclamacion
     WHERE numero LIKE 'REC-99%') AS rec_joel;

-- Muestra los casos que no pudieron insertarse.
SELECT d.numero AS autorizacion_no_creada,
       d.nss, d.usuario, d.procedimiento
FROM tmp_aut_joel d
LEFT JOIN autorizacion a ON a.numero = d.numero
WHERE a.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_aut_joel;



