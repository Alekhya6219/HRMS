
package com.peoplepulse.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.peoplepulse.model.Employee;
import java.time.LocalDate;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String designation;
    private String department;
    private LocalDate joiningDate;
    private String status;
    private Double salary;

    public EmployeeResponse(Employee employee, boolean includeSalary) {
        this.id = employee.getId();
        this.name = employee.getName();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.designation = employee.getDesignation();
        this.department = employee.getDepartment();
        this.joiningDate = employee.getJoiningDate();
        this.status = employee.getStatus();

        if (includeSalary) {
            this.salary = employee.getSalary();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public String getStatus() {
        return status;
    }

    public Double getSalary() {
        return salary;
    }
}
