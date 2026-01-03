package com.example.ClassRosterWebService.Validation;

import java.util.regex.Pattern;

public class InputValidator {
    
    // Common validation patterns
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s.'-]+$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern SINGLE_SPACE_PATTERN = Pattern.compile("^\\S+(\\s\\S+)*$");
    
    // Validation rules
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_STUDENT_ID_LENGTH = 3;
    private static final int MAX_STUDENT_ID_LENGTH = 20;
    
    public static String validateFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return "First name is required.";
        }
        
        firstName = firstName.trim();
        
        if (firstName.length() < MIN_NAME_LENGTH || firstName.length() > MAX_NAME_LENGTH) {
            return String.format("First name must be between %d and %d characters.", 
                MIN_NAME_LENGTH, MAX_NAME_LENGTH);
        }
        
        if (!SINGLE_SPACE_PATTERN.matcher(firstName).matches()) {
            return "First name cannot have consecutive spaces or leading/trailing spaces.";
        }
        
        if (!NAME_PATTERN.matcher(firstName).matches()) {
            return "First name can only contain letters, spaces, apostrophes, periods, and hyphens.";
        }
        
        return null; // No error
    }
    
    public static String validateLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return "Last name is required.";
        }
        
        lastName = lastName.trim();
        
        if (lastName.length() < MIN_NAME_LENGTH || lastName.length() > MAX_NAME_LENGTH) {
            return String.format("Last name must be between %d and %d characters.", 
                MIN_NAME_LENGTH, MAX_NAME_LENGTH);
        }
        
        if (!SINGLE_SPACE_PATTERN.matcher(lastName).matches()) {
            return "Last name cannot have consecutive spaces or leading/trailing spaces.";
        }
        
        if (!NAME_PATTERN.matcher(lastName).matches()) {
            return "Last name can only contain letters, spaces, apostrophes, periods, and hyphens.";
        }
        
        return null; // No error
    }
    
    public static String validateStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID is required.";
        }
        
        studentId = studentId.trim();
        
        if (studentId.length() < MIN_STUDENT_ID_LENGTH || studentId.length() > MAX_STUDENT_ID_LENGTH) {
            return String.format("Student ID must be between %d and %d characters.", 
                MIN_STUDENT_ID_LENGTH, MAX_STUDENT_ID_LENGTH);
        }
        
        if (!SINGLE_SPACE_PATTERN.matcher(studentId).matches()) {
            return "Student ID cannot contain spaces.";
        }
        
        if (!STUDENT_ID_PATTERN.matcher(studentId).matches()) {
            return "Student ID can only contain letters and numbers (no special characters).";
        }
        
        return null; // No error
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }
}