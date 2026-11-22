package edu.uasd.vitasalud.controller.interno;

import edu.uasd.vitasalud.dao.CoberturaDAO;
import edu.uasd.vitasalud.dao.ComunicadoDAO;
import edu.uasd.vitasalud.dao.PlanDAO;
import edu.uasd.vitasalud.dao.ProcedimientoDAO;
import edu.uasd.vitasalud.dao.TarifaDAO;

import edu.uasd.vitasalud.dao.impl.CoberturaDAOJdbc;
import edu.uasd.vitasalud.dao.impl.ComunicadoDAOJdbc;
import edu.uasd.vitasalud.dao.impl.PlanDAOJdbc;
import edu.uasd.vitasalud.dao.impl.ProcedimientoDAOJdbc;
import edu.uasd.vitasalud.dao.impl.TarifaDAOJdbc;

import edu.uasd.vitasalud.model.Cobertura;
import edu.uasd.vitasalud.model.Comunicado;
import edu.uasd.vitasalud.model.Plan;
import edu.uasd.vitasalud.model.Procedimiento;
import edu.uasd.vitasalud.model.Tarifa;

import edu.uasd.vitasalud.service.PlanService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Collections;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanesController {

    //  Este es el controlador pincipal que gestiona la interfaz y la logica de la pantalla de Planes
    // vinculando los datos  con las tablas en la listas aqui se maneja la logica de eventos
    // Es el cerrebro de la pantalla se encarga de que los botones y la lista funcionen bien.

    @FXML private TableView<Plan> tablaPlanes;
    @FXML private TableColumn<Plan, Integer> colId;
    @FXML private TableColumn<Plan, String> colCodigo;
    @FXML private TableColumn<Plan, String> colNombre;
    @FXML private TableColumn<Plan, String> colDescripcion;
    @FXML private TableColumn<Plan, BigDecimal> colPrima;
    @FXML private TableColumn<Plan, String> colEstado;

    @FXML private TableView<Cobertura> tablaCoberturas;
    @FXML private TableColumn<Cobertura, Long> colCoberturaId;
    @FXML private TableColumn<Cobertura, String> colCategoria;
    @FXML private TableColumn<Cobertura, BigDecimal> colPorcentaje;
    @FXML private TableColumn<Cobertura, BigDecimal> colCopago;
    @FXML private TableColumn<Cobertura, BigDecimal> colTope;
    @FXML private TableColumn<Cobertura, Integer> colCarencia;
    @FXML private TableColumn<Cobertura, Boolean> colPrecertificacion;

    @FXML private TableView<Procedimiento> tablaProcedimientos;
    @FXML private TableColumn<Procedimiento, Long> colProcedimientoId;
    @FXML private TableColumn<Procedimiento, String> colProcedimientoCodigo;
    @FXML private TableColumn<Procedimiento, String> colProcedimientoDescripcion;
    @FXML private TableColumn<Procedimiento, String> colProcedimientoCategoria;
    @FXML private TableColumn<Procedimiento, BigDecimal> colPrecioReferencia;

    @FXML private TableView<Tarifa> tablaTarifas;
    @FXML private TableColumn<Tarifa, Long> colTarifaId;
    @FXML private TableColumn<Tarifa, Long> colTarifaPrestadorId;
    @FXML private TableColumn<Tarifa, Long> colTarifaProcedimientoId;
    @FXML private TableColumn<Tarifa, BigDecimal> colTarifaMonto;

    @FXML private TableView<Comunicado> tablaComunicados;
    @FXML private TableColumn<Comunicado, Integer> colComunicadoId;
    @FXML private TableColumn<Comunicado, String> colComunicadoTitulo;
    @FXML private TableColumn<Comunicado, String> colComunicadoMensaje;
    @FXML private TableColumn<Comunicado, LocalDateTime> colComunicadoFecha;
    @FXML private TableColumn<Comunicado, Boolean> colComunicadoVigente;

    @FXML private ComboBox<String> filtroCategoria;
    @FXML private Label mensaje;

//  Estas son variable de intancia que guardan las herramientas y los datos para que el controlador funcione
//  esto lo que hace es que  prepara los acceso a la base de datos y activa la gestion logica del negocio y

    private final PlanDAO planDAO = new PlanDAOJdbc();
    private final CoberturaDAO coberturaDAO = new CoberturaDAOJdbc();
    private final ProcedimientoDAO procedimientoDAO = new ProcedimientoDAOJdbc();
    private final TarifaDAO tarifaDAO = new TarifaDAOJdbc();
    private final ComunicadoDAO comunicadoDAO = new ComunicadoDAOJdbc();
    private final PlanService planService = new PlanService(planDAO, coberturaDAO);

// creara las lista temporales para organizar la informacion antes de mostrarla.

    private List<Plan> planes = new ArrayList<>();
    private List<Cobertura> coberturas = new ArrayList<>();
    private List<Procedimiento> procedimientos = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Comunicado> comunicados = new ArrayList<>();

    // Este metodo se encarga de abrir la patalla
    // encargandoce de configuar las columnas, mostrando los mensajes iniciales de la tabla y cargando sus datos

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarMensajesVacios();
        cargarDatos();
    }

