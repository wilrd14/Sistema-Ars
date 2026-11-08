package edu.uasd.vitasalud.model;

/** Elemento de un combo: id de la base de datos y texto que ve el usuario. */
public record Opcion(int id, String nombre) {

    @Override
    public String toString() {
        return nombre;
    }
}