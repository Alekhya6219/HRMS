package com.peoplepulse.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="attendance", uniqueConstraints=@UniqueConstraint(columnNames={"employee_id","work_date"}))
public class Attendance {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="employee_id", nullable=false) private Long employeeId;
    @Column(name="work_date", nullable=false) private LocalDate date;
    private String status;
    private LocalTime checkIn;
    private LocalTime checkOut;
    public Attendance() {}
    public Long getId(){return id;} public Long getEmployeeId(){return employeeId;} public LocalDate getDate(){return date;}
    public String getStatus(){return status;} public LocalTime getCheckIn(){return checkIn;} public LocalTime getCheckOut(){return checkOut;}
    public void setId(Long v){id=v;} public void setEmployeeId(Long v){employeeId=v;} public void setDate(LocalDate v){date=v;}
    public void setStatus(String v){status=v;} public void setCheckIn(LocalTime v){checkIn=v;} public void setCheckOut(LocalTime v){checkOut=v;}
}
