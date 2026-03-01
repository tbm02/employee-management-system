package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.SessionUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionUserRepository extends JpaRepository<SessionUser, Long> {

    Optional<SessionUser> findBySessionIdAndEmployeeId(Long sessionId, Long employeeId);

    List<SessionUser> findBySessionId(Long sessionId);
}
