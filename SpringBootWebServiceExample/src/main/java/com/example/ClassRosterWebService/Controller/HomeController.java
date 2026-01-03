package com.example.ClassRosterWebService.Controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController implements ErrorController {

    /*    @GetMapping("home")
        public String homePage(Model model) {

            return "home";
        }*/

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == 404) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Page Not Found");
                model.addAttribute("errorDetails", "The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.");
            } else if (statusCode == 403) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorMessage", "Access Denied");
                model.addAttribute("errorDetails", "You don't have permission to access this page.");
            } else if (statusCode == 500) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Internal Server Error");
                model.addAttribute("errorDetails", "Something went wrong on our end. Please try again later.");
            } else {
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorMessage", "An error occurred");
                model.addAttribute("errorDetails", "Please try again or contact support if the problem persists.");
            }
        } else {
            model.addAttribute("errorCode", "Unknown");
            model.addAttribute("errorMessage", "An error occurred");
            model.addAttribute("errorDetails", "Please try again or contact support if the problem persists.");
        }
        
        return "error";
    }
}