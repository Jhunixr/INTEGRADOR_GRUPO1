package semana12;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Aplicación principal CORREGIDA que:
 * 1. Verifica si existe el archivo de entrada
 * 2. Lo genera automáticamente si no existe
 * 3. Procesa los datos normalmente
 */
public class EmployeeProcessorApp {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeProcessorApp.class);
    
    // Servicios
    private final ExcelService excelService;
    private final EmployeeValidator validator;
    private final StatisticsService statisticsService;
    
    public EmployeeProcessorApp() {
        this.excelService = new ExcelService();
        this.validator = new EmployeeValidator();
        this.statisticsService = new StatisticsService();
        
        logger.info("EmployeeProcessorApp inicializada");
        logger.info("Timestamp: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    

    public void processEmployeeFile(String inputFilePath, String outputFilePath) {
        
        // Usar Guava Stopwatch para medir tiempo de ejecución
        Stopwatch totalStopwatch = Stopwatch.createStarted();
        
        logger.info("=== INICIANDO PROCESAMIENTO DE EMPLEADOS ===");
        logger.info("Archivo de entrada: {}", inputFilePath);
        logger.info("Archivo de salida: {}", outputFilePath);
        
        try {
            // VERIFICAR Y CREAR ARCHIVO DE ENTRADA SI NO EXISTE
            if (!ensureInputFileExists(inputFilePath)) {
                logger.error("No se pudo crear o encontrar el archivo de entrada");
                return;
            }
            
            // PASO 1: Leer empleados del Excel
            logger.info("--- PASO 1: Leyendo empleados del archivo Excel ---");
            Stopwatch readStopwatch = Stopwatch.createStarted();
            
            List<Employee> employees = excelService.readEmployeesFromExcel(inputFilePath);
            
            readStopwatch.stop();
            logger.info("Lectura completada en {} ms. {} empleados leídos", 
                       readStopwatch.elapsed(TimeUnit.MILLISECONDS), employees.size());
            
            if (employees.isEmpty()) {
                logger.warn("No se encontraron empleados en el archivo");
                return;
            }
            
            // PASO 2: Validar y normalizar empleados
            logger.info("--- PASO 2: Validando y normalizando empleados ---");
            Stopwatch validationStopwatch = Stopwatch.createStarted();
            
            List<Employee> validEmployees = validateAndNormalizeEmployees(employees);
            
            validationStopwatch.stop();
            logger.info("Validación completada en {} ms. {} empleados válidos de {} totales", 
                       validationStopwatch.elapsed(TimeUnit.MILLISECONDS), 
                       validEmployees.size(), employees.size());
            
            // PASO 3: Generar estadísticas
            logger.info("--- PASO 3: Generando estadísticas ---");
            Stopwatch statsStopwatch = Stopwatch.createStarted();
            
            generateStatistics(validEmployees);
            
            statsStopwatch.stop();
            logger.info("Estadísticas generadas en {} ms", 
                       statsStopwatch.elapsed(TimeUnit.MILLISECONDS));
            
            // PASO 4: Escribir archivo de salida
            logger.info("--- PASO 4: Escribiendo archivo de salida ---");
            Stopwatch writeStopwatch = Stopwatch.createStarted();
            
            excelService.writeEmployeesToExcel(validEmployees, outputFilePath);
            
            writeStopwatch.stop();
            logger.info("Escritura completada en {} ms", 
                       writeStopwatch.elapsed(TimeUnit.MILLISECONDS));
            
            // PASO 5: Demostrar filtros avanzados
            logger.info("--- PASO 5: Demostrando filtros avanzados ---");
            demonstrateAdvancedFiltering(validEmployees);
            
        } catch (IOException e) {
            logger.error("Error de E/S durante el procesamiento: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado durante el procesamiento: {}", e.getMessage(), e);
        } finally {
            totalStopwatch.stop();
            logger.info("=== PROCESAMIENTO COMPLETADO EN {} ms ===", 
                       totalStopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }
    
    /**
     * NUEVO MÉTODO: Asegura que el archivo de entrada existe
     */
    private boolean ensureInputFileExists(String inputFilePath) {
        File inputFile = new File(inputFilePath);
        
        // Si el archivo existe, todo bien
        if (inputFile.exists()) {
            logger.info("✅ Archivo de entrada encontrado: {}", inputFilePath);
            return true;
        }
        
        // Si no existe, intentar generarlo
        logger.warn("⚠️ Archivo de entrada no encontrado: {}", inputFilePath);
        logger.info("🔄 Generando archivo de datos de prueba automáticamente...");
        
        try {
            // Crear directorio si no existe
            File parentDir = inputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                logger.info("📁 Directorio creado: {}", parentDir.getPath());
            }
            
            // Generar archivo con datos de prueba
            TestDataGenerator generator = new TestDataGenerator();
            generator.generateTestFile(inputFilePath, 50); // 50 empleados de prueba
            
            logger.info("✅ Archivo de entrada generado exitosamente");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error al generar archivo de entrada: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida y normaliza la lista de empleados
     */
    private List<Employee> validateAndNormalizeEmployees(List<Employee> employees) {
        logger.debug("Iniciando validación de {} empleados", employees.size());
        
        List<Employee> validEmployees = employees.stream()
            .peek(validator::normalizeEmployee) // Normalizar datos
            .filter(employee -> {
                List<String> errors = validator.validate(employee);
                if (errors.isEmpty()) {
                    return true;
                } else {
                    logger.warn("Empleado {} inválido. Errores: {}", 
                               employee.getId(), String.join(", ", errors));
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        int validCount = validEmployees.size();
        int invalidCount = employees.size() - validCount;
        
        logger.info("Validación completada: {} válidos, {} inválidos", validCount, invalidCount);
        
        if (invalidCount > 0) {
            logger.warn("Se encontraron {} empleados con datos inválidos que fueron excluidos", invalidCount);
        }
        
        return validEmployees;
    }
    
    /**
     * Genera y muestra estadísticas detalladas
     */
    private void generateStatistics(List<Employee> employees) {
        logger.debug("Generando estadísticas para {} empleados", employees.size());
        
        // Resumen general
        StatisticsService.EmployeeSummary summary = statisticsService.createSummary(employees);
        logger.info("RESUMEN GENERAL:");
        logger.info("  Total empleados: {}", summary.getTotalEmployees());
        logger.info("  Salario promedio: ${:,.2f}", summary.getAverageSalary());
        logger.info("  Edad promedio: {:.1f} años", summary.getAverageAge());
        logger.info("  Departamentos: {}", summary.getDepartments());
        
        // Estadísticas por departamento
        Map<String, StatisticsService.DepartmentStats> deptStats = 
            statisticsService.calculateDepartmentStatistics(employees);
        
        logger.info("ESTADÍSTICAS POR DEPARTAMENTO:");
        deptStats.forEach((dept, stats) -> {
            logger.info("  {} ({} empleados):", dept, stats.getEmployeeCount());
            logger.info("    Salario promedio: ${:,.2f}", stats.getAverageSalary());
            logger.info("    Edad promedio: {:.1f} años", stats.getAverageAge());
            
            if (stats.getHighestPaidEmployee() != null) {
                Employee highestPaid = stats.getHighestPaidEmployee();
                logger.info("    Mejor pagado: {} (${:,.2f})", 
                           highestPaid.getFullName(), highestPaid.getSalary());
            }
        });
        
        // Top empleados por salario
        List<Employee> topEmployees = statisticsService.getTopEmployeesBySalary(employees, 5);
        logger.info("TOP 5 EMPLEADOS POR SALARIO:");
        topEmployees.forEach(emp -> 
            logger.info("  {}: ${:,.2f} ({})", emp.getFullName(), emp.getSalary(), emp.getDepartment())
        );
    }
    
    /**
     * Demuestra el uso de filtros avanzados con Guava
     */
    private void demonstrateAdvancedFiltering(List<Employee> employees) {
        logger.info("Demostrando filtros avanzados:");
        
        // Filtro 1: Empleados con salario alto
        List<Employee> highEarners = statisticsService.filterEmployees(employees, 80000.0, null, null);
        logger.info("  Empleados con salario >= $80,000: {}", highEarners.size());
        
        // Filtro 2: Empleados senior por edad
        List<Employee> seniorEmployees = statisticsService.filterEmployees(employees, null, 45, null);
        logger.info("  Empleados con edad >= 45 años: {}", seniorEmployees.size());
        
        // Filtro 3: Empleados de un departamento específico
        List<Employee> itEmployees = statisticsService.filterEmployees(employees, null, null, "IT");
        logger.info("  Empleados del departamento IT: {}", itEmployees.size());
        
        // Agrupamiento avanzado
        Multimap<String, Employee> groupedByDept = statisticsService.groupByDepartment(employees);
        logger.info("  Agrupamiento por departamento completado: {} grupos", groupedByDept.keySet().size());
    }
    
    /**
     * Método main CORREGIDO
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║   EMPLOYEE PROCESSOR APPLICATION   ║");
        System.out.println("╚════════════════════════════════════╝");
        
        logger.info("=== INICIANDO EMPLOYEE PROCESSOR APPLICATION ===");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("User: {}", System.getProperty("user.name"));
        logger.info("Working Directory: {}", System.getProperty("user.dir"));
        
        try {
            EmployeeProcessorApp app = new EmployeeProcessorApp();
            
            // Archivos con nombres más simples
            String inputFile = "data/employees_input.xlsx";
            String outputFile = "data/employees_processed_" + 
                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                              ".xlsx";
            
            // Ejecutar procesamiento
            app.processEmployeeFile(inputFile, outputFile);
            
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║       PROCESAMIENTO EXITOSO!      ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("Archivos generados:");
            System.out.println("  • " + inputFile + " (datos de entrada)");
            System.out.println("  • " + outputFile + " (datos procesados)");
            
            logger.info("=== APLICACIÓN FINALIZADA EXITOSAMENTE ===");
            
        } catch (Exception e) {
            logger.error("Error fatal en la aplicación: {}", e.getMessage(), e);
            System.err.println("Error fatal: " + e.getMessage());
            System.exit(1);
        }
    }
}