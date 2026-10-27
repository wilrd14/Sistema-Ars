/**Implmenta los metodos de la interfaz */
package edu.uasd.vitasalud.dao.impl;

import edu.uasd.vitasalud.config.ConexionBD; //obtiene la conexion a MYSQL
import edu.uasd.vitasalud.model.Afiliado;
import edu.uasd.vitasalud.model.EstadoAfiliado;
import edu.uasd.vitasalud.dao.AfiliadoDAO;


import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



public class AfiliadoDAOJdbc implements AfiliadoDAO {

// Modifica los datos de un afiliado que ya está registrado,
// utilizando su ID para identificarlo. 
@Override
public void actualizarAfiliado(Afiliado afiliado) throws SQLException {

    String sql = "UPDATE afiliado SET "
            + "tipo_documento = ?, numero_documento = ?, nss = ?, "
            + "nombres = ?, apellidos = ?, fecha_nacimiento = ?, sexo = ?, "
            + "estado_civil = ?, nacionalidad = ?, tipo_telefono = ?, "
            + "telefono = ?, correo = ?, provincia_id = ?, municipio_id = ?, "
            + "sector = ?, direccion = ?, plan_id = ?, tipo_afiliado = ?, "
            + "titular_id = ?, parentesco = ?, es_estudiante = ?, "
            + "inicio_cobertura = ?, empleador_rnc = ?, es_borrador = ? "
            + "WHERE id = ?";

    try (Connection con = ConexionBD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, afiliado.getTipoDocumento());
        ps.setString(2, afiliado.getNumeroDocumento());
        ps.setString(3, afiliado.getNss());
        ps.setString(4, afiliado.getNombres());
        ps.setString(5, afiliado.getApellidos());
        ps.setDate(6, java.sql.Date.valueOf(afiliado.getFechaNacimiento()));
        ps.setString(7, String.valueOf(afiliado.getSexo()));
        ps.setString(8, afiliado.getEstadoCivil());
        ps.setString(9, afiliado.getNacionalidad());
        ps.setString(10, afiliado.getTipoTelefono());
        ps.setString(11, afiliado.getTelefono());
        ps.setString(12, afiliado.getCorreo());

        if (afiliado.getProvinciaId() == null) {
            ps.setNull(13, java.sql.Types.INTEGER);
        } else {
            ps.setInt(13, afiliado.getProvinciaId());
        }

        if (afiliado.getMunicipioId() == null) {
            ps.setNull(14, java.sql.Types.INTEGER);
        } else {
            ps.setInt(14, afiliado.getMunicipioId());
        }

        ps.setString(15, afiliado.getSector());
        ps.setString(16, afiliado.getDireccion());
        ps.setInt(17, afiliado.getPlanId());
        ps.setString(18, afiliado.getTipoAfiliado());

        if (afiliado.getTitularId() == null) {
            ps.setNull(19, java.sql.Types.BIGINT);
        } else {
            ps.setLong(19, afiliado.getTitularId());
        }

        ps.setString(20, afiliado.getParentesco());
        ps.setBoolean(21, afiliado.isEsEstudiante());
        ps.setDate(22, java.sql.Date.valueOf(afiliado.getInicioCobertura()));
        ps.setString(23, afiliado.getEmpleadorRnc());
        ps.setBoolean(24, afiliado.isEsBorrador());
        ps.setLong(25, afiliado.getId());

        ps.executeUpdate();
    }
}

  
// Busca afiliados por coincidencias parciales en sus nombres o apellidos.
@Override
public List<Afiliado> buscarPorNombre(String nombres) throws SQLException {
    List<Afiliado> afiliados = new ArrayList<>();

    String sql = "SELECT * FROM afiliado "
            + "WHERE nombres LIKE ? OR apellidos LIKE ?";

    try (Connection con = ConexionBD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {

        String criterio = "%" + nombres + "%";

        ps.setString(1, criterio);
        ps.setString(2, criterio);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                afiliados.add(mapear(rs));
            }
        }
    }

    return afiliados;
}


 // Busca un afiliado por su Número de Seguridad Social (NSS).
// Devuelve el afiliado encontrado o un resultado vacío si no existe.
@Override
public Optional<Afiliado> buscarPorNss(String nss) throws SQLException {

    String sql = "SELECT * FROM afiliado WHERE nss = ?";

    try (Connection con = ConexionBD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, nss);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));
            }
        }
    }

    return Optional.empty();
}