// Esto es un metodo que vincula cada columna coon el dato especifico del modelo
// y use ProperyValueFactory para que la informacion se muestre correectamente.

    private void configurarColumnas() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrima.setCellValueFactory(new PropertyValueFactory<>("prima_mensual"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colCoberturaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPorcentaje.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
        colCopago.setCellValueFactory(new PropertyValueFactory<>("copago_fijo"));
        colTope.setCellValueFactory(new PropertyValueFactory<>("tope_anual"));
        colCarencia.setCellValueFactory(new PropertyValueFactory<>("dias_carencia"));
        colPrecertificacion.setCellValueFactory(new PropertyValueFactory<>("requiere_precertificacion"));

        colProcedimientoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProcedimientoCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProcedimientoDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colProcedimientoCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioReferencia.setCellValueFactory(new PropertyValueFactory<>("precio_referencia"));

        colTarifaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTarifaPrestadorId.setCellValueFactory(new PropertyValueFactory<>("prestador_id"));
        colTarifaProcedimientoId.setCellValueFactory(new PropertyValueFactory<>("procedimiento_id"));
        colTarifaMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        colComunicadoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colComunicadoTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colComunicadoMensaje.setCellValueFactory(new PropertyValueFactory<>("mensaje"));
        colComunicadoFecha.setCellValueFactory(new PropertyValueFactory<>("fecha_publicacion"));
        colComunicadoVigente.setCellValueFactory(new PropertyValueFactory<>("vigente"));

// Aqui aplique un formato limpio a las columnas especifica  para quue el texto vea mas ordenado

        configurarColumnaTexto(colNombre);
        configurarColumnaTexto(colDescripcion);
        configurarColumnaTexto(colCodigo);
        configurarColumnaTexto(colEstado);
        configurarColumnaTexto(colCategoria);
        configurarColumnaTexto(colProcedimientoCodigo);
        configurarColumnaTexto(colProcedimientoDescripcion);
        configurarColumnaTexto(colProcedimientoCategoria);
        configurarColumnaTexto(colComunicadoTitulo);
        configurarColumnaTexto(colComunicadoMensaje);
    }

// Esto es un metodo que lo que hace es configura y personaliza las tablas actuando como una  fabrica de celda
// ante de  mostrar el texto de la tabla, es para que se vea limpio

    private <S> void configurarColumnaTexto(TableColumn<S, String> columna) {
        columna.setCellFactory(parametro -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacio) {
                super.updateItem(texto, vacio);
                setText(vacio ? null : limpiarTexto(texto));
            }
        });
    }

// Este un metodo que lo que hara es recorrer todos los carracteres de control uno por uno
// para a segurarse que solo que de texto limpio

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);

// FFFD esto es para datos dañados o que tienen errores
// FEFF para que el texto se interprete bien y evita que apparescan problemas con espacios ocultos

            if (!Character.isISOControl(caracter)
                    && caracter != '\uFFFD'
                    && caracter != '\uFEFF') {
                resultado.append(caracter);
            }
        }

        return resultado.toString();
    }

    // Este es un metodo que configurara los mensajes de aviso en las tablas
    // para indicar que no hay datos diponibles

    private void configurarMensajesVacios() {
        tablaPlanes.setPlaceholder(new Label("No hay planes para mostrar."));
        tablaCoberturas.setPlaceholder(new Label("No hay coberturas para mostrar."));
        tablaProcedimientos.setPlaceholder(new Label("No hay procedimientos para mostrar."));
        tablaTarifas.setPlaceholder(new Label("No hay tarifas para mostrar."));
        tablaComunicados.setPlaceholder(new Label("No hay comunicados para mostrar."));
    }

