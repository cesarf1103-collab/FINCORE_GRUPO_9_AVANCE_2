package modulo.empleado;

public class AtencionCliente extends Empleado {
    private String moduloAtencion;

    public AtencionCliente(String id, String nombre, String email, double salario, String moduloAtencion) {
        super(id, nombre, email, salario);
        this.moduloAtencion = moduloAtencion;
    }

    public String getModuloAtencion() {
        return moduloAtencion;
    }

    public void setModuloAtencion(String moduloAtencion) {
        this.moduloAtencion = moduloAtencion;
    }
}