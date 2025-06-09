package semana12;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase para validar y normalizar objetos Promocion.
 * Asegura la integridad de los datos antes de su uso o persistencia.
 */
public class PromocionValidator {

    private static final Logger logger = LoggerFactory.getLogger(PromocionValidator.class);

    /**
     * Valida los campos esenciales de una promoción.
     *
     * @param promocion El objeto Promocion a validar.
     * @return Una lista de errores; vacía si la promoción es válida.
     */
    public List<String> validate(Promocion promocion) {
        List<String> errors = new ArrayList<>();

        // Valida que el objeto promoción no sea nulo.
        if (promocion == null) {
            errors.add("La promoción no puede ser nula.");
            return errors;
        }

        // Validación de campos obligatorios/básicos.
        if (promocion.getIdPromocion() == null) {
            errors.add("ID de promoción no puede ser nulo.");
        }
        if (StringUtils.isBlank(promocion.getNombre())) {
            errors.add("El nombre de la promoción no puede estar vacío.");
        }
        if (StringUtils.isBlank(promocion.getTipoPromocion())) {
            errors.add("El tipo de promoción no puede estar vacío.");
        }
        if (promocion.getValorDescuento() == null || promocion.getValorDescuento() < 0) {
            errors.add("El valor de descuento debe ser un número positivo.");
        }
        if (promocion.getFechaInicio() == null) {
            errors.add("La fecha de inicio no puede ser nula.");
        }
        if (promocion.getFechaFin() == null) {
            errors.add("La fecha de fin no puede ser nula.");
        } else if (promocion.getFechaInicio() != null && promocion.getFechaFin().isBefore(promocion.getFechaInicio())) {
            errors.add("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        
        // Registro de advertencia si se encuentran errores.
        if (!errors.isEmpty()) {
            logger.warn("Errores de validación para promoción '{}': {}", promocion.getNombre(), String.join(", ", errors));
        }

        return errors;
    }

    /**
     * Normaliza los datos de texto de la promoción (ej., elimina espacios en blanco al inicio/final).
     *
     * @param promocion La promoción a normalizar.
     */
    public void normalizePromocion(Promocion promocion) {
        if (promocion == null) {
            return; // No se puede normalizar un objeto nulo.
        }

        // Aplica trim() a los campos String si no son nulos.
        if (promocion.getNombre() != null) {
            promocion.setNombre(promocion.getNombre().trim());
        }
        if (promocion.getDescripcion() != null) {
            promocion.setDescripcion(promocion.getDescripcion().trim());
        }
        if (promocion.getTipoPromocion() != null) {
            promocion.setTipoPromocion(promocion.getTipoPromocion().trim());
        }
        if (promocion.getCodigoPromocional() != null) {
            promocion.setCodigoPromocional(promocion.getCodigoPromocional().trim());
        }
        if (promocion.getAplicableA() != null) {
            promocion.setAplicableA(promocion.getAplicableA().trim());
        }
    }
}
