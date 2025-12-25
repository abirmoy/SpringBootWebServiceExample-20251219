package com.example.ClassRosterWebService.Entity;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private int id;
    private String studentId;  // ADD THIS FIELD
    private String firstName;
    private String lastName;
    private List<Course> courses; // Courses student is enrolled in

    public Student() {
        this.courses = new ArrayList<>();
    }

    public Student(int id, String studentId, String firstName, String lastName) {
        this.id = id;
        this.studentId = studentId;  // UPDATE CONSTRUCTOR
        this.firstName = firstName;
        this.lastName = lastName;
        this.courses = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // ADD GETTER/SETTER FOR STUDENT ID
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
    
    public void addCourse(Course course) {
        if (courses == null) {
            courses = new ArrayList<>();
        }
        courses.add(course);
    }
    
    public void removeCourse(Course course) {
        if (courses != null) {
            courses.remove(course);
        }
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +  // ADD TO STRING
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", courses=" + (courses != null ? courses.size() : 0) +
                '}';
    }
}