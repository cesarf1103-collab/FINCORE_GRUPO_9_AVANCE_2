package modulo.empleado;

import modulo.persona.Persona;

public abstract class Empleado extends Persona {
    private static final long serialVersionUID = 1L;

    protected double salario;

    public Empleado(String id, String nombre, String email, double salario) {
        super(id, nombre, email);
        this.salario = salario;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    @Override
    public String getTipoPersona() {
        return "EMPLEADO";
    }
}
