package com.peoplepulse.dto;

public class CreateEmployeeAccountRequest {

    private Long employeeId;
    private String password;

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getPassword() {
        return password;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}