// Este metodo lo que hace es carga los datos desde la base de datos a las lista temporales
// si todo sale bien carga las categorias y refresca las tablas y si hay un fallo muestra un error

    @FXML
    private void cargarDatos() {
        try {
            planes = planService.listarPlanes();
            coberturas = coberturaDAO.listarTodo();
            procedimientos = procedimientoDAO.listarTodo();
            tarifas = tarifaDAO.listarTodo();
            comunicados = comunicadoDAO.listarTodo();

            cargarCategorias();
            mostrarTodosLosDatos();

            mensaje.setText("Datos cargados correctamente. "
                    + "Planes: " + planes.size()
                    + " | Coberturas: " + coberturas.size()
                    + " | Procedimientos: " + procedimientos.size()
                    + " | Tarifas: " + tarifas.size()
                    + " | Comunicados: " + comunicados.size());

        } catch (Exception e) {
            mensaje.setText("No se pudieron cargar todos los datos.");
            mostrarError("Error al cargar los datos: " + e.getMessage());
        }
    }

// Este es un metodo que lo que hace es extraer las categorias las categoria de cobertura y procedimiento
// y la ordena alfabeticamente y las coloca enn el menu desplegable

    private void cargarCategorias() {
        Set<String> categorias = new HashSet<>();

        for (Cobertura cobertura : coberturas) {
            String categoria = cobertura.getCategoria();

            if (categoria != null && !categoria.isBlank()) {
                categorias.add(categoria);
            }
        }

        for (Procedimiento procedimiento : procedimientos) {
            String categoria = procedimiento.getCategoria();

            if (categoria != null && !categoria.isBlank()) {
                categorias.add(categoria);
            }
        }

        List<String> listaCategorias = new ArrayList<>(categorias);

        Collections.sort(listaCategorias, String.CASE_INSENSITIVE_ORDER);

        filtroCategoria.setItems(FXCollections.observableArrayList(listaCategorias));
    }

// Este es un metodo que vincula la collecion de datos de cada modelo con sus respectiva tablas
// Esto lo que hace es asegurar que toda la informacion se muestre en la interfaz

    private void mostrarTodosLosDatos() {
        tablaPlanes.setItems(FXCollections.observableArrayList(planes));
        tablaCoberturas.setItems(FXCollections.observableArrayList(coberturas));
        tablaProcedimientos.setItems(FXCollections.observableArrayList(procedimientos));
        tablaTarifas.setItems(FXCollections.observableArrayList(tarifas));
        tablaComunicados.setItems(FXCollections.observableArrayList(comunicados));
    }

    //Este es un metodo que lo que haces es verificar y friltrar todas las tablas selecionadas al mismo tiempo
    // aseguandose de solo motrarlos registro que cumpla con ese criterio

    @FXML
    private void filtrarCoberturas() {
        String categoria = filtroCategoria.getValue();

        if (categoria == null || categoria.isBlank()) {
            mostrarTodosLosDatos();
            actualizarMensaje("Filtro eliminado. Se muestran todos los registros.");
            return;
        }

        List<Cobertura> coberturasFiltradas = new ArrayList<>();

        for (Cobertura cobertura : coberturas) {
            if (cobertura.getCategoria() != null
                    && cobertura.getCategoria().equalsIgnoreCase(categoria)) {
                coberturasFiltradas.add(cobertura);
            }
        }

        Set<Integer> idsPlanes = new HashSet<>();

        for (Cobertura cobertura : coberturasFiltradas) {
            idsPlanes.add(cobertura.getPlan_id());
        }

        List<Plan> planesFiltrados = new ArrayList<>();

        for (Plan plan : planes) {
            if (idsPlanes.contains(plan.getId())) {
                planesFiltrados.add(plan);
            }
        }

        List<Procedimiento> procedimientosFiltrados = new ArrayList<>();

        for (Procedimiento procedimiento : procedimientos) {
            if (procedimiento.getCategoria() != null
                    && procedimiento.getCategoria().equalsIgnoreCase(categoria)) {
                procedimientosFiltrados.add(procedimiento);
            }
        }

        Set<Long> idsProcedimientos = new HashSet<>();

        for (Procedimiento procedimiento : procedimientosFiltrados) {
            idsProcedimientos.add(procedimiento.getId());
        }

        List<Tarifa> tarifasFiltradas = new ArrayList<>();

        for (Tarifa tarifa : tarifas) {
            if (idsProcedimientos.contains(tarifa.getProcedimiento_id())) {
                tarifasFiltradas.add(tarifa);
            }
        }

// Aqui se actualiza la tablas con los reultados filtrados y muetra y resumen con las cantidades de registro

        tablaPlanes.setItems(FXCollections.observableArrayList(planesFiltrados));
        tablaCoberturas.setItems(FXCollections.observableArrayList(coberturasFiltradas));
        tablaProcedimientos.setItems(FXCollections.observableArrayList(procedimientosFiltrados));
        tablaTarifas.setItems(FXCollections.observableArrayList(tarifasFiltradas));
        tablaComunicados.setItems(FXCollections.observableArrayList(comunicados));

        actualizarMensaje("Filtro: " + categoria
                + " | Planes: " + planesFiltrados.size()
                + " | Coberturas: " + coberturasFiltradas.size()
                + " | Procedimientos: " + procedimientosFiltrados.size()
                + " | Tarifas: " + tarifasFiltradas.size()
                + " | Comunicados sin filtrar: " + comunicados.size());
    }

    @FXML
    private void limpiarFiltro() {
        filtroCategoria.setValue(null);
        cargarDatos();
    }

