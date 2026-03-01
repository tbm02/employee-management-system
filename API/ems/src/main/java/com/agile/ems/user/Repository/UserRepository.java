package com.agile.ems.user.Repository;

import com.agile.ems.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmpId(String empId);

    Optional<User> findByEmailOrEmpId(String email, String empId);

    boolean existsByEmpId(String empId);

    boolean existsByEmail(String email);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(emp_id, 8) AS INTEGER)) FROM users WHERE emp_id LIKE CONCAT('EMP', :year, '%')", nativeQuery = true)
    Integer findMaxEmpIdNumberByYear(@Param("year") String year);
}
