package com.example.application.models;

/**
* The CompletedCourse class represents a completed course with a course code and grade.
 */
public class CompletedCourse {
    
    public String courseCode;
    public double grade;
    public int year;

    /**
     * @param courseCode
     * @param grade
     * @param year
     */
    public CompletedCourse(String courseCode, double grade, int year) {
        this.courseCode = courseCode;
        this.grade = grade;
        this.year = year;
    }

    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public double getGrade() {
        return grade;
    }
    
    public void setGrade(double grade) {
        this.grade = grade;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return courseCode + ": " + grade;
    }
}