package com.example.ClassRosterWebService.DAO;

import com.example.ClassRosterWebService.Entity.User;
import java.util.List;

public interface UserDao {
    User getUserById(int id);
    User getUserByUsername(String username);
    User getUserByStudentId(int studentId);  // ADD THIS METHOD
    List<User> getAllUsers();
    User createUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    void addRoleToUser(int userId, int roleId);
    void removeRoleFromUser(int userId, int roleId);
    List<String> getRolesForUser(String username);
    boolean userExists(String username);
}