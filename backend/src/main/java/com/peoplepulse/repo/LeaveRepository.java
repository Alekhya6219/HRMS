package com.peoplepulse.repo;

import com.peoplepulse.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findAllByOrderByIdDesc();

    List<LeaveRequest> findByEmployeeIdOrderByIdDesc(Long employeeId);
}
