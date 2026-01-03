package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.StudentDao;
import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.DAO.UserDao;
import com.example.ClassRosterWebService.Entity.Student;
import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.User;
import com.example.ClassRosterWebService.Validation.InputValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class StudentController {
    
    @Autowired
    StudentDao studentDao;
    
    @Autowired
    CourseDao courseDao;
    
    @Autowired
    UserDao userDao;

    @GetMapping("students")
    public String displayStudents(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDao.getUserByUsername(username);
        
        List<Student> students;
        boolean isStudentView = false;
        
        // Check if user is a STUDENT (not ADMIN or TEACHER)
        boolean hasStudentRole = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean hasAdminTeacherRole = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"));
        
        if (hasStudentRole && !hasAdminTeacherRole) {
            // Student can only see their own data
            if (user != null && user.getStudentId() != null) {
                Student student = studentDao.getStudentById(user.getStudentId());
                
                if (student != null) {
                    students = List.of(student); // Only show this student
                    isStudentView = true;
                } else {
                    // Student record not found
                    students = List.of();
                    model.addAttribute("errorMessage", "Student record not found for this account.");
                }
            } else {
                // Student user not linked to a student record
                students = List.of();
                if (user == null) {
                    model.addAttribute("errorMessage", "User account not found.");
                } else {
                    model.addAttribute("errorMessage", "Student account not properly linked to student record.");
                }
            }
        } else {
            // ADMIN/TEACHER can see all students
            students = studentDao.getAllStudents();
        }
        
        // Check for success messages
        String successMessage = request.getParameter("success");
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage.replace("+", " "));
        }
        
        model.addAttribute("students", students);
        model.addAttribute("isStudentView", isStudentView);
        
        return "students";
    }

    @GetMapping("editStudent")
    public String editStudent(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDao.getUserByUsername(username);
        
        int id;
        boolean isStudentView = false;
        
        // Check if user is a STUDENT (not ADMIN or TEACHER)
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"))) {
            
            // Student can only edit their own data
            if (user == null || user.getStudentId() == null) {
                model.addAttribute("errorMessage", "Student account not properly linked.");
                return displayStudents(model, request);
            }
            id = user.getStudentId();
            isStudentView = true;
        } else {
            // ADMIN/TEACHER can edit any student
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                model.addAttribute("errorMessage", "Student ID is required.");
                return displayStudents(model, request);
            }
            try {
                id = Integer.parseInt(idParam);
            } catch (NumberFormatException e) {
                model.addAttribute("errorMessage", "Invalid student ID format.");
                return displayStudents(model, request);
            }
        }
        
        Student student = studentDao.getStudentById(id);
        
        if (student == null) {
            model.addAttribute("errorMessage", "Student not found!");
            return displayStudents(model, request);
        }
        
        // Get all available courses
        List<Course> allCourses = courseDao.getAllCourses();
        // Get courses student is currently enrolled in
        List<Course> enrolledCourses = studentDao.getCoursesForStudent(id);
        
        model.addAttribute("student", student);
        model.addAttribute("allCourses", allCourses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("isStudentView", isStudentView);
        
        return "editStudent";
    }

    @PostMapping("addStudent")
    public String addStudent(HttpServletRequest request, Model model) {
        try {
            String studentId = request.getParameter("studentId");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validate inputs using InputValidator
            String studentIdError = InputValidator.validateStudentId(studentId);
            String firstNameError = InputValidator.validateFirstName(firstName);
            String lastNameError = InputValidator.validateLastName(lastName);
            
            if (studentIdError != null || firstNameError != null || lastNameError != null) {
                StringBuilder errorMsg = new StringBuilder();
                if (studentIdError != null) errorMsg.append(studentIdError).append(" ");
                if (firstNameError != null) errorMsg.append(firstNameError).append(" ");
                if (lastNameError != null) errorMsg.append(lastNameError).append(" ");
                throw new RuntimeException(errorMsg.toString().trim());
            }
            
            // Sanitize inputs
            studentId = InputValidator.sanitizeInput(studentId);
            firstName = InputValidator.sanitizeInput(firstName);
            lastName = InputValidator.sanitizeInput(lastName);
            
            // Check if student ID already exists
            if (studentDao.studentIdExists(studentId)) {
                throw new RuntimeException("Student ID '" + studentId + "' already exists!");
            }
            
            Student student = new Student();
            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            
            studentDao.addStudent(student);
            
            return "redirect:/students?success=Student+added+successfully";
            
        } catch (RuntimeException e) {
            // Get current user info for view
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userDao.getUserByUsername(username);
            
            boolean isStudentView = false;
            List<Student> students;
            
            if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
                authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_TEACHER"))) {
                
                if (user != null && user.getStudentId() != null) {
                    Student student = studentDao.getStudentById(user.getStudentId());
                    students = student != null ? List.of(student) : List.of();
                    isStudentView = true;
                } else {
                    students = List.of();
                }
            } else {
                students = studentDao.getAllStudents();
            }
            
            model.addAttribute("students", students);
            model.addAttribute("isStudentView", isStudentView);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "students";
        }
    }

    @PostMapping("updateStudent")
    public String updateStudent(HttpServletRequest request, Model model) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String studentId = request.getParameter("studentId");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validate inputs using InputValidator
            String studentIdError = InputValidator.validateStudentId(studentId);
            String firstNameError = InputValidator.validateFirstName(firstName);
            String lastNameError = InputValidator.validateLastName(lastName);
            
            if (studentIdError != null || firstNameError != null || lastNameError != null) {
                StringBuilder errorMsg = new StringBuilder();
                if (studentIdError != null) errorMsg.append(studentIdError).append(" ");
                if (firstNameError != null) errorMsg.append(firstNameError).append(" ");
                if (lastNameError != null) errorMsg.append(lastNameError).append(" ");
                throw new RuntimeException(errorMsg.toString().trim());
            }
            
            // Sanitize inputs
            studentId = InputValidator.sanitizeInput(studentId);
            firstName = InputValidator.sanitizeInput(firstName);
            lastName = InputValidator.sanitizeInput(lastName);
            
            Student student = studentDao.getStudentById(id);
            if (student == null) {
                throw new RuntimeException("Student not found!");
            }
            
            // Check if user is a student trying to change their own data
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userDao.getUserByUsername(username);
            boolean isStudentView = false;
            
            if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
                authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_TEACHER"))) {
                
                isStudentView = true;
                // Students can only update their own record
                if (user == null || user.getStudentId() != id) {
                    throw new RuntimeException("Students can only update their own information!");
                }
                // Students cannot change their student ID
                if (!student.getStudentId().equals(studentId)) {
                    throw new RuntimeException("Students cannot change their Student ID!");
                }
            }
            
            student.setStudentId(studentId);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            
            studentDao.updateStudent(student);
            
            if (isStudentView) {
                return "redirect:/students?success=Your+profile+updated+successfully";
            } else {
                return "redirect:/students?success=Student+updated+successfully";
            }
            
        } catch (RuntimeException e) {
            int id = Integer.parseInt(request.getParameter("id"));
            Student student = studentDao.getStudentById(id);
            List<Course> allCourses = courseDao.getAllCourses();
            List<Course> enrolledCourses = studentDao.getCoursesForStudent(id);
            
            // Determine if student view
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isStudentView = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
                authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_TEACHER"));
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("isStudentView", isStudentView);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "editStudent";
        }
    }

    @GetMapping("deleteStudent")
    public String deleteStudent(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
        
        try {
            studentDao.deleteStudentById(id);
            return "redirect:/students?success=Student+deleted+successfully";
            
        } catch (RuntimeException e) {
            // Get the current user to determine view type
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userDao.getUserByUsername(username);
            
            boolean isStudentView = false;
            List<Student> students;
            
            if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
                authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_TEACHER"))) {
                
                if (user != null && user.getStudentId() != null) {
                    Student student = studentDao.getStudentById(user.getStudentId());
                    students = student != null ? List.of(student) : List.of();
                    isStudentView = true;
                } else {
                    students = List.of();
                }
            } else {
                students = studentDao.getAllStudents();
            }
            
            model.addAttribute("students", students);
            model.addAttribute("isStudentView", isStudentView);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "students";
        }
    }

    @PostMapping("enrollStudent")
    public String enrollStudent(HttpServletRequest request, Model model) {
        try {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            int courseId = Integer.parseInt(request.getParameter("courseId"));
            
            studentDao.enrollStudentInCourse(studentId, courseId);
            
            return "redirect:/editStudent?id=" + studentId + "&success=Student+enrolled+in+course+successfully";
            
        } catch (RuntimeException e) {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            Student student = studentDao.getStudentById(studentId);
            List<Course> allCourses = courseDao.getAllCourses();
            List<Course> enrolledCourses = studentDao.getCoursesForStudent(studentId);
            
            // Determine if student view
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isStudentView = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
                authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_TEACHER"));
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("isStudentView", isStudentView);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "editStudent";
        }
    }

    @GetMapping("unenrollStudent")
    public String unenrollStudent(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if user is a STUDENT (not ADMIN or TEACHER)
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"))) {
            
            // Students cannot unenroll themselves
            model.addAttribute("errorMessage", "Students are not allowed to unenroll from courses.");
            
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            Student student = studentDao.getStudentById(studentId);
            List<Course> allCourses = courseDao.getAllCourses();
            List<Course> enrolledCourses = studentDao.getCoursesForStudent(studentId);
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("isStudentView", true);
            
            return "editStudent";
        }
        
        try {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            int courseId = Integer.parseInt(request.getParameter("courseId"));
            
            studentDao.unenrollStudentFromCourse(studentId, courseId);
            
            return "redirect:/editStudent?id=" + studentId + "&success=Student+unenrolled+from+course+successfully";
            
        } catch (RuntimeException e) {
            int studentId = Integer.parseInt(request.getParameter("studentId"));
            Student student = studentDao.getStudentById(studentId);
            List<Course> allCourses = courseDao.getAllCourses();
            List<Course> enrolledCourses = studentDao.getCoursesForStudent(studentId);
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("isStudentView", false);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "editStudent";
        }
        
    }
    
    @GetMapping("/debugUser")
    public String debugUser(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDao.getUserByUsername(username);
        
        System.out.println("=== DEBUG USER INFO ===");
        System.out.println("Username: " + username);
        System.out.println("User object: " + (user != null ? "exists" : "null"));
        if (user != null) {
            System.out.println("User ID: " + user.getId());
            System.out.println("Student ID in User object: " + user.getStudentId());
            System.out.println("Enabled: " + user.isEnabled());
        }
        
        // Get user roles
        List<String> roles = userDao.getRolesForUser(username);
        System.out.println("Roles: " + roles);
        
        model.addAttribute("username", username);
        model.addAttribute("userExists", user != null);
        if (user != null) {
            model.addAttribute("userId", user.getId());
            model.addAttribute("studentId", user.getStudentId());
            model.addAttribute("enabled", user.isEnabled());
            model.addAttribute("roles", roles);
        }
        
        return "debugUser";
    }
}