package com.agile.ems.user.Repository;

import com.agile.ems.user.entity.UserDetails;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUserId(Long userId);
}
