package com.utpintegrador.semana10;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventoReporteGUI extends JFrame {

    // --- Componentes de la UI ---
    private final JTextArea reportTextArea;
    private final JButton generateReportButton;
    private final JButton exportExcelButton;
    private final JButton generateTestDataButton;
    private final JLabel statusLabel;

    // --- Servicios de negocio ---
    private final EventoExcelService excelService;
    private final EventoValidator validator;
    private final StatisticsService statisticsService;
    private final TestDataGenerator testDataGenerator;

    // Almacena los eventos válidos para poder exportarlos después
    private List<Evento> eventosValidos;

    public EventoReporteGUI() {
        // --- Inicialización de servicios ---
        this.excelService = new EventoExcelService();
        this.validator = new EventoValidator();
        this.statisticsService = new StatisticsService();
        this.testDataGenerator = new TestDataGenerator();

        // --- Configuración de la ventana principal ---
        setTitle("Generador de Reportes de Eventos");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Panel de botones (Norte) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        generateTestDataButton = new JButton("1. Generar Datos de Prueba");
        generateReportButton = new JButton("2. Cargar y Generar Reporte");
        exportExcelButton = new JButton("3. Exportar Reporte a Excel");
        exportExcelButton.setEnabled(false);
        topPanel.add(generateTestDataButton);
        topPanel.add(generateReportButton);
        topPanel.add(exportExcelButton);

        // --- Área de texto para el reporte (Centro) ---
        reportTextArea = new JTextArea("Bienvenido. Haga clic en 'Cargar y Generar Reporte' para comenzar.");
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reportTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(reportTextArea);

        // --- Barra de estado (Sur) ---
        statusLabel = new JLabel("Listo.");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // --- Añadir componentes a la ventana ---
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // --- Configurar acciones de los botones ---
        setupActionListeners();
    }

    private void setupActionListeners() {
        generateTestDataButton.addActionListener(e -> generateTestData());
        generateReportButton.addActionListener(e -> generateReport());
        exportExcelButton.addActionListener(e -> exportToExcel());
    }

    private void generateTestData() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Esto creará 'data/eventos_input.xlsx' con 50 eventos de prueba.\n¿Desea continuar?",
            "Confirmar Generación de Datos",
            JOptionPane.YES_NO_OPTION
        );
        if (response != JOptionPane.YES_OPTION) {
            statusLabel.setText("Generación de datos cancelada.");
            return;
        }
        statusLabel.setText("Generando archivo de prueba...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                File dataDir = new File("data");
                if (!dataDir.exists()) dataDir.mkdirs();
                testDataGenerator.generarArchivoDemo("data/eventos_input.xlsx", 50);
                return "Archivo 'data/eventos_input.xlsx' generado con éxito.";
            }

            @Override
            protected void done() {
                try {
                    statusLabel.setText(get());
                } catch (Exception e) {
                    handleError("Error al generar datos: ", e);
                }
            }
        }.execute();
    }

    private void generateReport() {
        statusLabel.setText("Cargando y procesando archivo Excel...");
        reportTextArea.setText("Procesando, por favor espere...");
        exportExcelButton.setEnabled(false);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Evento> eventos = excelService.leerEventosDesdeExcel("data/eventos_input.xlsx");
                if (eventos.isEmpty()) {
                    return "No se encontraron eventos en 'data/eventos_input.xlsx'.\nPruebe generando datos de prueba primero.";
                }
                eventosValidos = eventos.stream()
                        .peek(validator::normalizar)
                        .filter(evento -> validator.validar(evento).isEmpty())
                        .collect(Collectors.toList());
                return buildReportString(eventos.size(), eventosValidos);
            }

            @Override
            protected void done() {
                try {
                    String report = get();
                    reportTextArea.setText(report);
                    reportTextArea.setCaretPosition(0);
                    if (eventosValidos != null && !eventosValidos.isEmpty()) {
                        exportExcelButton.setEnabled(true);
                        statusLabel.setText("Reporte generado. " + eventosValidos.size() + " eventos válidos.");
                    } else {
                        statusLabel.setText("Proceso completado, pero no hay eventos válidos.");
                    }
                } catch (Exception e) {
                    handleError("Error al procesar el archivo: ", e);
                }
            }
        }.execute();
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte Excel");
        String defaultFileName = "eventos_procesados_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        fileChooser.setSelectedFile(new File(defaultFileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            statusLabel.setText("Exportando a Excel: " + fileToSave.getName());

            new SwingWorker<Void, Void>() {
                @Override
protected Void doInBackground() throws IOException {
                    excelService.escribirEventosAExcel(eventosValidos, fileToSave.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        statusLabel.setText("Archivo exportado con éxito a: " + fileToSave.getAbsolutePath());
                        JOptionPane.showMessageDialog(EventoReporteGUI.this, "Reporte exportado exitosamente.", "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        handleError("Error al exportar: ", e);
                    }
                }
            }.execute();
        }
    }

    private String buildReportString(int totalLeidos, List<Evento> eventos) {
        StringBuilder sb = new StringBuilder();
        sb.append("===================================================\n");
        sb.append("       REPORTE DE PROCESAMIENTO DE EVENTOS\n");
        sb.append("===================================================\n\n");
        sb.append(String.format("Total de registros leídos: %d\n", totalLeidos));
        sb.append(String.format("Total de eventos válidos: %d\n", eventos.size()));
        sb.append(String.format("Total de registros descartados: %d\n\n", totalLeidos - eventos.size()));

        Map<String, Long> conteoPorUbicacion = statisticsService.agruparPorUbicacion(eventos).asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
        sb.append("--- EVENTOS POR UBICACIÓN ---\n");
        conteoPorUbicacion.forEach((k, v) -> sb.append(String.format("  %-20s: %d\n", k, v)));
        sb.append("\n");

        Map<String, Long> conteoPorEstado = statisticsService.contarPorEstado(eventos);
        sb.append("--- EVENTOS POR ESTADO ---\n");
        conteoPorEstado.forEach((k, v) -> sb.append(String.format("  %-20s: %d\n", k, v)));
        sb.append("\n");

        Map<String, Double> precioPromedio = statisticsService.precioPromedioPorUbicacion(eventos);
        sb.append("--- PRECIO PROMEDIO POR UBICACIÓN ---\n");
        precioPromedio.forEach((k, v) -> sb.append(String.format("  %-20s: S/ %.2f\n", k, v)));
        sb.append("\n");

        StatisticsService.ResumenEventos resumen = statisticsService.generarResumen(eventos);
        sb.append("--- RESUMEN GENERAL ---\n");
        sb.append(String.format("  Total de Eventos Válidos: %d\n", resumen.getTotalEventos()));
        sb.append(String.format("  Precio Promedio General : S/ %.2f\n", resumen.getPrecioPromedio()));
        sb.append(String.format("  Capacidad Total         : %d personas\n", resumen.getCapacidadTotal()));
        sb.append("  Ubicaciones únicas      : ").append(String.join(", ", resumen.getUbicaciones())).append("\n");

        return sb.toString();
    }

    private void handleError(String message, Exception e) {
        String errorMessage = message + e.getCause().getMessage();
        statusLabel.setText(errorMessage);
        reportTextArea.setText(errorMessage + "\n\nAsegúrese de que el archivo 'data/eventos_input.xlsx' exista y no esté abierto.");
        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventoReporteGUI().setVisible(true));
    }
}