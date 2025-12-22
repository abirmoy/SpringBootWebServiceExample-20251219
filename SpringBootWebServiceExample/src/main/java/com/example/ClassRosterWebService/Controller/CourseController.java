package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.DAO.TeacherDao;
import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Teacher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class CourseController {
    @Autowired
    TeacherDao teacherDao;

    @Autowired
    CourseDao courseDao;

    @GetMapping("courses")
    public String displayCourses(Model model) {
        List<Course> courses = courseDao.getAllCourses();
        List<Teacher> teachers = teacherDao.getAllTeachers();
        
        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
        return "courses";
    }

    @GetMapping("courseDetail")
    public String courseDetail(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
        Course course = courseDao.getCourseById(id);
        List<Teacher> teachers = teacherDao.getAllTeachers();
        
        // Check if course is in use (for warning message)
        boolean courseInUse = false;
        if (course != null) {
            // This requires adding isCourseInUse method to CourseDao interface
            // For now, we'll check if course has teacher assigned
            courseInUse = (course.getTeacher() != null);
        }
        
        model.addAttribute("course", course);
        model.addAttribute("teachers", teachers);
        model.addAttribute("courseInUse", courseInUse);
        
        return "courseDetail";
    }

    @PostMapping("addCourse")
    public String addCourse(HttpServletRequest request) {
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String teacherIdStr = request.getParameter("teacherId");
        
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        
        if (teacherIdStr != null && !teacherIdStr.isEmpty()) {
            int teacherId = Integer.parseInt(teacherIdStr);
            Teacher teacher = teacherDao.getTeacherById(teacherId);
            course.setTeacher(teacher);
        }
        
        courseDao.addCourse(course);
        return "redirect:/courses";
    }

    @PostMapping("editCourse")
    public String editCourse(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String teacherIdStr = request.getParameter("teacherId");
        
        Course course = courseDao.getCourseById(id);
        if (course == null) {
            return "redirect:/courses";
        }
        
        course.setName(name);
        course.setDescription(description);
        
        if (teacherIdStr != null && !teacherIdStr.isEmpty()) {
            int teacherId = Integer.parseInt(teacherIdStr);
            Teacher teacher = teacherDao.getTeacherById(teacherId);
            course.setTeacher(teacher);
        } else {
            course.setTeacher(null);
        }
        
        courseDao.updateCourse(course);
        return "redirect:/courses";
    }

    @GetMapping("deleteCourse")
    public String deleteCourse(HttpServletRequest request, Model model) {
        int id = Integer.parseInt(request.getParameter("id"));
        
        try {
            // Try to delete - will throw exception if course is in use
            courseDao.deleteCourseById(id);
            return "redirect:/courses";
        } catch (RuntimeException e) {
            // Course is in use - show error message
            List<Course> courses = courseDao.getAllCourses();
            List<Teacher> teachers = teacherDao.getAllTeachers();
            
            model.addAttribute("courses", courses);
            model.addAttribute("teachers", teachers);
            model.addAttribute("errorMessage", e.getMessage());
            
            return "courses";
        }
    }
}