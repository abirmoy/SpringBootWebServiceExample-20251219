package com.example.ClassRosterWebService.DAO;

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
public class TeacherDaoDB implements TeacherDao {

    @Autowired
    JdbcTemplate jdbc;


    @Override
    public Teacher getTeacherById(int id) {
        try {
            final String GET_TEACHER_BY_ID = "SELECT * FROM teacher WHERE id = ?";
            return jdbc.queryForObject(GET_TEACHER_BY_ID, new TeacherMapper(), id);
        } catch(DataAccessException ex) {
            return null;
        }    }


    @Override
    public List<Teacher> getAllTeachers() {
        Integer x = 2, y = 9;
        String val1 = x.toString();
        String val2 = y.toString();
        final String GET_ALL_TEACHERS = "SELECT * FROM teacher";
        return jdbc.query(GET_ALL_TEACHERS, new TeacherMapper());
    }

    public List<Teacher> getAllMIDAndTopicTeachers() {
        final String GET_ALL_TEACHERS = "SELECT id, specialty FROM " +
                "teacher where specialty" +
                "like 'M%";
        return jdbc.query(GET_ALL_TEACHERS, new MTopicsIDAndTopicTeacherMapper());
    }

    @Override
    public List<String> getTeacherByCourse() {
        // Updated to use both relationships
        final String GET_TEACHER_BY_COURSE = 
            "SELECT DISTINCT t.firstName, t.lastName, c.name " +
            "FROM teacher t " +
            "LEFT JOIN course c ON (t.id = c.teacherId OR t.specialty = c.name) " +
            "WHERE c.name IS NOT NULL " +
            "ORDER BY c.name, t.lastName";
        
        return jdbc.query(GET_TEACHER_BY_COURSE, new TeacherByCourseMapper());
    }

@Override
@Transactional
    public Teacher addTeacher(Teacher teacher) {
        final String INSERT_TEACHER = "INSERT INTO teacher(firstName, lastName, specialty) " +
                "VALUES(?,?,?)";
        jdbc.update(INSERT_TEACHER,
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getSpecialty());

        int newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        teacher.setId(newId);
        return teacher;
    }


    @Override
    public void updateTeacher(Teacher teacher) {
        final String UPDATE_TEACHER = "UPDATE teacher SET firstName = ?, lastName = ?, " +
            "specialty = ? WHERE id = ?";
        
        jdbc.update(UPDATE_TEACHER,
            teacher.getFirstName(),
            teacher.getLastName(),
            teacher.getSpecialty(),
            teacher.getId());
    }


    @Override
    @Transactional
    public void deleteTeacherById(int id) {
        // First, check if teacher is assigned to any courses
        final String CHECK_COURSE_ASSIGNMENT = 
            "SELECT COUNT(*) FROM course WHERE teacherId = ?";
        
        int courseCount = jdbc.queryForObject(CHECK_COURSE_ASSIGNMENT, Integer.class, id);
        
        if (courseCount > 0) {
            throw new RuntimeException("Teacher is assigned to " + courseCount + " course(s)");
        }
        
        // Remove teacher from courses (set teacherId to NULL)
        final String REMOVE_FROM_COURSES = "UPDATE course SET teacherId = NULL WHERE teacherId = ?";
        jdbc.update(REMOVE_FROM_COURSES, id);
        
        // Delete the teacher
        final String DELETE_TEACHER = "DELETE FROM teacher WHERE id = ?";
        jdbc.update(DELETE_TEACHER, id);
    }

    /*
    Simple RowMapper to convert logic of a Join to a String. We are only
    displaying the results to screen in this one
     */
    public static final class TeacherByCourseMapper implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int index) throws SQLException {
            // ArrayList <String> TeacherByCourseList = new ArrayList<>();
            String teacherByCourse = new String();
            teacherByCourse = rs.getString(1);
            teacherByCourse += "  ";
            teacherByCourse += rs.getString(2);
            teacherByCourse += " teaches ";
            teacherByCourse += rs.getString(3);
            //TeacherByCourseList.add(teacherByCourse);
            return teacherByCourse;
        }
    }

    /*
    RowMapper that converts the Teacher records straight to a Teacher object
    (in an ArrayList) in order to allow us to do further processing.
     */
        public static final class TeacherMapper implements RowMapper<Teacher> {
        @Override
        public Teacher mapRow(ResultSet rs, int index) throws SQLException {
            Teacher teacher = new Teacher();
            teacher.setId(rs.getInt("id"));
            teacher.setFirstName(rs.getString("firstName"));
            teacher.setLastName(rs.getString("lastName"));
            teacher.setSpecialty(rs.getString("specialty"));

            return teacher;
        }
    }

    public static final class MTopicsIDAndTopicTeacherMapper implements RowMapper<Teacher> {
        @Override
        public Teacher mapRow(ResultSet rs, int index) throws SQLException {
            Teacher teacher = new Teacher();
            teacher.setId(rs.getInt("id"));
            teacher.setSpecialty(rs.getString("specialty"));

            return teacher;
        }
    }
}

