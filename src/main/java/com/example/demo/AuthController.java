package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {
    //stores all the users in memory
    private Map<String, User> users = new HashMap<>();
    private final Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);

    //LOGIN
    @GetMapping("/")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
        @RequestParam String accountNumber,
        @RequestParam String pin,
        HttpSession session,
        Model model) {
        User user = users.get(accountNumber);
        if (user == null) {
            model.addAttribute("error", "Account not found!");
            return "login";
        }
        if (!argon2.matches(pin, user.getHashedPin())) {
            model.addAttribute("error", "Incorrect Pin!");
            return "login";
        }
        //save logged in user to session
        session.setAttribute("loggedInUser", user);
        return "redirect:/dashboard";
    }

    // REGISTER
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String mobileNumber,
        @RequestParam String pin,
        @RequestParam String confirmPin,
        Model model) {
        if (!pin.matches("\\d{6}")) {
            model.addAttribute("error", "Pin must be Exactly 6 digits!");
            return "register";
        }
        if (!pin.equals(confirmPin)) {
            model.addAttribute("error", "Pins Do Not Match!");
            return "register";
        }
        // check if Mobile Number is Already Registered
        for (User u : users.values()) {
            if (u.getMobileNumber().equals(mobileNumber)) {
                model.addAttribute("error", "This Mobile Number is Already Registered!");
                return "register";
            }
        }

        String hashedPin = argon2.encode(pin);
        User user = new User(firstName, lastName, mobileNumber, hashedPin);
        users.put(user.getAccountNumber(), user);
        model.addAttribute("Your Account has been Created Successfully", "Your Account Number is:" + user.getAccountNumber());
        return "register";
    }

    // DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // FORGOT PIN
    @GetMapping("/forgot-pin")
    public String showForgotPin() {
        return "forgot-pin";
    }

    @PostMapping("/forgot-pin")
    public String forgotPin(
        @RequestParam String accountNumber,
        @RequestParam String mobileNumber,
        @RequestParam String newPin,
        @RequestParam String confirmNewPin,
        Model model) {
        User user = users.get(accountNumber);
        if (user == null) {
            model.addAttribute("error", "Account not found!");
            return "forgot-pin";
        }
        if (!user.getMobileNumber().equals(mobileNumber)) {
            model.addAttribute("error", "Mobile number does not match!");
            return "forgot-pin";
        }
        if (!newPin.matches("\\d{6}")) {
            model.addAttribute("error", "New pin must be exactly 6 digits!");
            return "forgot-pin";
        }
        if (!newPin.equals(confirmNewPin)) {
            model.addAttribute("error", "New pins do not match!");
            return "forgot-pin";
        }
        String hashedNewPin = argon2.encode(newPin);
        user.setHashedPin(hashedNewPin);
        model.addAttribute("Success", "Pin updated successfully!");
        return "forgot-pin";
    }
}




