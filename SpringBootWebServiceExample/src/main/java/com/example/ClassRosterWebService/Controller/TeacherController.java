package com.example.ClassRosterWebService.Controller;

import com.example.ClassRosterWebService.DAO.TeacherDao;
import com.example.ClassRosterWebService.Entity.Teacher;
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

    @GetMapping("teachers")
    public String displayTeachers(Model model) {
        List<Teacher> teachers = teacherDao.getAllTeachers();
        List<String> tbc = teacherDao.getTeacherByCourse();
        for (String s : tbc)
        {
            System.out.println(s);
        }

        /*
         tbc (teacher by course) is then transferred to the teachercourse HTML attribute on the teachers
         page (see GetMapping tag). tbc is only an ArrayList of Strings, not objects.

         teachers ArrayList was mapped from the Teachers table to teacher objects and is now
         written to the HTML attribute teachers
         */
        model.addAttribute("teachercourse", tbc);
        model.addAttribute("teachers", teachers);
        return "teachers";
    }

    @GetMapping("deleteTeacher")
    public String deleteTeacher(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        teacherDao.deleteTeacherById(id);

        return "redirect:/teachers";
    }

    @PostMapping("addTeacher")
    public String addTeacher(HttpServletRequest request) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String specialityIn = request.getParameter("specialty");

        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSpecialty(specialityIn);

        teacherDao.addTeacher(teacher);
        if (firstName.length() > 3) {
            return "redirect:/teachers";
        }else {
            return "redirect:/courses";
        }
    }


}
