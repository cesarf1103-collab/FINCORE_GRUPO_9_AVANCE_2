package modulo.empleado;

public class Cajero extends Empleado {
    private String numCaja;

    public Cajero(String id, String nombre, String email, double salario, String numCaja) {
        super(id, nombre, email, salario);
        this.numCaja = numCaja;
    }

    public String getNumCaja() {
        return numCaja;
    }

    public void setNumCaja(String numCaja) {
        this.numCaja = numCaja;
    }
}