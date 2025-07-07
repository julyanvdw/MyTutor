package com.example.application.models;

import com.example.application.PublicEnums.Role;

public class CourseConvenor extends Lecturer {

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param employeeID
     * @param department
     * @param faculty
     */
    public CourseConvenor(String firstName, String lastName, String email, String employeeID, String department, String faculty) {
        super(firstName, lastName, email, employeeID, department, faculty);
        setRole();
    }

    public CourseConvenor(String firstName, String lastName, String email, String userID, String year) {
        super(firstName, lastName, email, userID, year);
        setRole();
    }

    public void setRole() {
        this.role = Role.CourseConvenor;
    }
}