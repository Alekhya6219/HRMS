package com.peoplepulse.controller;

import com.peoplepulse.model.*;
import com.peoplepulse.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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

    public ApiController(EmployeeRepository e, DepartmentRepository d, AttendanceRepository a, LeaveRepository l, PayrollRepository p) {
        employees=e; departments=d; attendance=a; leaves=l; payroll=p;
    }

    @GetMapping("/health") public Map<String,String> health(){ return Map.of("status","UP","application","PeoplePulse HRMS"); }

    @GetMapping("/employees") public List<Employee> getEmployees(@RequestParam(required=false) String search) {
        return (search==null || search.isBlank()) ? employees.findAll() :
            employees.findByNameContainingIgnoreCaseOrDepartmentContainingIgnoreCase(search, search);
    }
    @PostMapping("/employees") @ResponseStatus(HttpStatus.CREATED) public Employee addEmployee(@RequestBody Employee e) {
        e.setId(null); if(e.getStatus()==null) e.setStatus("ACTIVE"); return employees.save(e);
    }
    @PutMapping("/employees/{id}") public Employee updateEmployee(@PathVariable Long id,@RequestBody Employee e) {
        Employee old=employees.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        e.setId(id); return employees.save(e);
    }
    @DeleteMapping("/employees/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteEmployee(@PathVariable Long id) {
        if(!employees.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        employees.deleteById(id);
    }

    @GetMapping("/departments") public List<Department> getDepartments(){ return departments.findAll(); }

    @GetMapping("/attendance") public List<Attendance> getAttendance(@RequestParam(required=false) String date,
            @RequestParam(required=false) Long employeeId) {
        if(employeeId!=null) return attendance.findByEmployeeIdOrderByDateDesc(employeeId);
        return attendance.findByDateOrderByIdDesc(date==null?LocalDate.now():LocalDate.parse(date));
    }
    @PostMapping("/attendance") public Attendance markAttendance(@RequestBody Attendance a) {
        if(!employees.existsById(a.getEmployeeId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not found");
        if(a.getDate()==null) a.setDate(LocalDate.now());
        List<Attendance> existing=attendance.findByEmployeeIdOrderByDateDesc(a.getEmployeeId());
        Optional<Attendance> same=existing.stream().filter(x->x.getDate().equals(a.getDate())).findFirst();
        if(same.isPresent()) { a.setId(same.get().getId()); }
        return attendance.save(a);
    }

    @GetMapping("/leaves") public List<LeaveRequest> getLeaves(){ return leaves.findAllByOrderByIdDesc(); }
    @PostMapping("/leaves") @ResponseStatus(HttpStatus.CREATED) public LeaveRequest applyLeave(@RequestBody LeaveRequest l) {
        Employee e=employees.findById(l.getEmployeeId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not found"));
        l.setId(null); l.setEmployeeName(e.getName()); l.setStatus("PENDING"); return leaves.save(l);
    }
    @PutMapping("/leaves/{id}/status") public LeaveRequest updateLeave(@PathVariable Long id,@RequestBody Map<String,String> body) {
        LeaveRequest l=leaves.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        String status=body.get("status");
        if(!List.of("APPROVED","REJECTED","PENDING").contains(status)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid status");
        l.setStatus(status); return leaves.save(l);
    }

    @GetMapping("/payroll") public List<Payroll> getPayroll(){ return payroll.findAllByOrderByIdDesc(); }
    @PostMapping("/payroll") @ResponseStatus(HttpStatus.CREATED) public Payroll addPayroll(@RequestBody Payroll p) {
        Employee e=employees.findById(p.getEmployeeId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee not found"));
        p.setId(null); p.setEmployeeName(e.getName());
        if(p.getBasicSalary()==null) p.setBasicSalary(0.0);
        if(p.getDeductions()==null) p.setDeductions(0.0);
        p.setNetSalary(p.getBasicSalary()-p.getDeductions());
        if(p.getStatus()==null) p.setStatus("PENDING");
        return payroll.save(p);
    }
    @PutMapping("/payroll/{id}/status") public Payroll updatePayroll(@PathVariable Long id,@RequestBody Map<String,String> body) {
        Payroll p=payroll.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setStatus(body.get("status")); if("PAID".equals(p.getStatus())) p.setPaidOn(LocalDate.now());
        return payroll.save(p);
    }

    @GetMapping("/dashboard") public Map<String,Object> dashboard() {
        List<Employee> all=employees.findAll();
        List<Attendance> today=attendance.findByDateOrderByIdDesc(LocalDate.now());
        long present=today.stream().filter(a->"PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus())).count();
        long pending=leaves.findAll().stream().filter(l->"PENDING".equals(l.getStatus())).count();
        double payrollTotal=payroll.findAll().stream().mapToDouble(p->p.getNetSalary()==null?0:p.getNetSalary()).sum();
        return Map.of("totalEmployees",all.size(),"activeEmployees",all.stream().filter(e->"ACTIVE".equals(e.getStatus())).count(),
            "presentToday",present,"pendingLeaves",pending,"payrollTotal",payrollTotal);
    }
}
