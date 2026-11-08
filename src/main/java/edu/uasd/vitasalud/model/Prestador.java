package edu.uasd.vitasalud.model;

public class Prestador {

    private Long id;
    private String rncCedula;
    private String nombre;
    private String tipo;
    private String direccion;
    private Integer provinciaId;
    private String telefono;
    private String correo;
    private String estadoContrato;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRncCedula() {
        return rncCedula;
    }
    public void setRncCedula(String rncCedula) {
        this.rncCedula = rncCedula;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public Integer getProvinciaId() {
        return provinciaId;
    }
    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
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
    public String getEstadoContrato() {
        return estadoContrato;
    }
    public void setEstadoContrato(String estadoContrato) {
        this.estadoContrato = estadoContrato;
    }

    
    public Prestador() {
    }
    

    


    

    
}
