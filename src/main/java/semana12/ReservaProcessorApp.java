package semana12;

import com.google.common.base.Stopwatch;
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
 * Aplicación principal que procesa reservas:
 * 1. Verifica si existe archivo de entrada
 * 2. Lo genera si no existe (usando TestReservaDataGenerator)
 * 3. Procesa datos, valida y genera estadísticas
 */
public class ReservaProcessorApp {

    private static final Logger logger = LoggerFactory.getLogger(ReservaProcessorApp.class);

    // Servicios
    private final ExcelService excelService;
    private final ReservaValidator validator;
    private final ReservaStatisticsService statisticsService;
    private final TestReservaDataGenerator generator;

    public ReservaProcessorApp() {
        this.excelService      = new ExcelService();
        this.validator         = new ReservaValidator();
        this.statisticsService = new ReservaStatisticsService();
        this.generator         = new TestReservaDataGenerator();

        logger.info("ReservaProcessorApp inicializada");
        logger.info("Timestamp: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public void processReservaFile(String inputFilePath, String outputFilePath) {

        Stopwatch totalStopwatch = Stopwatch.createStarted();
        logger.info("=== INICIANDO PROCESAMIENTO DE RESERVAS ===");
        logger.info("Entrada: {}", inputFilePath);
        logger.info("Salida : {}", outputFilePath);

        try {
            if (!ensureInputFileExists(inputFilePath)) {
                logger.error("No se pudo crear o encontrar el archivo de entrada");
                return;
            }

            // 1) Leer reservas
            logger.info("--- PASO 1: Leyendo reservas ---");
            Stopwatch readSw = Stopwatch.createStarted();
            List<Reserva> reservas = excelService.readReservasFromExcel(inputFilePath);
            readSw.stop();
            logger.info("Lectura en {} ms: {} reservas", readSw.elapsed(TimeUnit.MILLISECONDS), reservas.size());
            if (reservas.isEmpty()) {
                logger.warn("No hay reservas en el archivo");
                return;
            }

            // 2) Validar
            logger.info("--- PASO 2: Validando reservas ---");
            Stopwatch valSw = Stopwatch.createStarted();
            List<Reserva> validas = validateReservas(reservas);
            valSw.stop();
            logger.info("Validación en {} ms: {} válidas de {}",
                        valSw.elapsed(TimeUnit.MILLISECONDS), validas.size(), reservas.size());
            if (validas.isEmpty()) {
                logger.warn("No hay reservas válidas para procesar");
                return;
            }

            // 3) Estadísticas
            logger.info("--- PASO 3: Generando estadísticas ---");
            Stopwatch statSw = Stopwatch.createStarted();
            generateStatistics(validas);
            statSw.stop();
            logger.info("Estadísticas en {} ms", statSw.elapsed(TimeUnit.MILLISECONDS));

            // 4) Escribir salida
            logger.info("--- PASO 4: Escribiendo archivo de salida ---");
            Stopwatch writeSw = Stopwatch.createStarted();
            excelService.writeReservasToExcel(validas, outputFilePath);
            writeSw.stop();
            logger.info("Escritura en {} ms", writeSw.elapsed(TimeUnit.MILLISECONDS));

        } catch (IOException e) {
            logger.error("Error E/S: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage(), e);
        } finally {
            totalStopwatch.stop();
            logger.info("=== PROCESAMIENTO COMPLETADO EN {} ms ===", totalStopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * Si no existe el Excel de entrada, lo genera usando
     * TestReservaDataGenerator y lo escribe con ExcelService.
     */
    private boolean ensureInputFileExists(String inputFilePath) {
        File in = new File(inputFilePath);
        if (in.exists()) {
            logger.info("✅ Archivo de entrada encontrado: {}", inputFilePath);
            return true;
        }
        logger.warn("⚠️ No existe: {} – Generando datos de prueba...", inputFilePath);
        try {
            File dir = in.getParentFile();
            if (dir != null && !dir.exists()) {
                if (dir.mkdirs()) logger.info("📁 Directorio creado: {}", dir.getPath());
                else              logger.warn("❌ No se pudo crear directorio: {}", dir.getPath());
            }
            // Generar y escribir Excel
            List<Reserva> prueba = generator.generateTestReservas(50);
            excelService.writeReservasToExcel(prueba, inputFilePath);
            logger.info("✅ Archivo de entrada generado con {} reservas", prueba.size());
            return true;
        } catch (Exception e) {
            logger.error("❌ Error generando archivo de entrada: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Aplica el validador y filtra inválidas.
     */
    private List<Reserva> validateReservas(List<Reserva> reservas) {
        return reservas.stream()
                .filter(r -> {
                    var errs = validator.validate(r);
                    if (!errs.isEmpty()) {
                        logger.warn("Reserva {} inválida: {}", r.getIdReserva(), String.join(", ", errs));
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Muestra resumen y detalles usando StatisticsService.
     */
    private void generateStatistics(List<Reserva> reservas) {
        var summary = statisticsService.createSummary(reservas);
        logger.info("RESUMEN: total={} | avgMonto={} | avgDuracion={} | estados={}",
                summary.getTotalReservas(),
                String.format("%,.2f", summary.getAverageMonto()),
                String.format("%.1f", summary.getAverageDuracionHoras()),
                summary.getEstados());

        var statsPorEstado = statisticsService.calculateEstadoStatistics(reservas);
        statsPorEstado.forEach((estado, stats) ->
                logger.info("  {} → count={} | avgMonto={}",
                        estado,
                        stats.getReservaCount(),
                        String.format("%,.2f", stats.getAverageMonto()))
        );

        var top5 = statisticsService.getTopReservasByMonto(reservas, 5);
        logger.info("TOP 5 por monto:");
        top5.forEach(r ->
                logger.info("  {} → ${}", r.getIdReserva(), String.format("%,.2f", r.getMonto()))
        );
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════╗");
        System.out.println("║ RESERVA APP 📊 ║");
        System.out.println("╚════════════════╝");
        logger.info("Inicio de ReservaProcessorApp");

        String in  = "data/reservas_input.xlsx";
        String out = "data/reservas_processed_" +
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                     ".xlsx";

        new ReservaProcessorApp().processReservaFile(in, out);
    }
}


