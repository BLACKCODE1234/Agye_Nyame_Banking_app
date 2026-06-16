package com.agyenyame.bank.repository;

import com.agyenyame.bank.domain.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {
    Optional<OtpChallenge> findByReference(String reference);
}
