# Trabajo de Williams Villavizar Hernández — SEG + REP (coordinación técnica)

**Fecha y hora de registro:** 10/10/2026, 16:10
**Estado:** implementado y con pruebas unitarias en verde (`.\mvnw.cmd test` → 53 pruebas, 0 fallos, BUILD SUCCESS).
**Sin commit todavía:** todo está en el árbol de trabajo (rama `main`); ver "Antes de integrar".

> **Lo que NO se ha comprobado:** ninguna pantalla se abrió con `javafx:run`, y las consultas SQL nuevas
> (`UsuarioDAOJdbc`, `BitacoraDAOJdbc`, `BusquedaGlobalDAOJdbc`, `ReporteDAOJdbc`) solo se compilaron; no se ejecutaron
> contra MySQL. Falta esa prueba manual con la base cargada.

---

## 1. Qué se hizo

### 1.1 Bitácora — RF-SEG-07
| Archivo | Contenido |
| --- | --- |
| `model/Bitacora` | POJO: usuario, perfil, fecha/hora, módulo, acción, tabla, registro, detalle |
| `dao/BitacoraDAO`, `dao/impl/BitacoraDAOJdbc` | `insertar` y `buscar(modulo, desde, hasta, limite)` (PreparedStatement) |
| `service/BitacoraService` | `registrar(...)` (nunca lanza excepción), `consultar(...)` y `listarRecientes(n)` (solo Administrador) |
| `BitacoraServiceTest` | 8 pruebas |

**Cambio de esquema:** tabla `bitacora` con columna nueva `perfil VARCHAR(10)`. Si ya tienes la BD creada:
`ALTER TABLE bitacora ADD COLUMN perfil VARCHAR(10) AFTER usuario_id;` (o recrea con los dos scripts).

### 1.2 Usuarios y roles — RF-SEG-04, 05, 06
- `UsuarioDAO` / `UsuarioDAOJdbc`: `buscarPorId`, `listar(filtro)`, `listarPrestadores()` (lectura simple de `prestador`), `insertar`, `actualizar`, `actualizarPassword`.
- `UsuarioService`: `listar`, `listarPrestadores`, `crear`, `editar`, `restablecerClave`, `cambiarEstado(id, activar)`, `desbloquear`. Todo exige rol Administrador. Los usuarios no se borran, se desactivan (RN-10).
- Reglas: usuario único, clave de 8+ caracteres con letras y números, prestador obligatorio para el perfil PRESTADOR y prohibido para INTERNO, un administrador no puede desactivarse ni quitarse su propio rol, una cuenta bloqueada solo se reactiva con "Desbloquear". Bloqueo tras 5 intentos fallidos.
- Pantalla `fxml/interno/usuarios.fxml` + `UsuariosController`: tarjetas por rol (descripción y alcance), tabla con chips de estado y panel lateral (Guardar, Activar/Desactivar, Desbloquear, Restablecer contraseña) con confirmación en acciones irreversibles.
- Bitácora registrada: `LOGIN`, `LOGIN_FALLIDO`, `BLOQUEO_CUENTA`, `CREAR_USUARIO`, `EDITAR_USUARIO`, `RESTABLECER_CLAVE`, `ACTIVAR_USUARIO`, `DESACTIVAR_USUARIO`, `DESBLOQUEAR_USUARIO`.
- `UsuarioServiceTest`: 20 pruebas (caso válido e inválido por regla).

### 1.3 Panel y Reportes — RF-REP-01 a 04
- `dao/ReporteDAO` + `ReporteDAOJdbc` (solo lectura), `service/ReporteService`, modelos `Reporte`, `TablaReporte`, `ResumenPanel`.
- `PanelController` + `panel.fxml`: 4 tarjetas (Pendientes, En revisión, Aprobadas, Negadas), "Acciones rápidas" (los botones se ocultan si el rol no tiene esa pantalla) y "Requieren tu atención". Carga en segundo plano; si falla la BD muestra ceros y un aviso.
- `ReportesController` + `reportes.fxml`: una tarjeta por reporte (Autorizaciones por estado con tiempo promedio de respuesta, Reclamaciones por prestador, Devoluciones con reincidencia, Pagos y cheques, Precertificaciones, Actividad de usuarios), filtros Desde/Hasta y exportación a CSV (UTF-8 con BOM, separador `;`).
- Roles por reporte (decisión del desarrollo, **no viene del PRD; confirmar**): Administrador los 6; Analista: autorizaciones y precertificaciones; Auditor: autorizaciones, reclamaciones, devoluciones y pagos; actividad de usuarios solo Administrador.
- `ReporteServiceTest`: 11 pruebas.

