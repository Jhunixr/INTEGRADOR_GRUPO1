package semana12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Clase principal para probar la integración de librerías y funcionalidades de Promocion.
 * Demuestra logging, manejo de colecciones, utilidades de String, comparaciones y guardado a Excel.
 */
public class PromocionDemo {

    private static final Logger logger = LoggerFactory.getLogger(PromocionDemo.class);

    public static void main(String[] args) {
        logger.info("INICIANDO PRUEBAS DE LIBRERÍAS Y CLASE PROMOCION.");

        // --- DEMOSTRACIÓN: LOGBACK (Registro de eventos) ---
        logger.info("\n--- Probando Logback ---");
        logger.error("Logback ERROR: Error crítico al aplicar promoción X.");
        logger.warn("Logback WARN: La promoción 'Envío Gratis' expira en 3 días.");
        logger.info("Logback INFO: Nuevo usuario registrado para promociones.");

        // --- DEMOSTRACIÓN: GUAVA Y CREACIÓN DE PROMOCIONES ---
        logger.info("\n--- Probando Guava y creación de Promociones ---");
        // Creación de promociones con datos claros para el Excel.
        Promocion promo1 = new Promocion(
            UUID.randomUUID(), // ID único
            "Promo Verano 2025",
            "Descuento del 15% en todos los servicios de verano.",
            "Porcentual",
            0.15,
            LocalDate.of(2025, 7, 1),
            LocalDate.of(2025, 8, 31),
            "VERANO15",
            100, // Cantidad máxima de usos
            "Servicios",
            true // Activa
        );

        Promocion promo2 = new Promocion(
            UUID.randomUUID(),
            "Oferta Finde Loco",
            "2x1 en entradas para conciertos seleccionados.",
            "2x1",
            0.0, // Descuento cero, la lógica es 2x1
            LocalDate.of(2025, 6, 7),
            LocalDate.of(2025, 6, 9),
            "FINDE2X1",
            50,
            "Eventos",
            false // Inactiva por fecha
        );

        Promocion promo3 = new Promocion(
            UUID.randomUUID(),
            "Cliente Nuevo",
            "10 USD de descuento en la primera compra.",
            "Fijo",
            10.0,
            LocalDate.now().minusDays(30),
            LocalDate.now().plusYears(1),
            null, // Sin código
            null, // Sin límite de usos
            "Todos",
            true
        );

        Promocion promo4 = new Promocion(
            UUID.randomUUID(),
            "Cyber Lunes",
            "30% de descuento en electrónica.",
            "Porcentual",
            0.30,
            LocalDate.of(2025, 11, 25),
            LocalDate.of(2025, 11, 26),
            "CYBERMONDAY30",
            500,
            "Productos",
            true
        );

        // Uso de Guava para crear la lista de promociones.
        List<Promocion> promociones = Lists.newArrayList(promo1, promo2, promo3, promo4);
        logger.info("Guava: Se creó una lista de {} promociones.", promociones.size());
        
        logger.info("\n--- Promociones en orden de creación ---");
        promociones.forEach(p -> logger.info("  - Nombre: '{}', Activa: {}", p.getNombre(), p.isActivo()));


        // --- DEMOSTRACIÓN: COMPARABLE (Orden Natural) ---
        logger.info("\n--- Demostrando Comparable (Orden Natural: Fecha Inicio ascendente, luego Nombre) ---");
        // Collections.sort() usa el método compareTo() de Promocion.
        Collections.sort(promociones);
        
        promociones.forEach(p -> logger.info("  - Nombre: '{}', Inicio: {}", p.getNombre(), p.getFechaInicio()));


        // --- DEMOSTRACIÓN: COMPARATOR (Orden Personalizado) ---
        logger.info("\n--- Demostrando Comparator (Orden Personalizado: Valor Descuento descendente) ---");
        // Ordena por el valor de descuento, del más alto al más bajo.
        Comparator<Promocion> porValorDescuentoDescendente = (pA, pB) -> {
            Double valorA = pA.getValorDescuento() != null ? pA.getValorDescuento() : 0.0;
            Double valorB = pB.getValorDescuento() != null ? pB.getValorDescuento() : 0.0;
            return Double.compare(valorB, valorA); // Descendente
        };
        Collections.sort(promociones, porValorDescuentoDescendente);
        promociones.forEach(p -> logger.info("  - Nombre: '{}', Descuento: ${:.2f}", 
                                           p.getNombre(), p.getValorDescuento() != null ? p.getValorDescuento() : 0.0));
        
        logger.info("\n--- Demostrando Otro Comparator (Orden Personalizado: Fecha Fin ascendente) ---");
        // Ordena por fecha de fin, colocando las promociones sin fecha de fin al principio.
        Comparator<Promocion> porFechaFinAscendente = (pA, pB) -> {
            return Comparator.nullsFirst(Comparator.comparing(Promocion::getFechaFin))
                             .compare(pA, pB);
        };
        Collections.sort(promociones, porFechaFinAscendente);
        promociones.forEach(p -> logger.info("  - Nombre: '{}', Fin: {}", p.getNombre(), p.getFechaFin()));


        // --- DEMOSTRACIÓN: APACHE COMMONS LANG3 (Utilidades de String) ---
        logger.info("\n--- Probando Apache Commons Lang3 ---");
        // Convierte el nombre a mayúsculas.
        String nombrePromoMayusculas = StringUtils.upperCase(promo1.getNombre());
        logger.info("Commons Lang3: Nombre en mayúsculas: {}", nombrePromoMayusculas);

        // Trunca la descripción si es muy larga.
        String descripcionTruncada = StringUtils.abbreviate(promo1.getDescripcion(), 25);
        logger.info("Commons Lang3: Descripción truncada: '{}'", descripcionTruncada);

        // Verifica si un código promocional está vacío o nulo.
        if (StringUtils.isBlank(promo3.getCodigoPromocional())) {
            logger.info("Commons Lang3: El código de '{}' está vacío/nulo, como se esperaba.", promo3.getNombre());
        }

        // --- DEMOSTRACIÓN: GUARDAR PROMOCIONES EN EXCEL ---
        logger.info("\n--- Guardando promociones a un archivo Excel ---");
        ExcelService excelService = new ExcelService();
        String outputFilePath = "data/promociones_generadas_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                                ".xlsx";
        try {
            // Crea el directorio 'data' si no existe.
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                logger.info("Directorio 'data' creado en: {}", dataDir.getAbsolutePath());
            }
            // Escribe la lista de promociones en el archivo Excel.
            excelService.writePromocionesToExcel(promociones, outputFilePath);
            logger.info("✅ Promociones guardadas exitosamente en: {}", outputFilePath);
        } catch (IOException e) {
            logger.error("❌ Error al guardar promociones en Excel: {}", e.getMessage(), e);
        }

        logger.info("\n--- TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE! ---");
    }
}
