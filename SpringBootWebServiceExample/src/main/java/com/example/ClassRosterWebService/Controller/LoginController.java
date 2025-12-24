package com.example.ClassRosterWebService.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "accessDenied";
    }


    @GetMapping("/testHash")
    @ResponseBody
    public String testHash(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash1 = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVbB9e";
        String hash2 = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        
        boolean matches1 = encoder.matches(password, hash1);
        boolean matches2 = encoder.matches(password, hash2);
        
        return "Testing password: " + password + "<br>" +
            "Hash1 matches (admin): " + matches1 + "<br>" +
            "Hash2 matches (test): " + matches2 + "<br>" +
            "New hash for '" + password + "': " + encoder.encode(password);
    }

    @GetMapping("/checkUser")
    @ResponseBody
    public String checkUser(@RequestParam String username) {
        // You'll need to autowire UserDao
        // This will check if user exists in database
        return "Check user endpoint - need to implement";
    }
}