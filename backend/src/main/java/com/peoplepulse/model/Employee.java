package com.peoplepulse.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="employees")
public class Employee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false, unique=true) private String email;
    private String phone;
    private String designation;
    private String department;
    private LocalDate joiningDate;
    private Double salary;
    private String status = "ACTIVE";
    public Employee() {}
    public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
    public String getPhone(){return phone;} public String getDesignation(){return designation;}
    public String getDepartment(){return department;} public LocalDate getJoiningDate(){return joiningDate;}
    public Double getSalary(){return salary;} public String getStatus(){return status;}
    public void setId(Long v){id=v;} public void setName(String v){name=v;} public void setEmail(String v){email=v;}
    public void setPhone(String v){phone=v;} public void setDesignation(String v){designation=v;}
    public void setDepartment(String v){department=v;} public void setJoiningDate(LocalDate v){joiningDate=v;}
    public void setSalary(Double v){salary=v;} public void setStatus(String v){status=v;}
}
