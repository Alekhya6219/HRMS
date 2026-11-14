package com.peoplepulse.repo;

import com.peoplepulse.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findAllByOrderByIdDesc();

    List<Payroll> findByEmployeeIdOrderByIdDesc(Long employeeId);
}
