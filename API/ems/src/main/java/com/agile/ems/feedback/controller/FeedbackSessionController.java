package com.agile.ems.feedback.controller;

import com.agile.ems.feedback.entity.FeedbackSession;
import com.agile.ems.feedback.service.FeedbackSessionService;
import com.agile.ems.utils.ApiResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback/sessions")
@RequiredArgsConstructor
public class FeedbackSessionController {

    private final FeedbackSessionService feedbackSessionService;

    /** List all feedback sessions. */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<FeedbackSession>>> getSessions() {
        List<FeedbackSession> sessions = feedbackSessionService.getSessions();
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Sessions fetched successfully", sessions)
        );
    }

    /** Create a new feedback session. */
    @PostMapping
    public ResponseEntity<ApiResponseDto<FeedbackSession>> createSession(
            @Valid @RequestBody CreateFeedbackSessionRequest request
    ) {
        FeedbackSession session = feedbackSessionService.createSession(
                request.getName().trim(),
                request.getMonth(),
                request.getYear()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success(HttpStatus.CREATED.value(), "Feedback session created successfully", session)
        );
    }

    /** Assign employees to a session with per-employee peer reviewer. */
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
                ApiResponseDto.success(HttpStatus.OK.value(), "Employees assigned to feedback session successfully", null)
        );
    }
}
