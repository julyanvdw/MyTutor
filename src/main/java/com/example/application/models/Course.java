package com.example.application.models;

public class Course {
    
    private String courseCode;
    private String name;
    private int taCapacity;
    private int tutorCapacity;

    private int year; //for Tutors, TAs, AccessibleCourses

    private Schedule schedule;

    /**
     * @param courseCode
     * @param name
     * @param tutorCapacity
     * @param taCapacity 
     */
    public Course(String courseCode, String name, int tutorCapacity, int taCapacity) {
        this.courseCode = courseCode;
        this.name = name;
        this.taCapacity = taCapacity;
        this.tutorCapacity = tutorCapacity;
        this.schedule = new Schedule();
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setName(String title) {
        this.name = title;
    }

    public int getTutorCapacity() {
        return tutorCapacity;
    }

    public void setTutorCapacity(int tutorCapacity) {
        this.tutorCapacity = tutorCapacity;
    }

    public int getTaCapacity() {
        return taCapacity;
    }

    public void setTaCapacity(int taCapacity) {
        this.taCapacity = taCapacity;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}