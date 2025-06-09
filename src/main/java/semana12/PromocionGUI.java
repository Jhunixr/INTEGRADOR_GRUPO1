package semana12;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interfaz gráfica de usuario (GUI) para la gestión de Promociones.
 * Permite visualizar promociones disponibles, todas las promociones registradas,
 * y subir un archivo Excel para su procesamiento.
 */
public class PromocionGUI extends JFrame {

    private JTextArea displayArea;
    private PromocionProcessorApp processorApp; // Instancia de la aplicación de procesamiento

    public PromocionGUI() {
        super("Gestor de Promociones"); // Título de la ventana
        this.processorApp = new PromocionProcessorApp(); // Inicializa la lógica de procesamiento

        // Configuración básica de la ventana
        setSize(1000, 700); // Aumentar tamaño para una mejor visualización
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar la ventana en la pantalla
        setLayout(new BorderLayout(15, 15)); // Usar BorderLayout con mayor espaciado
        getContentPane().setBackground(new Color(230, 240, 250)); // Color de fondo suave para la ventana

        // Panel principal para contenido
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Más margen general
        mainPanel.setBackground(new Color(230, 240, 250));

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 20, 10)); // 1 fila, 3 columnas con más espaciado
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10), // Padding interno
            new EtchedBorder(EtchedBorder.LOWERED, new Color(180, 200, 220), new Color(250, 250, 250)) // Borde grabado
        ));
        buttonPanel.setBackground(new Color(210, 225, 240)); // Color de fondo para el panel de botones

        // Estilo común para los botones
        Font buttonFont = new Font("SansSerif", Font.BOLD, 16);
        Dimension buttonSize = new Dimension(220, 60); // Tamaño preferido para los botones

        // Botón 1: Mostrar Promociones Disponibles
        JButton showAvailableButton = new JButton("<html><center>Mostrar Promociones<br>Disponibles</center></html>");
        showAvailableButton.setFont(buttonFont);
        showAvailableButton.setPreferredSize(buttonSize);
        showAvailableButton.setBackground(new Color(70, 130, 180)); // Azul acero
        showAvailableButton.setForeground(Color.WHITE);
        showAvailableButton.setFocusPainted(false);
        showAvailableButton.setBorder(new LineBorder(new Color(50, 100, 150), 2, true)); // Borde redondeado
        showAvailableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAvailablePromotions();
            }
        });
        buttonPanel.add(showAvailableButton);

        // Botón 2: Mostrar Todas las Promociones Registradas
        JButton showAllButton = new JButton("<html><center>Mostrar Todas las<br>Promociones</center></html>");
        showAllButton.setFont(buttonFont);
        showAllButton.setPreferredSize(buttonSize);
        showAllButton.setBackground(new Color(60, 179, 113)); // Verde medio
        showAllButton.setForeground(Color.WHITE);
        showAllButton.setFocusPainted(false);
        showAllButton.setBorder(new LineBorder(new Color(40, 140, 90), 2, true)); // Borde redondeado
        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAllPromotions();
            }
        });
        buttonPanel.add(showAllButton);

        // Botón 3: Subir y Procesar Excel
        JButton uploadExcelButton = new JButton("<html><center>Subir y Procesar<br>Archivo Excel</center></html>");
        uploadExcelButton.setFont(buttonFont);
        uploadExcelButton.setPreferredSize(buttonSize);
        uploadExcelButton.setBackground(new Color(255, 140, 0)); // Naranja oscuro
        uploadExcelButton.setForeground(Color.WHITE);
        uploadExcelButton.setFocusPainted(false);
        uploadExcelButton.setBorder(new LineBorder(new Color(200, 100, 0), 2, true)); // Borde redondeado
        uploadExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadAndProcessExcel();
            }
        });
        buttonPanel.add(uploadExcelButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH); // Añadir el panel de botones en la parte superior

        // Área de texto para mostrar resultados
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Fuente ligeramente más grande
        displayArea.setBackground(new Color(255, 255, 255)); // Fondo blanco para el texto
        displayArea.setForeground(new Color(50, 50, 50)); // Color de texto oscuro
        displayArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(150, 150, 150), 1), // Borde gris claro
            new EmptyBorder(10, 10, 10, 10) // Padding interno
        ));
        JScrollPane scrollPane = new JScrollPane(displayArea); // Hacer el área de texto scrollable
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Mostrar siempre la barra vertical
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Mostrar horizontal si es necesario
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Añadir el área de texto en el centro

        add(mainPanel, BorderLayout.CENTER); // Añadir el panel principal al centro de la ventana

        // Hacer la ventana visible
        setVisible(true);
    }

    /**
     * Muestra las promociones que están activas y son válidas a la fecha actual.
     * Lee desde el archivo de entrada y filtra los resultados.
     */
    private void displayAvailablePromotions() {
        displayArea.setText("Cargando y filtrando promociones disponibles...\n");
        try {
            // Asegurar que el archivo de entrada exista o se genere
            String inputFilePath = "data/promociones_input.xlsx";
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                String tempOutput = "data/temp_output_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                processorApp.processPromocionFile(inputFilePath, tempOutput);
            }
            
            // Leer todas las promociones
            ExcelService excelService = new ExcelService();
            List<Promocion> allPromotions = excelService.readPromocionesFromExcel(inputFilePath);

            // Filtrar las promociones activas y válidas
            List<Promocion> availablePromotions = allPromotions.stream()
                .filter(Promocion::esValida) // Utiliza el método esValida de la clase Promocion
                .filter(Promocion::isActivo) // Asegura que solo las activas se muestren
                .collect(Collectors.toList());

            if (availablePromotions.isEmpty()) {
                displayArea.setText("No se encontraron promociones disponibles actualmente.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("════════════════════════════════════════════════════════════════\n");
                sb.append("                         PROMOCIONES DISPONIBLES                      \n");
                sb.append("════════════════════════════════════════════════════════════════\n\n");
                for (Promocion p : availablePromotions) {
                    sb.append("╔══════════════════════════════════════════════════════════════╗\n");
                    sb.append(String.format("║ ID: %-58s ║\n", p.getIdPromocion()));
                    sb.append(String.format("║ Nombre: %-54s ║\n", p.getNombre()));
                    sb.append(String.format("║ Descripción: %-49s ║\n", p.getDescripcion()));
                    sb.append(String.format("║ Tipo: %-56s ║\n", p.getTipoPromocion()));
                    sb.append(String.format("║ Valor Descuento: $%-43.2f ║\n", p.getValorDescuento()));
                    sb.append(String.format("║ Fechas: %s al %-38s ║\n", p.getFechaInicio(), p.getFechaFin()));
                    sb.append(String.format("║ Código: %-54s ║\n", p.getCodigoPromocional() != null ? p.getCodigoPromocional() : "N/A"));
                    sb.append(String.format("║ Usos Restantes: %-44s ║\n", p.getUsosRestantes() != null ? p.getUsosRestantes() : "Ilimitado"));
                    sb.append(String.format("║ Aplicable a: %-49s ║\n", p.getAplicableA()));
                    sb.append(String.format("║ Activa: %-54s ║\n", p.isActivo() ? "Sí" : "No"));
                    sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");
                }
                displayArea.setText(sb.toString());
            }
        } catch (IOException ex) {
            displayArea.setText("Error al leer el archivo de promociones: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            displayArea.setText("Ocurrió un error inesperado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Muestra todas las promociones leídas del archivo de entrada, sin filtrar por disponibilidad.
     */
    private void displayAllPromotions() {
        displayArea.setText("Cargando todas las promociones registradas...\n");
        try {
            // Asegurar que el archivo de entrada exista o se genere
            String inputFilePath = "data/promociones_input.xlsx";
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                String tempOutput = "data/temp_output_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                processorApp.processPromocionFile(inputFilePath, tempOutput);
            }

            // Leer todas las promociones
            ExcelService excelService = new ExcelService();
            List<Promocion> allPromotions = excelService.readPromocionesFromExcel(inputFilePath);

            if (allPromotions.isEmpty()) {
                displayArea.setText("No se encontraron promociones en el archivo de entrada.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("════════════════════════════════════════════════════════════════\n");
                sb.append("                           TODAS LAS PROMOCIONES                      \n");
                sb.append("════════════════════════════════════════════════════════════════\n\n");
                for (Promocion p : allPromotions) {
                    sb.append("╔══════════════════════════════════════════════════════════════╗\n");
                    sb.append(String.format("║ ID: %-58s ║\n", p.getIdPromocion()));
                    sb.append(String.format("║ Nombre: %-54s ║\n", p.getNombre()));
                    sb.append(String.format("║ Descripción: %-49s ║\n", p.getDescripcion()));
                    sb.append(String.format("║ Tipo: %-56s ║\n", p.getTipoPromocion()));
                    sb.append(String.format("║ Valor Descuento: $%-43.2f ║\n", p.getValorDescuento()));
                    sb.append(String.format("║ Fechas: %s al %-38s ║\n", p.getFechaInicio(), p.getFechaFin()));
                    sb.append(String.format("║ Código: %-54s ║\n", p.getCodigoPromocional() != null ? p.getCodigoPromocional() : "N/A"));
                    sb.append(String.format("║ Usos Restantes: %-44s ║\n", p.getUsosRestantes() != null ? p.getUsosRestantes() : "Ilimitado"));
                    sb.append(String.format("║ Aplicable a: %-49s ║\n", p.getAplicableA()));
                    sb.append(String.format("║ Activa: %-54s ║\n", p.isActivo() ? "Sí" : "No"));
                    sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");
                }
                displayArea.setText(sb.toString());
            }
        } catch (IOException ex) {
            displayArea.setText("Error al leer el archivo de promociones: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            displayArea.setText("Ocurrió un error inesperado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Permite al usuario seleccionar un archivo Excel y luego lo procesa.
     */
    private void uploadAndProcessExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo Excel de promociones");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            displayArea.setText("Procesando archivo: " + selectedFile.getAbsolutePath() + "...\n");

            // Generar un nombre de archivo de salida único
            String outputFileName = "promociones_processed_" +
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                                    ".xlsx";
            String outputFilePath = "data/" + outputFileName; // Guardar en el directorio 'data'

            // Asegurar que el directorio 'data' exista
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try {
                // Llamar al método de procesamiento de la aplicación principal
                processorApp.processPromocionFile(selectedFile.getAbsolutePath(), outputFilePath);
                displayArea.append("\nArchivo procesado exitosamente.\n");
                displayArea.append("Resultados guardados en: " + outputFilePath + "\n");
                // Opcional: mostrar las promociones procesadas aquí.
                // Para simplificar, solo mostramos el mensaje de éxito.
            } catch (Exception ex) {
                displayArea.append("\nError durante el procesamiento del archivo: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        } else {
            displayArea.setText("Selección de archivo cancelada.");
        }
    }

    /**
     * Método principal para iniciar la aplicación GUI.
     */
    public static void main(String[] args) {
        // Ejecutar la GUI en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PromocionGUI();
            }
        });
    }
}
