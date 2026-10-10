# Entorno de desarrollo de ARS VitaSalud: JDK 21 + Maven (wrapper) + dependencias ya descargadas
# + pantalla virtual con acceso desde el navegador (noVNC) para ver la ventana JavaFX.
# La conexión a MySQL (servicio "db" de docker-compose.yml) viene incluida en la imagen.
FROM eclipse-temurin:21-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y --no-install-recommends \
        xvfb x11vnc novnc websockify openbox imagemagick netcat-openbsd \
        libgtk-3-0 libgl1 libxxf86vm1 libxtst6 libxrender1 libasound2 \
        fontconfig fonts-dejavu-core \
    && ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Se descargan Maven y todas las dependencias del pom.xml dentro de la imagen (capa cacheada):
# después se puede compilar y probar sin internet. Solo se vuelve a ejecutar si cambia el pom.xml.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -q -B dependency:go-offline

# Conexión a la base de datos incluida en la imagen (apunta al servicio "db").
COPY docker/db.properties.docker /etc/vitasalud/db.properties
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/entrypoint.sh && chmod +x /usr/local/bin/entrypoint.sh

ENV DISPLAY=:99 \
    JAVA_TOOL_OPTIONS="-Dvitasalud.config=/etc/vitasalud/db.properties -Dprism.order=sw -Dfile.encoding=UTF-8"

EXPOSE 6080
ENTRYPOINT ["entrypoint.sh"]
CMD ["./mvnw", "-B", "javafx:run"]
