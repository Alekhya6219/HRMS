package com.peoplepulse.config;

import com.peoplepulse.model.*;
import com.peoplepulse.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;

@Configuration
public class DataSeeder {
    @Bean CommandLineRunner seed(DepartmentRepository departments, EmployeeRepository employees) {
        return args -> {
            if(departments.count()==0) {
                departments.save(new Department("Engineering"));
                departments.save(new Department("Human Resources"));
                departments.save(new Department("Finance"));
                departments.save(new Department("Marketing"));
                departments.save(new Department("Operations"));
            }
            if(employees.count()==0) {
                Employee a=new Employee(); a.setName("Aarav Sharma"); a.setEmail("aarav@peoplepulse.demo");
                a.setPhone("+91 98765 43210"); a.setDesignation("Software Engineer"); a.setDepartment("Engineering");
                a.setJoiningDate(LocalDate.now().minusMonths(8)); a.setSalary(65000.0); a.setStatus("ACTIVE"); employees.save(a);
                Employee b=new Employee(); b.setName("Diya Reddy"); b.setEmail("diya@peoplepulse.demo");
                b.setPhone("+91 98765 43211"); b.setDesignation("HR Executive"); b.setDepartment("Human Resources");
                b.setJoiningDate(LocalDate.now().minusMonths(14)); b.setSalary(48000.0); b.setStatus("ACTIVE"); employees.save(b);
                Employee c=new Employee(); c.setName("Kabir Mehta"); c.setEmail("kabir@peoplepulse.demo");
                c.setPhone("+91 98765 43212"); c.setDesignation("Finance Analyst"); c.setDepartment("Finance");
                c.setJoiningDate(LocalDate.now().minusMonths(5)); c.setSalary(55000.0); c.setStatus("ACTIVE"); employees.save(c);
            }
        };
    }
}
