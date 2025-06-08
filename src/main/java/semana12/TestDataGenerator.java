package semana12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de datos de prueba CORREGIDO
 * Soluciona problemas de NullPointerException y mejora la generación de datos
 */
public class TestDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    
    private final Random random = new Random();
    
    // Datos de ejemplo para generar empleados realistas
    private final String[] firstNames = {
        "Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "Miguel", "Carmen", 
        "José", "Isabel", "Francisco", "Pilar", "Manuel", "Rosa", "Antonio",
        "Elena", "Javier", "Mónica", "David", "Patricia", "Daniel", "Cristina"
    };
    
    private final String[] lastNames = {
        "García", "Rodríguez", "González", "Fernández", "López", "Martínez", 
        "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández",
        "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez"
    };
    
    private final String[] departments = {
        "IT", "HR", "Finance", "Sales", "Marketing", "Operations", 
        "Legal", "R&D", "Customer Service", "Quality Assurance"
    };
    
    private final String[] domains = {
        "empresa.com", "techcorp.es", "company.net", "corporacion.com"
    };
    
    /**
     * Genera una lista de empleados de prueba
     */
    public List<Employee> generateTestEmployees(int count) {
        System.out.println("🔄 Generando " + count + " empleados de prueba...");
        
        List<Employee> employees = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            Employee employee = generateRandomEmployee((long) i);
            
            // VALIDAR que el empleado esté completo antes de agregarlo
            if (isEmployeeComplete(employee)) {
                employees.add(employee);
                
                if (i % 10 == 0) {
                    System.out.println("📊 Generados: " + i + " empleados");
                }
            } else {
                System.out.println("⚠️ Empleado " + i + " incompleto, regenerando...");
                i--; // Reintentar este empleado
            }
        }
        
        System.out.println("✅ Generación completada: " + employees.size() + " empleados");
        return employees;
    }
    
    /**
     * Valida que un empleado tenga todos los datos necesarios
     */
    private boolean isEmployeeComplete(Employee employee) {
        return employee != null && 
               employee.getId() != null &&
               employee.getFirstName() != null && !employee.getFirstName().trim().isEmpty() &&
               employee.getLastName() != null && !employee.getLastName().trim().isEmpty() &&
               employee.getEmail() != null && !employee.getEmail().trim().isEmpty() &&
               employee.getDepartment() != null && !employee.getDepartment().trim().isEmpty() &&
               employee.getSalary() != null && employee.getSalary() > 0 &&
               employee.getBirthDate() != null &&
               employee.getHireDate() != null;
    }
    
    /**
     * Genera un empleado aleatorio con datos realistas - VERSIÓN CORREGIDA
     */
    private Employee generateRandomEmployee(Long id) {
        Employee employee = new Employee();
        
        try {
            // ID
            employee.setId(id);
            
            // Nombre y apellido aleatorios
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            
            // Email basado en nombre y apellido
            String domain = domains[random.nextInt(domains.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                          (random.nextInt(100) < 20 ? String.valueOf(random.nextInt(99)) : "") + 
                          "@" + domain;
            employee.setEmail(email);
            
            // Departamento aleatorio
            employee.setDepartment(departments[random.nextInt(departments.length)]);
            
            // SALARIO - CORREGIDO para evitar null
            double baseSalary = getBaseSalaryForDepartment(employee.getDepartment());
            double variation = 0.7 + (random.nextDouble() * 0.6); // Factor entre 0.7 y 1.3
            double finalSalary = baseSalary * variation;
            
            // Redondear a múltiplos de 1000 y asegurar mínimo
            finalSalary = Math.max(25000, Math.round(finalSalary / 1000) * 1000);
            employee.setSalary(finalSalary);
            
            // Fecha de nacimiento (25-65 años) - CORREGIDA
            int age = 25 + random.nextInt(40); // Entre 25 y 64 años
            LocalDate birthDate = LocalDate.now().minusYears(age).minusDays(random.nextInt(365));
            employee.setBirthDate(birthDate);
            
            // Fecha de contratación - CORREGIDA
            LocalDate earliestHireDate = birthDate.plusYears(18); // Mínimo 18 años al contratar
            LocalDate latestHireDate = LocalDate.now().minusMonths(1); // Máximo hace 1 mes
            
            // Asegurar que la fecha de contratación sea válida
            if (earliestHireDate.isAfter(latestHireDate)) {
                earliestHireDate = latestHireDate.minusYears(1); // Al menos 1 año de experiencia
            }
            
            long daysBetween = latestHireDate.toEpochDay() - earliestHireDate.toEpochDay();
            if (daysBetween <= 0) {
                daysBetween = 365; // Fallback: 1 año
            }
            
            LocalDate hireDate = earliestHireDate.plusDays(random.nextLong() % daysBetween);
            employee.setHireDate(hireDate);
            
            return employee;
            
        } catch (Exception e) {
            System.err.println("❌ Error generando empleado " + id + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene salario base según departamento
     */
    private double getBaseSalaryForDepartment(String department) {
        if (department == null) return 50000;
        
        switch (department.toLowerCase()) {
            case "it":
            case "r&d":
                return 75000;
            case "finance":
            case "legal":
                return 70000;
            case "sales":
            case "marketing":
                return 60000;
            case "hr":
            case "operations":
                return 55000;
            case "customer service":
            case "quality assurance":
                return 45000;
            default:
                return 50000;
        }
    }
    
    /**
     * Genera archivo Excel completo con datos de prueba - VERSIÓN SIMPLIFICADA
     */
    public void generateTestFile(String filePath, int normalEmployees) throws IOException {
        
        System.out.println("🔄 Generando archivo de prueba: " + filePath);
        System.out.println("📊 Empleados a generar: " + normalEmployees);
        
        List<Employee> allEmployees = new ArrayList<>();
        
        // Agregar empleados normales
        if (normalEmployees > 0) {
            List<Employee> generatedEmployees = generateTestEmployees(normalEmployees);
            allEmployees.addAll(generatedEmployees);
        }
        
        // Escribir al archivo Excel
        ExcelService excelService = new ExcelService();
        excelService.writeEmployeesToExcel(allEmployees, filePath);
        
        System.out.println("✅ Archivo generado exitosamente: " + filePath + " (" + allEmployees.size() + " empleados)");
    }
    
    /**
     * Método main SIMPLIFICADO para generar archivos de prueba
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║     GENERADOR DE DATOS DE PRUEBA   ║");
        System.out.println("╚════════════════════════════════════╝");
        
        TestDataGenerator generator = new TestDataGenerator();
        
        try {
            // Crear directorio data si no existe
            java.io.File dataDir = new java.io.File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                System.out.println("📁 Directorio 'data' creado");
            }
            
            // Generar archivo pequeño para pruebas
            System.out.println("\n--- Generando archivo pequeño ---");
            generator.generateTestFile("data/empleados_prueba.xlsx", 10);
            
            // Generar archivo mediano
            System.out.println("\n--- Generando archivo mediano ---");
            generator.generateTestFile("data/empleados_100.xlsx", 100);
            
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║        GENERACIÓN COMPLETADA       ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("📁 Archivos generados:");
            System.out.println("  • data/empleados_prueba.xlsx (10 empleados)");
            System.out.println("  • data/empleados_100.xlsx (100 empleados)");
            
        } catch (IOException e) {
            System.err.println("❌ Error al generar archivos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}