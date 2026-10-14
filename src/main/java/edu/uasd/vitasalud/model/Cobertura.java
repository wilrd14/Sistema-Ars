package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

public class Cobertura {

    private Long id;
    private Integer plan_id;
    private String categoria;
    private BigDecimal porcentaje;
    private BigDecimal copago_fijo;
    private BigDecimal tope_anual;
    private Integer dias_carencia;
    private Boolean requiere_precertificacion;

    public Cobertura(Long id, Integer plan_id, String categoria, BigDecimal porcentaje, BigDecimal copago_fijo, BigDecimal tope_anual, Integer dias_carencia, Boolean requiere_precertificacion) {

        this.id = id;
        this.plan_id = plan_id;
        this.categoria = categoria;
        this.porcentaje = porcentaje;
        this.copago_fijo = copago_fijo;
        this.tope_anual = tope_anual;
        this.dias_carencia = dias_carencia;
        this.requiere_precertificacion = requiere_precertificacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(Integer plan_id) {
        this.plan_id = plan_id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getCopago_fijo() {
        return copago_fijo;
    }

    public void setCopago_fijo(BigDecimal copago_fijo) {
        this.copago_fijo = copago_fijo;
    }

    public BigDecimal getTope_anual() {
        return tope_anual;
    }

    public void setTope_anual(BigDecimal tope_anual) {
        this.tope_anual = tope_anual;
    }

    public Integer getDias_carencia() {
        return dias_carencia;
    }

    public void setDias_carencia(Integer dias_carencia) {
        this.dias_carencia = dias_carencia;
    }

    public Boolean getRequiere_precertificacion() {
        return requiere_precertificacion;
    }

    public void setRequiere_precertificacion(Boolean requiere_precertificacion) {
        this.requiere_precertificacion = requiere_precertificacion;
    }
}