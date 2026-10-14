
package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

public class Plan {

    private Integer id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal prima_mensual;
    private String estado;

    public Plan(Integer id, String codigo, String nombre, String descripcion, BigDecimal prima_mensual, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.prima_mensual = prima_mensual;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrima_mensual() {
        return prima_mensual;
    }

    public void setPrima_mensual(BigDecimal prima_mensual) {
        this.prima_mensual = prima_mensual;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
