# ARS VitaSalud

Sistema de gestión básica para una ARS — aplicación de escritorio en **Java 21 + JavaFX + MySQL 8**.
Proyecto de *Lenguaje de Programación II* (UASD). Requerimientos completos en
[`PRD_ARS_VitaSalud_v1.7.docx`](PRD_ARS_VitaSalud_v1.7.docx).

## 1. Qué debe instalar cada integrante

Los cuatro deben tener **las mismas versiones** (PRD 10.1).

| Herramienta | Versión | Para qué | Cómo instalarla |
| --- | --- | --- | --- |
| JDK | **21 (LTS)** | Compilar y ejecutar | `winget install EclipseAdoptium.Temurin.21.JDK` |
| MySQL Community Server | **8.x** (no MariaDB) | Base de datos | <https://dev.mysql.com/downloads/mysql/> |
| Git | Última | Control de versiones | `winget install Git.Git` |
| VS Code + **Extension Pack for Java** | Última | Editor, depurador y panel de pruebas | <https://code.visualstudio.com/> |
| Scene Builder (opcional) | 21 | Diseñar los FXML con arrastrar y soltar | <https://gluonhq.com/products/scene-builder/> |
| MySQL Workbench (opcional) | 8.0 | Ver las tablas y probar consultas | Viene con el instalador de MySQL |

**No hace falta instalar Maven:** el proyecto incluye `mvnw.cmd`, que lo descarga solo.
JavaFX, MySQL Connector/J, jBCrypt y JUnit 5 también se descargan solos (versiones fijas en `pom.xml`).

Después de instalar el JDK, la variable de entorno `JAVA_HOME` debe apuntar a su carpeta raíz
(la que contiene `bin\javac.exe`, por ejemplo `C:\Program Files\Eclipse Adoptium\jdk-21...`).
Comprueba con:

```powershell
echo $env:JAVA_HOME
java -version      # debe decir 21 o superior
git --version
```

> **XAMPP trae MariaDB, no MySQL 8.** El esquema usa funciones de MySQL 8, así que no sirve.
> Si tienes XAMPP encendido, ocupa el puerto 3306: apágalo o instala MySQL en otro puerto
> (por ejemplo 3307) y ponlo en tu `db.properties`.

## 2. Primer arranque

```powershell
# 1. Clonar y entrar a la carpeta
git clone <url-del-repositorio>
cd Sistema-Ars

# 2. Base de datos (pide la clave de root de MySQL)
mysql -u root -p < database/01_esquema.sql
mysql -u root -p vitasalud < database/02_datos_prueba.sql

# 3. Conexión: copia la plantilla y pon tu clave (db.properties NO se sube a git)
copy db.properties.example db.properties

# 4. Ejecutar la aplicación
.\mvnw.cmd javafx:run
```

Desde VS Code ejecuta la clase **`Launcher`** (no `MainApp`): si corres `MainApp` aparece
"JavaFX runtime components are missing".

Si cambian el esquema SQL, recreen su base con los dos scripts (el primero borra y vuelve a crear `vitasalud`).

Usuarios de prueba (contraseña de todos: `Vita2026!`):

| Perfil en el login | Usuario | Rol |
| --- | --- | --- |
| Personal interno | `admin` | Administrador |
| Personal interno | `analista` | Analista de autorizaciones |
| Personal interno | `auditor` | Auditor médico |
| Personal interno | `agente` | Agente de afiliación |
| Prestador | `clinica1` | Prestador (Clínica Ejemplo Uno) |
| Prestador | `laboratorio2` | Prestador (Laboratorio Ejemplo Dos) — para probar RN-07 |

## 2.1 Trabajar con Docker (sin instalar nada local)

