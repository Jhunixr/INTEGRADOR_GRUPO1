package semana12;
public enum EstadoReserva {
    PENDIENTE("Reserva pendiente de confirmación"),
    CONFIRMADA("Reserva confirmada"),
    CANCELADA("Reserva cancelada");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
