package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.FeedbackQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackQuestionRepository extends JpaRepository<FeedbackQuestion, Long> {

    List<FeedbackQuestion> findByTemplateId(Long templateId);
}
