package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.FeedbackSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackSessionRepository extends JpaRepository<FeedbackSession, Long> {
}
