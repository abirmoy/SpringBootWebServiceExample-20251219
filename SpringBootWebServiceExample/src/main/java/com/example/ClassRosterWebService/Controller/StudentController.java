package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.StudentDao;
import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.Entity.Student;
import com.example.ClassRosterWebService.Entity.Course;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("students")
    public String displayStudents(Model model, HttpServletRequest request) {
        List<Student> students = studentDao.getAllStudents();
        List<Course> courses = courseDao.getAllCourses();
        
        // Check for success messages
        String successMessage = request.getParameter("success");
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage.replace("+", " "));
        }
        
        model.addAttribute("students", students);
        model.addAttribute("courses", courses);
        
        return "students";
    }

    @GetMapping("editStudent")
    public String editStudent(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
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
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validate inputs
            if (firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty()) {
                throw new RuntimeException("First name and last name are required!");
            }
            
            Student student = new Student();
            student.setFirstName(firstName.trim());
            student.setLastName(lastName.trim());
            
            studentDao.addStudent(student);
            
            return "redirect:/students?success=Student+added+successfully";
            
        } catch (RuntimeException e) {
            List<Student> students = studentDao.getAllStudents();
            List<Course> courses = courseDao.getAllCourses();
            
            model.addAttribute("students", students);
            model.addAttribute("courses", courses);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "students";
        }
    }

    @PostMapping("updateStudent")
    public String updateStudent(HttpServletRequest request, Model model) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            
            // Validate inputs
            if (firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty()) {
                throw new RuntimeException("First name and last name are required!");
            }
            
            Student student = studentDao.getStudentById(id);
            if (student == null) {
                throw new RuntimeException("Student not found!");
            }
            
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
            List<Course> courses = courseDao.getAllCourses();
            
            model.addAttribute("students", students);
            model.addAttribute("courses", courses);
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