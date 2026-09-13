package modulo.cliente;

import modulo.persona.Persona;

/**
 * Subclase Cliente que hereda de Persona.
 */
public class Cliente extends Persona {
    private static final long serialVersionUID = 1L;

    private String estado;

    public Cliente(String idCliente, String nombre, String email) {
        super(idCliente, nombre, email);
        this.estado = "ACTIVO";
    }

    @Override
    public String getTipoPersona() {
        return "CLIENTE";
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}