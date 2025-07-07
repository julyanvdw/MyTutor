package com.example.application.models;

import java.util.List;

import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Role;

public class Tutor extends Student {
    
    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param studentID
     * @param qualificationLevel
     * @param applicationStatus
     * @param completedCourses
     */
    public Tutor(String firstName, String lastName, String email, String studentID, QualificationLevel qualificationLevel, ApplicationStatus applicationStatus, List<CompletedCourse> completedCourses) {
        super(firstName, lastName, email, studentID, qualificationLevel, applicationStatus, completedCourses);
        setRole();
    }

    public Tutor(String firstName, String lastName, String email, String userID, String year) {
        super(firstName, lastName, email, userID, year);
        setRole();
    }


    public void setRole() {
        this.role = Role.Tutor;
    }
}