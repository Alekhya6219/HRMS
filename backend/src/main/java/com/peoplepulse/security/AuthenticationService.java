
package com.peoplepulse.security;

import com.peoplepulse.dto.LoginRequest;
import com.peoplepulse.dto.LoginResponse;
import com.peoplepulse.dto.CreateEmployeeAccountRequest;
import com.peoplepulse.dto.CreateHrAccountRequest;

import com.peoplepulse.model.User;
import com.peoplepulse.model.Employee;

import com.peoplepulse.repo.UserRepository;
import com.peoplepulse.repo.EmployeeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // USER LOGIN

    public LoginResponse login(LoginRequest request) {

        if (request.getEmail() == null ||
                request.getPassword() == null ||
                request.getEmail().isBlank() ||
                request.getPassword().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required");
        }

        User user = userRepository.findByEmailIgnoreCase(
                request.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!user.isEnabled() ||
                !passwordEncoder.matches(
                        request.getPassword(), user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getRole(),
                user.getEmployeeId()
        );
    }

    // CREATE EMPLOYEE ACCOUNT

    public void createEmployeeAccount(
            CreateEmployeeAccountRequest request) {

        if (request.getEmployeeId() == null ||
                request.getPassword() == null ||
                request.getPassword().length() < 12) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID and a password of at least 12 characters are required");
        }

        Employee employee = employeeRepository.findById(
                request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found"));

        if (userRepository.existsByEmailIgnoreCase(employee.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this email");
        }

        if (userRepository.findByEmployeeId(employee.getId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this employee");
        }

        User user = new User();

        user.setEmail(employee.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole("EMPLOYEE");
        user.setEmployeeId(employee.getId());
        user.setEnabled(true);

        userRepository.save(user);
    }

    // CREATE HR ACCOUNT

    public void createHrAccount(CreateHrAccountRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().length() < 12) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid email and a password of at least 12 characters are required");
        }

        String email = request.getEmail().trim();

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email address");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this email");
        }

        User user = new User();

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));
        user.setRole("HR");
        user.setEmployeeId(null);
        user.setEnabled(true);

        userRepository.save(user);
    }
}
