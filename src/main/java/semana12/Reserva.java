package semana12;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Comparator;

/**
 * Representa una reserva de un local.
 */

public class Reserva implements Comparable<Reserva> {

    private UUID idReserva;
    private Integer usuarioId;
    private Integer localId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double monto;
    private EstadoReserva estado;

    /**
     * Constructor vacío: asigna un UUID y establece estado PENDIENTE.
     */
    public Reserva() {
        this.idReserva = UUID.randomUUID();
        this.estado = EstadoReserva.PENDIENTE;
    }

    /**
     * Constructor completo para Reserva.
     */
    public Reserva(UUID idReserva, Integer usuarioId, Integer localId,
                   LocalDateTime fechaInicio, LocalDateTime fechaFin,
                   Double monto, EstadoReserva estado) {
        this.idReserva = (idReserva != null) ? idReserva : UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.localId = localId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.monto = monto;
        this.estado = (estado != null) ? estado : EstadoReserva.PENDIENTE;
    }

    /**
     * Calcula la duración de la reserva en horas.
     */
    public long getDuracionHoras() {
        if (fechaInicio == null || fechaFin == null) {
            return 0;
        }
        return Duration.between(fechaInicio, fechaFin).toHours();
    }

    /**
     * Valida que la reserva tenga datos coherentes:
     * - IDs no nulos
     * - Fechas válidas
     * - Monto positivo
     * - Estado no CANCELADA
     */
    public boolean isValid() {
        if (usuarioId == null || localId == null) {
            return false;
        }
        if (fechaInicio == null || fechaFin == null || fechaInicio.isAfter(fechaFin)) {
            return false;
        }
        if (monto == null || monto <= 0) {
            return false;
        }
        if (estado == EstadoReserva.CANCELADA) {
            return false;
        }
        return true;
    }

    /**
     * Cancela la reserva.
     */
    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    /**
     * Confirma la reserva si está pendiente.
     */
    public void confirmar() {
        if (this.estado == EstadoReserva.PENDIENTE) {
            this.estado = EstadoReserva.CONFIRMADA;
        }
    }

    // Getters y Setters
    public UUID getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(UUID idReserva) {
        this.idReserva = idReserva;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getLocalId() {
        return localId;
    }

    public void setLocalId(Integer localId) {
        this.localId = localId;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    /**
     * Compara reservas según fecha de inicio (null-safe).
     */
    @Override
    public int compareTo(Reserva other) {
        return Comparator.nullsFirst(LocalDateTime::compareTo)
                .compare(this.fechaInicio, other.fechaInicio);
    }

    /**
     * Igualdad basada en idReserva.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reserva that = (Reserva) obj;
        return Objects.equal(idReserva, that.idReserva);
    }

    /**
     * HashCode basado en idReserva.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(idReserva);
    }

    /**
     * Representación en formato JSON-style.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("idReserva", idReserva)
                .append("usuarioId", usuarioId)
                .append("localId", localId)
                .append("fechaInicio", fechaInicio)
                .append("fechaFin", fechaFin)
                .append("duracionHoras", getDuracionHoras())
                .append("monto", monto)
                .append("estado", estado)
                .toString();
    }
}

