#!/bin/sh
# Arranca la pantalla virtual y el visor web (http://localhost:6080) y luego ejecuta el comando pedido.
set -e
rm -f /tmp/.X99-lock
Xvfb :99 -screen 0 1920x1080x24 -nolisten tcp >/dev/null 2>&1 &
sleep 1
openbox >/dev/null 2>&1 &
x11vnc -display :99 -forever -shared -nopw -quiet -rfbport 5900 >/dev/null 2>&1 &
websockify --web /usr/share/novnc 6080 localhost:5900 >/dev/null 2>&1 &
exec "$@"
