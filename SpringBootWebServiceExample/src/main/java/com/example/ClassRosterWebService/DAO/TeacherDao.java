package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.Teacher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeacherDao {
    Teacher getTeacherById(int id);

    List<Teacher> getAllTeachers();

    @Transactional
    Teacher addTeacher(Teacher teacher);

    void updateTeacher(Teacher teacher);

    void deleteTeacherById(int id);
    public List<String> getTeacherByCourse();
}
