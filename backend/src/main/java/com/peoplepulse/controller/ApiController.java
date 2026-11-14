
package com.peoplepulse.controller;

import com.peoplepulse.dto.EmployeeResponse;
import com.peoplepulse.model.*;
import com.peoplepulse.repo.*;
import com.peoplepulse.security.CurrentUserService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final AttendanceRepository attendance;
    private final LeaveRepository leaves;
    private final PayrollRepository payroll;
    private final CurrentUserService currentUserService;

    public ApiController(
            EmployeeRepository e,
            DepartmentRepository d,
            AttendanceRepository a,
            LeaveRepository l,
            PayrollRepository p,
            CurrentUserService currentUserService) {

        this.employees = e;
        this.departments = d;
        this.attendance = a;
        this.leaves = l;
        this.payroll = p;
        this.currentUserService = currentUserService;
    }

    // PUBLIC HEALTH CHECK

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "application", "PeoplePulse HRMS"
        );
    }

    // EMPLOYEE MANAGEMENT - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/employees")
    public List<EmployeeResponse> getEmployees(
            @RequestParam(required = false) String search) {

        List<Employee> employeeList =
                (search == null || search.isBlank())
                        ? employees.findAll()
                        : employees.findByNameContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                                search, search);

        boolean includeSalary = currentUserService.getCurrentUser()
                .getRole().equals("ADMIN");

        return employeeList.stream()
                .map(employee -> new EmployeeResponse(
                        employee, includeSalary))
                .toList();
    }

    // CREATE EMPLOYEE - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse addEmployee(@RequestBody Employee e) {

        boolean isAdmin = currentUserService.getCurrentUser()
                .getRole().equals("ADMIN");

        e.setId(null);

        if (e.getStatus() == null) {
            e.setStatus("ACTIVE");
        }

        // HR cannot assign salary
        if (!isAdmin) {
            e.setSalary(null);
        }

        Employee savedEmployee = employees.save(e);

        return new EmployeeResponse(savedEmployee, isAdmin);
    }

    // UPDATE EMPLOYEE - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/employees/{id}")
    public EmployeeResponse updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee e) {

        Employee existingEmployee = employees.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        boolean isAdmin = currentUserService.getCurrentUser()
                .getRole().equals("ADMIN");

        e.setId(id);

        // HR cannot change an existing employee's salary
        if (!isAdmin) {
            e.setSalary(existingEmployee.getSalary());
        }

        Employee updatedEmployee = employees.save(e);

        return new EmployeeResponse(updatedEmployee, isAdmin);
    }

    // DELETE EMPLOYEE - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Long id) {

        if (!employees.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Employee not found");
        }

        employees.deleteById(id);
    }

    // EMPLOYEE - OWN PROFILE ONLY

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employees/me")
    public Map<String, Object> getMyProfile() {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee profile not found"));

        Map<String, Object> profile = new LinkedHashMap<>();

        profile.put("id", employee.getId());
        profile.put("name", employee.getName());
        profile.put("email", employee.getEmail());
        profile.put("phone", employee.getPhone());
        profile.put("designation", employee.getDesignation());
        profile.put("department", employee.getDepartment());
        profile.put("joiningDate", employee.getJoiningDate());
        profile.put("status", employee.getStatus());

        return profile;
    }

    // DEPARTMENTS - ALL AUTHENTICATED ROLES

    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return departments.findAll();
    }

    // EMPLOYEE - OWN ATTENDANCE

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/attendance/me")
    public List<Attendance> getMyAttendance() {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        return attendance.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    // ATTENDANCE MANAGEMENT - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/attendance")
    public List<Attendance> getAttendance(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long employeeId) {

        if (employeeId != null) {
            return attendance.findByEmployeeIdOrderByDateDesc(employeeId);
        }

        return attendance.findByDateOrderByIdDesc(
                date == null ? LocalDate.now() : LocalDate.parse(date));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/attendance")
    public Attendance markAttendance(@RequestBody Attendance a) {

        if (a.getEmployeeId() == null ||
                !employees.existsById(a.getEmployeeId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Employee not found");
        }

        if (a.getDate() == null) {
            a.setDate(LocalDate.now());
        }

        List<Attendance> existing =
                attendance.findByEmployeeIdOrderByDateDesc(a.getEmployeeId());

        Optional<Attendance> same = existing.stream()
                .filter(x -> x.getDate().equals(a.getDate()))
                .findFirst();

        if (same.isPresent()) {
            a.setId(same.get().getId());
        }

        return attendance.save(a);
    }

    // LEAVE MANAGEMENT - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/leaves")
    public List<LeaveRequest> getLeaves() {
        return leaves.findAllByOrderByIdDesc();
    }

    // EMPLOYEE - OWN LEAVE HISTORY

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/leaves/me")
    public List<LeaveRequest> getMyLeaves() {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        return leaves.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    // ADMIN AND HR - CREATE LEAVE RECORD

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping("/leaves")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequest applyLeave(@RequestBody LeaveRequest l) {

        Employee e = employees.findById(l.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Employee not found"));

        l.setId(null);
        l.setEmployeeName(e.getName());
        l.setStatus("PENDING");

        return leaves.save(l);
    }

    // EMPLOYEE - APPLY OWN LEAVE

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/leaves/me")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequest applyMyLeave(
            @RequestBody LeaveRequest request) {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        request.setId(null);
        request.setEmployeeId(employeeId);
        request.setEmployeeName(employee.getName());
        request.setStatus("PENDING");

        return leaves.save(request);
    }

    // LEAVE APPROVAL - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/leaves/{id}/status")
    public LeaveRequest updateLeave(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        LeaveRequest l = leaves.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave request not found"));

        String status = body.get("status");

        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid status");
        }

        l.setStatus(status);
        return leaves.save(l);
    }

    // PAYROLL MANAGEMENT - ADMIN ONLY

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payroll")
    public List<Payroll> getPayroll() {
        return payroll.findAllByOrderByIdDesc();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/payroll")
    @ResponseStatus(HttpStatus.CREATED)
    public Payroll addPayroll(@RequestBody Payroll p) {

        Employee e = employees.findById(p.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Employee not found"));

        p.setId(null);
        p.setEmployeeName(e.getName());

        if (p.getBasicSalary() == null) {
            p.setBasicSalary(0.0);
        }

        if (p.getDeductions() == null) {
            p.setDeductions(0.0);
        }

        p.setNetSalary(p.getBasicSalary() - p.getDeductions());

        if (p.getStatus() == null) {
            p.setStatus("PENDING");
        }

        return payroll.save(p);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/payroll/{id}/status")
    public Payroll updatePayroll(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Payroll p = payroll.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payroll record not found"));

        p.setStatus(body.get("status"));

        if ("PAID".equals(p.getStatus())) {
            p.setPaidOn(LocalDate.now());
        }

        return payroll.save(p);
    }

    // EMPLOYEE - OWN PAYROLL ONLY

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/payroll/me")
    public List<Payroll> getMyPayroll() {

        Long employeeId = currentUserService.getCurrentEmployeeId();

        return payroll.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    // DASHBOARD - ADMIN AND HR

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        List<Employee> all = employees.findAll();

        List<Attendance> today =
                attendance.findByDateOrderByIdDesc(LocalDate.now());

        long present = today.stream()
                .filter(a -> "PRESENT".equals(a.getStatus())
                        || "LATE".equals(a.getStatus()))
                .count();

        long pending = leaves.findAll().stream()
                .filter(l -> "PENDING".equals(l.getStatus()))
                .count();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("totalEmployees", all.size());
        dashboard.put("activeEmployees", all.stream()
                .filter(e -> "ACTIVE".equals(e.getStatus())).count());
        dashboard.put("presentToday", present);
        dashboard.put("pendingLeaves", pending);

        // Only Admin can retrieve payroll totals
        if (currentUserService.getCurrentUser()
                .getRole().equals("ADMIN")) {

            double payrollTotal = payroll.findAll().stream()
                    .mapToDouble(p -> p.getNetSalary() == null
                            ? 0 : p.getNetSalary())
                    .sum();

            dashboard.put("payrollTotal", payrollTotal);
        }

        return dashboard;
    }
}
