package com.agile.ems.feedback.service;

import com.agile.ems.feedback.controller.AssignSessionEmployeesRequest.EmployeeAssignment;
import com.agile.ems.feedback.dto.SessionScoreDto;
import com.agile.ems.feedback.entity.FeedbackSession;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackSessionService {

    Page<FeedbackSession> getSessions(Pageable pageable);

    FeedbackSession createSession(String name, int month, int year);

    void assignSessionToEmployees(
            Long sessionId,
            List<EmployeeAssignment> assignments,
            Long selfTemplateId,
            Long peerTemplateId,
            Long managerTemplateId
    );

    List<SessionScoreDto> getSessionScores(Long sessionId);
}
