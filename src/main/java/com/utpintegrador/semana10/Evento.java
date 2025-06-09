package com.utpintegrador.semana10;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.time.Period;

public class Evento {
    private Long id;
    private String titulo;
    private String descripcion;
    private Integer capacidad;
    private Double precioHora;
    private String ubicacion;
    private LocalDate fecha;
    private Estado estado;

    public enum Estado {
        DISPONIBLE("Disponible", "#4CAF50"),
        RESERVADO("Reservado", "#2196F3"),
        CANCELADO("Cancelado", "#F44336"),
        COMPLETADO("Completado", "#9E9E9E");

        private final String texto;
        private final String color;

        Estado(String texto, String color) {
            this.texto = texto;
            this.color = color;
        }

        public String getTexto() { return texto; }
        public String getColor() { return color; }
    }

    // Constructores
    public Evento() {}

    public Evento(Long id, String titulo, String descripcion, Integer capacidad,
                 Double precioHora, String ubicacion, LocalDate fecha, Estado estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.precioHora = precioHora;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.estado = estado;
    }

    // Métodos de negocio
    public boolean estaDisponible() {
        return estado == Estado.DISPONIBLE;
    }

    public boolean esFuturo() {
        return fecha != null && fecha.isAfter(LocalDate.now());
    }

    public int diasParaEvento() {
        if (fecha == null) return 0;
        return Period.between(LocalDate.now(), fecha).getDays();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Double getPrecioHora() { return precioHora; }
    public void setPrecioHora(Double precioHora) { this.precioHora = precioHora; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equal(id, evento.id) &&
               Objects.equal(titulo, evento.titulo) &&
               Objects.equal(fecha, evento.fecha);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, titulo, fecha);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("titulo", titulo)
                .append("ubicacion", ubicacion)
                .append("fecha", fecha)
                .append("estado", estado.getTexto())
                .append("diasParaEvento", diasParaEvento())
                .toString();
    }
}