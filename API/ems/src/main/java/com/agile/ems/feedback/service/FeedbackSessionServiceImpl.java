package com.agile.ems.feedback.service;

import com.agile.ems.feedback.controller.AssignSessionEmployeesRequest.EmployeeAssignment;
import com.agile.ems.feedback.entity.FeedbackSession;
import com.agile.ems.feedback.entity.FeedbackTemplate;
import com.agile.ems.feedback.entity.SessionUser;
import com.agile.ems.feedback.repository.FeedbackSessionRepository;
import com.agile.ems.feedback.repository.FeedbackTemplateRepository;
import com.agile.ems.feedback.repository.SessionUserRepository;
import com.agile.ems.user.Repository.UserRepository;
import com.agile.ems.user.entity.User;
import com.agile.ems.utils.MailService;
import com.agile.ems.utils.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackSessionServiceImpl implements FeedbackSessionService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackSessionServiceImpl.class);

    private final FeedbackSessionRepository feedbackSessionRepository;
    private final FeedbackTemplateRepository feedbackTemplateRepository;
    private final SessionUserRepository sessionUserRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackSession> getSessions() {
        return feedbackSessionRepository.findAll();
    }

    @Override
    @Transactional
    public FeedbackSession createSession(String name, int month, int year) {
        FeedbackSession session = new FeedbackSession();
        session.setName(name);
        session.setMonth(month);
        session.setYear(year);
        session.setIsActive(true);
        return feedbackSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void assignSessionToEmployees(
            Long sessionId,
            List<EmployeeAssignment> assignments,
            Long selfTemplateId,
            Long peerTemplateId,
            Long managerTemplateId
    ) {
        FeedbackSession session = feedbackSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback session not found with id: " + sessionId));

        FeedbackTemplate selfTemplate = feedbackTemplateRepository.findById(selfTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Self template not found: " + selfTemplateId));
        FeedbackTemplate peerTemplate = feedbackTemplateRepository.findById(peerTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Peer template not found: " + peerTemplateId));
        FeedbackTemplate managerTemplate = feedbackTemplateRepository.findById(managerTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager template not found: " + managerTemplateId));

        // Collect all user IDs needed (employees + peer reviewers)
        List<Long> allUserIds = assignments.stream()
                .flatMap(a -> java.util.stream.Stream.of(a.getEmployeeId(), a.getPeerReviewerId()))
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (EmployeeAssignment assignment : assignments) {
            User employee = userMap.get(assignment.getEmployeeId());
            if (employee == null) {
                throw new ResourceNotFoundException("Employee not found with id: " + assignment.getEmployeeId());
            }
            User peerReviewer = userMap.get(assignment.getPeerReviewerId());
            if (peerReviewer == null) {
                throw new ResourceNotFoundException("Peer reviewer not found with id: " + assignment.getPeerReviewerId());
            }

            SessionUser sessionUser = new SessionUser();
            sessionUser.setSession(session);
            sessionUser.setEmployee(employee);
            sessionUser.setPeerReviewer(peerReviewer);
            sessionUser.setSelfTemplate(selfTemplate);
            sessionUser.setPeerTemplate(peerTemplate);
            sessionUser.setManagerTemplate(managerTemplate);
            sessionUserRepository.save(sessionUser);

            sendAssignmentEmail(employee, peerReviewer, session, peerTemplate, managerTemplate);
        }
    }

    private void sendAssignmentEmail(
            User employee,
            User peerReviewer,
            FeedbackSession session,
            FeedbackTemplate peerTemplate,
            FeedbackTemplate managerTemplate
    ) {
        // Notify the employee
        String employeeSubject = "Feedback Session Assignment — " + session.getName();
        String employeeBody = String.format(
                "Hi %s,%n%nYou have been assigned to the feedback session '%s' (%02d/%d).%n" +
                "Your peer reviewer: %s %s%n" +
                "Peer review template: %s%nManager review template: %s%n%n" +
                "Please log in to the EMS portal to complete your self-review.",
                employee.getFirstName(),
                session.getName(), session.getMonth(), session.getYear(),
                peerReviewer.getFirstName(), peerReviewer.getLastName(),
                peerTemplate.getName(), managerTemplate.getName()
        );
        sendSafe(employee.getEmail(), employeeSubject, employeeBody);

        // Notify the peer reviewer
        String peerSubject = "Peer Review Assignment — " + session.getName();
        String peerBody = String.format(
                "Hi %s,%n%nYou have been assigned to peer-review %s %s in the feedback session '%s' (%02d/%d).%n" +
                "Template: %s%n%nPlease log in to the EMS portal to submit your peer review.",
                peerReviewer.getFirstName(),
                employee.getFirstName(), employee.getLastName(),
                session.getName(), session.getMonth(), session.getYear(),
                peerTemplate.getName()
        );
        sendSafe(peerReviewer.getEmail(), peerSubject, peerBody);
    }

    private void sendSafe(String to, String subject, String body) {
        try {
            mailService.sendEmail(to, subject, body);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