// Este metodo lo que hace es capturar los datos de entrada para crear un nuevo plan
// y lo envia al servicio para guardarlo en laa base de datos y muetra un mensaje de confirmacion si sale bien

    @FXML
    private void registrarPlan() {
        try {
            // Formulario personalizado en GridPane para capturar todos los datos del plan.
            List<String> datos = mostrarFormulario("Registrar plan",
                    "Código:", "",
                    "Nombre:", "",
                    "Descripción:", "",
                    "Prima mensual:", "",
                    "Estado (ACTIVO/INACTIVO):", "");

            if (datos == null) {
                return;
            }

            String codigo = campoObligatorio(datos, 0);
            String nombre = campoObligatorio(datos, 1);
            String descripcion = campoObligatorio(datos, 2);
            BigDecimal prima = new BigDecimal(campoObligatorio(datos, 3));
            String estado = campoObligatorio(datos, 4);

            planService.registrar(
                    new Plan(0, codigo, nombre, descripcion, prima, estado));

            operacionCorrecta("Plan registrado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo registrar el plan: " + e.getMessage());
        }
    }

// Este es un metodo que se encarga de verificar que hayas selecionado un plan
// y pedirles los datos nuevos para modificarlos y guardar los cambios

    @FXML
    private void actualizarPlan() {
        Plan seleccionado =
                tablaPlanes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un plan de la tabla.");
            return;
        }

        try {
            // El formulario carga los valores actuales y conserva el ID original.
            List<String> datos = mostrarFormulario("Actualizar plan",
                    "Código:", seleccionado.getCodigo(),
                    "Nombre:", seleccionado.getNombre(),
                    "Descripción:", seleccionado.getDescripcion(),
                    "Prima mensual:", seleccionado.getPrima_mensual().toString(),
                    "Estado:", seleccionado.getEstado());

            if (datos == null) {
                return;
            }

            String codigo = campoObligatorio(datos, 0);
            String nombre = campoObligatorio(datos, 1);
            String descripcion = campoObligatorio(datos, 2);
            BigDecimal prima = new BigDecimal(campoObligatorio(datos, 3));
            String estado = campoObligatorio(datos, 4);

            planService.actualizar(
                    new Plan(seleccionado.getId(), codigo, nombre,
                            descripcion, prima, estado));

            operacionCorrecta("Plan actualizado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo actualizar el plan: " + e.getMessage());
        }
    }

// Este es un metodo  que se encarga de selecionar un plan y continuar con su desativacion

    @FXML
    private void eliminarPlan() {
        Plan seleccionado =
                tablaPlanes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un plan de la tabla.");
            return;
        }

        if (!confirmar(
                "¿Deseas desactivar el plan " + seleccionado.getNombre() + "?")) {
            return;
        }

        try {
            planService.eliminar(seleccionado.getId());

            operacionCorrecta("Plan desactivado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo desactivar el plan: " + e.getMessage());
        }
    }

