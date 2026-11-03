#!/bin/sh
# Se ejecuta solo la primera vez que se crea el volumen de MySQL: carga el esquema y los datos de prueba.
set -e
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 < /sql/01_esquema.sql
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 vitasalud < /sql/02_datos_prueba.sql
