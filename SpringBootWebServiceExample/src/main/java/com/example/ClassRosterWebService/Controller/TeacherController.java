package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.TeacherDao;
import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.Entity.Teacher;
import com.example.ClassRosterWebService.Entity.Course;
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
public class TeacherController {
    @Autowired
    TeacherDao teacherDao;
    
    @Autowired
    CourseDao courseDao;

    @GetMapping("teachers")
    public String displayTeachers(Model model, HttpServletRequest request) {
        // Check if user has permission to access teachers page
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"));
        
        if (!hasPermission) {
            model.addAttribute("errorMessage", "Access denied. You don't have permission to view teachers.");
            return "accessDenied";
        }
        
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
        // Check if user has permission to edit teachers
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"));
        
        if (!hasPermission) {
            model.addAttribute("errorMessage", "Access denied. You don't have permission to edit teachers.");
            return "accessDenied";
        }
        
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Teacher ID is required.");
            return displayTeachers(model, request);
        }
        
        try {
            int id = Integer.parseInt(idParam);
            Teacher teacher = teacherDao.getTeacherById(id);
            List<Course> courses = courseDao.getAllCourses();
            
            if (teacher == null) {
                model.addAttribute("errorMessage", "Teacher not found!");
                return displayTeachers(model, request);
            }
            
            // Check if teacher is assigned to any courses via teacherId
            List<Course> assignedCourses = courseDao.getCoursesForTeacher(teacher);
            boolean teacherAssignedToCourses = !assignedCourses.isEmpty();
            
            model.addAttribute("teacher", teacher);
            model.addAttribute("courses", courses);
            model.addAttribute("teacherAssignedToCourses", teacherAssignedToCourses);
            
            return "editTeacher";
            
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "Invalid teacher ID format.");
            return displayTeachers(model, request);
        }
    }

    @PostMapping("updateTeacher")
    public String updateTeacher(HttpServletRequest request, Model model) {
        // Check if user has permission to update teachers
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                           a.getAuthority().equals("ROLE_TEACHER"));
        
        if (!hasPermission) {
            model.addAttribute("errorMessage", "Access denied. You don't have permission to update teachers.");
            return "accessDenied";
        }
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String newSpecialty = request.getParameter("specialty");
            
            // Validate inputs
            if (firstName == null || firstName.trim().isEmpty() || 
                lastName == null || lastName.trim().isEmpty() ||
                newSpecialty == null || newSpecialty.trim().isEmpty()) {
                throw new RuntimeException("All fields are required!");
            }
            
            Teacher teacher = teacherDao.getTeacherById(id);
            if (teacher == null) {
                throw new RuntimeException("Teacher not found!");
            }
            
            String oldSpecialty = teacher.getSpecialty();
            
            teacher.setFirstName(firstName.trim());
            teacher.setLastName(lastName.trim());
            teacher.setSpecialty(newSpecialty.trim());
            
            teacherDao.updateTeacher(teacher);
            
            // Update course assignments
            updateCourseTeacherAssignment(oldSpecialty, newSpecialty, teacher);
            
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
        // Check if user has permission to delete teachers
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!hasPermission) {
            model.addAttribute("errorMessage", "Access denied. Only administrators can delete teachers.");
            return "accessDenied";
        }
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            // First check if teacher is assigned to any courses via teacherId
            Teacher teacher = teacherDao.getTeacherById(id);
            if (teacher == null) {
                throw new RuntimeException("Teacher not found!");
            }
            
            List<Course> assignedCourses = courseDao.getCoursesForTeacher(teacher);
            if (!assignedCourses.isEmpty()) {
                throw new RuntimeException("Cannot delete teacher. They are assigned to " + 
                    assignedCourses.size() + " course(s) via teacherId. Please reassign courses first.");
            }
            
            // If teacher has a specialty, remove them from that course's teacherId
            String specialty = teacher.getSpecialty();
            if (specialty != null && !specialty.isEmpty() && !specialty.equals("Unassigned")) {
                Course course = findCourseByName(specialty);
                if (course != null && course.getTeacher() != null && course.getTeacher().getId() == id) {
                    course.setTeacher(null);
                    courseDao.updateCourse(course);
                }
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
        // Check if user has permission to add teachers
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!hasPermission) {
            model.addAttribute("errorMessage", "Access denied. Only administrators can add teachers.");
            return "accessDenied";
        }
        
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
            
            // Add the teacher first
            teacherDao.addTeacher(teacher);
            
            // Find the course with this name and assign teacherId
            Course course = findCourseByName(specialtyIn.trim());
            if (course != null) {
                // Check if course already has a teacher assigned
                if (course.getTeacher() != null) {
                    throw new RuntimeException("Course '" + specialtyIn + "' is already assigned to another teacher!");
                }
                course.setTeacher(teacher);
                courseDao.updateCourse(course);
            }
            
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
    
    // Helper method to find course by name
    private Course findCourseByName(String courseName) {
        List<Course> allCourses = courseDao.getAllCourses();
        for (Course course : allCourses) {
            if (course.getName().equals(courseName)) {
                return course;
            }
        }
        return null;
    }
    
    // Helper method to update course teacher assignments
    private void updateCourseTeacherAssignment(String oldSpecialty, String newSpecialty, Teacher teacher) {
        // Remove teacher from old course's teacherId
        Course oldCourse = findCourseByName(oldSpecialty);
        if (oldCourse != null && oldCourse.getTeacher() != null && oldCourse.getTeacher().getId() == teacher.getId()) {
            oldCourse.setTeacher(null);
            courseDao.updateCourse(oldCourse);
        }
        
        // Assign teacher to new course's teacherId
        Course newCourse = findCourseByName(newSpecialty);
        if (newCourse != null) {
            // Check if new course already has a teacher assigned
            if (newCourse.getTeacher() != null && newCourse.getTeacher().getId() != teacher.getId()) {
                throw new RuntimeException("Course '" + newSpecialty + "' is already assigned to another teacher!");
            }
            newCourse.setTeacher(teacher);
            courseDao.updateCourse(newCourse);
        }
    }
}