package com.agyenyame.bank.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    /**
     * Argon2id password encoder (saltLength=16, hashLength=32, parallelism=1,
     * memory=1<<14 KiB, iterations=3). Requires Bouncy Castle on the classpath.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 14, 3);
    }
}
