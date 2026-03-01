package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.SessionSelfReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionSelfReviewRepository extends JpaRepository<SessionSelfReview, Long> {

    List<SessionSelfReview> findBySessionUserId(Long sessionUserId);

    /** Average score across all self-review responses for a given SessionUser. */
    @Query("SELECT AVG(r.score) FROM SessionSelfReview r WHERE r.sessionUser.id = :sessionUserId AND r.score IS NOT NULL")
    Double avgScoreBySessionUserId(@Param("sessionUserId") Long sessionUserId);
}
