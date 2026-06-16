package com.agyenyame.bank.repository;

import com.agyenyame.bank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobileNumber(String mobileNumber);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByEmail(String email);
}
