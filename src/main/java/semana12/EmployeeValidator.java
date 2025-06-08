package semana12;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de validación de empleados
 * Demuestra el uso de Apache Commons Validator y Logback
 */
public class EmployeeValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeValidator.class);
    private final EmailValidator emailValidator;
    
    public EmployeeValidator() {
        this.emailValidator = EmailValidator.getInstance();
        logger.debug("EmployeeValidator inicializado");
    }
    
    /**
     * Valida un empleado y retorna una lista de errores
     * @param employee Empleado a validar
     * @return Lista de errores encontrados (vacía si es válido)
     */
    public List<String> validate(Employee employee) {
        logger.debug("Iniciando validación del empleado: {}", 
                    employee != null ? employee.getId() : "null");
        
        List<String> errors = new ArrayList<>();
        
        if (employee == null) {
            errors.add("El empleado no puede ser nulo");
            logger.warn("Intento de validar empleado nulo");
            return errors;
        }
        
        // Validar ID
        if (employee.getId() == null || employee.getId() <= 0) {
            errors.add("ID debe ser un número positivo");
            logger.debug("Error de validación: ID inválido para empleado");
        }
        
        // Validar nombre
        if (StringUtils.isBlank(employee.getFirstName())) {
            errors.add("El nombre es obligatorio");
        } else if (employee.getFirstName().length() < 2) {
            errors.add("El nombre debe tener al menos 2 caracteres");
        }
        
        // Validar apellido
        if (StringUtils.isBlank(employee.getLastName())) {
            errors.add("El apellido es obligatorio");
        } else if (employee.getLastName().length() < 2) {
            errors.add("El apellido debe tener al menos 2 caracteres");
        }
        
        // Validar email usando Apache Commons Validator
        if (StringUtils.isBlank(employee.getEmail())) {
            errors.add("El email es obligatorio");
        } else if (!emailValidator.isValid(employee.getEmail())) {
            errors.add("El formato del email es inválido: " + employee.getEmail());
            logger.warn("Email inválido detectado: {}", employee.getEmail());
        }
        
        // Validar departamento
        if (StringUtils.isBlank(employee.getDepartment())) {
            errors.add("El departamento es obligatorio");
        }
        
        // Validar salario
        if (employee.getSalary() == null) {
            errors.add("El salario es obligatorio");
        } else if (employee.getSalary() <= 0) {
            errors.add("El salario debe ser mayor que cero");
        } else if (employee.getSalary() > 1000000) {
            errors.add("El salario parece excesivamente alto, revisar");
            logger.warn("Salario muy alto detectado: {} para empleado {}", 
                       employee.getSalary(), employee.getId());
        }
        
        // Validar fecha de nacimiento
        if (employee.getBirthDate() == null) {
            errors.add("La fecha de nacimiento es obligatoria");
        } else {
            LocalDate minDate = LocalDate.now().minusYears(100);
            LocalDate maxDate = LocalDate.now().minusYears(16);
            
            if (employee.getBirthDate().isBefore(minDate)) {
                errors.add("La fecha de nacimiento es muy antigua");
            } else if (employee.getBirthDate().isAfter(maxDate)) {
                errors.add("El empleado debe ser mayor de 16 años");
            }
        }
        
        // Validar fecha de contratación
        if (employee.getHireDate() == null) {
            errors.add("La fecha de contratación es obligatoria");
        } else {
            if (employee.getHireDate().isAfter(LocalDate.now())) {
                errors.add("La fecha de contratación no puede ser futura");
            }
            
            if (employee.getBirthDate() != null && 
                employee.getHireDate().isBefore(employee.getBirthDate().plusYears(16))) {
                errors.add("La fecha de contratación debe ser al menos 16 años después del nacimiento");
            }
        }
        
        if (errors.isEmpty()) {
            logger.debug("Empleado {} validado correctamente", employee.getId());
        } else {
            logger.info("Empleado {} tiene {} errores de validación", 
                       employee.getId(), errors.size());
        }
        
        return errors;
    }
    
    /**
     * Valida si un empleado es válido (sin errores)
     */
    public boolean isValid(Employee employee) {
        return validate(employee).isEmpty();
    }
    
    /**
     * Normaliza los datos de un empleado
     */
    public void normalizeEmployee(Employee employee) {
        if (employee == null) return;
        
        logger.debug("Normalizando datos del empleado {}", employee.getId());
        
        // Normalizar nombres (capitalizar primera letra)
        if (StringUtils.isNotBlank(employee.getFirstName())) {
            employee.setFirstName(StringUtils.capitalize(employee.getFirstName().toLowerCase().trim()));
        }
        
        if (StringUtils.isNotBlank(employee.getLastName())) {
            employee.setLastName(StringUtils.capitalize(employee.getLastName().toLowerCase().trim()));
        }
        
        // Normalizar email (convertir a minúsculas)
        if (StringUtils.isNotBlank(employee.getEmail())) {
            employee.setEmail(employee.getEmail().toLowerCase().trim());
        }
        
        // Normalizar departamento
        if (StringUtils.isNotBlank(employee.getDepartment())) {
            employee.setDepartment(StringUtils.capitalize(employee.getDepartment().toLowerCase().trim()));
        }
        
        logger.debug("Empleado {} normalizado", employee.getId());
    }
}
