package com.example.application.microservices;

import java.util.List;

import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;
import com.example.application.database.DatabaseController;
import com.example.application.models.TA;
import com.example.application.models.Tutor;
import com.example.application.models.Student;

// import java.util.ArrayList;
// import java.util.List;

// import com.example.application.models.Course;
// import com.example.application.models.Student;

/**
 * This class manages the Student and all the related funcationality as
 * indicated by their flow
 * 
 * The class has support for
 * 1) Fetching the courses a Student is allocated to
 */
public class StudentManager {

    // #region // !! AUX METHODS

    /**
     * This method returns the role related to a certian student for a certain course for a certain year
     * @param courseCode
     * @param year
     * @param studentID
     * @return
     */
    public static Role isTutorOrTA(String courseCode, String year, String studentID) {

        // *****************************************************************
        return DatabaseController.isTutorOrTA(courseCode, year, studentID);
        // *****************************************************************

    }

    /**
     * This method returns the motivation a student made for their application
     * @param studentID
     * @return
     */
    public static String getMotivation(String studentID) {

        // *******************************************************
        return DatabaseController.getMotivation(studentID);
        // *******************************************************

    }

    /**
     * This method returns the grade a student has for a certain course
     * @param courseCode
     * @param studentID
     * @return
     */
    public static String getGradeFor(String courseCode, String studentID) {

        // ****************************************************************
        return DatabaseController.getGradeFor(courseCode, studentID);
        // ****************************************************************

    }

    // #endregion

    // #region // !! GETTING STUDENTS

    /**
     * This method returns tutors of a certain course for a certain year
     * @param courseCode
     * @param year
     * @return
     */
    public static List<Tutor> getActiveTutors(String courseCode, String year) {

        // **********************************************************
        return DatabaseController.getActiveTutors(courseCode, year);
        // **********************************************************

    }

    /**
     * This method returns TAs for a given course for a given year
     * @param courseCode
     * @param year
     * @return
     */
    public static List<TA> getActiveTAs(String courseCode, String year) {

        // **********************************************************
        return DatabaseController.getActiveTAs(courseCode, year);
        // **********************************************************

    }

    /**
     * This method returns all the applicants who have completed a given course
     * @param courseCode
     * @return
     */
    public static List<Student> getPendingApplicants(String courseCode) {

        // ********************************************************
        return DatabaseController.getPendingApplicants(courseCode);
        // ********************************************************

    }

    // #endregion

    // #region // !! RESETTING THE SYSTEM

    /**
     * This method resets a system such that it can be used for a new year
     * @return
     */
    public static Response resetSystem() {

        // **************************************************
        if (DatabaseController.resetSystem()) {
            return Response.SUCCESS;
        }
        // **************************************************

        return Response.UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! GETTING STATS

    /**
     * This method returns the statistics in the form of attendance count for a particular course for a particular student
     * @param courseCode
     * @param studentID
     * @return
     */
    public static int getTutorStatsFor(String courseCode, String studentID) {

        // ****************************************************************
        return DatabaseController.getTutorStatsFor(courseCode, studentID);
        // ****************************************************************

    }

    // #endregion
}