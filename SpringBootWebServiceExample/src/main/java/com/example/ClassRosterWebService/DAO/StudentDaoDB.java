package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class StudentDaoDB implements StudentDao {
    
    @Autowired
    JdbcTemplate jdbc;

    @Override
    public Student getStudentById(int id) {
        try {
            final String GET_STUDENT_BY_ID = "SELECT * FROM student WHERE id = ?";
            Student student = jdbc.queryForObject(GET_STUDENT_BY_ID, new StudentMapper(), id);
            
            // Get courses for this student
            if (student != null) {
                List<Course> courses = getCoursesForStudent(id);
                student.setCourses(courses);
            }
            
            return student;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public List<Student> getAllStudents() {
        final String GET_ALL_STUDENTS = "SELECT * FROM student ORDER BY lastName, firstName";
        List<Student> students = jdbc.query(GET_ALL_STUDENTS, new StudentMapper());
        
        // Get courses for each student
        for (Student student : students) {
            List<Course> courses = getCoursesForStudent(student.getId());
            student.setCourses(courses);
        }
        
        return students;
    }

    @Override
    @Transactional
    public Student addStudent(Student student) {
        final String INSERT_STUDENT = "INSERT INTO student(firstName, lastName) VALUES(?,?)";
        jdbc.update(INSERT_STUDENT,
                student.getFirstName(),
                student.getLastName());

        int newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        student.setId(newId);
        return student;
    }

    @Override
    public void updateStudent(Student student) {
        final String UPDATE_STUDENT = "UPDATE student SET firstName = ?, lastName = ? WHERE id = ?";
        jdbc.update(UPDATE_STUDENT,
                student.getFirstName(),
                student.getLastName(),
                student.getId());
    }

    @Override
    @Transactional
    public void deleteStudentById(int id) {
        // Check if student is enrolled in any courses
        if (isStudentEnrolledInAnyCourse(id)) {
            throw new RuntimeException("Cannot delete student. They are enrolled in one or more courses.");
        }
        
        final String DELETE_STUDENT = "DELETE FROM student WHERE id = ?";
        jdbc.update(DELETE_STUDENT, id);
    }

    @Override
    @Transactional
    public void enrollStudentInCourse(int studentId, int courseId) {
        // Check if already enrolled
        final String CHECK_ENROLLMENT = "SELECT COUNT(*) FROM course_student WHERE studentId = ? AND courseId = ?";
        int count = jdbc.queryForObject(CHECK_ENROLLMENT, Integer.class, studentId, courseId);
        
        if (count > 0) {
            throw new RuntimeException("Student is already enrolled in this course.");
        }
        
        final String ENROLL_STUDENT = "INSERT INTO course_student(studentId, courseId) VALUES(?,?)";
        jdbc.update(ENROLL_STUDENT, studentId, courseId);
    }

    @Override
    @Transactional
    public void unenrollStudentFromCourse(int studentId, int courseId) {
        final String UNENROLL_STUDENT = "DELETE FROM course_student WHERE studentId = ? AND courseId = ?";
        jdbc.update(UNENROLL_STUDENT, studentId, courseId);
    }

    @Override
    public List<Course> getCoursesForStudent(int studentId) {
        final String GET_COURSES_FOR_STUDENT = 
            "SELECT c.* FROM course c " +
            "JOIN course_student cs ON c.id = cs.courseId " +
            "WHERE cs.studentId = ? " +
            "ORDER BY c.name";
        
        return jdbc.query(GET_COURSES_FOR_STUDENT, new CourseDaoDB.CourseMapper(), studentId);
    }

    @Override
    public List<Student> getStudentsForCourse(int courseId) {
        final String GET_STUDENTS_FOR_COURSE = 
            "SELECT s.* FROM student s " +
            "JOIN course_student cs ON s.id = cs.studentId " +
            "WHERE cs.courseId = ? " +
            "ORDER BY s.lastName, s.firstName";
        
        return jdbc.query(GET_STUDENTS_FOR_COURSE, new StudentMapper(), courseId);
    }

    @Override
    public boolean isStudentEnrolledInAnyCourse(int studentId) {
        final String CHECK_ENROLLMENTS = "SELECT COUNT(*) FROM course_student WHERE studentId = ?";
        int count = jdbc.queryForObject(CHECK_ENROLLMENTS, Integer.class, studentId);
        return count > 0;
    }

    public static final class StudentMapper implements RowMapper<Student> {
        @Override
        public Student mapRow(ResultSet rs, int index) throws SQLException {
            Student student = new Student();
            student.setId(rs.getInt("id"));
            student.setFirstName(rs.getString("firstName"));
            student.setLastName(rs.getString("lastName"));
            return student;
        }
    }
}