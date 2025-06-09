package semana12;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId; // Para conversión de fechas
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date; // Para conversión de fechas
import java.util.UUID; // Para manejar IDs de Promocion

/**
 * Servicio para leer y escribir datos de Promociones desde/hacia archivos Excel.
 */
public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    /**
     * Reads a list of promotions from the first sheet of an Excel file.
     * Expects a specific column format for each Promotion attribute.
     *
     * @param filePath The path to the Excel file.
     * @return A list of Promotion objects.
     * @throws IOException If an error occurs while reading the file.
     */
    public List<Promocion> readPromocionesFromExcel(String filePath) throws IOException {
        List<Promocion> promociones = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Works with the first sheet.
            Iterator<Row> rowIterator = sheet.iterator();

            // Skips the header row.
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            // Iterates over each row to read promotions.
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // Ignores completely empty rows (based on the first cell).
                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                try {
                    Promocion promocion = new Promocion();

                    // Reads the ID (expects String for UUID, handles Numeric with warning).
                    Cell idCell = row.getCell(0);
                    if (idCell != null) {
                        if (idCell.getCellType() == CellType.STRING) {
                            promocion.setIdPromocion(UUID.fromString(idCell.getStringCellValue()));
                        } else if (idCell.getCellType() == CellType.NUMERIC) {
                            logger.warn("Numeric Promotion ID in Excel (Row {}). Expected String for UUID.", row.getRowNum());
                        }
                    } else {
                        // Generates a new UUID if the ID is not found in the cell.
                        promocion.setIdPromocion(UUID.randomUUID());
                    }

                    // Reading other promotion attributes by column index.
                    promocion.setNombre(getStringCellValue(row.getCell(1)));
                    promocion.setDescripcion(getStringCellValue(row.getCell(2)));
                    promocion.setTipoPromocion(getStringCellValue(row.getCell(3)));
                    
                    Cell valorDescuentoCell = row.getCell(4);
                    if (valorDescuentoCell != null && valorDescuentoCell.getCellType() == CellType.NUMERIC) {
                        promocion.setValorDescuento(valorDescuentoCell.getNumericCellValue());
                    }

                    Cell fechaInicioCell = row.getCell(5);
                    if (fechaInicioCell != null && DateUtil.isCellDateFormatted(fechaInicioCell)) {
                        promocion.setFechaInicio(convertToLocalDate(fechaInicioCell.getDateCellValue()));
                    }
                    
                    Cell fechaFinCell = row.getCell(6);
                    if (fechaFinCell != null && DateUtil.isCellDateFormatted(fechaFinCell)) {
                        promocion.setFechaFin(convertToLocalDate(fechaFinCell.getDateCellValue()));
                    }

                    promocion.setCodigoPromocional(getStringCellValue(row.getCell(7)));
                    
                    Cell cantidadUsosCell = row.getCell(8);
                    if (cantidadUsosCell != null && cantidadUsosCell.getCellType() == CellType.NUMERIC) {
                        promocion.setCantidadMaximaUsos((int) cantidadUsosCell.getNumericCellValue());
                        promocion.setUsosRestantes((int) cantidadUsosCell.getNumericCellValue()); // Initializes remaining uses.
                    }
                    
                    promocion.setAplicableA(getStringCellValue(row.getCell(9)));
                    
                    // Reads the 'activo' status (boolean, string, or numeric).
                    Cell activoCell = row.getCell(10);
                    if (activoCell != null) {
                        if (activoCell.getCellType() == CellType.BOOLEAN) {
                            promocion.setActivo(activoCell.getBooleanCellValue());
                        } else if (activoCell.getCellType() == CellType.STRING) {
                            promocion.setActivo(Boolean.parseBoolean(activoCell.getStringCellValue()));
                        } else if (activoCell.getCellType() == CellType.NUMERIC) {
                            promocion.setActivo(activoCell.getNumericCellValue() == 1); // 1 = true, 0 = false.
                        }
                    }

                    promociones.add(promocion);
                } catch (Exception e) {
                    logger.error("Error reading promotion row (Row {}): {}", row.getRowNum(), e.getMessage(), e);
                }
            }
        }
        logger.info("{} promotions read from Excel file: {}", promociones.size(), filePath);
        return promociones;
    }

    /**
     * Writes a list of promotions to a new Excel file.
     * Creates a sheet named "Promociones" with defined headers and auto-fits column widths.
     *
     * @param promociones The list of Promotion objects to write.
     * @param filePath    The path to the output Excel file.
     * @throws IOException If an error occurs while writing the file.
     */
    public void writePromocionesToExcel(List<Promocion> promociones, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Promociones");

            // Defines and creates the header row.
            Row headerRow = sheet.createRow(0);
            String[] headers = {"idPromocion", "nombre", "descripcion", "tipoPromocion", "valorDescuento",
                                 "fechaInicio", "fechaFin", "codigoPromocional", "cantidadMaximaUsos",
                                 "aplicableA", "activo"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Defines date cell style to display dates correctly.
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            // Writes data for each promotion in consecutive rows.
            int rowNum = 1;
            for (Promocion promocion : promociones) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(promocion.getIdPromocion() != null ? promocion.getIdPromocion().toString() : "");
                row.createCell(1).setCellValue(promocion.getNombre());
                row.createCell(2).setCellValue(promocion.getDescripcion());
                row.createCell(3).setCellValue(promocion.getTipoPromocion());
                row.createCell(4).setCellValue(promocion.getValorDescuento() != null ? promocion.getValorDescuento() : 0.0);

                Cell fechaInicioCell = row.createCell(5);
                if (promocion.getFechaInicio() != null) {
                    fechaInicioCell.setCellValue(promocion.getFechaInicio());
                    fechaInicioCell.setCellStyle(dateCellStyle); // Applies date format.
                }

                Cell fechaFinCell = row.createCell(6);
                if (promocion.getFechaFin() != null) {
                    fechaFinCell.setCellValue(promocion.getFechaFin());
                    fechaFinCell.setCellStyle(dateCellStyle); // Applies date format.
                }

                row.createCell(7).setCellValue(promocion.getCodigoPromocional());
                row.createCell(8).setCellValue(promocion.getCantidadMaximaUsos() != null ? promocion.getCantidadMaximaUsos() : 0);
                row.createCell(9).setCellValue(promocion.getAplicableA());
                row.createCell(10).setCellValue(promocion.isActivo());
            }

            // Auto-fits all columns to their content for better readability.
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos); // Saves the workbook to the file.
            logger.info("{} promotions written to Excel file: {}", promociones.size(), filePath);

        } catch (Exception e) {
            logger.error("Error writing promotions to Excel: {}", e.getMessage(), e);
            throw e; // Rethrows the exception for the caller to handle.
        }
    }

    /**
     * Gets the cell value as a String, handling different cell types.
     *
     * @param cell The Excel cell.
     * @return The cell value as a String; empty string if the cell is null or the type is not handled.
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                // If numeric and date formatted, returns the date as a String.
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula(); // Returns the formula if it's a formula cell.
            default: return ""; // For unexpected cell types.
        }
    }

    /**
     * Converts a java.util.Date object to java.time.LocalDate.
     * Uses the system's default time zone.
     *
     * @param dateToConvert The Date object to convert.
     * @return The resulting LocalDate object.
     */
    private LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
