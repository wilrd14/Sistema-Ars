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

-- ======================= DATOS DEMO AUT + REC (Joel) =======================
-- Datos 100% ficticios. Reiniciar esquema antes de volver a cargarlos.
-- 12 afiliados, tarifas reales de ejemplo, 12 autorizaciones y una reclamación de auditoría.
-- IMPORTANTE: la fecha de inicio anterior a 2026 permite cumplir carencias de imágenes.

INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000001','CEDULA','90000000001','NSS-900000001','Paciente01','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000002','CEDULA','90000000002','NSS-900000002','Paciente02','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000003','CEDULA','90000000003','NSS-900000003','Paciente03','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000004','CEDULA','90000000004','NSS-900000004','Paciente04','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000005','CEDULA','90000000005','NSS-900000005','Paciente05','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000006','CEDULA','90000000006','NSS-900000006','Paciente06','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000007','CEDULA','90000000007','NSS-900000007','Paciente07','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000008','CEDULA','90000000008','NSS-900000008','Paciente08','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000009','CEDULA','90000000009','NSS-900000009','Paciente09','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PDSS';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000010','CEDULA','90000000010','NSS-900000010','Paciente10','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PLATA';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000011','CEDULA','90000000011','NSS-900000011','Paciente11','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','ACTIVO' FROM plan WHERE codigo='PLATA';
INSERT INTO afiliado (numero_afiliado,tipo_documento,numero_documento,nss,nombres,apellidos,fecha_nacimiento,sexo,plan_id,tipo_afiliado,inicio_cobertura,estado) SELECT 'AF-000012','CEDULA','90000000012','NSS-900000012','Paciente12','Ejemplo', '1998-04-20','F',id,'TITULAR','2025-01-01','SUSPENDIDO' FROM plan WHERE codigo='ORO';

-- Ampliación de coberturas para probar planes diferentes; consultas por plan + categoría.
INSERT INTO cobertura(plan_id,categoria,porcentaje,copago_fijo,tope_anual,dias_carencia,requiere_precertificacion)
 SELECT p.id,x.cat,x.pct,x.cop,x.tope,0,FALSE FROM plan p
 JOIN (SELECT 'CONSULTA' cat,90.00 pct,NULL cop,50000.00 tope UNION ALL
       SELECT 'LABORATORIO',90.00,NULL,70000.00 UNION ALL SELECT 'IMAGENES',80.00,NULL,90000.00) x
 WHERE p.codigo='PLATA';
INSERT INTO cobertura(plan_id,categoria,porcentaje,copago_fijo,tope_anual,dias_carencia,requiere_precertificacion)
 SELECT p.id,x.cat,x.pct,x.cop,x.tope,0,FALSE FROM plan p
 JOIN (SELECT 'CONSULTA' cat,95.00 pct,50.00 cop,100000.00 tope UNION ALL
       SELECT 'LABORATORIO',95.00,NULL,150000.00 UNION ALL SELECT 'IMAGENES',90.00,NULL,200000.00) x
 WHERE p.codigo='ORO';
-- Tarifas propias de cada prestador. Si falta una, RN-05 usa precio_referencia.
INSERT INTO tarifa(prestador_id,procedimiento_id,monto)
 SELECT pr.id,p.id,CASE p.codigo WHEN 'CON001' THEN 900 WHEN 'LAB001' THEN 350
        WHEN 'IMG001' THEN 13000 WHEN 'IMG002' THEN 1050 ELSE 750 END
 FROM prestador pr CROSS JOIN procedimiento p WHERE pr.rnc_cedula='101000011';
INSERT INTO tarifa(prestador_id,procedimiento_id,monto)
 SELECT pr.id,p.id,CASE p.codigo WHEN 'CON001' THEN 1000 WHEN 'LAB001' THEN 380
        WHEN 'IMG001' THEN 12500 WHEN 'IMG002' THEN 1020 ELSE 700 END
 FROM prestador pr CROSS JOIN procedimiento p WHERE pr.rnc_cedula='101000022';

INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(1,1,5,DATE_SUB(NOW(),INTERVAL 1 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(1,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(2,2,6,DATE_SUB(NOW(),INTERVAL 2 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,304.00,146.00,'APROBADA',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(CURDATE(), INTERVAL 29 DAY),NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(2,2,1,450.00,304.00);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(3,1,5,DATE_SUB(NOW(),INTERVAL 3 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(3,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(4,2,6,DATE_SUB(NOW(),INTERVAL 4 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,304.00,146.00,'APROBADA',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(CURDATE(), INTERVAL 29 DAY),NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(4,2,1,450.00,304.00);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(5,1,5,DATE_SUB(NOW(),INTERVAL 5 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'NEGADA',DATE_SUB(NOW(),INTERVAL 1 DAY),NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(5,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(6,2,6,DATE_SUB(NOW(),INTERVAL 6 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,NULL,NULL,'EN_REVISION',NULL,NULL,'Documento adicional solicitado');
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(6,2,1,450.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(7,1,5,DATE_SUB(NOW(),INTERVAL 7 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(7,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(8,2,6,DATE_SUB(NOW(),INTERVAL 8 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(8,2,1,450.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(9,1,5,DATE_SUB(NOW(),INTERVAL 9 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(9,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(10,2,6,DATE_SUB(NOW(),INTERVAL 10 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(10,2,1,450.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(11,1,5,DATE_SUB(NOW(),INTERVAL 11 DAY),'CONSULTA',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',1200.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(11,1,1,1200.00,NULL);
INSERT INTO autorizacion(afiliado_id,prestador_id,usuario_solicita_id,fecha_solicitud,tipo_servicio,fecha_servicio,tipo_telefono,medico_prescriptor,telefono_contacto,correo_contacto,diagnostico_cie10,diagnostico_desc,monto_solicitado,monto_cubierto,copago,estado,fecha_decision,fecha_vencimiento,nota_revision) VALUES(12,2,6,DATE_SUB(NOW(),INTERVAL 12 DAY),'LABORATORIO',CURDATE(),'CELULAR','Doctor Demo','809-555-0199','demo@example.test','Z00.0','Caso ficticio para clases',450.00,NULL,NULL,'PENDIENTE',NULL,NULL,NULL);
INSERT INTO detalle_autorizacion(autorizacion_id,procedimiento_id,cantidad,monto_solicitado,monto_cubierto) VALUES(12,2,1,450.00,NULL);

-- Se asignan números según los ids reales dentro de una base recién creada.
UPDATE autorizacion SET numero=CONCAT('AUT-',LPAD(id,6,'0')) WHERE numero IS NULL;
-- Reclamación recibida lista para probar con auditor (AUT id=2, prestador=2).
INSERT INTO reclamacion(autorizacion_id,prestador_id,factura_prestador,fecha_servicio,monto_reclamado,monto_glosado,monto_pagar,estado)
 VALUES(2,2,'FACT-DEMO-001',CURDATE(),250.00,0,250.00,'RECIBIDA');
UPDATE reclamacion SET numero=CONCAT('REC-',LPAD(id,6,'0')) WHERE numero IS NULL;
-- Hay otra AUT aprobada (id=4) sin reclamar: probar alta de REC con laboratorio2.

