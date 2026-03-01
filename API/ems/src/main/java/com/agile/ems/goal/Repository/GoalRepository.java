package com.agile.ems.goal.Repository;

import com.agile.ems.goal.entity.Goal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByEmployeeIdOrCreatedByOrderByCreatedAtDesc(Long employeeId, Long createdBy);
}
