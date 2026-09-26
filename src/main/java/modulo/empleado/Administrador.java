package modulo.empleado;

public class Administrador extends Empleado {
    private String nivelAcceso;

    public Administrador(String id, String nombre, String email, double salario, String nivelAcceso) {
        super(id, nombre, email, salario);
        this.nivelAcceso = nivelAcceso;
    }

    public String getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(String nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }
}