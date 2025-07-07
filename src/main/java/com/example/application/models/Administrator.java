package com.example.application.models;

import com.example.application.PublicEnums.Role;

public class Administrator extends Person {

    private String employeeID;

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param employeeID
     */
    public Administrator(String firstName, String lastName, String email, String employeeID) {
        super(firstName, lastName, email);
        this.employeeID = employeeID;
        setRole();
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public void setRole() {
        this.role = Role.Admin;
    }
}