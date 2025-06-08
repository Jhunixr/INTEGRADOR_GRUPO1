package semana12;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    public Multimap<String, Employee> groupByDepartment(List<Employee> employees) {
        logger.info("Agrupando {} empleados por departamento", employees.size());
        
        // Crear un Multimap inmutable usando Guava
        ImmutableListMultimap.Builder<String, Employee> builder = ImmutableListMultimap.builder();
        
        employees.forEach(emp -> {
            if (emp.getDepartment() != null) {
                builder.put(emp.getDepartment(), emp);
            }
        });
        
        Multimap<String, Employee> groupedEmployees = builder.build();
        
        logger.debug("Empleados agrupados en {} departamentos", groupedEmployees.keySet().size());
        groupedEmployees.keySet().forEach(dept -> 
            logger.debug("Departamento '{}': {} empleados", dept, groupedEmployees.get(dept).size())
        );
        
        return groupedEmployees;
    }
    
    /**
     * Calcula estadísticas por departamento
     */
    public Map<String, DepartmentStats> calculateDepartmentStatistics(List<Employee> employees) {
        logger.info("Calculando estadísticas por departamento");
        
        Multimap<String, Employee> groupedEmployees = groupByDepartment(employees);
        Map<String, DepartmentStats> statistics = Maps.newHashMap();
        
        for (String department : groupedEmployees.keySet()) {
            Collection<Employee> deptEmployees = groupedEmployees.get(department);
            DepartmentStats stats = calculateStatsForDepartment(department, deptEmployees);
            statistics.put(department, stats);
        }
        
        logger.info("Estadísticas calculadas para {} departamentos", statistics.size());
        return statistics;
    }
    
    /**
     * Calcula estadísticas para un departamento específico
     */
    private DepartmentStats calculateStatsForDepartment(String department, Collection<Employee> employees) {
        
        // Convertir a lista para facilitar operaciones
        List<Employee> empList = Lists.newArrayList(employees);
        
        // Calcular estadísticas usando Guava y Java Streams
        int count = empList.size();
        
        OptionalDouble avgSalary = empList.stream()
            .filter(emp -> emp.getSalary() != null)
            .mapToDouble(Employee::getSalary)
            .average();
        
        OptionalDouble avgAge = empList.stream()
            .mapToInt(Employee::getAge)
            .average();
        
        Optional<Employee> highestPaid = empList.stream()
            .filter(emp -> emp.getSalary() != null)
            .max(Comparator.comparing(Employee::getSalary));
        
        Optional<Employee> oldestEmployee = empList.stream()
            .max(Comparator.comparing(Employee::getAge));
        
        // Calcular distribución de rangos salariales
        Map<String, Long> salaryRanges = calculateSalaryRanges(empList);
        
        logger.debug("Estadísticas calculadas para departamento '{}': {} empleados", 
                    department, count);
        
        return new DepartmentStats(
            department,
            count,
            avgSalary.orElse(0.0),
            avgAge.orElse(0.0),
            highestPaid.orElse(null),
            oldestEmployee.orElse(null),
            salaryRanges
        );
    }
    
    /**
     * Calcula distribución de rangos salariales
     */
    private Map<String, Long> calculateSalaryRanges(List<Employee> employees) {
        return employees.stream()
            .filter(emp -> emp.getSalary() != null)
            .collect(Collectors.groupingBy(
                this::getSalaryRange,
                Collectors.counting()
            ));
    }
    
    /**
     * Determina el rango salarial de un empleado
     */
    private String getSalaryRange(Employee employee) {
        double salary = employee.getSalary();
        if (salary < 30000) return "Menos de $30,000";
        else if (salary < 50000) return "$30,000 - $50,000";
        else if (salary < 80000) return "$50,000 - $80,000";
        else if (salary < 120000) return "$80,000 - $120,000";
        else return "Más de $120,000";
    }
    
    /**
     * Filtra empleados usando predicados de Guava
     */
    public List<Employee> filterEmployees(List<Employee> employees, 
                                         Double minSalary, 
                                         Integer minAge, 
                                         String department) {
        
        logger.info("Filtrando empleados con criterios: salary >= {}, age >= {}, dept = {}", 
                   minSalary, minAge, department);
        
        // Crear predicados usando Guava (aunque es más común usar Java 8+ streams ahora)
        List<Predicate<Employee>> predicates = Lists.newArrayList();
        
        if (minSalary != null) {
            predicates.add(emp -> emp.getSalary() != null && emp.getSalary() >= minSalary);
        }
        
        if (minAge != null) {
            predicates.add(emp -> emp.getAge() >= minAge);
        }
        
        if (department != null && !department.trim().isEmpty()) {
            predicates.add(emp -> department.equalsIgnoreCase(emp.getDepartment()));
        }
        
        // Aplicar todos los predicados
        List<Employee> filtered = employees.stream()
            .filter(emp -> predicates.stream().allMatch(predicate -> predicate.apply(emp)))
            .collect(Collectors.toList());
        
        logger.info("Filtrado completado: {} empleados cumplen los criterios", filtered.size());
        return filtered;
    }
    
    /**
     * Obtiene los top N empleados por salario usando Guava Ordering
     */
    public List<Employee> getTopEmployeesBySalary(List<Employee> employees, int topN) {
        logger.info("Obteniendo top {} empleados por salario", topN);
        
        // Usar Guava Ordering para ordenamiento avanzado
        Ordering<Employee> salaryOrdering = Ordering.natural()
            .nullsLast()
            .onResultOf(Employee::getSalary)
            .reverse(); // Mayor a menor
        
        List<Employee> topEmployees = salaryOrdering
            .sortedCopy(employees)
            .stream()
            .limit(topN)
            .collect(Collectors.toList());
        
        logger.debug("Top {} empleados obtenidos", topEmployees.size());
        return topEmployees;
    }
    
    /**
     * Crea un resumen general usando colecciones inmutables de Guava
     */
    public EmployeeSummary createSummary(List<Employee> employees) {
        logger.info("Creando resumen general de {} empleados", employees.size());
        
        if (employees.isEmpty()) {
            return new EmployeeSummary(
                0, 0.0, 0.0, 
                ImmutableSet.of(), 
                ImmutableMap.of()
            );
        }
        
        int totalEmployees = employees.size();
        
        double avgSalary = employees.stream()
            .filter(emp -> emp.getSalary() != null)
            .mapToDouble(Employee::getSalary)
            .average()
            .orElse(0.0);
        
        double avgAge = employees.stream()
            .mapToInt(Employee::getAge)
            .average()
            .orElse(0.0);
        
        // Crear conjunto inmutable de departamentos únicos
        ImmutableSet<String> departments = ImmutableSet.copyOf(
            employees.stream()
                .map(Employee::getDepartment)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
        );
        
        // Crear mapa inmutable de conteo por departamento
        ImmutableMap<String, Long> departmentCounts = ImmutableMap.copyOf(
            employees.stream()
                .filter(emp -> emp.getDepartment() != null)
                .collect(Collectors.groupingBy(
                    Employee::getDepartment,
                    Collectors.counting()
                ))
        );
        
        EmployeeSummary summary = new EmployeeSummary(
            totalEmployees, avgSalary, avgAge, departments, departmentCounts
        );
        
        logger.info("Resumen creado: {} empleados en {} departamentos", 
                   totalEmployees, departments.size());
        
        return summary;
    }
    
    /**
     * Clase interna para estadísticas de departamento
     */
    public static class DepartmentStats {
        private final String department;
        private final int employeeCount;
        private final double averageSalary;
        private final double averageAge;
        private final Employee highestPaidEmployee;
        private final Employee oldestEmployee;
        private final Map<String, Long> salaryRangeDistribution;
        
        public DepartmentStats(String department, int employeeCount, double averageSalary, 
                              double averageAge, Employee highestPaidEmployee, 
                              Employee oldestEmployee, Map<String, Long> salaryRangeDistribution) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.averageSalary = averageSalary;
            this.averageAge = averageAge;
            this.highestPaidEmployee = highestPaidEmployee;
            this.oldestEmployee = oldestEmployee;
            this.salaryRangeDistribution = salaryRangeDistribution;
        }
        
        // Getters
        public String getDepartment() { return department; }
        public int getEmployeeCount() { return employeeCount; }
        public double getAverageSalary() { return averageSalary; }
        public double getAverageAge() { return averageAge; }
        public Employee getHighestPaidEmployee() { return highestPaidEmployee; }
        public Employee getOldestEmployee() { return oldestEmployee; }
        public Map<String, Long> getSalaryRangeDistribution() { return salaryRangeDistribution; }
        
        @Override
        public String toString() {
            return String.format("DepartmentStats{dept='%s', count=%d, avgSalary=%.2f, avgAge=%.1f}", 
                               department, employeeCount, averageSalary, averageAge);
        }
    }
    
    /**
     * Clase interna para resumen general
     */
    public static class EmployeeSummary {
        private final int totalEmployees;
        private final double averageSalary;
        private final double averageAge;
        private final ImmutableSet<String> departments;
        private final ImmutableMap<String, Long> departmentCounts;
        
        public EmployeeSummary(int totalEmployees, double averageSalary, double averageAge, 
                              ImmutableSet<String> departments, ImmutableMap<String, Long> departmentCounts) {
            this.totalEmployees = totalEmployees;
            this.averageSalary = averageSalary;
            this.averageAge = averageAge;
            this.departments = departments;
            this.departmentCounts = departmentCounts;
        }
        
        // Getters
        public int getTotalEmployees() { return totalEmployees; }
        public double getAverageSalary() { return averageSalary; }
        public double getAverageAge() { return averageAge; }
        public ImmutableSet<String> getDepartments() { return departments; }
        public ImmutableMap<String, Long> getDepartmentCounts() { return departmentCounts; }
        
        @Override
        public String toString() {
            return String.format("EmployeeSummary{total=%d, avgSalary=%.2f, avgAge=%.1f, depts=%d}", 
                               totalEmployees, averageSalary, averageAge, departments.size());
        }
    }
}