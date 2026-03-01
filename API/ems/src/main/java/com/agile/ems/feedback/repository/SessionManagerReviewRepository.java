package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.SessionManagerReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionManagerReviewRepository extends JpaRepository<SessionManagerReview, Long> {

    List<SessionManagerReview> findBySessionUserId(Long sessionUserId);

    /** Average manager score for a given SessionUser. */
    @Query("SELECT AVG(r.score) FROM SessionManagerReview r WHERE r.sessionUser.id = :sessionUserId AND r.score IS NOT NULL")
    Double avgScoreBySessionUserId(@Param("sessionUserId") Long sessionUserId);
}