### 1.4 Núcleo, login y recursos — RF-SEG-01/02/03/08/09/10
- **Búsqueda global (RF-SEG-08):** `BuscadorGlobal` en el encabezado de la consola interna (oculto para prestadores), `BusquedaGlobalService/DAO`. Mínimo 3 caracteres, espera de 300 ms, hasta 8 resultados por tipo, atajo Ctrl+K, y solo ofrece lo que el rol puede abrir. Excluye afiliados en borrador. `BusquedaGlobalServiceTest`: 4 pruebas.
- **Ayuda de acceso (RF-SEG-09):** el enlace del login abre un diálogo con contacto ficticio del administrador, configurable con las claves `ayuda.contacto.*` de `db.properties.example`.
- **Log y errores (RF-SEG-10):** `MainApp` escribe `logs/vitasalud.log` (rotación 1 MB × 3) y captura excepciones no controladas; `logs/` está en `.gitignore`. Nuevo `AppConfig.get(clave, porDefecto)`.
- **Recursos de fase 0:** fuente Figtree (Regular, Medium, SemiBold, Bold + `OFL.txt`) en `resources/fonts`, cargada con `Font.loadFont`; logo `img/logo-vitasalud.png` (112 px), `-44` y `-34`, usado en login y barra lateral. *El logo lo dibujó el desarrollo; falta que el equipo lo apruebe contra PRD 11.1.*
- **Datos de prueba:** `HashPruebaTest` confirma que el hash de `02_datos_prueba.sql` corresponde a `Vita2026!`.

---

## 2. Cómo integrarlo con el trabajo de los demás

### Para todos
- **Esquema:** recreen la BD (`01_esquema.sql` y `02_datos_prueba.sql`) para recibir la columna `bitacora.perfil`.
- **Bitácora:** llamen a `new BitacoraService().registrar(usuarioId, "MODULO", "ACCION", "tabla", registroId, "detalle")`. No lanza excepciones, así que no rompe su transacción. Úsenlo al crear, cambiar estado o anular (RF-SEG-07). Con `Sesion.getUsuario().getId()` obtienen el usuario.
- **Estilos:** usen solo clases de `vitasalud.css`; no escriban colores en FXML ni en Java.

### Liannelis Pérez (AFI + PSS)
- **Búsqueda global:** en `initialize()` de `AfiliadosController` y `PrestadoresController`:
  `Navegador.consumirBusquedaPendiente("afiliados").ifPresent(v -> { txtBuscar.setText(v); buscar(); });`
  Para prestadores, el tipo es `"prestadores"` y el valor es el RNC o cédula. Para afiliados el valor es el número de afiliado (`AF-000001`), así que su búsqueda debe aceptar ese formato.
- **Esquema que consultan mis DAO de solo lectura:** `afiliado` (`numero_afiliado`, `numero_documento`, `nss`, `nombres`, `apellidos`, `es_borrador`) y `prestador` (`id`, `nombre`, `rnc_cedula`, `estado_contrato`). Si renombran columnas, avisen y se ajusta `BusquedaGlobalDAOJdbc` y `UsuarioDAOJdbc.listarPrestadores`.
- Al crear usuarios de prestador, el combo de prestadores sale de la tabla `prestador`: necesito que sus datos de prueba incluyan prestadores con `estado_contrato = 'ACTIVO'`.

### Luis Miguel Magallanes (PLA + PRE)
- El portal usa el mismo `Shell`, `Sesion` y `Menu`; sus FXML ya aparecen en el menú. Para el prestador use `Sesion.getPrestadorId()` y filtre siempre en el DAO (RN-07).
- No hay conflicto con `reportes-prestador`: esa pantalla es suya; mi `reportes.fxml` es solo de la consola interna.

### Joel Marte (AUT + REC)
- **Búsqueda global:** `Navegador.consumirBusquedaPendiente("bandeja")` en `BandejaController`; el valor es el número de autorización (`AUT-000001`).
- **Estados que consultan Panel y Reportes** (si cambian algo, avisen y se ajusta `ReporteDAOJdbc`):
  - `autorizacion`: `estado`, `fecha_solicitud`, `fecha_decision`, `numero`, `prestador_id`, `numero_precertificacion`, `estado_precertificacion`.
  - `reclamacion`: `estado` con valores `DEVUELTA`, `PAGADA`, `EN_AUDITORIA`; `fecha_servicio`, `monto_reclamado`, `monto_glosado`, `monto_pagar`, `motivo_devolucion`, `fecha_pago`, `referencia_pago`.
  - Las tarjetas del Panel usan los valores de `EstadoAutorizacion` (Pendiente, En revisión, Aprobada, Negada). `fecha_decision` debe llenarse al decidir, porque de ahí sale el tiempo promedio de respuesta.
- Registren en bitácora la creación y cambios de estado de autorizaciones y reclamaciones.

---

## 3. Antes de integrar (pendiente de Williams)
1. Probar con MySQL real: recrear la BD, `.\mvnw.cmd javafx:run`, entrar con `admin / Vita2026!` y recorrer login, panel, reportes, usuarios y búsqueda global (Ctrl+K).
2. Revisar el logo y los roles por reporte.
3. Crear la rama `feature/seg`, hacer commit y abrir PR para que lo revisen Liannelis y Joel (según el README).

## 4. Pendientes y límites conocidos
- **PDF de reportes (RF-REP-04):** no hecho; el `pom.xml` no tiene librería de PDF y agregar dependencias requiere acuerdo del grupo. CSV sí está.
- `UsuariosController` consulta la BD en el hilo de la interfaz (sin `Task`); el login y el panel sí usan segundo plano.
- El username no se puede cambiar al editar un usuario.
- No implementado porque no era de Williams: cualquier pantalla o DAO de afiliados, prestadores, planes, autorizaciones y reclamaciones.
