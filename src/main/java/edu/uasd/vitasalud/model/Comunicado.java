
package edu.uasd.vitasalud.model;

import java.time.LocalDateTime;

public class Comunicado {

    private Integer id;
    private String titulo;
    private String mensaje;
    private LocalDateTime fecha_publicacion;
    private Boolean vigente;

    public Comunicado(Integer id, String titulo, String mensaje,
                       LocalDateTime fecha_publicacion, Boolean vigente) {
        this.id = id;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha_publicacion = fecha_publicacion;
        this.vigente = vigente;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha_publicacion() {
        return fecha_publicacion;
    }

    public void setFecha_publicacion(LocalDateTime fecha_publicacion) {
        this.fecha_publicacion = fecha_publicacion;
    }

    public Boolean getVigente() {
        return vigente;
    }

    public void setVigente(Boolean vigente) {
        this.vigente = vigente;
    }
}
