package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Teacher;

import java.util.List;

public interface CourseDao {
    Course getCourseById(int id);

    List<Course> getAllCourses();

    Course addCourse(Course course);

    void updateCourse(Course course);

    void deleteCourseById(int id);

    List<Course> getCoursesForTeacher(Teacher teacher);

 //   List<Course> getCoursesForStudent(Student student);
}
