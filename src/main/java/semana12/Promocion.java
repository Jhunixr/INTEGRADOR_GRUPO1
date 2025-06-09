package semana12;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.Comparator; // Importar Comparator para las comparaciones funcionales
import java.util.UUID; // Importar UUID para un identificador único

// Implementa la interfaz Comparable para definir un orden natural
public class Promocion implements Comparable<Promocion> {
    private UUID idPromocion;
    private String nombre;
    private String descripcion;
    private String tipoPromocion; // Ej., "Porcentual", "Fijo", "2x1"
    private Double valorDescuento; // Puede ser porcentaje (0.10 para 10%) o monto fijo
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String codigoPromocional; // Opcional, si la promoción requiere un código
    private Integer cantidadMaximaUsos; // Opcional, límite de usos globales
    private Integer usosRestantes; // Opcional, contador de usos restantes
    private String aplicableA; // Ej., "Todos", "Eventos", "Servicios" (podría ser una enumeración)
    private boolean activo;

    // Constructor vacío
    public Promocion() {
        this.idPromocion = UUID.randomUUID(); // Asignar un ID único al crear
        this.activo = true; // Por defecto, una promoción al crearla está activa
    }

    // Constructor con 9 parámetros (sin id y activo)
    public Promocion(String nombre, String descripcion, String tipoPromocion,
                     Double valorDescuento, LocalDate fechaInicio, LocalDate fechaFin,
                     String codigoPromocional, Integer cantidadMaximaUsos,
                     String aplicableA) {
        this(); // Llama al constructor vacío para inicializar idPromocion y activo
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoPromocion = tipoPromocion;
        this.valorDescuento = valorDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.codigoPromocional = codigoPromocional;
        this.cantidadMaximaUsos = cantidadMaximaUsos;
        this.usosRestantes = cantidadMaximaUsos; // Inicialmente, usosRestantes es igual a cantidadMaximaUsos
        this.aplicableA = aplicableA;
    }

    // Constructor con 11 parámetros (incluyendo id y activo)
    public Promocion(UUID idPromocion, String nombre, String descripcion, String tipoPromocion,
                     Double valorDescuento, LocalDate fechaInicio, LocalDate fechaFin,
                     String codigoPromocional, Integer cantidadMaximaUsos,
                     String aplicableA, boolean activo) {
        this.idPromocion = idPromocion;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoPromocion = tipoPromocion;
        this.valorDescuento = valorDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.codigoPromocional = codigoPromocional;
        this.cantidadMaximaUsos = cantidadMaximaUsos;
        this.usosRestantes = cantidadMaximaUsos; // Asume que se inicializan los usos restantes
        this.aplicableA = aplicableA;
        this.activo = activo;
    }

    /**
     * Verifica si la promoción es válida en la fecha actual y si no ha excedido sus usos.
     */
    public boolean esValida() {
        LocalDate hoy = LocalDate.now();
        boolean porFecha = (fechaInicio == null || !hoy.isBefore(fechaInicio)) &&
                           (fechaFin == null || !hoy.isAfter(fechaFin));
        boolean porUsos = (cantidadMaximaUsos == null || usosRestantes > 0);

        return this.activo && porFecha && porUsos;
    }

    /**
     * Registra un uso de la promoción, decrementando el contador si aplica.
     */
    public void registrarUso() {
        if (cantidadMaximaUsos != null && usosRestantes != null && usosRestantes > 0) {
            usosRestantes--;
        }
    }

    /**
     * Cancela la promoción, marcándola como inactiva.
     */
    public void cancelarPromocion() {
        this.activo = false;
    }

    // Getters y Setters
    public UUID getIdPromocion() { return idPromocion; }
    public void setIdPromocion(UUID idPromocion) { this.idPromocion = idPromocion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoPromocion() { return tipoPromocion; }
    public void setTipoPromocion(String tipoPromocion) { this.tipoPromocion = tipoPromocion; }

    public Double getValorDescuento() { return valorDescuento; }
    public void setValorDescuento(Double valorDescuento) { this.valorDescuento = valorDescuento; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getCodigoPromocional() { return codigoPromocional; }
    public void setCodigoPromocional(String codigoPromocional) { this.codigoPromocional = codigoPromocional; }

    public Integer getCantidadMaximaUsos() { return cantidadMaximaUsos; }
    public void setCantidadMaximaUsos(Integer cantidadMaximaUsos) {
        this.cantidadMaximaUsos = cantidadMaximaUsos;
        if (this.usosRestantes == null || this.usosRestantes > cantidadMaximaUsos) {
            this.usosRestantes = cantidadMaximaUsos;
        }
    }

    public Integer getUsosRestantes() { return usosRestantes; }
    public void setUsosRestantes(Integer usosRestantes) { this.usosRestantes = usosRestantes; }

    public String getAplicableA() { return aplicableA; }
    public void setAplicableA(String aplicableA) { this.aplicableA = aplicableA; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /**
     * Implementación de equals usando Google Guava Objects
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Promocion promocion = (Promocion) obj;
        return Objects.equal(idPromocion, promocion.idPromocion);
    }

    /**
     * Implementación de hashCode usando Google Guava Objects
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(idPromocion);
    }

    /**
     * Implementación de toString usando Apache Commons ToStringBuilder
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("idPromocion", idPromocion)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("tipoPromocion", tipoPromocion)
                .append("valorDescuento", valorDescuento)
                .append("fechaInicio", fechaInicio)
                .append("fechaFin", fechaFin)
                .append("codigoPromocional", codigoPromocional)
                .append("cantidadMaximaUsos", cantidadMaximaUsos)
                .append("usosRestantes", usosRestantes)
                .append("aplicableA", aplicableA)
                .append("activo", activo)
                .toString();
    }

    /**
     * Implementación del método compareTo para el orden natural de las promociones.
     * Ordena primero por fecha de inicio (ascendente, nulos primero),
     * y si las fechas son iguales (o ambas nulas), luego por nombre (ascendente, nulos primero).
     *
     * @param otraPromocion La otra promoción con la que comparar.
     * @return Un valor negativo, cero, o un valor positivo si esta promoción
     * es menor que, igual a, o mayor que la promoción especificada.
     */
    @Override
    public int compareTo(Promocion otraPromocion) {
        // Compara por fecha de inicio, colocando los nulos al principio.
        // Si las fechas son iguales, encadena la comparación por nombre.
        return Comparator.nullsFirst(Comparator.comparing(Promocion::getFechaInicio))
                         .thenComparing(Comparator.comparing(Promocion::getNombre, Comparator.nullsFirst(String::compareTo)))
                         .compare(this, otraPromocion);
    }
}
