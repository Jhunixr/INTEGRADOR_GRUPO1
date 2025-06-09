package com.utpintegrador.semana10;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventoValidator {
    private static final Logger logger = LoggerFactory.getLogger(EventoValidator.class);

    public List<String> validar(Evento evento) {
        List<String> errores = new ArrayList<>();

        if (evento == null) {
            errores.add("El evento no puede ser nulo");
            return errores;
        }

        // Validar título
        if (StringUtils.isBlank(evento.getTitulo())) {
            errores.add("El título es obligatorio");
        } else if (evento.getTitulo().length() < 5) {
            errores.add("El título debe tener al menos 5 caracteres");
        }

        // Validar capacidad
        if (evento.getCapacidad() == null) {
            errores.add("La capacidad es obligatoria");
        } else if (evento.getCapacidad() <= 0) {
            errores.add("La capacidad debe ser mayor que cero");
        } else if (evento.getCapacidad() > 1000) {
            errores.add("La capacidad no puede exceder 1000 personas");
        }

        // Validar precio
        if (evento.getPrecioHora() == null) {
            errores.add("El precio por hora es obligatorio");
        } else if (evento.getPrecioHora() <= 0) {
            errores.add("El precio por hora debe ser mayor que cero");
        }

        // Validar ubicación
        if (StringUtils.isBlank(evento.getUbicacion())) {
            errores.add("La ubicación es obligatoria");
        }

        // Validar fecha
        if (evento.getFecha() == null) {
            errores.add("La fecha es obligatoria");
        } else if (evento.getFecha().isBefore(LocalDate.now())) {
            errores.add("La fecha no puede ser en el pasado");
        }

        // Validar estado
        if (evento.getEstado() == null) {
            errores.add("El estado es obligatorio");
        }

        return errores;
    }

    public void normalizar(Evento evento) {
        if (evento == null) return;

        if (evento.getTitulo() != null) {
            evento.setTitulo(StringUtils.capitalize(evento.getTitulo().trim().toLowerCase()));
        }

        if (evento.getDescripcion() != null) {
            evento.setDescripcion(evento.getDescripcion().trim());
        }

        if (evento.getUbicacion() != null) {
            evento.setUbicacion(StringUtils.capitalize(evento.getUbicacion().trim().toLowerCase()));
        }
    }
}