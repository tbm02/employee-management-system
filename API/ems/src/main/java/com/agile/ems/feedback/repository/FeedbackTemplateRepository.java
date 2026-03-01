package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.FeedbackTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackTemplateRepository extends JpaRepository<FeedbackTemplate, Long> {
}
