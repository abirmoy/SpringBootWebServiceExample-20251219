package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.StudentDao;
import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.DAO.UserDao;
import com.example.ClassRosterWebService.Entity.Student;
import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"))) {
            
            // Student can only see their own data
            if (user.getStudentId() != null) {
                Student student = studentDao.getStudentById(user.getStudentId());
                students = List.of(student); // Only show this student
                isStudentView = true;
                model.addAttribute("studentView", true);
            } else {
                // Student user not linked to a student record
                students = List.of();
                model.addAttribute("errorMessage", "Student account not properly linked to student record.");
            }
        } else {
            // ADMIN/TEACHER can see all students
            students = studentDao.getAllStudents();
            model.addAttribute("studentView", false);
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
        
        // Check if user is a STUDENT (not ADMIN or TEACHER)
        if (authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")) &&
            authentication.getAuthorities().stream()
            .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"))) {
            
            // Student can only edit their own data
            if (user.getStudentId() == null) {
                model.addAttribute("errorMessage", "Student account not properly linked.");
                return displayStudents(model, request);
            }
            id = user.getStudentId();
            model.addAttribute("isStudentView", true);
        } else {
            // ADMIN/TEACHER can edit any student
            id = Integer.parseInt(request.getParameter("id"));
            model.addAttribute("isStudentView", false);
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
        
        return "editStudent";
    }

    @PostMapping("addStudent")
    public String addStudent(HttpServletRequest request, Model model) {
        try {
            String studentId = request.getParameter("studentId");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validate inputs
            if (studentId == null || studentId.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty()) {
                throw new RuntimeException("All fields are required!");
            }
            
            // Check if student ID already exists
            if (studentDao.studentIdExists(studentId.trim())) {
                throw new RuntimeException("Student ID '" + studentId.trim() + "' already exists!");
            }
            
            Student student = new Student();
            student.setStudentId(studentId.trim());
            student.setFirstName(firstName.trim());
            student.setLastName(lastName.trim());
            
            studentDao.addStudent(student);
            
            return "redirect:/students?success=Student+added+successfully";
            
        } catch (RuntimeException e) {
            List<Student> students = studentDao.getAllStudents();
            
            model.addAttribute("students", students);
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
            
            // Validate inputs
            if (studentId == null || studentId.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty()) {
                throw new RuntimeException("All fields are required!");
            }
            
            Student student = studentDao.getStudentById(id);
            if (student == null) {
                throw new RuntimeException("Student not found!");
            }
            
            student.setStudentId(studentId.trim());
            student.setFirstName(firstName.trim());
            student.setLastName(lastName.trim());
            
            studentDao.updateStudent(student);
            
            return "redirect:/students?success=Student+updated+successfully";
            
        } catch (RuntimeException e) {
            int id = Integer.parseInt(request.getParameter("id"));
            Student student = studentDao.getStudentById(id);
            List<Course> allCourses = courseDao.getAllCourses();
            List<Course> enrolledCourses = studentDao.getCoursesForStudent(id);
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
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
            List<Student> students = studentDao.getAllStudents();
            
            model.addAttribute("students", students);
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
            
            model.addAttribute("student", student);
            model.addAttribute("allCourses", allCourses);
            model.addAttribute("enrolledCourses", enrolledCourses);
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
            model.addAttribute("errorMessage", e.getMessage());
            
            return "editStudent";
        }
    }
}