//Busca un afiliado por el tipo y número de su documento.
@Override
public Optional<Afiliado> buscarPorNumeroDocumento(
        String tipoDocumento, String numeroDocumento) throws SQLException {

    String sql = "SELECT * FROM afiliado "
            + "WHERE tipo_documento = ? AND numero_documento = ?";

    try (Connection con = ConexionBD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, tipoDocumento);
        ps.setString(2, numeroDocumento);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));
            }
        }
    }


    return Optional.empty();



}
   
@Override
public boolean existeConyugePorTitular(long titularId) throws SQLException {

    String sql = "SELECT 1 FROM afiliado "
               + "WHERE titular_id = ? "
               + "AND UPPER(parentesco) = 'CONYUGE' LIMIT 1";

    try (Connection conn = ConexionBD.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, titularId);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}


@Override
public void cambiarEstado(long id, EstadoAfiliado estado) throws SQLException {

    String sqlTitular =
            "UPDATE afiliado SET estado = ? WHERE id = ?";

    String sqlDependientes =
            "UPDATE afiliado SET estado = ? WHERE titular_id = ?";

    try (Connection con = ConexionBD.getConexion()) {

        con.setAutoCommit(false);

        try {
            // Cambiar el estado del afiliado seleccionado.
            try (PreparedStatement ps =
                    con.prepareStatement(sqlTitular)) {

                ps.setString(1, estado.name());
                ps.setLong(2, id);
                ps.executeUpdate();
            }

            // RN-03: propagar SUSPENDIDO o CANCELADO
            // a los dependientes del titular.
            if (estado == EstadoAfiliado.SUSPENDIDO
                    || estado == EstadoAfiliado.CANCELADO) {

                try (PreparedStatement ps =
                        con.prepareStatement(sqlDependientes)) {

                    ps.setString(1, estado.name());
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        }
    }
}


   // Consulta todos los afiliados registrados en la base de datos
// y devuelve una lista con sus datos.
@Override
public List<Afiliado> listarAfiliados() throws SQLException {
    List<Afiliado> afiliados = new ArrayList<>();

    String sql = "SELECT * FROM afiliado";

    try (Connection con = ConexionBD.getConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            afiliados.add(mapear(rs));
        }
    }

    return afiliados;
}

  // Registra un nuevo afiliado en la base de datos,
// obtiene su ID generado y le asigna un número único
// con el formato AF-000001. 
@Override
public long registrarAfiliado(Afiliado afiliado) throws SQLException {
    String sql = "INSERT INTO afiliado ("
            + "tipo_documento, numero_documento, nss, nombres, apellidos, "
            + "fecha_nacimiento, sexo, estado_civil, nacionalidad, "
            + "tipo_telefono, telefono, correo, provincia_id, municipio_id, "
            + "sector, direccion, plan_id, tipo_afiliado, titular_id, "
            + "parentesco, es_estudiante, inicio_cobertura, empleador_rnc, "
            + "estado, motivo_estado, es_borrador"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = ConexionBD.getConexion()) {
        con.setAutoCommit(false);

        try {
            long id;

            try (PreparedStatement ps = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, afiliado.getTipoDocumento());
                ps.setString(2, afiliado.getNumeroDocumento());
                ps.setString(3, afiliado.getNss());
                ps.setString(4, afiliado.getNombres());
                ps.setString(5, afiliado.getApellidos());
                ps.setDate(6, java.sql.Date.valueOf(
                        afiliado.getFechaNacimiento()));
                ps.setString(7, String.valueOf(afiliado.getSexo()));
                ps.setString(8, afiliado.getEstadoCivil());
                ps.setString(9, afiliado.getNacionalidad());
                ps.setString(10, afiliado.getTipoTelefono());
                ps.setString(11, afiliado.getTelefono());
                ps.setString(12, afiliado.getCorreo());

                if (afiliado.getProvinciaId() == null) {
                    ps.setNull(13, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(13, afiliado.getProvinciaId());
                }

                if (afiliado.getMunicipioId() == null) {
                    ps.setNull(14, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(14, afiliado.getMunicipioId());
                }

                ps.setString(15, afiliado.getSector());
                ps.setString(16, afiliado.getDireccion());
                ps.setInt(17, afiliado.getPlanId());
                ps.setString(18, afiliado.getTipoAfiliado());

                if (afiliado.getTitularId() == null) {
                    ps.setNull(19, java.sql.Types.BIGINT);
                } else {
                    ps.setLong(19, afiliado.getTitularId());
                }

                ps.setString(20, afiliado.getParentesco());
                ps.setBoolean(21, afiliado.isEsEstudiante());
                ps.setDate(22, java.sql.Date.valueOf(
                        afiliado.getInicioCobertura()));
                ps.setString(23, afiliado.getEmpleadorRnc());

                EstadoAfiliado estado = afiliado.getEstado() == null
                        ? EstadoAfiliado.ACTIVO
                        : afiliado.getEstado();

                ps.setString(24, estado.name());
                ps.setString(25, afiliado.getMotivoEstado());
                ps.setBoolean(26, afiliado.isEsBorrador());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se pudo obtener el ID del afiliado.");
                    }
                    id = rs.getLong(1);
                }
            }

            String numeroAfiliado = String.format("AF-%06d", id);
            String sqlNumero = "UPDATE afiliado "
                    + "SET numero_afiliado = ? WHERE id = ?";

            try (PreparedStatement psNumero =
                    con.prepareStatement(sqlNumero)) {
                psNumero.setString(1, numeroAfiliado);
                psNumero.setLong(2, id);
                psNumero.executeUpdate();
            }

            con.commit();

            afiliado.setId(id);
            afiliado.setNumeroAfiliado(numeroAfiliado);

            return id;

        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        }
    }
}

    // Busca un afiliado por su id.
    @Override
    public Optional<Afiliado> buscarPorId(long id) throws SQLException {

        String sql = "SELECT * FROM afiliado WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }

        return Optional.empty();
    }

    // Lista los dependientes de un titular.
    @Override
    public List<Afiliado> listarDependientes(long titularId) throws SQLException {

        List<Afiliado> afiliados = new ArrayList<>();
        String sql = "SELECT * FROM afiliado WHERE titular_id = ? "
                + "ORDER BY nombres, apellidos";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, titularId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    afiliados.add(mapear(rs));
                }
            }
        }

        return afiliados;
    }

    // Busca por documento, NSS, número de afiliado o nombre completo.
    // Con el criterio vacío devuelve todos los afiliados que no son borrador.
    @Override
    public List<Afiliado> buscar(String criterio) throws SQLException {

        List<Afiliado> afiliados = new ArrayList<>();

        String sql = "SELECT * FROM afiliado WHERE es_borrador = FALSE AND ("
                + "numero_documento LIKE ? OR nss LIKE ? "
                + "OR numero_afiliado LIKE ? "
                + "OR CONCAT(nombres, ' ', apellidos) LIKE ?) "
                + "ORDER BY apellidos, nombres";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String patron = "%" + (criterio == null ? "" : criterio) + "%";

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    afiliados.add(mapear(rs));
                }
            }
        }

        return afiliados;
    }

    
