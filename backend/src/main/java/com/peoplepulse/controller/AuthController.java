
package com.peoplepulse.controller;

import com.peoplepulse.dto.LoginRequest;
import com.peoplepulse.dto.LoginResponse;
import com.peoplepulse.dto.CreateEmployeeAccountRequest;
import com.peoplepulse.dto.CreateHrAccountRequest;
import com.peoplepulse.security.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // USER LOGIN

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    // ADMIN ONLY - CREATE EMPLOYEE ACCOUNT

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-employee-account")
    @ResponseStatus(HttpStatus.CREATED)
    public String createEmployeeAccount(
            @RequestBody CreateEmployeeAccountRequest request) {

        authenticationService.createEmployeeAccount(request);

        return "Employee account created successfully";
    }

    // ADMIN ONLY - CREATE HR ACCOUNT

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-hr-account")
    @ResponseStatus(HttpStatus.CREATED)
    public String createHrAccount(
            @RequestBody CreateHrAccountRequest request) {

        authenticationService.createHrAccount(request);

        return "HR account created successfully";
    }
}
