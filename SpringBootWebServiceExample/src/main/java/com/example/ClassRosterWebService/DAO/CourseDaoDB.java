package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.Course;
import com.example.ClassRosterWebService.Entity.Teacher;
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
public class CourseDaoDB implements CourseDao {
    @Autowired
    JdbcTemplate jdbc;

    @Override
    public Course getCourseById(int id) {
        try {
            final String GET_COURSE_BY_ID = "SELECT * FROM course WHERE id = ?";
            Course course = jdbc.queryForObject(GET_COURSE_BY_ID, new CourseMapper(), id);
            
            // Associate teacher if exists
            if (course != null) {
                Teacher teacher = getTeacherForCourse(course.getId());
                course.setTeacher(teacher);
                
                // Check if any teacher has this course as specialty
                boolean hasTeachersViaSpecialty = checkIfCourseHasTeachersViaSpecialty(course.getName());
                course.setHasTeachersViaSpecialty(hasTeachersViaSpecialty);
            }
            
            return course;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public List<Course> getAllCourses() {
        final String SELECT_ALL_COURSES = "SELECT * FROM course";
        List<Course> courses = jdbc.query(SELECT_ALL_COURSES, new CourseMapper());
        associateTeachers(courses);
        return courses;
    }

    @Override
    @Transactional
    public Course addCourse(Course course) {
        final String INSERT_COURSE = "INSERT INTO course(name, description, teacherId) VALUES(?,?,?)";
        jdbc.update(INSERT_COURSE,
                course.getName(),
                course.getDescription(),
                course.getTeacher() != null ? course.getTeacher().getId() : null);

        int newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        course.setId(newId);
        return course;
    }

    @Override
    @Transactional
    public void updateCourse(Course course) {
        // Get old course info before update
        Course oldCourse = getCourseById(course.getId());
        String oldName = oldCourse != null ? oldCourse.getName() : null;
        String newName = course.getName();
        
        // Update course
        final String UPDATE_COURSE = "UPDATE course SET name = ?, description = ?, teacherId = ? WHERE id = ?";
        jdbc.update(UPDATE_COURSE,
                newName,
                course.getDescription(),
                course.getTeacher() != null ? course.getTeacher().getId() : null,
                course.getId());
        
        // If course name changed, update teachers' specialties
        if (oldName != null && !oldName.equals(newName)) {
            final String UPDATE_TEACHER_SPECIALTIES = 
                "UPDATE teacher SET specialty = ? WHERE specialty = ?";
            jdbc.update(UPDATE_TEACHER_SPECIALTIES, newName, oldName);
        }
    }

    @Override
    @Transactional
    public void deleteCourseById(int id) {
        // Get the course first
        Course course = getCourseById(id);
        if (course == null) {
            return; // Course doesn't exist
        }
        
        String courseName = course.getName();
        
        // Check if course is assigned to any teacher via teacherId
        final String CHECK_TEACHER_ASSIGNMENT = 
            "SELECT COUNT(*) FROM course WHERE id = ? AND teacherId IS NOT NULL";
        
        // Check if any teacher has this course as their specialty
        final String CHECK_SPECIALTY_REFERENCE = 
            "SELECT COUNT(*) FROM teacher WHERE specialty = ?";
        
        int teacherAssignedCount = jdbc.queryForObject(CHECK_TEACHER_ASSIGNMENT, Integer.class, id);
        int specialtyReferenceCount = jdbc.queryForObject(CHECK_SPECIALTY_REFERENCE, Integer.class, courseName);
        
        // Build error message if course is in use via teacherId
        if (teacherAssignedCount > 0) {
            throw new RuntimeException("Cannot delete course. It has a teacher assigned via teacherId.");
        }
        
        // If teachers have this course as specialty, update their specialties to "Unassigned"
        if (specialtyReferenceCount > 0) {
            final String UPDATE_TEACHER_SPECIALTIES = 
                "UPDATE teacher SET specialty = 'Unassigned' WHERE specialty = ?";
            jdbc.update(UPDATE_TEACHER_SPECIALTIES, courseName);
        }
        
        // Delete the course
        final String DELETE_COURSE = "DELETE FROM course WHERE id = ?";
        jdbc.update(DELETE_COURSE, id);
    }

    @Override
    public List<Course> getCoursesForTeacher(Teacher teacher) {
        final String SELECT_COURSES_FOR_TEACHER = "SELECT * FROM course WHERE teacherId = ?";
        List<Course> courses = jdbc.query(SELECT_COURSES_FOR_TEACHER, new CourseMapper(), teacher.getId());
        associateTeachers(courses);
        return courses;
    }

    // Helper method to associate teachers with courses
    private void associateTeachers(List<Course> courses) {
        for (Course course : courses) {
            Teacher teacher = getTeacherForCourse(course.getId());
            course.setTeacher(teacher);
            
            // Check if any teacher has this course as specialty
            boolean hasTeachersViaSpecialty = checkIfCourseHasTeachersViaSpecialty(course.getName());
            course.setHasTeachersViaSpecialty(hasTeachersViaSpecialty);
        }
    }

    private Teacher getTeacherForCourse(int courseId) {
        final String SELECT_TEACHER_FOR_COURSE = 
            "SELECT t.* FROM teacher t " +
            "JOIN course c ON t.id = c.teacherId " +
            "WHERE c.id = ?";
        
        try {
            return jdbc.queryForObject(SELECT_TEACHER_FOR_COURSE, new TeacherDaoDB.TeacherMapper(), courseId);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    
    private boolean checkIfCourseHasTeachersViaSpecialty(String courseName) {
        final String CHECK_SPECIALTY = "SELECT COUNT(*) FROM teacher WHERE specialty = ?";
        int count = jdbc.queryForObject(CHECK_SPECIALTY, Integer.class, courseName);
        return count > 0;
    }
    
    // Add this method to CourseDao interface
    public boolean isCourseInUse(int courseId) {
        Course course = getCourseById(courseId);
        if (course == null) return false;
        
        String courseName = course.getName();
        
        final String CHECK_TEACHER_ASSIGNMENT = 
            "SELECT COUNT(*) FROM course WHERE id = ? AND teacherId IS NOT NULL";
        
        final String CHECK_SPECIALTY_REFERENCE = 
            "SELECT COUNT(*) FROM teacher WHERE specialty = ?";
        
        int teacherAssignedCount = jdbc.queryForObject(CHECK_TEACHER_ASSIGNMENT, Integer.class, courseId);
        int specialtyReferenceCount = jdbc.queryForObject(CHECK_SPECIALTY_REFERENCE, Integer.class, courseName);
        
        return (teacherAssignedCount > 0) || (specialtyReferenceCount > 0);
    }

    public static final class CourseMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int index) throws SQLException {
            Course course = new Course();
            course.setId(rs.getInt("id"));
            course.setName(rs.getString("name"));
            course.setDescription(rs.getString("description"));
            return course;
        }
    }
}