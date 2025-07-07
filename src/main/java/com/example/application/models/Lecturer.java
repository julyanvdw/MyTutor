package com.example.application.models;

import com.example.application.PublicEnums.Role;

public class Lecturer extends Person {

    private String employeeID;
    private String department;
    private String faculty;
    private String year;

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param employeeID
     * @param department
     * @param faculty
     */
    public Lecturer(String firstName, String lastName, String email, String employeeID, String department, String faculty) {
        super(firstName, lastName, email);
        this.employeeID = employeeID;
        this.department = department;
        this.faculty = faculty;
        setRole();
    }

    public Lecturer(String firstName, String lastName, String email, String userID, String year) {
        super(firstName, lastName, email);
        this.employeeID = userID;
        this.year = year;
        setRole();
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    
    public void setRole() {
        role = Role.Lecturer;
    }
}