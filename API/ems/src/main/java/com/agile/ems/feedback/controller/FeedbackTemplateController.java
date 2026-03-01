package com.agile.ems.feedback.controller;

import com.agile.ems.feedback.entity.FeedbackQuestion;
import com.agile.ems.feedback.entity.FeedbackTemplate;
import com.agile.ems.feedback.repository.FeedbackQuestionRepository;
import com.agile.ems.feedback.repository.FeedbackTemplateRepository;
import com.agile.ems.utils.ApiResponseDto;
import com.agile.ems.utils.exceptions.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback/templates")
@RequiredArgsConstructor
public class FeedbackTemplateController {

    private final FeedbackTemplateRepository feedbackTemplateRepository;
    private final FeedbackQuestionRepository feedbackQuestionRepository;

    /** List all seeded templates. */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<FeedbackTemplate>>> getAll() {
        List<FeedbackTemplate> templates = feedbackTemplateRepository.findAll();
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Templates fetched successfully", templates)
        );
    }

    /** Get all questions for a specific template. */
    @GetMapping("/{id}/questions")
    public ResponseEntity<ApiResponseDto<List<FeedbackQuestion>>> getQuestions(@PathVariable Long id) {
        // Verify template exists
        feedbackTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        List<FeedbackQuestion> questions = feedbackQuestionRepository.findByTemplateId(id);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Questions fetched successfully", questions)
        );
    }
}
