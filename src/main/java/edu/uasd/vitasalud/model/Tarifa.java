package edu.uasd.vitasalud.model;

import java.math.BigDecimal;

public class Tarifa {

    private Long id;
    private Long prestador_id;
    private Long procedimiento_id;
    private BigDecimal monto;

    public Tarifa(Long id, Long prestador_id, Long procedimiento_id, BigDecimal monto) {
        this.id = id;
        this.prestador_id = prestador_id;
        this.procedimiento_id = procedimiento_id;
        this.monto = monto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrestador_id() {
        return prestador_id;
    }

    public void setPrestador_id(Long prestador_id) {
        this.prestador_id = prestador_id;
    }

    public Long getProcedimiento_id() {
        return procedimiento_id;
    }

    public void setProcedimiento_id(Long procedimiento_id) {
        this.procedimiento_id = procedimiento_id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}