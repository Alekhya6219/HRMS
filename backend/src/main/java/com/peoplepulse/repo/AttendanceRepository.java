package com.peoplepulse.repo;
import com.peoplepulse.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AttendanceRepository extends JpaRepository<Attendance,Long> {
    List<Attendance> findByDateOrderByIdDesc(java.time.LocalDate date);
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);
}
