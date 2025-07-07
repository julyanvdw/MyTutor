package com.example.application.microservices;

import java.util.Set;

import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;
import com.example.application.database.DatabaseController;

import com.example.application.models.Student;

/**
 * This class manages applications made by users to become TAs or Tutors
 */
public class ApplyManager {

    // #region // !! MAKING AN APPLICATION
    /**
     * This method first validates the application against the user profile
     * 
     * @param student
     * @param motivationText
     * @return
     */
    public static Response apply(String email, String studentID, String motivationText) {

        // *****************************************************
        Student student = DatabaseController.getStudent(email);
        // *****************************************************

        Response message = validateApplication(student);

        if (message == Response.SUCCESS) {
            // *******************************************************************************************
            boolean applicationSuccess = DatabaseController.submitApplication(studentID, motivationText);
            // *******************************************************************************************

            if (applicationSuccess) {

                // **********************************************************************************
                boolean statusChangeSuccess = DatabaseController.updateApplicationStatus(studentID);
                // **********************************************************************************

                if (statusChangeSuccess) {
                    return Response.SUCCESS;
                }
            }

            return Response.APPLICATION_UNSUCCESSFUL;
        }

        return message;
    }

    /**
     * This method validates an application made by a particular user based on 
     * the qualification level of the user
     * 
     * @param student
     * @return
     */
    public static Response validateApplication(Student student) {

        if (student.getQualificationLevel() == QualificationLevel.FirstYear) {
            return Response.QUALIFICATION_LEVEL_TOO_LOW;
        }

        return Response.SUCCESS;
    }

    // #endregion

    // #region // !! ACCEPTING APPLICAIONS

    /**
     * The function accepts a set of selected students for a specific course and year, and returns a
     * response indicating whether the operation of accepting them was successful or not.
     * 
     * @param courseCode The course code is a unique identifier for a specific course. It is used to
     * identify the course for which students are being accepted.
     * @param year The "year" parameter represents the academic year for which the students are being
     * accepted into the course.
     * @param selectedStudents The "selectedStudents" parameter is a Set of Student objects. It
     * represents the students who have been selected for acceptance into a course.
     * @param applicantRole The parameter "applicantRole" is the role of the student who is applying
     * for the course. It is used to determine if the student is eligible to be accepted into the
     * course.
     * @return The method is returning a Response object. If the students are successfully accepted, it
     * returns Response.SUCCESS. Otherwise, it returns Response.UNSUCCESSFUL.
     */
    public static Response acceptStudents(String courseCode, String year, Set<Student> selectedStudents,
            Role applicantRole) {

        // ****************************************************************************************
        if (DatabaseController.acceptStudents(courseCode, year, selectedStudents, applicantRole)) {
            // ****************************************************************************************
            return Response.SUCCESS;
        }

        return Response.UNSUCCESSFUL;
    }

    // #endregion
}