// Este es un metodo que registra la nueva cobertura  y muestra un aviso de que esta registrada correctamente.

    @FXML
    private void registrarCobertura() {
        try {
            // Formulario personalizado con todos los atributos editables de cobertura.
            List<String> datos = mostrarFormulario("Registrar cobertura",
                    "ID del plan:", "",
                    "Categoría:", "",
                    "Porcentaje:", "",
                    "Copago fijo:", "",
                    "Tope anual:", "",
                    "Días de carencia:", "",
                    "¿Requiere precertificación? (true/false):", "");

            if (datos == null) {
                return;
            }

            int planId = Integer.parseInt(campoObligatorio(datos, 0));
            String categoria = campoObligatorio(datos, 1);
            BigDecimal porcentaje = new BigDecimal(campoObligatorio(datos, 2));
            BigDecimal copago = new BigDecimal(campoObligatorio(datos, 3));
            BigDecimal tope = new BigDecimal(campoObligatorio(datos, 4));
            int carencia = Integer.parseInt(campoObligatorio(datos, 5));
            boolean precertificacion = Boolean.parseBoolean(campoObligatorio(datos, 6));

            coberturaDAO.registrar(new Cobertura(0L, planId, categoria,
                    porcentaje, copago, tope, carencia, precertificacion));

            operacionCorrecta("Cobertura registrada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo registrar la cobertura: " + e.getMessage());
        }
    }

// este un metodo que captura los datos actulizados los valida contra las reglas de negocio y actualiza eel registro en la base de datos

    @FXML
    private void actualizarCobertura() {
        Cobertura cobertura =
                tablaCoberturas.getSelectionModel().getSelectedItem();

        if (cobertura == null) {
            mostrarAviso("Selecciona una cobertura de la tabla.");
            return;
        }

        try {
            // Se muestran los datos actuales; el ID de la cobertura no se modifica.
            List<String> datos = mostrarFormulario("Actualizar cobertura",
                    "ID del plan:", String.valueOf(cobertura.getPlan_id()),
                    "Categoría:", cobertura.getCategoria(),
                    "Porcentaje:", cobertura.getPorcentaje().toString(),
                    "Copago fijo:", cobertura.getCopago_fijo().toString(),
                    "Tope anual:", cobertura.getTope_anual().toString(),
                    "Días de carencia:", String.valueOf(cobertura.getDias_carencia()),
                    "¿Requiere precertificación? (true/false):",
                    String.valueOf(cobertura.getRequiere_precertificacion()));

            if (datos == null) {
                return;
            }

            int planId = Integer.parseInt(campoObligatorio(datos, 0));
            String categoria = campoObligatorio(datos, 1);
            BigDecimal porcentaje = new BigDecimal(campoObligatorio(datos, 2));
            BigDecimal copago = new BigDecimal(campoObligatorio(datos, 3));
            BigDecimal tope = new BigDecimal(campoObligatorio(datos, 4));
            int carencia = Integer.parseInt(campoObligatorio(datos, 5));
            boolean precertificacion = Boolean.parseBoolean(campoObligatorio(datos, 6));

            coberturaDAO.actualizar(new Cobertura(cobertura.getId(), planId,
                    categoria, porcentaje, copago, tope, carencia, precertificacion));

            operacionCorrecta("Cobertura actualizada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo actualizar la cobertura: " + e.getMessage());
        }
    }

// Este metodo que seleciona una cobertura y verifica que no este vacia
// y manda un aviso de si dea eliminarla y luego con la confimacion la elimina

    @FXML
    private void eliminarCobertura() {
        Cobertura cobertura =
                tablaCoberturas.getSelectionModel().getSelectedItem();

        if (cobertura == null) {
            mostrarAviso("Selecciona una cobertura de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar la cobertura seleccionada?")) {
            return;
        }

        try {
            coberturaDAO.eliminar(cobertura.getId());

            operacionCorrecta("Cobertura eliminada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar la cobertura: " + e.getMessage());
        }
    }

