package semana12;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.time.ZoneId;

public class ExcelService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);
    
    // Índices de columnas en el archivo Excel
    private static final int COL_ID = 0;
    private static final int COL_FIRST_NAME = 1;
    private static final int COL_LAST_NAME = 2;
    private static final int COL_EMAIL = 3;
    private static final int COL_DEPARTMENT = 4;
    private static final int COL_SALARY = 5;
    private static final int COL_BIRTH_DATE = 6;
    private static final int COL_HIRE_DATE = 7;
    
    /**
     * Lee empleados desde un archivo Excel
     */
    public List<Employee> readEmployeesFromExcel(String filePath) throws IOException {
        logger.info("Iniciando lectura de archivo Excel: {}", filePath);
        
        List<Employee> employees = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            logger.debug("Leyendo hoja: {} con {} filas", sheet.getSheetName(), sheet.getLastRowNum());
            
            // Saltar la fila de encabezados (fila 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Employee employee = extractEmployeeFromRow(row);
                    if (employee != null) {
                        employees.add(employee);
                        logger.debug("Empleado leído: {}", employee.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error al procesar fila {}: {}", i, e.getMessage());
                }
            }
            
            logger.info("Se leyeron {} empleados del archivo {}", employees.size(), filePath);
        }
        
        return employees;
    }
    
    /**
     * Extrae un empleado de una fila del Excel
     */
    private Employee extractEmployeeFromRow(Row row) {
        try {
            Employee employee = new Employee();
            
            // ID
            Cell idCell = row.getCell(COL_ID);
            if (idCell != null && idCell.getCellType() == CellType.NUMERIC) {
                employee.setId((long) idCell.getNumericCellValue());
            }
            
            // Nombre
            Cell firstNameCell = row.getCell(COL_FIRST_NAME);
            if (firstNameCell != null) {
                employee.setFirstName(getCellValueAsString(firstNameCell));
            }
            
            // Apellido
            Cell lastNameCell = row.getCell(COL_LAST_NAME);
            if (lastNameCell != null) {
                employee.setLastName(getCellValueAsString(lastNameCell));
            }
            
            // Email
            Cell emailCell = row.getCell(COL_EMAIL);
            if (emailCell != null) {
                employee.setEmail(getCellValueAsString(emailCell));
            }
            
            // Departamento
            Cell deptCell = row.getCell(COL_DEPARTMENT);
            if (deptCell != null) {
                employee.setDepartment(getCellValueAsString(deptCell));
            }
            
            // Salario
            Cell salaryCell = row.getCell(COL_SALARY);
            if (salaryCell != null && salaryCell.getCellType() == CellType.NUMERIC) {
                employee.setSalary(salaryCell.getNumericCellValue());
            }
            
            // Fecha de nacimiento
            Cell birthDateCell = row.getCell(COL_BIRTH_DATE);
            if (birthDateCell != null && DateUtil.isCellDateFormatted(birthDateCell)) {
                Date birthDate = birthDateCell.getDateCellValue();
                employee.setBirthDate(birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            // Fecha de contratación
            Cell hireDateCell = row.getCell(COL_HIRE_DATE);
            if (hireDateCell != null && DateUtil.isCellDateFormatted(hireDateCell)) {
                Date hireDate = hireDateCell.getDateCellValue();
                employee.setHireDate(hireDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            return employee;
            
        } catch (Exception e) {
            logger.error("Error al extraer empleado de fila: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene el valor de una celda como String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * Escribe empleados a un archivo Excel con formato profesional
     */
    public void writeEmployeesToExcel(List<Employee> employees, String filePath) throws IOException {
        logger.info("Iniciando escritura de {} empleados a archivo: {}", employees.size(), filePath);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear hoja de empleados
            Sheet employeeSheet = workbook.createSheet("Empleados");
            createEmployeeSheet(workbook, employeeSheet, employees);
            
            // Escribir archivo
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                logger.info("Archivo Excel creado exitosamente: {}", filePath);
            }
        }
    }
    

    /**
     * Crea estilo para encabezados
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * Crea estilo para datos generales
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    /**
     * Crea estilo para moneda
     */
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    /**
     * Crea estilo para fechas
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    
    private void createEmployeeSheet(Workbook workbook, Sheet sheet, List<Employee> employees) {

        // Crear estilos (mantener código original)
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // Crear encabezados (mantener código original)
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nombre", "Apellido", "Email", "Departamento", 
                           "Salario", "Fecha Nacimiento", "Fecha Contratación", "Edad", "Años Servicio"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos - VERSIÓN CORREGIDA CON VALIDACIONES NULL
        int rowNum = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowNum++);

            // ID - con validación null
            Cell idCell = row.createCell(0);
            idCell.setCellValue(emp.getId() != null ? emp.getId() : 0);
            idCell.setCellStyle(dataStyle);

            // Nombre - con validación null
            Cell firstNameCell = row.createCell(1);
            firstNameCell.setCellValue(emp.getFirstName() != null ? emp.getFirstName() : "");
            firstNameCell.setCellStyle(dataStyle);

            // Apellido - con validación null
            Cell lastNameCell = row.createCell(2);
            lastNameCell.setCellValue(emp.getLastName() != null ? emp.getLastName() : "");
            lastNameCell.setCellStyle(dataStyle);

            // Email - con validación null
            Cell emailCell = row.createCell(3);
            emailCell.setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
            emailCell.setCellStyle(dataStyle);

            // Departamento - con validación null
            Cell deptCell = row.createCell(4);
            deptCell.setCellValue(emp.getDepartment() != null ? emp.getDepartment() : "");
            deptCell.setCellStyle(dataStyle);

            // Salario - CORREGIDO para evitar NullPointerException
            Cell salaryCell = row.createCell(5);
            if (emp.getSalary() != null) {
                salaryCell.setCellValue(emp.getSalary());
            } else {
                salaryCell.setCellValue(0.0);
            }
            salaryCell.setCellStyle(currencyStyle);

            // Fecha nacimiento - con validación null
            Cell birthDateCell = row.createCell(6);
            if (emp.getBirthDate() != null) {
                birthDateCell.setCellValue(Date.from(emp.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                birthDateCell.setCellStyle(dateStyle);
            } else {
                birthDateCell.setCellValue("");
                birthDateCell.setCellStyle(dataStyle);
            }

            // Fecha contratación - con validación null
            Cell hireDateCell = row.createCell(7);
            if (emp.getHireDate() != null) {
                hireDateCell.setCellValue(Date.from(emp.getHireDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                hireDateCell.setCellStyle(dateStyle);
            } else {
                hireDateCell.setCellValue("");
                hireDateCell.setCellStyle(dataStyle);
            }

            // Edad - calculada de forma segura
            Cell ageCell = row.createCell(8);
            ageCell.setCellValue(emp.getAge()); // getAge() ya maneja null internamente
            ageCell.setCellStyle(dataStyle);

            // Años de servicio - calculado de forma segura
            Cell serviceCell = row.createCell(9);
            serviceCell.setCellValue(emp.getYearsOfService()); // getYearsOfService() ya maneja null
            serviceCell.setCellStyle(dataStyle);
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        System.out.println("📊 Hoja Excel creada con " + employees.size() + " empleados");
    }
    
}
