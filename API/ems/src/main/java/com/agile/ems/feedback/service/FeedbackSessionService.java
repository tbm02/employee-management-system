package com.agile.ems.feedback.service;

import com.agile.ems.feedback.controller.AssignSessionEmployeesRequest.EmployeeAssignment;
import com.agile.ems.feedback.entity.FeedbackSession;
import java.util.List;

public interface FeedbackSessionService {

    List<FeedbackSession> getSessions();

    FeedbackSession createSession(String name, int month, int year);

    void assignSessionToEmployees(
            Long sessionId,
            List<EmployeeAssignment> assignments,
            Long selfTemplateId,
            Long peerTemplateId,
            Long managerTemplateId
    );
}
