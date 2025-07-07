package com.example.application.microservices;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.example.application.PublicEnums.Response;
import com.example.application.database.DatabaseController;
import com.example.application.models.Student;

/**
 * Manages the sign-up process for new users for MyTutor.
 */
public class SignUpManager {

    /**
     * This method is called to sign the user up. It calls 2 sepearate db methods: 
     * 1) Checks wether the user already exists. 
     * 2) If the user does not exist, add the user to the db
     * 
     * It also performs the necessary validation on the sign-up and returns appropriate reponses
     * 
     * @param newStudent
     * @param password
     * @param confirmPassword
     * @return
     */
    public static Response signUp(Student newStudent, String password, String confirmPassword) {

        //****************************************************
        if (DatabaseController.doesStudentExist(newStudent)) {
        //****************************************************

            return Response.USER_ALREADY_EXISTS;
        }

        Response message = validateSignUp(newStudent, password, confirmPassword);
        if (message == Response.SUCCESS) {
           
            //******************************************************************
            if (DatabaseController.studentSignUp(newStudent, confirmPassword)) {
            //******************************************************************

                return Response.SUCCESS;
            } else {
                return Response.SIGN_UP_NOT_SUCCESSFUL;
            }

        }
        return message;
    }

    /**
     * This method performs validation on the new sign-up. 
     * @param newStudent
     * @param password
     * @param confirmPassword
     * @return
     */
    public static Response validateSignUp(Student newStudent, String password, String confirmPassword) {

        // Validate passwords
        if (!password.equals(confirmPassword)) {
            return Response.PASSWORD_MISMATCH;
        }

        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(newStudent.getEmail());
        if (!emailMatcher.matches()) {
            return Response.INVALID_EMAIL;
        }

        // Validates student number format
        String studentNumberRegex = "^[A-Za-z]{6}\\d{3}$";
        Pattern studentNumberPattern = Pattern.compile(studentNumberRegex );
        Matcher studentNumberMatcher = studentNumberPattern.matcher(newStudent.getStudentID());
        if (!studentNumberMatcher.matches()) {
            return Response.INVALID_STUDENTNUMBER;
        }  

        return Response.SUCCESS;
    }
}