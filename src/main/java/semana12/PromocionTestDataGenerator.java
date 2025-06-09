package semana12;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de datos de prueba para objetos Promocion.
 * Proporciona una forma sencilla de crear promociones ficticias para testing.
 */
public class PromocionTestDataGenerator {

    private final Random random = new Random();

    // Arrays con datos predefinidos para generar promociones realistas.
    private final String[] nombres = {"Descuento Verano", "Oferta Flash", "2x1 Entradas", "Envío Gratis", "Happy Hour", "Especial Temporada"};
    private final String[] descripciones = {
        "Descuento aplicable a todos los servicios.",
        "Oferta por tiempo limitado en productos seleccionados.",
        "Compra una entrada y obtén otra gratis.",
        "Elimina el costo de envío en compras superiores a X.",
        "Precios especiales en bebidas de 5 PM a 7 PM.",
        "Descuento en eventos culturales."
    };
    private final String[] tipos = {"Porcentual", "Fijo", "2x1", "EnvioGratis"};
    private final String[] aplicablesA = {"Todos", "Eventos", "Servicios", "Productos"};

    /**
     * Genera una lista de promociones de prueba.
     *
     * @param count El número de promociones a generar.
     * @return Una lista de objetos Promocion con datos ficticios.
     */
    public List<Promocion> generateTestPromociones(int count) {
        List<Promocion> promociones = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Genera nombre y descripción combinando datos predefinidos.
            String nombre = nombres[random.nextInt(nombres.length)] + " " + (i + 1);
            String descripcion = descripciones[random.nextInt(descripciones.length)];
            String tipo = tipos[random.nextInt(tipos.length)];
            Double valorDescuento;

            // Define el valor de descuento según el tipo de promoción.
            if (tipo.equals("Porcentual")) {
                valorDescuento = (double) (random.nextInt(25) + 5) / 100.0; // 5% a 30%.
            } else if (tipo.equals("Fijo")) {
                valorDescuento = (double) (random.nextInt(50) + 5); // $5 a $55.
            } else {
                valorDescuento = 0.0; // Para 2x1 o envío gratis.
            }

            // Genera fechas de inicio y fin dentro de un rango razonable.
            LocalDate fechaInicio = LocalDate.now().plusDays(random.nextInt(30) - 15);
            LocalDate fechaFin = fechaInicio.plusDays(random.nextInt(60) + 30);

            // Determina aleatoriamente si tendrá código promocional.
            String codigoPromocional = random.nextBoolean() ? "PROMO" + (1000 + random.nextInt(9000)) : null;

            // Determina aleatoriamente si tendrá un límite de usos.
            Integer cantidadMaximaUsos = random.nextBoolean() ? random.nextInt(200) + 10 : null;

            // Asigna a qué es aplicable la promoción.
            String aplicableA = aplicablesA[random.nextInt(aplicablesA.length)];
            // Determina si la promoción estará activa.
            boolean activo = random.nextBoolean();

            // Crea la instancia de Promocion.
            Promocion promocion = new Promocion(nombre, descripcion, tipo, valorDescuento,
                                                 fechaInicio, fechaFin, codigoPromocional,
                                                 cantidadMaximaUsos, aplicableA);
            promocion.setActivo(activo); // Establece el estado activo.

            // Ajusta usos restantes si aplica y la promoción es válida.
            if (promocion.getCantidadMaximaUsos() != null) {
                if (promocion.isActivo() && promocion.esValida()) {
                    promocion.setUsosRestantes(random.nextInt(promocion.getCantidadMaximaUsos()) + 1); // Al menos 1 uso.
                } else {
                    promocion.setUsosRestantes(0); // Cero usos si inactiva o inválida.
                }
            }

            promociones.add(promocion);
        }
        return promociones;
    }
}