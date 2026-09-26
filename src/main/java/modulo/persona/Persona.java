package modulo.persona;

import java.io.Serializable;

public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String nombre;
    protected String email;
    protected String telefono;
    protected String direccion;

    public Persona(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

public Persona(String id, String nombre, String email, String telefono, String direccion) {
    this(id, nombre, email);
    this.telefono = telefono;
    this.direccion = direccion;
}


    public abstract String getTipoPersona();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCorreo() { return email; }
    public void setCorreo(String correo) { this.email = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
}
