package com.example.application.models;

import java.util.ArrayList;

public class Schedule {

    // Attributes
    private int scheduleID;
    private Course course;
    private ArrayList<TutoringSession> slots;

    public Schedule() {
        slots = new ArrayList<>();
    }

    // Constructor
    public Schedule(Course course) {
        this.course = course;
        this.slots = new ArrayList<>();
    }

    // Methods for adding and removing tutoring slots
    public void addTutoringSession(TutoringSession slot) {
        slots.add(slot);
    }

    public void removeTutoringSession(TutoringSession slot) {
        slots.remove(slot);
    }

    // Getters
    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public int getScheduleID() {
        return this.scheduleID;
    }

    public ArrayList<TutoringSession> getTutoringSessions() {
        return slots;
    }

    public ArrayList<TutoringSession> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<TutoringSession> slots) {
        this.slots = slots;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}