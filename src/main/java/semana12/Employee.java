package semana12;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.time.Period;

public class Employee {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private Double salary;
    private LocalDate birthDate;
    private LocalDate hireDate;
    
    // Constructor vacío
    public Employee() {}
    
    // Constructor completo
    public Employee(Long id, String firstName, String lastName, String email, 
                   String department, Double salary, LocalDate birthDate, LocalDate hireDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.birthDate = birthDate;
        this.hireDate = hireDate;
    }
    
    /**
     * Obtiene el nombre completo del empleado
     * Utiliza Apache Commons StringUtils para manejo seguro de cadenas
     */
    public String getFullName() {
        return StringUtils.join(
            StringUtils.defaultString(firstName), 
            " ", 
            StringUtils.defaultString(lastName)
        ).trim();
    }
    
    /**
     * Calcula la edad del empleado
     */
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * Calcula los años de servicio
     */
    public int getYearsOfService() {
        if (hireDate == null) {
            return 0;
        }
        return Period.between(hireDate, LocalDate.now()).getYears();
    }
    
    /**
     * Valida si los datos del empleado son válidos
     * Utiliza Apache Commons StringUtils
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(firstName) && 
               StringUtils.isNotBlank(lastName) && 
               StringUtils.isNotBlank(email) && 
               StringUtils.isNotBlank(department) && 
               salary != null && salary > 0 &&
               birthDate != null && 
               hireDate != null;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    /**
     * Implementación de equals usando Google Guava Objects
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Employee employee = (Employee) obj;
        return Objects.equal(id, employee.id) &&
               Objects.equal(firstName, employee.firstName) &&
               Objects.equal(lastName, employee.lastName) &&
               Objects.equal(email, employee.email);
    }
    
    /**
     * Implementación de hashCode usando Google Guava Objects
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id, firstName, lastName, email);
    }
    
    /**
     * Implementación de toString usando Apache Commons ToStringBuilder
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("fullName", getFullName())
                .append("email", email)
                .append("department", department)
                .append("salary", salary)
                .append("age", getAge())
                .append("yearsOfService", getYearsOfService())
                .toString();
    }
}
