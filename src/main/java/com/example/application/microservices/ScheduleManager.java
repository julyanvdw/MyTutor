package com.example.application.microservices;

import com.example.application.PublicEnums.Response;
import com.example.application.database.DatabaseController;
import com.example.application.models.Schedule;

/**
 * This class manages the tutoring schedule for each respective course
 */
public class ScheduleManager {

    // #region // !! EDITING A SCHEDULE

    /**
     * This method takes in a schedule object to replace the current schedule stored in the DB. 
     * This causes the schedule to effectlively be updated in the DB. 
     * @param oldSchedule
     * @param newSchedule
     * @return Response type
     */
    public static Response updateSchedule(Schedule oldSchedule, Schedule newSchedule) {

        // ****************************************************************
        if (DatabaseController.updateSchedule(oldSchedule, newSchedule)) {
            // ************************************************************
            return Response.SUCCESS;
        }
        return Response.UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! GETTING A SCHEDULE

    /**
     * This method returns a schedule object as stored in the DB. 
     * Population of this object occurs in the DB controller
     * @param courseCode
     * @param year
     * @return Scehdule object - fully populated
     */
    public static Schedule getSchedule(String courseCode, int year) {

        // **************************************************
        return DatabaseController.getSchedule(courseCode, year);
        // **************************************************

    }

    // #endregion

    // #region // !! ADDING / REMOVING A TUTOR FROM A SLOT

    /**
     * This method causes a tutor to be allocated to a tutoring session
     * @param tutoringSessionID
     * @param studentID
     * @return
     */
    public static Response tutorSignUp(int tutoringSessionID, String studentID) {

        // **************************************************
        if (DatabaseController.addTutorToSession(tutoringSessionID, studentID) == true) {
            return Response.SUCCESS;
        }
        // **************************************************

        return Response.UNSUCCESSFUL;
    }

    /**
     * This method causes a tutor to be deallocated from a tutoring session
     * @param tutoringSessionID
     * @param studentID
     * @return
     */
    public static Response tutorLeave(int tutoringSessionID, String studentID) {
        // **************************************************
        if (DatabaseController.removeTutorFromSession(tutoringSessionID, studentID) == true) {
            return Response.SUCCESS;
        }
        // **************************************************

        return Response.UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! CHECKING IN

    /**
     * This method allows a tutor to confirm their attendance to a specific tutoring session
     * @param tutoringSessionID
     * @param courseCode
     * @param studentID
     * @param date
     * @return
     */
    public static Response tutorCheckIn(int tutoringSessionID, String courseCode, String studentID, String date) {
        // **************************************************
        if (DatabaseController.tutorCheckIn(tutoringSessionID, courseCode, studentID, date) == true) {
            return Response.SUCCESS;
        }
        // **************************************************

        return Response.UNSUCCESSFUL;
    }

    /**
     * This method conveys wether or not a tutor has already signed in to a particular session on a particular day
     * @param tutoringSessionID
     * @param studentID
     * @param date
     * @return
     */
    public static boolean hasCheckedIn(int tutoringSessionID, String studentID, String date) {

        // ************************************************************************************
        return DatabaseController.hasTutorAlreadyCheckedIn(tutoringSessionID, studentID, date);
        // ************************************************************************************

    }

    // #endregion

    // #region // !! GETTING STATS

    /**
     * This method obtains the statistics (attendance count) for a specific tutoring session
     * @param tutoringSessionID
     * @return
     */
    public static int getTutoringSessionStatsFor(int tutoringSessionID) {

        // ***************************************************************
        return DatabaseController.getTutoringSessionStatsFor(tutoringSessionID);
        // ***************************************************************

    }

    // #endregion
}
