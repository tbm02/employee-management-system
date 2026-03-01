package com.agile.ems.feedback.controller;

import com.agile.ems.feedback.dto.SessionScoreDto;
import com.agile.ems.feedback.entity.FeedbackSession;
import com.agile.ems.feedback.service.FeedbackSessionService;
import com.agile.ems.utils.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback/sessions")
@RequiredArgsConstructor
public class FeedbackSessionController {

    private final FeedbackSessionService feedbackSessionService;

    /**
     * GET /api/feedback/sessions?page=0&size=10&sort=createdAt,desc
     * Lists paginated feedback sessions.
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<FeedbackSession>>> getSessions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<FeedbackSession> page = feedbackSessionService.getSessions(pageable);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Sessions fetched successfully", page)
        );
    }

    /** POST /api/feedback/sessions — create a new session. */
    @PostMapping
    public ResponseEntity<ApiResponseDto<FeedbackSession>> createSession(
            @Valid @RequestBody CreateFeedbackSessionRequest request
    ) {
        FeedbackSession session = feedbackSessionService.createSession(
                request.getName().trim(), request.getMonth(), request.getYear());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success(HttpStatus.CREATED.value(), "Feedback session created", session)
        );
    }

    /** POST /api/feedback/sessions/{sessionId}/assign — assign employees with peer reviewers. */
    @PostMapping("/{sessionId}/assign")
    public ResponseEntity<ApiResponseDto<Object>> assignEmployees(
            @PathVariable Long sessionId,
            @Valid @RequestBody AssignSessionEmployeesRequest request
    ) {
        feedbackSessionService.assignSessionToEmployees(
                sessionId,
                request.getAssignments(),
                request.getSelfTemplateId(),
                request.getPeerTemplateId(),
                request.getManagerTemplateId()
        );
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Employees assigned successfully", null)
        );
    }

    /**
     * GET /api/feedback/sessions/{sessionId}/scores?departmentId={id}
     * departmentId is optional — omit to get all employees, pass to filter by dept.
     */
    @GetMapping("/{sessionId}/scores")
    public ResponseEntity<ApiResponseDto<List<SessionScoreDto>>> getScores(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long departmentId
    ) {
        List<SessionScoreDto> scores = feedbackSessionService.getSessionScores(sessionId, departmentId);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Scores fetched successfully", scores)
        );
    }
}
