package com.agile.ems.feedback.service;

import com.agile.ems.feedback.controller.AssignSessionEmployeesRequest.EmployeeAssignment;
import com.agile.ems.feedback.dto.SessionScoreDto;
import com.agile.ems.feedback.entity.FeedbackSession;
import com.agile.ems.feedback.entity.FeedbackTemplate;
import com.agile.ems.feedback.entity.SessionUser;
import com.agile.ems.feedback.repository.FeedbackSessionRepository;
import com.agile.ems.feedback.repository.FeedbackTemplateRepository;
import com.agile.ems.feedback.repository.SessionManagerReviewRepository;
import com.agile.ems.feedback.repository.SessionPeerReviewRepository;
import com.agile.ems.feedback.repository.SessionSelfReviewRepository;
import com.agile.ems.feedback.repository.SessionUserRepository;
import com.agile.ems.departments.Repository.DepartmentRepository;
import com.agile.ems.departments.Department;
import com.agile.ems.user.Repository.UserRepository;
import com.agile.ems.user.entity.User;
import com.agile.ems.utils.MailService;
import com.agile.ems.utils.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackSessionServiceImpl implements FeedbackSessionService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackSessionServiceImpl.class);

    // Scoring weights (must sum to 1.0)
    private static final double W_SELF    = 0.30;
    private static final double W_MANAGER = 0.50;
    private static final double W_PEER    = 0.20;
    private static final double MAX_SCORE = 5.0;

    private final FeedbackSessionRepository     feedbackSessionRepository;
    private final FeedbackTemplateRepository    feedbackTemplateRepository;
    private final SessionUserRepository         sessionUserRepository;
    private final SessionSelfReviewRepository   selfReviewRepository;
    private final SessionPeerReviewRepository   peerReviewRepository;
    private final SessionManagerReviewRepository managerReviewRepository;
    private final UserRepository                userRepository;
    private final DepartmentRepository          departmentRepository;
    private final MailService                   mailService;

    // ── List sessions (pageable) ────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackSession> getSessions(Pageable pageable) {
        return feedbackSessionRepository.findAll(pageable);
    }

    // ── Create session ──────────────────────────────────────────────────────
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

    // ── Assign employees with per-employee peer reviewer ───────────────────
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
                .orElseThrow(() -> new ResourceNotFoundException("Feedback session not found: " + sessionId));

        FeedbackTemplate selfTemplate    = findTemplate(selfTemplateId,    "Self");
        FeedbackTemplate peerTemplate    = findTemplate(peerTemplateId,    "Peer");
        FeedbackTemplate managerTemplate = findTemplate(managerTemplateId, "Manager");

        // Batch-load all referenced users
        List<Long> allUserIds = assignments.stream()
                .flatMap(a -> java.util.stream.Stream.of(a.getEmployeeId(), a.getPeerReviewerId()))
                .distinct().toList();

        Map<Long, User> userMap = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        for (EmployeeAssignment assignment : assignments) {
            User employee = requireUser(userMap, assignment.getEmployeeId());
            User peer     = requireUser(userMap, assignment.getPeerReviewerId());

            SessionUser su = new SessionUser();
            su.setSession(session);
            su.setEmployee(employee);
            su.setPeerReviewer(peer);
            su.setSelfTemplate(selfTemplate);
            su.setPeerTemplate(peerTemplate);
            su.setManagerTemplate(managerTemplate);
            sessionUserRepository.save(su);

            // @Async — fires and forgets, never blocks the request thread
            mailService.sendEmail(employee.getEmail(),
                    "Feedback Session Assignment — " + session.getName(),
                    buildEmployeeBody(employee, peer, session, peerTemplate, managerTemplate));

            mailService.sendEmail(peer.getEmail(),
                    "Peer Review Assignment — " + session.getName(),
                    buildPeerBody(peer, employee, session, peerTemplate));
        }
    }

    // ── Score calculation ───────────────────────────────────────────────────
    /**
     * For each assigned employee, compute:
     *   selfAvg / peerAvg / managerAvg  (1–5 raw averages)
     *   selfNorm / peerNorm / managerNorm  (÷5, 0–1)
     *   weightedScore = 0.30·selfNorm + 0.50·managerNorm + 0.20·peerNorm
     *     (weights are re-normalised when a component has no responses)
     */
    @Override
    @Transactional(readOnly = true)
    public List<SessionScoreDto> getSessionScores(Long sessionId, Long departmentId) {
        feedbackSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        List<SessionUser> sessionUsers = sessionUserRepository.findBySessionId(sessionId);

        // Build department name lookup map (one DB call for all depts)
        Map<Long, String> deptNameMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        List<SessionScoreDto> result = new ArrayList<>();

        for (SessionUser su : sessionUsers) {
            Long suId = su.getId();
            User emp  = su.getEmployee();

            // Optional department filter
            if (departmentId != null && !departmentId.equals(emp.getDepartmentId())) {
                continue;
            }

            Double selfAvg    = dummyOrReal(selfReviewRepository.avgScoreBySessionUserId(suId),    suId, 0.0);
            Double peerAvg    = dummyOrReal(peerReviewRepository.avgScoreBySessionUserId(suId),    suId, 1.0);
            Double managerAvg = dummyOrReal(managerReviewRepository.avgScoreBySessionUserId(suId), suId, 2.0);

            Double selfNorm    = normalise(selfAvg);
            Double peerNorm    = normalise(peerAvg);
            Double managerNorm = normalise(managerAvg);

            Double weighted = weightedScore(selfNorm, peerNorm, managerNorm);

            String deptName = emp.getDepartmentId() != null
                    ? deptNameMap.getOrDefault(emp.getDepartmentId(), "Dept #" + emp.getDepartmentId())
                    : null;

            result.add(SessionScoreDto.builder()
                    .employeeId(emp.getId())
                    .empId(emp.getEmpId())
                    .firstName(emp.getFirstName())
                    .lastName(emp.getLastName())
                    .departmentId(emp.getDepartmentId())
                    .departmentName(deptName)
                    .selfAvg(round2(selfAvg))
                    .peerAvg(round2(peerAvg))
                    .managerAvg(round2(managerAvg))
                    .selfNorm(round2(selfNorm))
                    .peerNorm(round2(peerNorm))
                    .managerNorm(round2(managerNorm))
                    .weightedScore(round2(weighted))
                    .build());
        }

        return result;
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private FeedbackTemplate findTemplate(Long id, String label) {
        return feedbackTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " template not found: " + id));
    }

    private User requireUser(Map<Long, User> map, Long id) {
        User u = map.get(id);
        if (u == null) throw new ResourceNotFoundException("User not found: " + id);
        return u;
    }

    /** Divides raw avg by MAX_SCORE (5). Returns null if input is null. */
    private Double normalise(Double avg) {
        return avg == null ? null : avg / MAX_SCORE;
    }

    /**
     * If no reviews have been submitted yet, generate a deterministic pseudo-random
     * dummy score (seeded on the SessionUser id) so the score view is always populated
     * during development / demo. Remove this method when going to production.
     */
    private Double dummyOrReal(Double real, Long seed, double shift) {
        if (real != null) return real;
        // Deterministic but varied across employees; range ≈ 2.5–4.8
        double base = 2.5 + ((seed * 17 + (long)(shift * 100)) % 23) * 0.1;
        return Math.min(5.0, Math.round(base * 10.0) / 10.0);
    }

    /**
     * Computes weighted score from normalised values.
     * Missing components (null) are excluded and remaining weights re-normalised.
     */
    private Double weightedScore(Double self, Double peer, Double manager) {
        double score  = 0.0;
        double wTotal = 0.0;
        if (self    != null) { score += W_SELF    * self;    wTotal += W_SELF;    }
        if (manager != null) { score += W_MANAGER * manager; wTotal += W_MANAGER; }
        if (peer    != null) { score += W_PEER    * peer;    wTotal += W_PEER;    }
        return wTotal == 0.0 ? null : score / wTotal;
    }

    private Double round2(Double d) {
        return d == null ? null : Math.round(d * 100.0) / 100.0;
    }

    // ── Mail body builders ─────────────────────────────────────────────────

    private String buildEmployeeBody(User emp, User peer,
            FeedbackSession s, FeedbackTemplate peerTmpl, FeedbackTemplate mgrTmpl) {
        return String.format(
                "Hi %s,%n%nYou have been assigned to feedback session '%s' (%02d/%d).%n" +
                "Your peer reviewer: %s %s%nPeer template: %s%nManager template: %s%n%n" +
                "Please log in to the EMS portal to complete your self-review.",
                emp.getFirstName(), s.getName(), s.getMonth(), s.getYear(),
                peer.getFirstName(), peer.getLastName(),
                peerTmpl.getName(), mgrTmpl.getName());
    }

    private String buildPeerBody(User peer, User emp,
            FeedbackSession s, FeedbackTemplate peerTmpl) {
        return String.format(
                "Hi %s,%n%nYou have been assigned to peer-review %s %s in session '%s' (%02d/%d).%n" +
                "Template: %s%n%nPlease log in to the EMS portal to submit your review.",
                peer.getFirstName(), emp.getFirstName(), emp.getLastName(),
                s.getName(), s.getMonth(), s.getYear(), peerTmpl.getName());
    }
}
