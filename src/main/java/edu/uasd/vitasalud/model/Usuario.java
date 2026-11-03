package edu.uasd.vitasalud.model;

/** Usuario del sistema, interno o prestador (tabla {@code usuario}). */
public class Usuario {

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";
    public static final String ESTADO_BLOQUEADO = "BLOQUEADO";

    private long id;
    private String nombre;
    private String username;
    private String passwordHash;
    private Perfil perfil;
    private Rol rol;
    /** Obligatorio cuando el perfil es PRESTADOR; null para personal interno. */
    private Long prestadorId;
    /** Solo lectura: nombre del prestador, lo llena el listado de usuarios. */
    private String prestadorNombre;
    private String cargo;
    private String estado;
    private int intentosFallidos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Long getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Long prestadorId) {
        this.prestadorId = prestadorId;
    }

    public String getPrestadorNombre() {
        return prestadorNombre;
    }

    public void setPrestadorNombre(String prestadorNombre) {
        this.prestadorNombre = prestadorNombre;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    /** Iniciales para el avatar del encabezado (máximo dos letras). */
    public String iniciales() {
        StringBuilder sb = new StringBuilder();
        for (String parte : nombre.trim().split("\\s+")) {
            if (!parte.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }
}
