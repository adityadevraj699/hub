package com.fashion.hub.Controller;

import com.fashion.hub.Model.User;
import com.fashion.hub.Repository.UserRepository;
import com.fashion.hub.Service.EmailService;
import com.fashion.hub.Model.Role;
import jakarta.servlet.http.HttpSession;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;


    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {

        User user = userRepository.findAll()
                .stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }

        session.setAttribute("loggedUser", user);
        session.setAttribute("userRole", user.getRole().toString());

        if (user.getRole().name().equalsIgnoreCase("ADMIN")) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin/dashboard";
        } else {
            session.setAttribute("isCustomer", true);
            return "redirect:/";
        }
    }
    
    
 // Show registration form
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model, HttpSession session) {

        // Check email exists
        if (userRepository.findAll().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }

        // Check phone exists
        if (user.getPhone() != null && !user.getPhone().isEmpty() &&
                userRepository.findAll().stream().anyMatch(u -> u.getPhone().equals(user.getPhone()))) {
            model.addAttribute("error", "Phone number already exists");
            return "register";
        }

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Save user temporarily in session
        session.setAttribute("tempUser", user);
        session.setAttribute("otp", otp);

        // Send OTP email
        try {
            emailService.sendOtpEmail(user.getEmail(), "Verify your FashionHub Account", otp);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send OTP: " + e.getMessage());
            return "register";
        }

        return "redirect:/verify-otp";
    }

    // Show OTP verification form
    @GetMapping("/verify-otp")
    public String showOtpForm() {
        return "verify_otp";
    }

    // Handle OTP verification
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otpInput, HttpSession session, Model model) {
        String otp = (String) session.getAttribute("otp");
        User tempUser = (User) session.getAttribute("tempUser");

        if (otp == null || tempUser == null) {
            return "redirect:/register";
        }

        if (!otp.equals(otpInput)) {
            model.addAttribute("error", "Invalid OTP");
            return "verify_otp";
        }

        // OTP valid → save user as CUSTOMER
        tempUser.setRole(Role.CUSTOMER);
        userRepository.save(tempUser);

        // Remove session attributes
        session.removeAttribute("tempUser");
        session.removeAttribute("otp");

        model.addAttribute("success", "Registration successful! You can now login.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