Alternativa a la sección 1: **solo necesitas [Docker Desktop](https://www.docker.com/products/docker-desktop/)**
(no hace falta JDK, Maven ni MySQL). Docker levanta dos contenedores:

- **`db`**: MySQL 8.4 que carga solo `01_esquema.sql` y `02_datos_prueba.sql` la primera vez.
- **`app`**: JDK 21 + Maven con todas las dependencias ya descargadas en la imagen, y la conexión a la base
  de datos incluida (`docker/db.properties.docker`; **no necesitas `db.properties`** local).

```powershell
docker compose up -d --build        # primera vez: tarda unos minutos (después es inmediato)
```

Abre **<http://localhost:6080>** en el navegador y pulsa *Connect*: ahí aparece la ventana de la aplicación
(JavaFX corre en una pantalla virtual dentro del contenedor). Entra con los usuarios de la sección 2
(`admin` / `Vita2026!` por "Personal interno"). Al arrancar, la app compila tu código tal como esté en tu carpeta.

| Quiero… | Comando |
| --- | --- |
| Correr **todas las pruebas** | `docker compose run --rm --no-deps app ./mvnw test` |
| Correr una clase de pruebas | `docker compose run --rm --no-deps app ./mvnw test -Dtest=UsuarioServiceTest` |
| **Ver mis cambios** en la app | `docker compose restart app` (recompila y vuelve a abrir la ventana) |
| Ver el log de la app | `docker compose logs -f app` |
| Abrir una consola SQL | `docker compose exec db mysql -uroot -pvitasalud vitasalud` |
| Apagar todo (conserva los datos) | `docker compose down` |
| **Reiniciar la base** (si cambió el esquema) | `docker compose down -v` y luego `docker compose up -d` |

Detalles:

- Tu código se comparte con el contenedor (carpeta del proyecto montada en `/app`): editas en VS Code como siempre.
- La base está disponible también desde tu PC (por ejemplo MySQL Workbench) en `localhost:3308`, usuario `root`,
  clave `vitasalud`. Si el puerto está ocupado, cambia `DB_PORT` (y `APP_PORT` para el 6080) en un archivo `.env`.
- Si cambia el `pom.xml` (dependencias nuevas), reconstruye la imagen: `docker compose build app`.
- El entorno Docker es **independiente** del MySQL local de la sección 1: puedes usar uno u otro.

## 3. Cómo ejecutar las pruebas

Las pruebas son JUnit 5 y viven en `src/test/java/edu/uasd/vitasalud/`. Los servicios reciben su DAO por el
constructor, así que **las reglas de negocio se prueban con un DAO falso en memoria, sin necesitar MySQL**
(mira `service/UsuarioServiceTest.java` como ejemplo).

```powershell
.\mvnw.cmd test                                   # todas las pruebas
.\mvnw.cmd test -Dtest=UsuarioServiceTest         # una clase
.\mvnw.cmd test -Dtest=UsuarioServiceTest#cuentaSeBloqueaTrasCincoIntentosFallidos   # un método
.\mvnw.cmd test -Dtest="Afiliado*Test"            # por patrón
```

- **En VS Code:** abre el panel *Testing* (icono del matraz) y pulsa ▶ junto a la clase o el método.
- **Resultado:** en la terminal debe terminar con `BUILD SUCCESS`. El detalle queda en `target/surefire-reports/`.
- **Antes de abrir un pull request** corre `.\mvnw.cmd test`; no se integra nada que rompa pruebas.
- Cada regla de negocio (RN-01 a RN-11) lleva **al menos un caso válido y uno inválido**.

Pruebas que ya existen: `app/MenuTest` (menú por rol), `service/UsuarioServiceTest` (login y bloqueo),
`util/ValidadorTest` (cédula, RNC, teléfono, correo).

## 4. Qué archivos le toca a cada integrante

Rutas relativas a `src/main/java/edu/uasd/vitasalud/` (**Java**), `src/main/resources/` (**recursos**),
`src/test/java/edu/uasd/vitasalud/` (**pruebas**) y `database/` (**SQL**).
**(existe)** = ya está en el repositorio, pártelo desde ahí. **(crear)** = lo creas tú.
La lista de pantallas y su ruta exacta está en [`Menu.java`](src/main/java/edu/uasd/vitasalud/app/Menu.java).

### Williams Villavizar Hernández — SEG + REP (coordinación técnica)

| Qué | Dónde |
| --- | --- |
| Núcleo de la app | Java: `app/MainApp`, `Launcher`, `Navegador`, `Sesion`, `Menu` (existen) · `config/AppConfig`, `ConexionBD` (existen) |
| Usuarios y roles | Java: `model/Usuario`, `Rol`, `Perfil` (existen) · `dao/UsuarioDAO`, `dao/impl/UsuarioDAOJdbc` (existen) · `service/UsuarioService` (existe) |
| Bitácora y reportes | Java (crear): `model/Bitacora`, `dao/BitacoraDAO`, `dao/impl/BitacoraDAOJdbc`, `service/ReporteService` |
| Pantallas comunes | Recursos: `fxml/comun/login.fxml`, `shell.fxml` · Java: `controller/comun/LoginController`, `ShellController` (existen) |
| Pantallas de la consola | Recursos (crear): `fxml/interno/panel.fxml`, `reportes.fxml`, `usuarios.fxml` · Java: `controller/interno/PanelController`, `ReportesController`, `UsuariosController` |
| Estilos y componentes | Recursos: `css/vitasalud.css` (existe), `fonts/` y `img/` (faltan Figtree y el logo) · Java: `util/Alertas`, `Chips`, `Formatos` (existen) |
| Tablas SQL | `01_esquema.sql`: `rol`, `usuario`, `bitacora` |
| Pruebas | `service/UsuarioServiceTest`, `app/MenuTest` (existen) |

### Liannelis Pérez Pérez — AFI + PSS

| Qué | Dónde |
| --- | --- |
| Modelo | Java (crear): `model/Afiliado`, `Prestador` · enum `model/EstadoAfiliado` (existe) |
| Acceso a datos | Java (crear): `dao/AfiliadoDAO`, `PrestadorDAO` y `dao/impl/AfiliadoDAOJdbc`, `PrestadorDAOJdbc` |
| Reglas de negocio | Java (crear): `service/AfiliadoService` (RN-01, RN-02, RN-03), `service/PrestadorService` |
| Validaciones compartidas | Java: `util/Validador` (existe: cédula, RNC, teléfono, correo; el resto del equipo la usa) |
| Pantallas | Recursos (crear): `fxml/interno/afiliados.fxml`, `nuevo_afiliado.fxml`, `prestadores.fxml` · Java: `controller/interno/AfiliadosController`, `NuevoAfiliadoController`, `PrestadoresController` |
| Tablas SQL | `01_esquema.sql`: `afiliado`, `provincia`, `municipio`, `prestador` · `02_datos_prueba.sql`: sección AFI/PSS (completar municipios y 10+ afiliados) |
| Pruebas | `util/ValidadorTest` (existe) · crear `service/AfiliadoServiceTest` (RN-01 a RN-03) |

### Luis Miguel Magallanes — PLA + PRE

| Qué | Dónde |
| --- | --- |
| Modelo | Java (crear): `model/Plan`, `Cobertura`, `Procedimiento`, `Tarifa`, `Comunicado` |
| Acceso a datos | Java (crear): `dao/PlanDAO`, `CoberturaDAO`, `ProcedimientoDAO`, `TarifaDAO` y sus `dao/impl/...Jdbc` |
| Reglas de negocio | Java (crear): `service/PlanService`, incluido el método de consulta de cobertura que usa `AutorizacionService` |
| Pantallas de la consola | Recursos (crear): `fxml/interno/planes.fxml` · Java: `controller/interno/PlanesController` |
| Pantallas del portal | Recursos (crear): `fxml/prestador/inicio.fxml`, `busqueda_avanzada.fxml`, `mis_solicitudes.fxml`, `reportes.fxml`, `precertificaciones.fxml` · Java: `controller/prestador/InicioController`, `BusquedaController`, `MisSolicitudesController`, `ReportesPrestadorController`, `PrecertificacionesController` |
| Tablas SQL | `01_esquema.sql`: `plan`, `procedimiento`, `cobertura`, `tarifa`, `comunicado` · `02_datos_prueba.sql`: sección PLA |
| Pruebas | Crear `service/PlanServiceTest` (no desactivar planes con afiliados, cobertura por categoría) |
| Ojo | El portal lee autorizaciones de Joel Marte: usa sus DAO en modo consulta y **siempre filtra por `Sesion.getPrestadorId()`** (RN-07) |

### Joel Marte — AUT + REC

| Qué | Dónde |
| --- | --- |
| Modelo | Java (crear): `model/Autorizacion`, `DetalleAutorizacion`, `Reclamacion`, `Glosa` · enums `model/EstadoAutorizacion`, `EstadoReclamacion` (existen) |
| Acceso a datos | Java (crear): `dao/AutorizacionDAO`, `ReclamacionDAO` y `dao/impl/AutorizacionDAOJdbc`, `ReclamacionDAOJdbc` (con el filtro por prestador, RN-07) |
| Reglas de negocio | Java (crear): `service/AutorizacionService` (RN-04 a RN-06, RN-11), `service/ReclamacionService` (RN-08, RN-09, RN-11) |
| Pantallas de la consola | Recursos (crear): `fxml/interno/bandeja_autorizaciones.fxml`, `reclamaciones.fxml` · Java: `controller/interno/BandejaController`, `ReclamacionesController` |
| Pantallas del portal | Recursos (crear): `fxml/prestador/solicitar_autorizacion.fxml`, `hospitalizacion.fxml` · Java: `controller/prestador/SolicitarController`, `HospitalizacionController` |
| Tablas SQL | `01_esquema.sql`: `autorizacion`, `detalle_autorizacion`, `reclamacion`, `glosa`, `hospitalizacion` |
| Pruebas | Crear `service/AutorizacionServiceTest` y `ReclamacionServiceTest` (RN-04 a RN-09 y RN-11, caso válido e inválido de cada una) |

### Archivos compartidos (avisar al grupo antes de cambiarlos)

- `database/01_esquema.sql` — cada quien edita solo las tablas de su módulo; cualquier cambio de columnas se avisa.
- `database/02_datos_prueba.sql` — cada integrante agrega 10+ registros en **su** sección.
- `app/Menu.java` — si cambia el nombre de tu FXML, actualiza la ruta aquí.
- `css/vitasalud.css` — si necesitas una clase nueva, pídela o agrégala con un pull request pequeño.
- `pom.xml` — agregar dependencias solo con acuerdo del grupo (versiones fijas).

## 5. Estructura del proyecto

```text
src/main/java/edu/uasd/vitasalud/
  app/          MainApp, Launcher, Navegador, Sesion, Menu (pantallas y roles que las ven)
  config/       AppConfig (db.properties), ConexionBD
  model/        POJOs y enums compartidos
  dao/ dao/impl/   interfaces y DAO JDBC (PreparedStatement siempre)
  service/      reglas de negocio RN-01..RN-11, transacciones y permisos
  controller/   comun/ · interno/ · prestador/
  util/         Validador, Alertas, Formatos, Chips
src/main/resources/
  fxml/comun  fxml/interno  fxml/prestador     css/vitasalud.css    fonts/    img/
src/test/java/edu/uasd/vitasalud/   pruebas JUnit 5
database/       01_esquema.sql · 02_datos_prueba.sql
```

> El PRD propone el paquete `do.uasd.vitasalud`, pero `do` es una palabra reservada de Java y no compila;
> por eso el paquete es `edu.uasd.vitasalud`.

## 6. Cómo agregar tu pantalla

1. Crea tu FXML en la ruta indicada en [`Menu.java`](src/main/java/edu/uasd/vitasalud/app/Menu.java)
   (por ejemplo `fxml/interno/afiliados.fxml`) y su controlador en `controller/interno` o `controller/prestador`.
2. Ya aparece en el menú de los roles permitidos. Mientras tu FXML no exista, se muestra un aviso
   "Pantalla en construcción" con tu nombre.
3. Para navegar desde código: `Navegador.irA("nuevo-afiliado")`.
4. **Estilos:** solo clases de `vitasalud.css` (`.card`, `.btn`, `.btn-ghost`, `.btn-danger`, `.field`, `.chip-*`…).
   Prohibido escribir colores en el FXML o en Java.
5. Estados con color: `Chips.crear(EstadoAutorizacion.PENDIENTE)`.
6. Datos del usuario conectado: `Sesion.getRol()`, `Sesion.getPrestadorId()`. Si es prestador, **el DAO debe
   filtrar por `prestador_id`** (RN-07), no solo la pantalla.
7. SQL siempre con `PreparedStatement`; las reglas de negocio van en el `service`, no en el controlador.

## 7. Flujo de trabajo con Git

- Una rama por módulo (`feature/seg`, `feature/afi-pss`, `feature/pla-pre`, `feature/aut-rec`).
  **Nadie trabaja directo en `main`.**
- Cada integración pasa por un *pull request* pequeño, revisado por otro integrante
  (Williams → Liannelis y Joel Marte · Liannelis → Luis Miguel · Luis Miguel → Williams · Joel Marte → Liannelis).
- Williams integra a `main` dos veces por semana.
- Antes del pull request: `git pull origin main`, `.\mvnw.cmd test` y comprobar que la app abre.
- Cambios al esquema SQL: avisar al grupo y modificar `01_esquema.sql` por pull request.

## 8. Trabajo entregado por integrante

- **Williams Villavizar Hernández (SEG + REP)** — 10/10/2026 16:10: ver
  [`docs/TRABAJO_WILLIAMS_VILLAVIZAR.md`](docs/TRABAJO_WILLIAMS_VILLAVIZAR.md) (qué se hizo y cómo integrarlo con los demás módulos).

## 9. Pendiente de la fase 0

- [ ] Agregar la fuente **Figtree** (`Figtree-Regular/Medium/SemiBold/Bold.ttf`, licencia OFL) en `src/main/resources/fonts/`.
      Sin ella la app usa Segoe UI.
- [ ] Logo en `src/main/resources/img/logo-vitasalud.png` (hoy se usa una "V" provisional en `.logo-mark`).
- [ ] Búsqueda global del encabezado (RF-SEG-08, P1) y bitácora (RF-SEG-07, P1).
- [ ] Acordar entre los cuatro el esquema `01_esquema.sql`.
