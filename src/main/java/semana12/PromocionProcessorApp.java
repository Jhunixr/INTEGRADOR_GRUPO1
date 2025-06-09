package semana12;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Aplicación principal para gestionar promociones.
 * Realiza lectura, validación y escritura de datos de promociones en archivos Excel.
 */
public class PromocionProcessorApp {

    private static final Logger logger = LoggerFactory.getLogger(PromocionProcessorApp.class);

    private final ExcelService excelService;
    private final PromocionValidator validator;
    private final PromocionTestDataGenerator testDataGenerator;

    /**
     * Constructor que inicializa los servicios y el generador de datos.
     */
    public PromocionProcessorApp() {
        this.excelService = new ExcelService();
        this.validator = new PromocionValidator();
        this.testDataGenerator = new PromocionTestDataGenerator();
        logger.info("PromocionProcessorApp iniciada. Timestamp: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Orquesta el proceso de una promoción: lee, valida y escribe.
     * Si el archivo de entrada no existe, genera uno con datos de prueba.
     *
     * @param inputFilePath  Ruta del archivo Excel de entrada.
     * @param outputFilePath Ruta del archivo Excel de salida.
     */
    public void processPromocionFile(String inputFilePath, String outputFilePath) {

        Stopwatch totalStopwatch = Stopwatch.createStarted();

        logger.info("=== INICIANDO PROCESAMIENTO DE PROMOCIONES ===");
        logger.info("Archivo de entrada: {}", inputFilePath);
        logger.info("Archivo de salida: {}", outputFilePath);

        try {
            // Asegura que el archivo de entrada exista; si no, lo genera.
            if (!ensureInputFileExists(inputFilePath)) {
                logger.error("No se pudo preparar el archivo de entrada para promociones. Abortando.");
                return;
            }

            // 1. Lee promociones del Excel.
            logger.info("--- PASO 1: Leyendo promociones del archivo Excel ---");
            Stopwatch readStopwatch = Stopwatch.createStarted();
            List<Promocion> promociones = excelService.readPromocionesFromExcel(inputFilePath);
            readStopwatch.stop();
            logger.info("Lectura completada en {} ms. {} promociones leídas.",
                         readStopwatch.elapsed(TimeUnit.MILLISECONDS), promociones.size());

            if (promociones.isEmpty()) {
                logger.warn("No se encontraron promociones en el archivo de entrada. Nada que procesar.");
                return;
            }

            // 2. Valida y normaliza las promociones.
            logger.info("--- PASO 2: Validando y normalizando promociones ---");
            Stopwatch validationStopwatch = Stopwatch.createStarted();
            List<Promocion> validPromociones = validateAndNormalizePromociones(promociones);
            validationStopwatch.stop();
            logger.info("Validación completada en {} ms. {} promociones válidas de {} totales.",
                         validationStopwatch.elapsed(TimeUnit.MILLISECONDS),
                         validPromociones.size(), promociones.size());

            // PASO 3 (Opcional): Integración con un servicio de estadísticas de promociones.
            // Si se necesitara, este sería el punto para generar informes o métricas avanzadas.

            // 4. Escribe las promociones válidas en el archivo de salida.
            logger.info("--- PASO 4: Escribiendo archivo de salida ---");
            Stopwatch writeStopwatch = Stopwatch.createStarted();
            excelService.writePromocionesToExcel(validPromociones, outputFilePath);
            writeStopwatch.stop();
            logger.info("Escritura completada en {} ms.", writeStopwatch.elapsed(TimeUnit.MILLISECONDS));

        } catch (IOException e) {
            logger.error("Error de E/S durante el procesamiento de promociones: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado durante el procesamiento de promociones: {}", e.getMessage(), e);
        } finally {
            totalStopwatch.stop();
            logger.info("=== PROCESAMIENTO DE PROMOCIONES COMPLETADO EN {} ms ===",
                         totalStopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Verifica la existencia del archivo de entrada. Si no existe, lo genera
     * con un conjunto de promociones de prueba.
     *
     * @param inputFilePath La ruta del archivo a verificar/generar.
     * @return true si el archivo existe o fue generado con éxito; false en caso contrario.
     */
    private boolean ensureInputFileExists(String inputFilePath) {
        File inputFile = new File(inputFilePath);

        if (inputFile.exists()) {
            logger.info("✅ Archivo de entrada de promociones encontrado: {}", inputFilePath);
            return true;
        }

        logger.warn("⚠️ Archivo de entrada de promociones no encontrado: {}", inputFilePath);
        logger.info("🔄 Generando archivo de datos de prueba automáticamente (20 promociones)...");

        try {
            // Crea el directorio padre si no existe.
            File parentDir = inputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
                logger.info("📁 Directorio 'data' creado: {}", parentDir.getPath());
            }

            // Genera y escribe promociones de prueba en el archivo de entrada.
            List<Promocion> testPromociones = testDataGenerator.generateTestPromociones(20);
            excelService.writePromocionesToExcel(testPromociones, inputFilePath);

            logger.info("✅ Archivo de entrada de promociones generado exitosamente en: {}", inputFilePath);
            return true;

        } catch (Exception e) {
            logger.error("❌ Error al generar archivo de entrada de promociones: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Normaliza y filtra una lista de promociones, manteniendo solo las válidas.
     *
     * @param promociones La lista original de promociones.
     * @return Una nueva lista con las promociones que pasaron la validación.
     */
    private List<Promocion> validateAndNormalizePromociones(List<Promocion> promociones) {
        logger.debug("Iniciando validación y normalización de {} promociones.", promociones.size());

        List<Promocion> validPromociones = promociones.stream()
                .peek(validator::normalizePromocion) // Normaliza los datos de cada promoción.
                .filter(promocion -> {
                    List<String> errors = validator.validate(promocion);
                    if (errors.isEmpty()) {
                        return true; // La promoción es válida.
                    } else {
                        // Registra los errores para promociones inválidas.
                        logger.warn("Promoción '{}' inválida. Errores: {}",
                                     promocion.getNombre(), String.join(", ", errors));
                        return false; // Excluye la promoción inválida.
                    }
                })
                .collect(Collectors.toList());

        int validCount = validPromociones.size();
        int invalidCount = promociones.size() - validCount;

        logger.info("Resumen de validación: {} válidas, {} inválidas.", validCount, invalidCount);

        if (invalidCount > 0) {
            logger.warn("Se excluyeron {} promociones con datos inválidos.", invalidCount);
        }

        return validPromociones;
    }

    /**
     * Punto de entrada principal de la aplicación.
     * Configura y ejecuta el proceso de manejo de promociones.
     *
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║    PROMOCION PROCESSOR APPLICATION   ║");
        System.out.println("╚════════════════════════════════════╝");

        logger.info("=== INICIANDO PROMOCION PROCESSOR APPLICATION ===");
        logger.info("Versión Java: {}", System.getProperty("java.version"));
        logger.info("Usuario: {}", System.getProperty("user.name"));
        logger.info("Directorio de trabajo: {}", System.getProperty("user.dir"));

        try {
            PromocionProcessorApp app = new PromocionProcessorApp();

            String inputFile = "data/promociones_input.xlsx";
            String outputFile = "data/promociones_processed_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                                ".xlsx";

            app.processPromocionFile(inputFile, outputFile);

            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║          PROCESAMIENTO EXITOSO!          ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("Archivos generados:");
            System.out.println("  • " + inputFile + " (datos de entrada)");
            System.out.println("  • " + outputFile + " (datos procesados)");

            logger.info("=== APLICACIÓN FINALIZADA EXITOSAMENTE ===");

        } catch (Exception e) {
            logger.error("Error fatal en la aplicación de promociones: {}", e.getMessage(), e);
            System.err.println("Error fatal: " + e.getMessage());
            System.exit(1);
        }
    }
}
