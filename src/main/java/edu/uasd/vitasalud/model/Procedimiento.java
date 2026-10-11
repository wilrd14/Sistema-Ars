package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

public class Procedimiento {

    private Long id;
    private String codigo;
    private String descripcion;
    private String categoria;
    private BigDecimal precio_referencia;

    public Procedimiento(Long id, String codigo, String descripcion, String categoria, BigDecimal precio_referencia) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio_referencia = precio_referencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio_referencia() {
        return precio_referencia;
    }

    public void setPrecio_referencia(BigDecimal precio_referencia) {
        this.precio_referencia = precio_referencia;
    }
}