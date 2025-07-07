package com.example.application.models;

import java.util.ArrayList;
import java.util.List;

import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Role;

public class Student extends Person {

    private String studentID;
    private QualificationLevel qualificationLevel;
    private ApplicationStatus applicationStatus;
    private List<CompletedCourse> completedCourses;
    private String year;

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param studentID
     * @param qualificationLevel
     * @param applicationStatus
     * @param completedCourses
     */
    public Student(String firstName, String lastName, String email, String studentID, QualificationLevel qualificationLevel, ApplicationStatus applicationStatus, List<CompletedCourse> completedCourses) {
        super(firstName, lastName, email);
        this.studentID = studentID;
        this.qualificationLevel = qualificationLevel;
        this.applicationStatus = applicationStatus;
        this.completedCourses = completedCourses != null ? completedCourses : new ArrayList<>();
        setRole();
    }

    public Student(String firstName, String lastName, String email, String userID, String year) {
        super(firstName, lastName, email);
        this.studentID = userID;
        this.year = year;
        setRole();
    }

    public String getStudentID() {
        return studentID;
    }

    public QualificationLevel getQualificationLevel() {
        return qualificationLevel;
    }

    public void setQualificationLevel(QualificationLevel qualificationLevel) {
        this.qualificationLevel = qualificationLevel;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public List<CompletedCourse> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<CompletedCourse> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setRole() {
        this.role = Role.Student;
    }
}