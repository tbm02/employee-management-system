package com.agile.ems.feedback.repository;

import com.agile.ems.feedback.entity.SessionPeerReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionPeerReviewRepository extends JpaRepository<SessionPeerReview, Long> {

    List<SessionPeerReview> findBySessionUserId(Long sessionUserId);

    /** Average peer score across all peer reviewers for a given SessionUser. */
    @Query("SELECT AVG(r.score) FROM SessionPeerReview r WHERE r.sessionUser.id = :sessionUserId AND r.score IS NOT NULL")
    Double avgScoreBySessionUserId(@Param("sessionUserId") Long sessionUserId);
}
