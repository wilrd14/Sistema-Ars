-- Para bases ya creadas con un 01_esquema.sql anterior (ejecutar una sola vez).
ALTER TABLE autorizacion
    ADD COLUMN fecha_servicio DATE AFTER medico_prescriptor,
    ADD COLUMN tipo_telefono  VARCHAR(15) AFTER fecha_servicio;