// Este es un metodo que registra un procedimiento y captura los datos de entrada los validad
// revisa que cumpla con los requisisto y guarda los datos

    @FXML
    private void registrarProcedimiento() {
        try {
            // Formulario personalizado en cuadrícula para los datos del procedimiento.
            List<String> datos = mostrarFormulario("Registrar procedimiento",
                    "Código:", "",
                    "Descripción:", "",
                    "Categoría:", "",
                    "Precio de referencia:", "");

            if (datos == null) {
                return;
            }

            String codigo = campoObligatorio(datos, 0);
            String descripcion = campoObligatorio(datos, 1);
            String categoria = campoObligatorio(datos, 2);
            BigDecimal precio = new BigDecimal(campoObligatorio(datos, 3));

            procedimientoDAO.guardar(new Procedimiento(0L, codigo,
                    descripcion, categoria, precio));

            operacionCorrecta("Procedimiento registrado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo registrar el procedimiento: " + e.getMessage());
        }
    }

// Este es un metodo que captura los datos actualizo para que cumplan las reglas
// y los limites establecidos una vez lo cuumpla el sistema registra los cambios

    @FXML
    private void actualizarProcedimiento() {
        Procedimiento procedimiento =
                tablaProcedimientos.getSelectionModel().getSelectedItem();

        if (procedimiento == null) {
            mostrarAviso("Selecciona un procedimiento de la tabla.");
            return;
        }

        try {
            // El ID se mantiene desde el objeto seleccionado y no se ofrece para edición.
            List<String> datos = mostrarFormulario("Actualizar procedimiento",
                    "Código:", procedimiento.getCodigo(),
                    "Descripción:", procedimiento.getDescripcion(),
                    "Categoría:", procedimiento.getCategoria(),
                    "Precio de referencia:", procedimiento.getPrecio_referencia().toString());

            if (datos == null) {
                return;
            }

            String codigo = campoObligatorio(datos, 0);
            String descripcion = campoObligatorio(datos, 1);
            String categoria = campoObligatorio(datos, 2);
            BigDecimal precio = new BigDecimal(campoObligatorio(datos, 3));

            procedimientoDAO.modificar(new Procedimiento(procedimiento.getId(),
                    codigo, descripcion, categoria, precio));

            operacionCorrecta("Procedimiento actualizado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo actualizar el procedimiento: " + e.getMessage());
        }
    }

// Esto es un metodo que confirma la selecion del procedimiento verifica que no esten vacio y que cumpla
// con todos los criterios establecidos pone una ventana de si desea eliminar y muestra el aviso de existo

    @FXML
    private void eliminarProcedimiento() {
        Procedimiento procedimiento =
                tablaProcedimientos.getSelectionModel().getSelectedItem();

        if (procedimiento == null) {
            mostrarAviso("Selecciona un procedimiento de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar el procedimiento seleccionado?")) {
            return;
        }

        try {
            procedimientoDAO.eliminar(procedimiento.getId());

            operacionCorrecta("Procedimiento eliminado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar el procedimiento: " + e.getMessage());
        }
    }

// Este metodo captura los datos de  la tarifa lo validad y lo registra en la base de datos

    @FXML
    private void registrarTarifa() {
        try {
            // Formulario personalizado para registrar los tres datos de la tarifa.
            List<String> datos = mostrarFormulario("Registrar tarifa",
                    "ID del prestador:", "",
                    "ID del procedimiento:", "",
                    "Monto:", "");

            if (datos == null) {
                return;
            }

            long prestadorId = Long.parseLong(campoObligatorio(datos, 0));
            long procedimientoId = Long.parseLong(campoObligatorio(datos, 1));
            BigDecimal monto = new BigDecimal(campoObligatorio(datos, 2));

            tarifaDAO.registrar(new Tarifa(0L, prestadorId, procedimientoId, monto));

            operacionCorrecta("Tarifa registrada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo registrar la tarifa: " + e.getMessage());
        }
    }

// Esto es un metodo validad que alla una tarifa selecionada verifa que no este vacia
// y solicita los nuevos datos y confirma la actualizacioon en el sistema.

    @FXML
    private void actualizarTarifa() {
        Tarifa tarifa =
                tablaTarifas.getSelectionModel().getSelectedItem();

        if (tarifa == null) {
            mostrarAviso("Selecciona una tarifa de la tabla.");
            return;
        }

        try {
            // Se precargan los datos y se conserva el ID original de la tarifa.
            List<String> datos = mostrarFormulario("Actualizar tarifa",
                    "ID del prestador:", String.valueOf(tarifa.getPrestador_id()),
                    "ID del procedimiento:", String.valueOf(tarifa.getProcedimiento_id()),
                    "Monto:", tarifa.getMonto().toString());

            if (datos == null) {
                return;
            }

            long prestadorId = Long.parseLong(campoObligatorio(datos, 0));
            long procedimientoId = Long.parseLong(campoObligatorio(datos, 1));
            BigDecimal monto = new BigDecimal(campoObligatorio(datos, 2));

            tarifaDAO.actualizar(new Tarifa(tarifa.getId(), prestadorId,
                    procedimientoId, monto));

            operacionCorrecta("Tarifa actualizada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo actualizar la tarifa: " + e.getMessage());
        }
    }

