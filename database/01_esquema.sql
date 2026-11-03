-- ARS VitaSalud — esquema MySQL 8.x (PRD 9.4)
-- PROPUESTA INICIAL: el equipo debe revisarla y acordarla antes de programar (PRD 10.1).
-- Cada integrante es dueño de las tablas de su módulo; cualquier cambio se avisa al grupo
-- y se hace por pull request sobre este archivo.
--
-- Uso:  mysql -u root -p < database/01_esquema.sql
-- Convenciones: estados como VARCHAR con los valores de los enums de model/ (sin borrado físico, RN-10);
-- montos en DECIMAL(12,2); números de documento (AF-000001, AUT-000001, REC-000001) los asigna el DAO
-- dentro de la misma transacción del INSERT, a partir del id.

DROP DATABASE IF EXISTS vitasalud;
CREATE DATABASE vitasalud CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE vitasalud;

-- ======================= SEG (Williams Villavizar) =======================

CREATE TABLE rol (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    codigo  VARCHAR(40)  NOT NULL UNIQUE,   -- valores de model.Rol
    nombre  VARCHAR(80)  NOT NULL
) ENGINE=InnoDB;

-- ======================= AFI / PSS (Liannelis Pérez) =======================

CREATE TABLE provincia (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE municipio (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    provincia_id  INT NOT NULL,
    nombre        VARCHAR(80) NOT NULL,
    UNIQUE (provincia_id, nombre),
    FOREIGN KEY (provincia_id) REFERENCES provincia (id)
) ENGINE=InnoDB;

CREATE TABLE prestador (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    rnc_cedula       VARCHAR(15)  NOT NULL UNIQUE,   -- RNC (9) o cédula del médico independiente
    nombre           VARCHAR(150) NOT NULL,
    tipo             VARCHAR(30)  NOT NULL,          -- CLINICA, HOSPITAL, LABORATORIO, CENTRO_DIAGNOSTICO, MEDICO_INDEPENDIENTE, FARMACIA
    direccion        VARCHAR(250),
    provincia_id     INT,
    telefono         VARCHAR(20),
    correo           VARCHAR(120),
    estado_contrato  VARCHAR(15)  NOT NULL DEFAULT 'ACTIVO',   -- ACTIVO, SUSPENDIDO, TERMINADO
    FOREIGN KEY (provincia_id) REFERENCES provincia (id)
) ENGINE=InnoDB;

-- ======================= PLA (Luis Miguel Magallanes) =======================

CREATE TABLE plan (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    codigo        VARCHAR(20)  NOT NULL UNIQUE,
    nombre        VARCHAR(80)  NOT NULL,
    descripcion   VARCHAR(250),
    prima_mensual DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO'      -- ACTIVO, INACTIVO
) ENGINE=InnoDB;

CREATE TABLE procedimiento (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo             VARCHAR(20)  NOT NULL UNIQUE,
    descripcion        VARCHAR(200) NOT NULL,
    categoria          VARCHAR(20)  NOT NULL,   -- CONSULTA, LABORATORIO, IMAGENES, EMERGENCIA, HOSPITALIZACION, CIRUGIA, TERAPIA, MEDICAMENTOS
    precio_referencia  DECIMAL(12,2) NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE cobertura (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id                    INT NOT NULL,
    categoria                  VARCHAR(20) NOT NULL,
    porcentaje                 DECIMAL(5,2) NOT NULL,          -- 0 a 100
    copago_fijo                DECIMAL(12,2),                  -- null si no aplica
    tope_anual                 DECIMAL(12,2) NOT NULL,         -- RD$ por afiliado y categoría
    dias_carencia              INT NOT NULL DEFAULT 0,
    requiere_precertificacion  BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (plan_id, categoria),
    FOREIGN KEY (plan_id) REFERENCES plan (id),
    CHECK (porcentaje BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE tarifa (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    prestador_id     BIGINT NOT NULL,
    procedimiento_id BIGINT NOT NULL,
    monto            DECIMAL(12,2) NOT NULL,
    UNIQUE (prestador_id, procedimiento_id),
    FOREIGN KEY (prestador_id) REFERENCES prestador (id),
    FOREIGN KEY (procedimiento_id) REFERENCES procedimiento (id)
) ENGINE=InnoDB;

CREATE TABLE comunicado (   -- P2
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    titulo             VARCHAR(150) NOT NULL,
    mensaje            TEXT NOT NULL,
    fecha_publicacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vigente            BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ======================= SEG: usuarios y bitácora =======================

CREATE TABLE usuario (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(120) NOT NULL,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password_hash     VARCHAR(100) NOT NULL,          -- BCrypt
    perfil            VARCHAR(10)  NOT NULL,          -- INTERNO, PRESTADOR (model.Perfil)
    rol_id            INT NOT NULL,
    prestador_id      BIGINT,                         -- obligatorio si perfil = PRESTADOR
    cargo             VARCHAR(80),
    estado            VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',   -- ACTIVO, INACTIVO, BLOQUEADO
    intentos_fallidos INT NOT NULL DEFAULT 0,
    FOREIGN KEY (rol_id) REFERENCES rol (id),
    FOREIGN KEY (prestador_id) REFERENCES prestador (id),
    CHECK (perfil <> 'PRESTADOR' OR prestador_id IS NOT NULL)
) ENGINE=InnoDB;

CREATE TABLE bitacora (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT,
    perfil      VARCHAR(10),                  -- INTERNO, PRESTADOR (perfil del usuario al actuar)
    fecha_hora  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modulo      VARCHAR(30)  NOT NULL,
    accion      VARCHAR(60)  NOT NULL,
    tabla       VARCHAR(40),
    registro_id BIGINT,
    detalle     VARCHAR(500),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

-- ======================= AFI =======================

CREATE TABLE afiliado (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_afiliado   VARCHAR(12) UNIQUE,              -- AF-000001, lo asigna el DAO tras el INSERT
    tipo_documento    VARCHAR(20) NOT NULL,            -- CEDULA, PASAPORTE, ACTA_NACIMIENTO
    numero_documento  VARCHAR(30) NOT NULL,
    nss               VARCHAR(20) NOT NULL,
    nombres           VARCHAR(100) NOT NULL,
    apellidos         VARCHAR(100) NOT NULL,
    fecha_nacimiento  DATE NOT NULL,
    sexo              CHAR(1) NOT NULL,                -- M, F
    estado_civil      VARCHAR(20),
    nacionalidad      VARCHAR(40),
    tipo_telefono     VARCHAR(15),                     -- CELULAR, RESIDENCIAL, TRABAJO
    telefono          VARCHAR(20),
    correo            VARCHAR(120),
    provincia_id      INT,
    municipio_id      INT,
    sector            VARCHAR(80),
    direccion         VARCHAR(250),
    plan_id           INT NOT NULL,
    tipo_afiliado     VARCHAR(12) NOT NULL,            -- TITULAR, DEPENDIENTE
    titular_id        BIGINT,                          -- obligatorio si es DEPENDIENTE
    parentesco        VARCHAR(20),
    es_estudiante     BOOLEAN NOT NULL DEFAULT FALSE,
    inicio_cobertura  DATE NOT NULL,
    empleador_rnc     VARCHAR(15),
    estado            VARCHAR(12) NOT NULL DEFAULT 'ACTIVO',   -- model.EstadoAfiliado
    motivo_estado     VARCHAR(250),
    es_borrador       BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tipo_documento, numero_documento),         -- RN-01
    UNIQUE (nss),                                      -- RN-01
    FOREIGN KEY (provincia_id) REFERENCES provincia (id),
    FOREIGN KEY (municipio_id) REFERENCES municipio (id),
    FOREIGN KEY (plan_id) REFERENCES plan (id),
    FOREIGN KEY (titular_id) REFERENCES afiliado (id)
) ENGINE=InnoDB;

-- ======================= AUT + REC ([Integrante 4]) =======================

CREATE TABLE autorizacion (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero                    VARCHAR(12) UNIQUE,       -- AUT-000001, lo asigna el DAO tras el INSERT
    afiliado_id               BIGINT NOT NULL,
    prestador_id              BIGINT NOT NULL,
    usuario_solicita_id       BIGINT NOT NULL,
    usuario_revisa_id         BIGINT,
    fecha_solicitud           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision            DATETIME,
    tipo_servicio             VARCHAR(30),
    medico_prescriptor        VARCHAR(120),
    telefono_contacto         VARCHAR(20),
    correo_contacto           VARCHAR(120),
    diagnostico_cie10         VARCHAR(10),
    diagnostico_desc          VARCHAR(250),
    origen_laboral            BOOLEAN NOT NULL DEFAULT FALSE,   -- campo "ARL" del diseño
    monto_solicitado          DECIMAL(12,2) NOT NULL DEFAULT 0,
    monto_cubierto            DECIMAL(12,2),
    copago                    DECIMAL(12,2),
    estado                    VARCHAR(12) NOT NULL DEFAULT 'PENDIENTE',   -- model.EstadoAutorizacion
    nota_revision             VARCHAR(500),
    fecha_vencimiento         DATE,
    numero_precertificacion   VARCHAR(20),
    estado_precertificacion   VARCHAR(15),
    FOREIGN KEY (afiliado_id) REFERENCES afiliado (id),
    FOREIGN KEY (prestador_id) REFERENCES prestador (id),
    FOREIGN KEY (usuario_solicita_id) REFERENCES usuario (id),
    FOREIGN KEY (usuario_revisa_id) REFERENCES usuario (id),
    INDEX idx_aut_prestador_estado (prestador_id, estado),   -- RN-07: toda consulta del portal filtra por prestador
    INDEX idx_aut_estado_fecha (estado, fecha_solicitud)
) ENGINE=InnoDB;

CREATE TABLE detalle_autorizacion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    autorizacion_id  BIGINT NOT NULL,
    procedimiento_id BIGINT NOT NULL,
    cantidad         INT NOT NULL DEFAULT 1,
    monto_solicitado DECIMAL(12,2) NOT NULL,
    monto_cubierto   DECIMAL(12,2),
    FOREIGN KEY (autorizacion_id) REFERENCES autorizacion (id),
    FOREIGN KEY (procedimiento_id) REFERENCES procedimiento (id)
) ENGINE=InnoDB;

CREATE TABLE reclamacion (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero              VARCHAR(12) UNIQUE,             -- REC-000001
    autorizacion_id     BIGINT NOT NULL UNIQUE,         -- RN-08: una reclamación por autorización
    prestador_id        BIGINT NOT NULL,
    factura_prestador   VARCHAR(40) NOT NULL,
    fecha_servicio      DATE NOT NULL,
    monto_reclamado     DECIMAL(12,2) NOT NULL,
    monto_glosado       DECIMAL(12,2) NOT NULL DEFAULT 0,
    monto_pagar         DECIMAL(12,2),
    estado              VARCHAR(12) NOT NULL DEFAULT 'RECIBIDA',   -- model.EstadoReclamacion
    motivo_devolucion   VARCHAR(250),
    fecha_pago          DATE,
    referencia_pago     VARCHAR(40),
    FOREIGN KEY (autorizacion_id) REFERENCES autorizacion (id),
    FOREIGN KEY (prestador_id) REFERENCES prestador (id),
    INDEX idx_rec_prestador_estado (prestador_id, estado)
) ENGINE=InnoDB;

CREATE TABLE glosa (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reclamacion_id  BIGINT NOT NULL,
    motivo          VARCHAR(40) NOT NULL,   -- SERVICIO_NO_AUTORIZADO, MONTO_SUPERIOR_TARIFA, DOCUMENTACION_INCOMPLETA, OTRO
    monto           DECIMAL(12,2) NOT NULL,
    observacion     VARCHAR(250),
    usuario_id      BIGINT NOT NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reclamacion_id) REFERENCES reclamacion (id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE hospitalizacion (   -- P2
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    autorizacion_id  BIGINT NOT NULL,
    fecha_ingreso    DATETIME NOT NULL,
    fecha_egreso     DATETIME,
    observacion      VARCHAR(250),
    FOREIGN KEY (autorizacion_id) REFERENCES autorizacion (id)
) ENGINE=InnoDB;
