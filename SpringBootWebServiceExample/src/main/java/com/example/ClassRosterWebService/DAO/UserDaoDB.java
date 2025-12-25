package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.User;
import com.example.ClassRosterWebService.Entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDaoDB implements UserDao {
    
    @Autowired
    JdbcTemplate jdbc;

    @Override
    public User getUserById(int id) {
        try {
            final String GET_USER_BY_ID = "SELECT * FROM `user` WHERE id = ?";
            User user = jdbc.queryForObject(GET_USER_BY_ID, new UserMapper(), id);
            
            if (user != null) {
                user.setRoles(getRolesForUserId(id));
            }
            
            return user;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            final String GET_USER_BY_USERNAME = "SELECT * FROM `user` WHERE username = ?";
            User user = jdbc.queryForObject(GET_USER_BY_USERNAME, new UserMapper(), username);
            
            if (user != null) {
                user.setRoles(getRolesForUserId(user.getId()));
            }
            
            return user;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public User getUserByStudentId(int studentId) {
        try {
            final String GET_USER_BY_STUDENT_ID = "SELECT * FROM `user` WHERE student_id = ?";
            User user = jdbc.queryForObject(GET_USER_BY_STUDENT_ID, new UserMapper(), studentId);
            
            if (user != null) {
                user.setRoles(getRolesForUserId(user.getId()));
            }
            
            return user;
        } catch (DataAccessException ex) {
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        final String GET_ALL_USERS = "SELECT * FROM `user` ORDER BY username";
        List<User> users = jdbc.query(GET_ALL_USERS, new UserMapper());
        
        for (User user : users) {
            user.setRoles(getRolesForUserId(user.getId()));
        }
        
        return users;
    }

    @Override
    public User createUser(User user) {
        final String INSERT_USER = "INSERT INTO `user` (username, password, enabled, student_id) VALUES (?, ?, ?, ?)";
        jdbc.update(INSERT_USER, 
                   user.getUsername(), 
                   user.getPassword(), 
                   user.isEnabled(),
                   user.getStudentId());
        
        int newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        user.setId(newId);
        return user;
    }

    @Override
    public void updateUser(User user) {
        final String UPDATE_USER = "UPDATE `user` SET username = ?, password = ?, enabled = ?, student_id = ? WHERE id = ?";
        jdbc.update(UPDATE_USER, 
                   user.getUsername(), 
                   user.getPassword(), 
                   user.isEnabled(), 
                   user.getStudentId(),
                   user.getId());
    }

    @Override
    public void deleteUser(int id) {
        final String DELETE_USER = "DELETE FROM `user` WHERE id = ?";
        jdbc.update(DELETE_USER, id);
    }

    @Override
    public void addRoleToUser(int userId, int roleId) {
        final String ADD_ROLE = "INSERT IGNORE INTO user_role (user_id, role_id) VALUES (?, ?)";
        jdbc.update(ADD_ROLE, userId, roleId);
    }

    @Override
    public void removeRoleFromUser(int userId, int roleId) {
        final String REMOVE_ROLE = "DELETE FROM user_role WHERE user_id = ? AND role_id = ?";
        jdbc.update(REMOVE_ROLE, userId, roleId);
    }

    @Override
    public List<String> getRolesForUser(String username) {
        final String GET_ROLES = 
            "SELECT r.name FROM `role` r " +
            "JOIN user_role ur ON r.id = ur.role_id " +
            "JOIN `user` u ON ur.user_id = u.id " +
            "WHERE u.username = ?";
        
        return jdbc.query(GET_ROLES, 
                         (rs, rowNum) -> rs.getString("name"), 
                         username);
    }

    @Override
    public boolean userExists(String username) {
        final String CHECK_USER = "SELECT COUNT(*) FROM `user` WHERE username = ?";
        int count = jdbc.queryForObject(CHECK_USER, Integer.class, username);
        return count > 0;
    }

    private List<Role> getRolesForUserId(int userId) {
        final String GET_ROLES_FOR_USER = 
            "SELECT r.* FROM `role` r " +
            "JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = ?";
        
        return jdbc.query(GET_ROLES_FOR_USER, new RoleMapper(), userId);
    }

    public static final class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int index) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEnabled(rs.getBoolean("enabled"));
            
            // FIXED LINE: Properly map student_id column
            user.setStudentId(rs.getObject("student_id") != null ? rs.getInt("student_id") : null);
            
            return user;
        }
    }

    public static final class RoleMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int index) throws SQLException {
            Role role = new Role();
            role.setId(rs.getInt("id"));
            role.setName(rs.getString("name"));
            return role;
        }
    }
}