/**
 * Convierte una fila de la tabla afiliado en un objeto Afiliado.
 */
private Afiliado mapear(ResultSet rs) throws SQLException {
    Afiliado a = new Afiliado();

    a.setId(rs.getLong("id"));
    a.setNumeroAfiliado(rs.getString("numero_afiliado"));
    a.setTipoDocumento(rs.getString("tipo_documento"));
    a.setNumeroDocumento(rs.getString("numero_documento"));
    a.setNss(rs.getString("nss"));
    a.setNombres(rs.getString("nombres"));
    a.setApellidos(rs.getString("apellidos"));

    a.setFechaNacimiento(
        rs.getDate("fecha_nacimiento").toLocalDate()
    );

    a.setSexo(rs.getString("sexo").charAt(0));
    a.setEstadoCivil(rs.getString("estado_civil"));
    a.setNacionalidad(rs.getString("nacionalidad"));
    a.setTipoTelefono(rs.getString("tipo_telefono"));
    a.setTelefono(rs.getString("telefono"));
    a.setCorreo(rs.getString("correo"));

    int provinciaId = rs.getInt("provincia_id");
    a.setProvinciaId(rs.wasNull() ? null : provinciaId);

    int municipioId = rs.getInt("municipio_id");
    a.setMunicipioId(rs.wasNull() ? null : municipioId);

    a.setSector(rs.getString("sector"));
    a.setDireccion(rs.getString("direccion"));
    a.setPlanId(rs.getInt("plan_id"));
    a.setTipoAfiliado(rs.getString("tipo_afiliado"));

    long titularId = rs.getLong("titular_id");
    a.setTitularId(rs.wasNull() ? null : titularId);

    a.setParentesco(rs.getString("parentesco"));
    a.setEsEstudiante(rs.getBoolean("es_estudiante"));
    a.setInicioCobertura(
        rs.getDate("inicio_cobertura").toLocalDate()
    );
    a.setEmpleadorRnc(rs.getString("empleador_rnc"));

    a.setEstado(
        EstadoAfiliado.valueOf(rs.getString("estado"))
    );

    a.setMotivoEstado(rs.getString("motivo_estado"));
    a.setEsBorrador(rs.getBoolean("es_borrador"));

    return a;
}

    
}
