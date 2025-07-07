package com.example.application.models;

import com.example.application.PublicEnums.Role;

public class Person {
    
    private String firstName;
    private String lastName;
    private String email;
    protected Role role;

    /**
     * @param firstName
     * @param lastName
     * @param email
     */
    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        setRole();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole() {
        this.role = Role.NoRole;
    }
}