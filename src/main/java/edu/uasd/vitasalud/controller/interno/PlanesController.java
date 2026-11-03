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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanesController {

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

    private final PlanDAO planDAO = new PlanDAOJdbc();
    private final CoberturaDAO coberturaDAO = new CoberturaDAOJdbc();
    private final ProcedimientoDAO procedimientoDAO = new ProcedimientoDAOJdbc();
    private final TarifaDAO tarifaDAO = new TarifaDAOJdbc();
    private final ComunicadoDAO comunicadoDAO = new ComunicadoDAOJdbc();

    private final PlanService planService =
            new PlanService(planDAO, coberturaDAO);

    private List<Plan> planes = new ArrayList<>();
    private List<Cobertura> coberturas = new ArrayList<>();
    private List<Procedimiento> procedimientos = new ArrayList<>();
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Comunicado> comunicados = new ArrayList<>();

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarMensajesVacios();
        cargarDatos();
    }

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
        colPrecertificacion.setCellValueFactory(
                new PropertyValueFactory<>("requiere_precertificacion"));

        colProcedimientoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProcedimientoCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProcedimientoDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colProcedimientoCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecioReferencia.setCellValueFactory(
                new PropertyValueFactory<>("precio_referencia"));

        colTarifaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTarifaPrestadorId.setCellValueFactory(new PropertyValueFactory<>("prestador_id"));
        colTarifaProcedimientoId.setCellValueFactory(
                new PropertyValueFactory<>("procedimiento_id"));
        colTarifaMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        colComunicadoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colComunicadoTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colComunicadoMensaje.setCellValueFactory(new PropertyValueFactory<>("mensaje"));
        colComunicadoFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha_publicacion"));
        colComunicadoVigente.setCellValueFactory(new PropertyValueFactory<>("vigente"));

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

    private <S> void configurarColumnaTexto(TableColumn<S, String> columna) {
        columna.setCellFactory(parametro -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacio) {
                super.updateItem(texto, vacio);
                setText(vacio ? null : limpiarTexto(texto));
            }
        });
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);

            if (!Character.isISOControl(caracter)
                    && caracter != '\uFFFD'
                    && caracter != '\uFEFF') {
                resultado.append(caracter);
            }
        }

        return resultado.toString();
    }

    private void configurarMensajesVacios() {
        tablaPlanes.setPlaceholder(new Label("No hay planes para mostrar."));
        tablaCoberturas.setPlaceholder(new Label("No hay coberturas para mostrar."));
        tablaProcedimientos.setPlaceholder(new Label("No hay procedimientos para mostrar."));
        tablaTarifas.setPlaceholder(new Label("No hay tarifas para mostrar."));
        tablaComunicados.setPlaceholder(new Label("No hay comunicados para mostrar."));
    }


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

    private void cargarCategorias() {
        Set<String> categorias = new HashSet<>();

        coberturas.stream()
                .map(Cobertura::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .forEach(categorias::add);

        procedimientos.stream()
                .map(Procedimiento::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .forEach(categorias::add);

        List<String> listaCategorias = categorias.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        filtroCategoria.setItems(
                FXCollections.observableArrayList(listaCategorias));
    }

    private void mostrarTodosLosDatos() {
        tablaPlanes.setItems(FXCollections.observableArrayList(planes));
        tablaCoberturas.setItems(FXCollections.observableArrayList(coberturas));
        tablaProcedimientos.setItems(FXCollections.observableArrayList(procedimientos));
        tablaTarifas.setItems(FXCollections.observableArrayList(tarifas));
        tablaComunicados.setItems(FXCollections.observableArrayList(comunicados));
    }


    @FXML
    private void filtrarCoberturas() {
        String categoria = filtroCategoria.getValue();

        if (categoria == null || categoria.isBlank()) {
            mostrarTodosLosDatos();
            actualizarMensaje("Filtro eliminado. Se muestran todos los registros.");
            return;
        }

        List<Cobertura> coberturasFiltradas = coberturas.stream()
                .filter(c -> c.getCategoria() != null
                        && c.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());

        Set<Integer> idsPlanes = coberturasFiltradas.stream()
                .map(Cobertura::getPlan_id)
                .collect(Collectors.toSet());

        List<Plan> planesFiltrados = planes.stream()
                .filter(p -> idsPlanes.contains(p.getId()))
                .collect(Collectors.toList());

        List<Procedimiento> procedimientosFiltrados = procedimientos.stream()
                .filter(p -> p.getCategoria() != null
                        && p.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());

        Set<Long> idsProcedimientos = procedimientosFiltrados.stream()
                .map(Procedimiento::getId)
                .collect(Collectors.toSet());

        List<Tarifa> tarifasFiltradas = tarifas.stream()
                .filter(t -> idsProcedimientos.contains(t.getProcedimiento_id()))
                .collect(Collectors.toList());

        tablaPlanes.setItems(FXCollections.observableArrayList(planesFiltrados));
        tablaCoberturas.setItems(FXCollections.observableArrayList(coberturasFiltradas));
        tablaProcedimientos.setItems(
                FXCollections.observableArrayList(procedimientosFiltrados));
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


    @FXML
    private void registrarPlan() {
        try {
            String codigo = pedirObligatorio("Registrar plan", "Código:");
            String nombre = pedirObligatorio("Registrar plan", "Nombre:");
            String descripcion = pedirTexto("Registrar plan", "Descripción:");

            if (descripcion == null) {
                return;
            }

            BigDecimal prima = new BigDecimal(
                    pedirObligatorio("Registrar plan", "Prima mensual:"));

            String estado = pedirObligatorio(
                    "Registrar plan", "Estado (ACTIVO/INACTIVO):");

            planService.registrar(
                    new Plan(0, codigo, nombre, descripcion, prima, estado));

            operacionCorrecta("Plan registrado correctamente.");

        } catch (CanceladoException e) {

        } catch (Exception e) {
            mostrarError("No se pudo registrar el plan: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarPlan() {
        Plan seleccionado =
                tablaPlanes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAviso("Selecciona un plan de la tabla.");
            return;
        }

        try {
            String codigo = pedirObligatorio(
                    "Actualizar plan", "Código:", seleccionado.getCodigo());

            String nombre = pedirObligatorio(
                    "Actualizar plan", "Nombre:", seleccionado.getNombre());

            String descripcion = pedirTexto(
                    "Actualizar plan", "Descripción:", seleccionado.getDescripcion());

            if (descripcion == null) {
                return;
            }

            BigDecimal prima = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar plan",
                            "Prima mensual:",
                            seleccionado.getPrima_mensual().toString()));

            String estado = pedirObligatorio(
                    "Actualizar plan", "Estado:", seleccionado.getEstado());

            planService.actualizar(
                    new Plan(
                            seleccionado.getId(),
                            codigo,
                            nombre,
                            descripcion,
                            prima,
                            estado
                    )
            );

            operacionCorrecta("Plan actualizado correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el plan: " + e.getMessage());
        }
    }

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


    @FXML
    private void registrarCobertura() {
        try {
            int planId = Integer.parseInt(
                    pedirObligatorio("Registrar cobertura", "ID del plan:"));

            String categoria = pedirObligatorio(
                    "Registrar cobertura", "Categoría:");

            BigDecimal porcentaje = new BigDecimal(
                    pedirObligatorio("Registrar cobertura", "Porcentaje:"));

            BigDecimal copago = new BigDecimal(
                    pedirObligatorio("Registrar cobertura", "Copago fijo:"));

            BigDecimal tope = new BigDecimal(
                    pedirObligatorio("Registrar cobertura", "Tope anual:"));

            int carencia = Integer.parseInt(
                    pedirObligatorio("Registrar cobertura", "Días de carencia:"));

            boolean precertificacion = pedirBooleano(
                    "Registrar cobertura", "¿Requiere precertificación?");

            coberturaDAO.registrar(
                    new Cobertura(
                            0L,
                            planId,
                            categoria,
                            porcentaje,
                            copago,
                            tope,
                            carencia,
                            precertificacion
                    )
            );

            operacionCorrecta("Cobertura registrada correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo registrar la cobertura: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarCobertura() {
        Cobertura c =
                tablaCoberturas.getSelectionModel().getSelectedItem();

        if (c == null) {
            mostrarAviso("Selecciona una cobertura de la tabla.");
            return;
        }

        try {
            int planId = Integer.parseInt(
                    pedirObligatorio(
                            "Actualizar cobertura",
                            "ID del plan:",
                            String.valueOf(c.getPlan_id())));

            String categoria = pedirObligatorio(
                    "Actualizar cobertura", "Categoría:", c.getCategoria());

            BigDecimal porcentaje = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar cobertura",
                            "Porcentaje:",
                            c.getPorcentaje().toString()));

            BigDecimal copago = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar cobertura",
                            "Copago fijo:",
                            c.getCopago_fijo().toString()));

            BigDecimal tope = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar cobertura",
                            "Tope anual:",
                            c.getTope_anual().toString()));

            int carencia = Integer.parseInt(
                    pedirObligatorio(
                            "Actualizar cobertura",
                            "Días de carencia:",
                            String.valueOf(c.getDias_carencia())));

            boolean precertificacion = pedirBooleano(
                    "Actualizar cobertura",
                    "¿Requiere precertificación?",
                    c.getRequiere_precertificacion());

            coberturaDAO.actualizar(
                    new Cobertura(
                            c.getId(),
                            planId,
                            categoria,
                            porcentaje,
                            copago,
                            tope,
                            carencia,
                            precertificacion
                    )
            );

            operacionCorrecta("Cobertura actualizada correctamente.");

        } catch (CanceladoException e) {
         
        } catch (Exception e) {
            mostrarError("No se pudo actualizar la cobertura: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarCobertura() {
        Cobertura c =
                tablaCoberturas.getSelectionModel().getSelectedItem();

        if (c == null) {
            mostrarAviso("Selecciona una cobertura de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar la cobertura seleccionada?")) {
            return;
        }

        try {
            coberturaDAO.eliminar(c.getId());

            operacionCorrecta("Cobertura eliminada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar la cobertura: " + e.getMessage());
        }
    }


    @FXML
    private void registrarProcedimiento() {
        try {
            String codigo = pedirObligatorio(
                    "Registrar procedimiento", "Código:");

            String descripcion = pedirObligatorio(
                    "Registrar procedimiento", "Descripción:");

            String categoria = pedirObligatorio(
                    "Registrar procedimiento", "Categoría:");

            BigDecimal precio = new BigDecimal(
                    pedirObligatorio(
                            "Registrar procedimiento", "Precio de referencia:"));

            procedimientoDAO.guardar(
                    new Procedimiento(
                            0L,
                            codigo,
                            descripcion,
                            categoria,
                            precio
                    )
            );

            operacionCorrecta("Procedimiento registrado correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo registrar el procedimiento: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarProcedimiento() {
        Procedimiento p =
                tablaProcedimientos.getSelectionModel().getSelectedItem();

        if (p == null) {
            mostrarAviso("Selecciona un procedimiento de la tabla.");
            return;
        }

        try {
            String codigo = pedirObligatorio(
                    "Actualizar procedimiento", "Código:", p.getCodigo());

            String descripcion = pedirObligatorio(
                    "Actualizar procedimiento",
                    "Descripción:",
                    p.getDescripcion());

            String categoria = pedirObligatorio(
                    "Actualizar procedimiento",
                    "Categoría:",
                    p.getCategoria());

            BigDecimal precio = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar procedimiento",
                            "Precio de referencia:",
                            p.getPrecio_referencia().toString()));

            procedimientoDAO.modificar(
                    new Procedimiento(
                            p.getId(),
                            codigo,
                            descripcion,
                            categoria,
                            precio
                    )
            );

            operacionCorrecta("Procedimiento actualizado correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el procedimiento: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarProcedimiento() {
        Procedimiento p =
                tablaProcedimientos.getSelectionModel().getSelectedItem();

        if (p == null) {
            mostrarAviso("Selecciona un procedimiento de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar el procedimiento seleccionado?")) {
            return;
        }

        try {
            procedimientoDAO.eliminar(p.getId());

            operacionCorrecta("Procedimiento eliminado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar el procedimiento: " + e.getMessage());
        }
    }


    @FXML
    private void registrarTarifa() {
        try {
            long prestadorId = Long.parseLong(
                    pedirObligatorio("Registrar tarifa", "ID del prestador:"));

            long procedimientoId = Long.parseLong(
                    pedirObligatorio("Registrar tarifa", "ID del procedimiento:"));

            BigDecimal monto = new BigDecimal(
                    pedirObligatorio("Registrar tarifa", "Monto:"));

            tarifaDAO.registrar(
                    new Tarifa(
                            0L,
                            prestadorId,
                            procedimientoId,
                            monto
                    )
            );

            operacionCorrecta("Tarifa registrada correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo registrar la tarifa: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarTarifa() {
        Tarifa t =
                tablaTarifas.getSelectionModel().getSelectedItem();

        if (t == null) {
            mostrarAviso("Selecciona una tarifa de la tabla.");
            return;
        }

        try {
            long prestadorId = Long.parseLong(
                    pedirObligatorio(
                            "Actualizar tarifa",
                            "ID del prestador:",
                            String.valueOf(t.getPrestador_id())));

            long procedimientoId = Long.parseLong(
                    pedirObligatorio(
                            "Actualizar tarifa",
                            "ID del procedimiento:",
                            String.valueOf(t.getProcedimiento_id())));

            BigDecimal monto = new BigDecimal(
                    pedirObligatorio(
                            "Actualizar tarifa",
                            "Monto:",
                            t.getMonto().toString()));

            tarifaDAO.actualizar(
                    new Tarifa(
                            t.getId(),
                            prestadorId,
                            procedimientoId,
                            monto
                    )
            );

            operacionCorrecta("Tarifa actualizada correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo actualizar la tarifa: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarTarifa() {
        Tarifa t =
                tablaTarifas.getSelectionModel().getSelectedItem();

        if (t == null) {
            mostrarAviso("Selecciona una tarifa de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar la tarifa seleccionada?")) {
            return;
        }

        try {
            tarifaDAO.eliminar(t.getId());

            operacionCorrecta("Tarifa eliminada correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar la tarifa: " + e.getMessage());
        }
    }


    @FXML
    private void registrarComunicado() {
        try {
            String titulo = pedirObligatorio(
                    "Registrar comunicado", "Título:");

            String texto = pedirObligatorio(
                    "Registrar comunicado", "Mensaje:");

            boolean vigente = pedirBooleano(
                    "Registrar comunicado", "¿Está vigente?");

            comunicadoDAO.registrar(
                    new Comunicado(
                            0,
                            titulo,
                            texto,
                            LocalDateTime.now(),
                            vigente
                    )
            );

            operacionCorrecta("Comunicado registrado correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo registrar el comunicado: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarComunicado() {
        Comunicado c =
                tablaComunicados.getSelectionModel().getSelectedItem();

        if (c == null) {
            mostrarAviso("Selecciona un comunicado de la tabla.");
            return;
        }

        try {
            String titulo = pedirObligatorio(
                    "Actualizar comunicado", "Título:", c.getTitulo());

            String texto = pedirObligatorio(
                    "Actualizar comunicado", "Mensaje:", c.getMensaje());

            boolean vigente = pedirBooleano(
                    "Actualizar comunicado",
                    "¿Está vigente?",
                    c.getVigente());

            comunicadoDAO.actualizar(
                    new Comunicado(
                            c.getId(),
                            titulo,
                            texto,
                            c.getFecha_publicacion(),
                            vigente
                    )
            );

            operacionCorrecta("Comunicado actualizado correctamente.");

        } catch (CanceladoException e) {
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el comunicado: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarComunicado() {
        Comunicado c =
                tablaComunicados.getSelectionModel().getSelectedItem();

        if (c == null) {
            mostrarAviso("Selecciona un comunicado de la tabla.");
            return;
        }

        if (!confirmar("¿Deseas eliminar el comunicado seleccionado?")) {
            return;
        }

        try {
            comunicadoDAO.eliminar(c.getId());

            operacionCorrecta("Comunicado eliminado correctamente.");

        } catch (Exception e) {
            mostrarError("No se pudo eliminar el comunicado: " + e.getMessage());
        }
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

    private boolean confirmar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);

        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);

        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void operacionCorrecta(String texto) {
        cargarDatos();
        mensaje.setText(texto);
    }

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

    private void mostrarError(String detalle) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("ARS VitaSalud");
        alerta.setHeaderText("Error al procesar la operación");
        alerta.setContentText(detalle);
        alerta.getDialogPane().setMinWidth(450);

        alerta.showAndWait();
    }

    private static class CanceladoException extends RuntimeException {
    }
}