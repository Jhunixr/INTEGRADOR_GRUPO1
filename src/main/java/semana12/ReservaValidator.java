package semana12;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de validación de reservas
 * Adaptado desde EmployeeValidator
 */
public class ReservaValidator {

    private static final Logger logger = LoggerFactory.getLogger(ReservaValidator.class);

    public ReservaValidator() {
        logger.debug("ReservaValidator inicializado");
    }

    /**
     * Valida una reserva y retorna una lista de errores
     * @param reserva Reserva a validar
     * @return Lista de errores encontrados (vacía si es válida)
     */
    public List<String> validate(Reserva reserva) {
        logger.debug("Iniciando validación de la reserva: {}", 
                     reserva != null ? reserva.getIdReserva() : "null");

        List<String> errors = new ArrayList<>();

        if (reserva == null) {
            errors.add("La reserva no puede ser nula");
            logger.warn("Intento de validar reserva nula");
            return errors;
        }

        // Validar usuarioId
        if (reserva.getUsuarioId() == null || reserva.getUsuarioId() <= 0) {
            errors.add("El ID de usuario debe ser un número positivo");
            logger.debug("Error de validación: usuarioId inválido");
        }

        // Validar localId
        if (reserva.getLocalId() == null || reserva.getLocalId() <= 0) {
            errors.add("El ID de local debe ser un número positivo");
            logger.debug("Error de validación: localId inválido");
        }

        // Validar fechas
        if (reserva.getFechaInicio() == null) {
            errors.add("La fecha de inicio es obligatoria");
        }

        if (reserva.getFechaFin() == null) {
            errors.add("La fecha de fin es obligatoria");
        }

        if (reserva.getFechaInicio() != null && reserva.getFechaFin() != null) {
            if (reserva.getFechaInicio().isAfter(reserva.getFechaFin())) {
                errors.add("La fecha de inicio no puede ser posterior a la fecha de fin");
            }
            if (reserva.getFechaInicio().isBefore(LocalDateTime.now())) {
                errors.add("La fecha de inicio no puede ser en el pasado");
            }
        }

        // Validar monto
        if (reserva.getMonto() == null) {
            errors.add("El monto es obligatorio");
        } else if (reserva.getMonto() <= 0) {
            errors.add("El monto debe ser mayor que cero");
        } else if (reserva.getMonto() > 10000) {
            errors.add("El monto parece excesivamente alto, revisar");
            logger.warn("Monto muy alto detectado: {} para reserva {}", 
                        reserva.getMonto(), reserva.getIdReserva());
        }

        // Validar estado
        if (reserva.getEstado() == null) {
            errors.add("El estado de la reserva es obligatorio");
        }

        if (errors.isEmpty()) {
            logger.debug("Reserva {} validada correctamente", reserva.getIdReserva());
        } else {
            logger.info("Reserva {} tiene {} errores de validación", 
                        reserva.getIdReserva(), errors.size());
        }

        return errors;
    }

    /**
     * Valida si una reserva es válida (sin errores)
     */
    public boolean isValid(Reserva reserva) {
        return validate(reserva).isEmpty();
    }
}