// Este es un metodo que seleciona una tarifa verifica que no este vacio y que cumpla con los criterio
// confirmar que quiere eliminarla y notifica que esta correctamente eliminada

    @FXML
    private void eliminarTarifa() {
        Tarifa tarifa =
                tablaTarifas.getSelectionModel().getSelectedItem();

        if (tarifa == null) {
            mostrarAviso("Selecciona una tarifa de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar la tarifa seleccionada?")) {
            return;
        }

        try {
            tarifaDAO.eliminar(tarifa.getId());

            operacionCorrecta("Tarifa eliminada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar la tarifa: " + e.getMessage());
        }
    }

//Este es un metodo que regitra un nuevo comunicado valida los datodos lo verifica en el sistema
// y confirma la operacion en la pantalla

    @FXML
    private void registrarComunicado() {
        try {
            // El formulario incluye la fecha de publicación y el estado del comunicado.
            List<String> datos = mostrarFormulario("Registrar comunicado",
                    "Título:", "",
                    "Mensaje:", "",
                    "Fecha de publicación (yyyy-MM-ddTHH:mm:ss):", LocalDateTime.now().toString(),
                    "¿Está vigente? (true/false):", "true");

            if (datos == null) {
                return;
            }

            String titulo = campoObligatorio(datos, 0);
            String texto = campoObligatorio(datos, 1);
            LocalDateTime fecha = LocalDateTime.parse(campoObligatorio(datos, 2));
            boolean vigente = Boolean.parseBoolean(campoObligatorio(datos, 3));

            comunicadoDAO.registrar(new Comunicado(0, titulo, texto, fecha, vigente));

            operacionCorrecta("Comunicado registrado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo registrar el comunicado: " + e.getMessage());
        }
    }

// Este es un metodo que captura los datos modificados los valida que no esten vacio
// y actualiza el registro correspondiente en la base de datos para mantener todo al dia

    @FXML
    private void actualizarComunicado() {
        Comunicado comunicado =
                tablaComunicados.getSelectionModel().getSelectedItem();

        if (comunicado == null) {
            mostrarAviso("Selecciona un comunicado de la tabla.");
            return;
        }

        try {
            // Se cargan todos los atributos editables y se conserva el ID del comunicado.
            List<String> datos = mostrarFormulario("Actualizar comunicado",
                    "Título:", comunicado.getTitulo(),
                    "Mensaje:", comunicado.getMensaje(),
                    "Fecha de publicación (yyyy-MM-ddTHH:mm:ss):",
                    comunicado.getFecha_publicacion().toString(),
                    "¿Está vigente? (true/false):", String.valueOf(comunicado.getVigente()));

            if (datos == null) {
                return;
            }

            String titulo = campoObligatorio(datos, 0);
            String texto = campoObligatorio(datos, 1);
            LocalDateTime fecha = LocalDateTime.parse(campoObligatorio(datos, 2));
            boolean vigente = Boolean.parseBoolean(campoObligatorio(datos, 3));

            comunicadoDAO.actualizar(new Comunicado(comunicado.getId(),
                    titulo, texto, fecha, vigente));

            operacionCorrecta("Comunicado actualizado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo actualizar el comunicado: " + e.getMessage());
        }
    }

// Este es un metodo que elimina una comunicado lo hace selecionado un comunicado lo verifica que no este vacio
//  y que este selecionado notifica que si desea eliminarlo y confirmala la eliminacion y se elimina

    @FXML
    private void eliminarComunicado() {
        Comunicado comunicado =
                tablaComunicados.getSelectionModel().getSelectedItem();

        if (comunicado == null) {
            mostrarAviso("Selecciona un comunicado de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar el comunicado seleccionado?")) {
            return;
        }

        try {
            comunicadoDAO.eliminar(comunicado.getId());

            operacionCorrecta("Comunicado eliminado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar el comunicado: " + e.getMessage());
        }
    }

