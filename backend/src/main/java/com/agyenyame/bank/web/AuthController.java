package com.agyenyame.bank.web;

import com.agyenyame.bank.domain.OtpChallenge;
import com.agyenyame.bank.domain.User;
import com.agyenyame.bank.otp.OtpService;
import com.agyenyame.bank.repository.UserRepository;
import com.agyenyame.bank.security.JwtService;
import com.agyenyame.bank.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Authentication and signup. Signup is OTP-gated:
 *   POST /api/auth/signup       -> validates details, issues an OTP, returns a reference
 *   POST /api/auth/signup/verify -> verifies the OTP, then creates the account
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthController(UserRepository userRepository, PasswordEncoder encoder,
                         JwtService jwtService, OtpService otpService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @PostMapping("/signup")
    public OtpIssuedResponse signup(@Valid @RequestBody SignupRequest req) throws Exception {
        if (!req.pin.equals(req.confirmPin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PIN and confirm PIN do not match");
        }
        if (userRepository.existsByEmail(req.email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userRepository.existsByMobileNumber(req.mobileNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "Mobile number already registered");
        }
        // Store the pending signup (with hashed PIN) in the OTP challenge payload.
        Map<String, String> payload = Map.of(
                "firstName", req.firstName,
                "lastName", req.lastName,
                "email", req.email,
                "mobileNumber", req.mobileNumber,
                "pinHash", encoder.encode(req.pin));
        var issued = otpService.issue(OtpChallenge.Purpose.SIGNUP, req.mobileNumber,
                mapper.writeValueAsString(payload));
        return new OtpIssuedResponse(issued.reference(),
                "OTP sent to " + req.mobileNumber + ". Verify it to create your account.", issued.devCode());
    }

    @PostMapping("/signup/verify")
    public Map<String, Object> verifySignup(@Valid @RequestBody OtpVerifyRequest req) throws Exception {
        OtpChallenge challenge = otpService.verify(req.reference, "SIGNUP", req.code);
        Map<String, String> payload = mapper.readValue(challenge.getPayloadJson(), Map.class);

        // Re-check uniqueness in case something registered while OTP was pending.
        if (userRepository.existsByEmail(payload.get("email"))
                || userRepository.existsByMobileNumber(payload.get("mobileNumber"))) {
            throw new ApiException(HttpStatus.CONFLICT, "Account already exists for these details");
        }
        User user = new User();
        user.setFirstName(payload.get("firstName"));
        user.setLastName(payload.get("lastName"));
        user.setEmail(payload.get("email"));
        user.setMobileNumber(payload.get("mobileNumber"));
        user.setPinHash(payload.get("pinHash"));
        user.setBalance(BigDecimal.ZERO);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getMobileNumber());
        return Map.of(
                "message", "Account created successfully",
                "token", token,
                "mobileNumber", user.getMobileNumber(),
                "firstName", user.getFirstName());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByMobileNumber(req.mobileNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(req.pin, user.getPinHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getMobileNumber());
        return Map.of(
                "token", token,
                "mobileNumber", user.getMobileNumber(),
                "firstName", user.getFirstName());
    }
}
