
package edu.uasd.vitasalud.model;
import java.time.LocalDate;


/**Clase que representa un afiliado de la ARS */

public class Afiliado{

    private long id;
    private String numeroAfiliado;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nss;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private char sexo;
    private String estadoCivil;
    private String nacionalidad;
    private String tipoTelefono;
    private String telefono;
    private String correo;
    private Integer provinciaId;
    private Integer municipioId;
    private String sector;
    private String direccion;
    private int planId;
    private String tipoAfiliado;
    private Long titularId;
    private String parentesco;
    private boolean esEstudiante;
    private LocalDate inicioCobertura;
    private String empleadorRnc;
    private EstadoAfiliado estado; /**Porque ya existe el enum EstadoAfiliado, que define los estados permitidos de un afiliado. */
    private String motivoEstado;
    private boolean esBorrador;

    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getNumeroAfiliado() {
        return numeroAfiliado;
    }
    public void setNumeroAfiliado(String numeroAfiliado) {
        this.numeroAfiliado = numeroAfiliado;
    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    public String getNss() {
        return nss;
    }
    public void setNss(String nss) {
        this.nss = nss;
    }
    public String getNombres() {
        return nombres;
    }
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    public char getSexo() {
        return sexo;
    }
    public void setSexo(char sexo) {
        this.sexo = sexo;
    }
    public String getEstadoCivil() {
        return estadoCivil;
    }
    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }
    public String getNacionalidad() {
        return nacionalidad;
    }
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }
    public String getTipoTelefono() {
        return tipoTelefono;
    }
    public void setTipoTelefono(String tipoTelefono) {
        this.tipoTelefono = tipoTelefono;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public Integer getProvinciaId() {
        return provinciaId;
    }
    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
    }
    public Integer getMunicipioId() {
        return municipioId;
    }
    public void setMunicipioId(Integer municipioId) {
        this.municipioId = municipioId;
    }
    public String getSector() {
        return sector;
    }
    public void setSector(String sector) {
        this.sector = sector;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public int getPlanId() {
        return planId;
    }
    public void setPlanId(int planId) {
        this.planId = planId;
    }
    public String getTipoAfiliado() {
        return tipoAfiliado;
    }
    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }
    public Long getTitularId() {
        return titularId;
    }
    public void setTitularId(Long titularId) {
        this.titularId = titularId;
    }
    public String getParentesco() {
        return parentesco;
    }
    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }
    public boolean isEsEstudiante() {
        return esEstudiante;
    }
    public void setEsEstudiante(boolean esEstudiante) {
        this.esEstudiante = esEstudiante;
    }
    public LocalDate getInicioCobertura() {
        return inicioCobertura;
    }
    public void setInicioCobertura(LocalDate inicioCobertura) {
        this.inicioCobertura = inicioCobertura;
    }
    public String getEmpleadorRnc() {
        return empleadorRnc;
    }
    public void setEmpleadorRnc(String empleadorRnc) {
        this.empleadorRnc = empleadorRnc;
    }
    public EstadoAfiliado getEstado() {
        return estado;
    }
    public void setEstado(EstadoAfiliado estado) {
        this.estado = estado;
    }
    public String getMotivoEstado() {
        return motivoEstado;
    }
    public void setMotivoEstado(String motivoEstado) {
        this.motivoEstado = motivoEstado;
    }
    public boolean isEsBorrador() {
        return esBorrador;
    }
    public void setEsBorrador(boolean esBorrador) {
        this.esBorrador = esBorrador;
    }

    public Afiliado() {

    }



    


}