// este es una venta que se despliaga al usurio para pedir que introduca texto
// para el nombre, codigos o desscriciones lo que requiera el sistema

    // Este metodo crea un formulario personalizado con GridPane y un campo de texto
    // por cada atributo editable. Guardar devuelve los valores y Cancelar cierra sin cambios.
    private List<String> mostrarFormulario(String titulo, String... campos) {
        Dialog<List<String>> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText("Complete los campos solicitados");

        ButtonType botonGuardar =
                new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(
                botonGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        List<TextField> entradas = new ArrayList<>();

        for (int i = 0; i < campos.length; i += 2) {
            Label etiqueta = new Label(campos[i]);
            TextField campo = new TextField(i + 1 < campos.length ? campos[i + 1] : "");
            campo.setPrefWidth(300);

            grid.add(etiqueta, 0, i / 2);
            grid.add(campo, 1, i / 2);
            entradas.add(campo);
        }

        dialogo.getDialogPane().setContent(grid);
        dialogo.getDialogPane().setMinWidth(450);

        dialogo.setResultConverter(boton -> {
            if (boton == botonGuardar) {
                List<String> valores = new ArrayList<>();
                for (TextField entrada : entradas) {
                    valores.add(entrada.getText().trim());
                }
                return valores;
            }
            return null;
        });

        Optional<List<String>> resultado = dialogo.showAndWait();
        return resultado.orElse(null);
    }

    // Este metodo verifica que cada campo obligatorio tenga contenido antes de procesarlo.
    private String campoObligatorio(List<String> datos, int indice) {
        String valor = datos.get(indice);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }
        return valor.trim();
    }

    private String pedirTexto(String titulo, String etiqueta) {
        return pedirTexto(titulo, etiqueta, "");
    }

    private String pedirTexto(
            String titulo,
            String etiqueta,
            String valorInicial) {

        TextInputDialog dialogo = new TextInputDialog(valorInicial);

        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setContentText(etiqueta);

        Optional<String> resultado = dialogo.showAndWait();

        return resultado.map(String::trim).orElse(null);
    }

    private String pedirObligatorio(String titulo, String etiqueta) {
        return pedirObligatorio(titulo, etiqueta, "");
    }

// esto verifica que no este vacio

    private String pedirObligatorio(
            String titulo,
            String etiqueta,
            String valorInicial) {

        String valor = pedirTexto(titulo, etiqueta, valorInicial);

        if (valor == null) {
            throw new CanceladoException();
        }

        if (valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Todos los campos son obligatorios.");
        }

        return valor;
    }

// Este metodo que muestra una ventana solicitando una opcion de verdareo o falso
// caturando la decicion del usuario

    private boolean pedirBooleano(String titulo, String pregunta) {
        return pedirBooleano(titulo, pregunta, false);
    }

    private boolean pedirBooleano(
            String titulo,
            String pregunta,
            boolean valorInicial) {

        ChoiceDialog<Boolean> dialogo =
                new ChoiceDialog<>(valorInicial, true, false);

        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setContentText(pregunta);

        Optional<Boolean> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) {
            throw new CanceladoException();
        }

        return resultado.get();
    }

// Este es un metodo que crea una ventana para que decirle al usuario si desea continuar con la accion
//  si es true el sistema interpreta que ha confirmado la accion y la  ejecuta la tarea

    private boolean confirmar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);

        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);

        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

// Este metodo muetra notificaciones y recarga los datos y avisa del existo

    private void operacionCorrecta(String texto) {
        cargarDatos();
        mensaje.setText(texto);
    }

//Cambia un texto en pallata y muestra un aviso con se despliega

    private void actualizarMensaje(String texto) {
        mensaje.setText(texto);
    }

    private void mostrarAviso(String detalle) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText(null);
        alerta.setContentText(detalle);

        alerta.showAndWait();
    }

// Este metodo presenta una alerta  por si hay un error o fallo con el titulo
// y pausando la interfaz hasta que el usuari la reconosca

    private void mostrarError(String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText("Error al procesar esta operación");
        alerta.setContentText(detalle);
        alerta.getDialogPane().setMinWidth(450);

        alerta.showAndWait();
    }

// Esto se usa para manejar las interruciones en los flujos anteriores

    private static class CanceladoException extends RuntimeException {
    }
}