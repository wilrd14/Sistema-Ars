/**Interfaz AfiliadoDAO */

package edu.uasd.vitasalud.dao;
import edu.uasd.vitasalud.model.Afiliado; /**permite usar la clase Afiliado*/
import edu.uasd.vitasalud.model.EstadoAfiliado;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AfiliadoDAO {

   Optional<Afiliado> buscarPorNumeroDocumento(
        String tipoDocumento, String numeroDocumento) throws SQLException;
    long registrarAfiliado(Afiliado afiliado) throws SQLException;

    void actualizarAfiliado(Afiliado afiliado)throws SQLException;

    void cambiarEstado(long id, EstadoAfiliado estado)throws SQLException;

    List<Afiliado>listarAfiliados() throws SQLException;



    List<Afiliado>buscarPorNombre(String nombres)throws SQLException;
    Optional<Afiliado>buscarPorNss(String nss)throws SQLException;
    
    boolean existeConyugePorTitular(long titularId) throws SQLException;


}   