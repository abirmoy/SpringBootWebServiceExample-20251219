package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.TeacherDao;
import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.Entity.Teacher;
import com.example.ClassRosterWebService.Entity.Course;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class TeacherController {
    @Autowired
    TeacherDao teacherDao;
    
    @Autowired
    CourseDao courseDao;

    @GetMapping("teachers")
    public String displayTeachers(Model model, HttpServletRequest request) {
        List<Teacher> teachers = teacherDao.getAllTeachers();
        List<String> tbc = teacherDao.getTeacherByCourse();
        List<Course> courses = courseDao.getAllCourses();
        
        // Check for success messages from redirect
        String successMessage = request.getParameter("success");
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage.replace("+", " "));
        }
        
        model.addAttribute("teachercourse", tbc);
        model.addAttribute("teachers", teachers);
        model.addAttribute("courses", courses);
        
        return "teachers";
    }

    @GetMapping("editTeacher")
    public String editTeacher(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
        Teacher teacher = teacherDao.getTeacherById(id);
        List<Course> courses = courseDao.getAllCourses();
        
        if (teacher == null) {
            model.addAttribute("errorMessage", "Teacher not found!");
            return displayTeachers(model, request);
        }
        
        // Check if teacher is assigned to any courses
        List<Course> assignedCourses = courseDao.getCoursesForTeacher(teacher);
        boolean teacherAssignedToCourses = !assignedCourses.isEmpty();
        
        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        model.addAttribute("teacherAssignedToCourses", teacherAssignedToCourses);
        
        return "editTeacher";
    }

    @PostMapping("updateTeacher")
    public String updateTeacher(HttpServletRequest request, Model model) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String specialty = request.getParameter("specialty");
            
            // Validate inputs
            if (firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty() ||
                specialty == null || specialty.trim().isEmpty()) {
                throw new RuntimeException("All fields are required!");
            }
            
            Teacher teacher = teacherDao.getTeacherById(id);
            if (teacher == null) {
                throw new RuntimeException("Teacher not found!");
            }
            
            teacher.setFirstName(firstName.trim());
            teacher.setLastName(lastName.trim());
            teacher.setSpecialty(specialty.trim());
            
            teacherDao.updateTeacher(teacher);
            
            return "redirect:/teachers?success=Teacher+updated+successfully";
            
        } catch (RuntimeException e) {
            // Show error on edit page
            int id = Integer.parseInt(request.getParameter("id"));
            Teacher teacher = teacherDao.getTeacherById(id);
            List<Course> courses = courseDao.getAllCourses();
            
            model.addAttribute("teacher", teacher);
            model.addAttribute("courses", courses);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "editTeacher";
        }
    }

    @GetMapping("deleteTeacher")
    public String deleteTeacher(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
        
        try {
            // First check if teacher is assigned to any courses
            Teacher teacher = teacherDao.getTeacherById(id);
            if (teacher == null) {
                throw new RuntimeException("Teacher not found!");
            }
            
            List<Course> assignedCourses = courseDao.getCoursesForTeacher(teacher);
            if (!assignedCourses.isEmpty()) {
                throw new RuntimeException("Cannot delete teacher. They are assigned to " + 
                    assignedCourses.size() + " course(s). Please reassign courses first.");
            }
            
            teacherDao.deleteTeacherById(id);
            return "redirect:/teachers?success=Teacher+deleted+successfully";
            
        } catch (RuntimeException e) {
            // Show error message on teachers page
            List<Teacher> teachers = teacherDao.getAllTeachers();
            List<String> tbc = teacherDao.getTeacherByCourse();
            List<Course> courses = courseDao.getAllCourses();
            
            model.addAttribute("teachercourse", tbc);
            model.addAttribute("teachers", teachers);
            model.addAttribute("courses", courses);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "teachers";
        }
    }

    @PostMapping("addTeacher")
    public String addTeacher(HttpServletRequest request, Model model) {
        try {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String specialtyIn = request.getParameter("specialty");
            
            // Validate inputs
            if (firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty() ||
                specialtyIn == null || specialtyIn.trim().isEmpty()) {
                throw new RuntimeException("All fields are required!");
            }
            
            Teacher teacher = new Teacher();
            teacher.setFirstName(firstName.trim());
            teacher.setLastName(lastName.trim());
            teacher.setSpecialty(specialtyIn.trim());
            
            teacherDao.addTeacher(teacher);
            
            return "redirect:/teachers?success=Teacher+added+successfully";
            
        } catch (RuntimeException e) {
            // Show error message on teachers page
            List<Teacher> teachers = teacherDao.getAllTeachers();
            List<String> tbc = teacherDao.getTeacherByCourse();
            List<Course> courses = courseDao.getAllCourses();
            
            model.addAttribute("teachercourse", tbc);
            model.addAttribute("teachers", teachers);
            model.addAttribute("courses", courses);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "teachers";
        }
    }
}