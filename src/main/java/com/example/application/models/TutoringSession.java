package com.example.application.models;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class TutoringSession {

    // Attributes
    private int tutoringSessionID;
    private double start;
    private double end;
    private String day;
    private String location;
    private String whatsappLink;
    private int tutoringCapacity;
    private List<Tutor> signedUpTutors;

    /**
     * @param start
     * @param end
     * @param day
     * @param location
     * @param whatsappLink
     * @param tutoringCapacity
     */
    public TutoringSession(double start, double end, String day, String location, String whatsappLink,
            int tutoringCapacity) {
        this.start = start;
        this.end = end;
        this.day = day;
        this.location = location;
        this.whatsappLink = whatsappLink;
        this.tutoringCapacity = tutoringCapacity;
    }

    /**
     * @param start
     * @param end
     * @param day
     * @param location
     * @param whatsappLink
     * @param tutoringCapacity
     */
    public TutoringSession(LocalTime start, LocalTime end, String day, String location, String whatsappLink,
            int tutoringCapacity) {
        this.start = convertLocalTimeToDouble(start);
        this.end = convertLocalTimeToDouble(end);
        this.day = day;
        this.location = location;
        this.whatsappLink = whatsappLink;
        this.tutoringCapacity = tutoringCapacity;
    }

    public TutoringSession() {

    }

    // Getters
    public void setSessionID(int sessionID) {
        this.tutoringSessionID = sessionID;
    }

    public int getSessionID() {
        return this.tutoringSessionID;
    }

    public double getStartTimeAsDouble() {
        return start;
    }

    public double getEndTimeAsDouble() {
        return end;
    }

    public LocalTime getStartTimeAsLocalTime() {
        return convertDoubleToLocalTime(start);
    }

    public LocalTime getEndTimeAsLocalTime() {
        return convertDoubleToLocalTime(end);
    }

    public String getDay() {
        return day;
    }

    public String getWhatsappLink() {
        return whatsappLink;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setWhatsappLink(String whatsappLink) {
        this.whatsappLink = whatsappLink;
    }

    public int getTutoringCapacity() {
        return tutoringCapacity;
    }

    public void setTutoringCapacity(int tutoringCapacity) {
        this.tutoringCapacity = tutoringCapacity;
    }

    public List<Tutor> getSignedUpTutors() {
        return signedUpTutors;
    }

    public void setSignedUpTutors(List<Tutor> assignedTutors) {
        this.signedUpTutors = assignedTutors;
    }

    public void addTutor(Tutor t) {
        signedUpTutors.add(t);
    }

    public void removeTutor(Tutor t) {
        signedUpTutors.remove(t);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Helper method to convert LocalTime to double
    private double convertLocalTimeToDouble(LocalTime localTime) {
        int hours = localTime.getHour();
        int minutes = localTime.getMinute();
        return hours + (minutes / 60.0);
    }

    // Helper method to convert double to LocalTime
    private LocalTime convertDoubleToLocalTime(double doubleTime) {
        int hours = (int) doubleTime;
        int minutes = (int) ((doubleTime - hours) * 60);
        return LocalTime.of(hours, minutes);
    }

    @Override
    public String toString() {
        return "" + tutoringCapacity;
    }

    // COMPARISON METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TutoringSession that = (TutoringSession) o;
        return tutoringSessionID == that.tutoringSessionID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tutoringSessionID);
    }
}