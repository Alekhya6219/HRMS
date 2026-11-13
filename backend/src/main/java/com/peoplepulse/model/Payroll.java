package com.peoplepulse.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="payroll")
public class Payroll {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long employeeId;
    private String employeeName;
    @Column(name = "payroll_month")
private String month;
    private Double basicSalary;
    private Double deductions;
    private Double netSalary;
    private LocalDate paidOn;
    private String status = "PENDING";
    public Payroll() {}
    public Long getId(){return id;} public Long getEmployeeId(){return employeeId;} public String getEmployeeName(){return employeeName;}
    public String getMonth(){return month;} public Double getBasicSalary(){return basicSalary;} public Double getDeductions(){return deductions;}
    public Double getNetSalary(){return netSalary;} public LocalDate getPaidOn(){return paidOn;} public String getStatus(){return status;}
    public void setId(Long v){id=v;} public void setEmployeeId(Long v){employeeId=v;} public void setEmployeeName(String v){employeeName=v;}
    public void setMonth(String v){month=v;} public void setBasicSalary(Double v){basicSalary=v;} public void setDeductions(Double v){deductions=v;}
    public void setNetSalary(Double v){netSalary=v;} public void setPaidOn(LocalDate v){paidOn=v;} public void setStatus(String v){status=v;}
}
