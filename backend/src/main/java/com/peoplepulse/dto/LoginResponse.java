
package com.peoplepulse.dto;

public class LoginResponse {

    private String token;
    private String email;
    private String role;
    private Long employeeId;

    public LoginResponse(String token, String email, String role, Long employeeId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getEmployeeId() { return employeeId; }
}
