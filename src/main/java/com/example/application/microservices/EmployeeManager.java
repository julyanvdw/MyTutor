package com.example.application.microservices;

import java.util.List;
import java.util.Set;

import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;
import com.example.application.database.DatabaseController;
import com.example.application.models.Course;
import com.example.application.models.Lecturer;

/**
 * This class manages the Employee and all the related funcationality as
 * indicated by their flow
 * 
 * The class has support for
 * 1) Fetching the courses a employee is allocated to
 */
public class EmployeeManager {

    // #region // !! ADDING Emps to a course and populating it in the DB

    /**
     * This method records which Emps (Lectureres / CourseConveners) are added to a
     * specific course for a specific year
     * 
     * @param selectedEmps
     * @param course
     * @param year
     * @return
     */
    public static Lecturer doesEmployeeExist(Set<Lecturer> selectedEmps, Course course, int year) {
        for (Lecturer lecturer : selectedEmps) {
            // ******************************************************************************************
            if (DatabaseController.doesEmployeeExist(lecturer, course, year)) {
                // ******************************************************************************************
                return lecturer;
            }
        }

        return null;
    }

    /**
     * The function adds employees to a course in a database and returns a response indicating whether
     * the operation was successful or not.
     * 
     * @param selectedEmps A set of Lecturer objects representing the employees selected to be added to
     * the course.
     * @param course The "course" parameter is an object of the Course class. It represents the course
     * to which the employees are being added.
     * @param role The "role" parameter represents the role that the employee will have in the course.
     * It could be a lecturer, teaching assistant, or any other role defined in the system.
     * @param year The "year" parameter represents the year in which the course is being offered. It is
     * an integer value that specifies the year.
     * @return The method is returning a Response object. If the addEmpToCourse operation is
     * successful, it returns Response.SUCCESS. Otherwise, it returns Response.UPDATE_UNSUCCESSFUL.
     */
    public static Response addEmpToCourse(Set<Lecturer> selectedEmps, Course course, Role role, int year) {

        // ******************************************************************************************
        boolean successfulAdd = DatabaseController.addEmpToCourse(selectedEmps, course, role, year);
        // ******************************************************************************************

        if (successfulAdd) {
            return Response.SUCCESS;
        }

        return Response.UPDATE_UNSUCCESSFUL;
    }

    // #endregion

    // #region // !! Getting emps for a specific course

    /**
     * The function returns a list of lecturers for a given course code and year.
     * 
     * @param courseCode The course code is a unique identifier for a specific course. It is typically
     * a combination of letters and numbers that represents a particular subject or topic. For example,
     * "CS101" could be the course code for an introductory computer science course.
     * @param year The "year" parameter represents the academic year for which you want to retrieve the
     * employees.
     * @return The method is returning a List of Lecturer objects.
     */
    public static List<Lecturer> getEmployeesFor(String courseCode, String year) {
        // ************************************************
        return DatabaseController.getEmployeesFor(courseCode, year);
        // ************************************************
    }

    // #endregion

}