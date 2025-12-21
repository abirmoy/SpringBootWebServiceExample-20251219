package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.CourseDao;
import com.example.ClassRosterWebService.DAO.TeacherDao;
import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CourseController {
    @Autowired
    TeacherDao teacherDao;

 //   @Autowired
   // StudentDao studentDao;

    @Autowired
    CourseDao courseDao;

    @GetMapping("courses")
    public String displayCourses(Model model) {
        List<Course> courses = courseDao.getAllCourses();
        List<Teacher> teachers = teacherDao.getAllTeachers();
        //List<Student> students = studentDao.getAllStudents();
        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
     //   model.addAttribute("students", students);
        return "courses";
    }

}
