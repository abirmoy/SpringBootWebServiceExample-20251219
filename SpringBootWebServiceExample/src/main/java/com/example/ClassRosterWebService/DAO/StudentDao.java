package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Student;

import java.util.List;

public interface StudentDao {
    Student getStudentById(int id);
    Student getStudentByStudentId(String studentId);  // ADD THIS METHOD
    List<Student> getAllStudents();
    Student addStudent(Student student);
    void updateStudent(Student student);
    void deleteStudentById(int id);
    
    // Enrollment methods
    void enrollStudentInCourse(int studentId, int courseId);
    void unenrollStudentFromCourse(int studentId, int courseId);
    List<Course> getCoursesForStudent(int studentId);
    List<Student> getStudentsForCourse(int courseId);
    
    // Check if student is enrolled in any courses
    boolean isStudentEnrolledInAnyCourse(int studentId);
    
    // ADD THIS METHOD
    boolean studentIdExists(String studentId);
}