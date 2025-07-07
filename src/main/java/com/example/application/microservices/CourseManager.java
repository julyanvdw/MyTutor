package com.example.application.microservices;

import java.util.List;

import com.example.application.PublicEnums.Response;
import com.example.application.database.DatabaseController;
import com.example.application.models.Course;
import com.example.application.models.Lecturer;
import com.example.application.models.Student;

/**
 * This microservice holds all the functionality relating to the management of
 * courses
 * From the Admin dashboard view, it provides functionality to:
 * 1) View courses
 * 2) Add course
 * 3) Edit a course
 * 4) Delete a course
 */
public class CourseManager {

    // #region // !! GETTING A LIST OF COURSES

    /**
     * Returns a list of course codes by calling a method from the
     * DatabaseController class.
     * 
     * @return List of Strings.
     */
    public static List<String> getCourseCodes() {
        // ****************************************
        return DatabaseController.getCourseCodes();
        // ****************************************
    }

    // #endregion

    // #region // !! VIEWING courses */

    /**
     * This method returns a list of all courses as listed in the database
     *
     * @return
     */
    public static List<Course> getCourses() {
        // ****************************************
        return DatabaseController.getAllCourses();
        // ****************************************
    }

    /**
     * Returns a list of courses for a given employee.
     * 
     * @param employee Lecturer, which represents an employee.
     * @return List of Course objects.
     */
    public static List<Course> getCoursesFor(Lecturer employee) {
        // ************************************************************************
        return DatabaseController.getCoursesForEmployee(employee.getEmployeeID());
        // ************************************************************************
    }

    /**
     * Returns a list of courses for a given student.
     * 
     * @param employee Stduent, which represents either an Tutor or TA.
     * @return List of Course objects.
     */
    public static List<Course> getCoursesFor(Student student) {
        // *********************************************************************
        return DatabaseController.getCoursesForStudent(student.getStudentID());
        // *********************************************************************
    }

    /**
     * Returns a Course object for a given course code by calling a method in the
     * DatabaseController class.
     * 
     * @param courseCode String that represents the unique identifier for a specific
     *                   course.
     * @return Course object.
     */
    public static Course getCourseFor(String courseCode) {

        // **************************************************
        return DatabaseController.getCourseFor(courseCode);
        // **************************************************
    }

    // #endregion

    // #region // !! CREATING a course */

    /**
     * This method calls the createCourse method on the database
     * 
     * @param course
     * @return
     */
    public static Response create(Course course) {

        // *****************************************************************
        boolean successfulCreate = DatabaseController.createCourse(course);
        // *****************************************************************

        if (successfulCreate) {
            return Response.SUCCESS;
        }

        return Response.UPDATE_UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! UPDATING courses */

    /**
     * This method updates a course in the database
     * 
     * @param selectedCourse
     * @param newCourse
     * @return
     */
    public static Response update(Course selectedCourse, Course newCourse) {

        // ************************************************************************************
        boolean successfulUpdate = DatabaseController.updateCourse(selectedCourse, newCourse);
        // ************************************************************************************

        if (successfulUpdate) {
            return Response.SUCCESS;
        }

        return Response.UPDATE_UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! DELETING courses */

    /**
     * This method deletes the selected course from the database
     * 
     * @param course
     * @return
     */
    public static Response delete(Course course) {

        // *****************************************************************
        boolean successfulDelete = DatabaseController.deleteCourse(course);
        // *****************************************************************

        if (successfulDelete) {
            return Response.SUCCESS;
        }

        return Response.UPDATE_UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! GETTING STATS

    /**
     * The function returns the statistics for a given course code by calling a method in the
     * DatabaseController class.
     * 
     * @param courseCode The course code is a string parameter that represents the code or identifier
     * for a specific course.
     * @return The method is returning the course statistics for the given course code.
     */
    public static int getCourseStatsFor(String courseCode) {

        // ******************************************************
        return DatabaseController.getCourseStatsFor(courseCode);
        // ******************************************************

    }

    // #endregion
}