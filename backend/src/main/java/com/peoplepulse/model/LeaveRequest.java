package com.peoplepulse.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="leave_requests")
public class LeaveRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status = "PENDING";
    public LeaveRequest() {}
    public Long getId(){return id;} public Long getEmployeeId(){return employeeId;} public String getEmployeeName(){return employeeName;}
    public String getLeaveType(){return leaveType;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;}
    public String getReason(){return reason;} public String getStatus(){return status;}
    public void setId(Long v){id=v;} public void setEmployeeId(Long v){employeeId=v;} public void setEmployeeName(String v){employeeName=v;}
    public void setLeaveType(String v){leaveType=v;} public void setStartDate(LocalDate v){startDate=v;} public void setEndDate(LocalDate v){endDate=v;}
    public void setReason(String v){reason=v;} public void setStatus(String v){status=v;}
}
