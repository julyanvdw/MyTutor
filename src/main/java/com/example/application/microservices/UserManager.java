package com.example.application.microservices;

import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.mail.*;
import javax.mail.internet.*;

import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;

import com.example.application.database.DatabaseController;

import com.example.application.models.Administrator;
import com.example.application.models.CourseConvenor;
import com.example.application.models.Lecturer;
import com.example.application.models.Tutor;
import com.vaadin.flow.server.VaadinSession;
import com.example.application.models.TA;
import com.example.application.models.Student;
import com.example.application.models.Person;

/**
 * This class manages users and user interactions for the Admin's "User Management" dashboard tab. 
 * 
 * Class functionality: 
 * 1) Provide functionality to display users (according to their types)
 * 2) Provide functionality to Add Users
 * 3) Provide functionality to Edit Users
 * 4) Provide functionality to Delete Users
 */
public class UserManager {
    
    //#region // !! Passing data to the frontend to DISPLAY users */

    /**
     * This method calls the database to return a list of admins reflecting the current state of the database
     * 
     * @return List<Administrator>
     */
    public static List<Administrator> getAdministrators() {
        //***************************************
        return DatabaseController.getAllAdmins();
        //***************************************
    }

    /**
     * Returns a list of all employees from the database.
     * 
     * @return List of Lecturer objects.
     */
    public static List<Lecturer> getEmployees() {
        //******************************************
        return DatabaseController.getAllEmployees();
        //******************************************
    }

    /**
     * This method calls the database to return a list of lecturers reflecting the current state of the database
     * 
     * @return List<Lecturer>
     */
    public static List<Lecturer> getLecturers() {
        //******************************************
        return DatabaseController.getAllLecturers();
        //******************************************
    }

    /**
     * This method calls the database to return a list of CourseConveners reflecting the current state of the database
     * 
     * @return List<CourseConvenor>
     */
    public static List<CourseConvenor> getCourseConvenors() {
        //************************************************
        return DatabaseController.getAllCourseConveners();
        //************************************************
    }

    /**
     * This method calls the database to return a list of Tutor reflecting the current state of the database
     * 
     * @return List<Tutor>
     */
    public static List<Tutor> getTutors() {
        //***************************************
        return DatabaseController.getAllTutors();
        //***************************************
    }

    /**
     * This method calls the database to return a list of TA reflecting the current state of the database
     * 
     * @return List<TA>
     */
    public static List<TA> getTAs() {
        //*************************************
         return DatabaseController.getAllTAs();
        //*************************************
    }

    /**
     * This method calls the database to return a list of Students reflecting the current state of the database.
     * 
     * @return List<Student>
     */
    public static List<Student> getStudents() {
        //*****************************************
        return DatabaseController.getAllStudents();
        //*****************************************
    }

    /**
     * Retrieves a password from the database based on the given email and role.
     * 
     * @param email The email parameter is a string that represents the email address of the user.
     * @param role The role parameter is an enum type that represents the role of the user.
     * @return The method is returning a password as a String.
     * !! DEPRECATED
     */
    public static String getPassword(String email, Role role) {
        //*************************************************
        return DatabaseController.getPassword(email, role);
        //*************************************************
    }

    //#endregion

    //#region // !! ADDING new users to the DB */

    /**
     * This method is responsibile for creating a user: 
     * 1) it first checks wether or not the user exists
     *      if the user does, then it repsponds appropriately
     * 2) if the user does not exist, it performs validation
     *      if the validation fails, then it responds appropriately
     * 3) if all is well, a random password is generated
     * 4) the user is added to the db
     * 5) appropriate reponse is made
     * 
     * @param user
     * @return
     */
    public static Response create(Person user, String role) {

        //****************************************************************
        boolean userExists = DatabaseController.doesUserExist(user, role);
        //****************************************************************

        if (userExists) {
            return Response.USER_ALREADY_EXISTS;
        }

        System.out.println("==============================" + user);

        //else if user does not exist
        Response message = validateUserCreation(user);
        System.out.println(message);

        if (message == Response.SUCCESS) {
              
           String generatedPassword = generatePassword(); 
           //***********************************************************
           DatabaseController.createUser(user, role, generatedPassword);
           //***********************************************************
           Response emailAttempt = emailTemporaryPassword(user, generatedPassword); 
           return emailAttempt;
        }

        return message;
    }

    /**
     * This method validates the user based on it's specific type
     * 
     * @param user
     * @return
     */
    public static Response validateUserCreation(Person user) {

        //validate email
        if (!validateEmail(user.getEmail())) {
            return Response.INVALID_EMAIL;
        }

        //validations: role specific
        if (user.getRole() == Role.Lecturer) {
            Lecturer lecturer = (Lecturer) user;

            if (!validateEMPID(lecturer.getEmployeeID())) {
                return Response.INVALID_EMPID;
            }
        }

        if (user.getRole() == Role.CourseConvenor) {
            CourseConvenor convener = (CourseConvenor) user;

            if (!validateEMPID(convener.getEmployeeID())) {
                return Response.INVALID_EMPID;
            }
        }

        if (user.getRole() == Role.TA) {
            TA ta = (TA) user;

            if (!validateSTUID(ta.getStudentID())) {
                return Response.INVALID_STUDENTNUMBER;
            }
        }

        if (user.getRole() == Role.Tutor) {
            Tutor tutor = (Tutor) user;

            if (!validateSTUID(tutor.getStudentID())) {
                return Response.INVALID_STUDENTNUMBER;
            }
        }
        
        return Response.SUCCESS;
    }

    /**
     * This method validates email
     * 
     * @param email
     * @return
     */
    public static Boolean validateEmail(String email) {

        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        
        }

        return true;
    }

    /**
     * This method validated EMPID
     * 
     * @param empID
     * @return
     */
    public static Boolean validateEMPID(String empID) {
        
        String studentNumberRegex = "^\\d{9}$";
        Pattern studentNumberPattern = Pattern.compile(studentNumberRegex );
        Matcher studentNumberMatcher = studentNumberPattern.matcher(empID);
        if (!studentNumberMatcher.matches()) {
            return false;
        } 

        return true;
    }

    /**
     * This method validated STUDENT ID
     * 
     * @param stuID
     * @return
     */
    public static Boolean validateSTUID(String stuID) {

        String studentNumberRegex = "^[A-Za-z]{6}\\d{3}$";
        Pattern studentNumberPattern = Pattern.compile(studentNumberRegex );
        Matcher studentNumberMatcher = studentNumberPattern.matcher(stuID);
        if (!studentNumberMatcher.matches()) {
            return false;
        } 

        return true;
    }

    /**
     * This method generates a new, random password
     * 
     * @return
     */
    public static String generatePassword() {
        int passwordLength = 10;
        StringBuilder password = new StringBuilder(passwordLength);
        
        String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        
        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            password.append(randomChar);
        }

        return password.toString();

    }

    /**
     * This method connects to the MyTutor gmail account and sends a sign-up email to the user. 
     * 
     * @param user
     * @param password
     * @return
     */
    private static Response emailTemporaryPassword(Person user, String password) {
        //GMAIL ACCOUNT DETAILS
        //email: mytutor.capstone@gmail.com
        //password: MYTUTORcapstoneelite123
        //password for this app: spfexfbovajeghns
        
        // Configure email properties for Gmail
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server host
        properties.put("mail.smtp.port", "587"); // TLS Port for Gmail
        properties.put("mail.smtp.auth", "true"); // Enable authentication
        properties.put("mail.smtp.starttls.enable", "true"); // Enable TLS encryption

        // Your Gmail email address and application-specific password
        String email = "mytutor.capstone@gmail.com";
        String appPassword = "spfexfbovajeghns"; // Replace with your app-specific password

        // Create a session with Gmail authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, appPassword);
            }
        });

        try {
            // Compose the email
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            message.setSubject("MyTutor Temporary Password");

            String applicationURL = "https://mytutor-production.lm.r.appspot.com";

            String messageText = "Dear " + user.getFirstName() + "\n\n" +
                                " Please complete your MyTutor profile by visiting:  " + applicationURL + 
                                " and creating a new password. \n\n" + 
                                "Here is your temporary password for your first sign-in: " + password +  "\n\n" +
                                "Warm regards \n The MyTutor Team";

            message.setText(messageText);

            // Send the email
            Transport.send(message);

        } catch (MessagingException e) {
            // e.printStackTrace();
            return Response.PASSWORD_EMAIL_FAILED;
        }
        
        return Response.SUCCESS;
    }

    //#endregion

    //#region // !! UPDATING users in the DB */

    /**
     * This method takes in 2 objects of type user (downcasted)
     * The first is the current, selected user - which needs to be updated
     * The second, is a temporary object which stores the new information, which will be used whne updating the relevant
     * recond in the databse.
     * 
     * @param selectedUser
     * @param newUser
     * @return
     */
    public static Response update(Person selectedUser, Person newUser, String role) {
        //***********************************************************************
        boolean success = DatabaseController.updateUser(selectedUser, newUser, role);
        //***********************************************************************

        if (success) {
            return Response.SUCCESS;
        }
    
        return Response.UPDATE_UNSUCCESSFUL;
    }

    /**
     * Updates the password for a given email in the database and returns
     * a success response if the update is successful, otherwise it returns an unsuccessful response.
     * 
     * @param email The email parameter is a string that represents the email address of the user whose
     * password needs to be changed.
     * @param newPassword The `newPassword` parameter is a String that represents the new password that
     * the user wants to set for their account.
     * @return The method is returning a Response object. If the password update is successful, it
     * returns Response.SUCCESS. If the password update is unsuccessful, it returns
     * Response.UPDATE_UNSUCCESSFUL.
     */
    public static Response changePassword(String email, Role role, String newPassword) {

        //****************************************************************************
        boolean success = DatabaseController.updatePassword(email, role, newPassword);
        //****************************************************************************

        if (success) {
            return Response.SUCCESS;
        }
    
        return Response.UPDATE_UNSUCCESSFUL;
    }

    //#endregion

    //#region // !! DELETING users from the DB */

    /**
     * This method deletes a user (downcasted here) form the DB
     * 
     * @param selectedUser
     * @return
     */
    public static Response delete(Person selectedUser, String role) {

        String your_email =  (String) VaadinSession.getCurrent().getAttribute("email");

        if (your_email.equals(selectedUser.getEmail())) {
            System.out.println("You cannot delete yourself");
            return Response.CANNOT_DELETE_SELF;
        }

        //**************************************************
        if (DatabaseController.deleteUser(selectedUser, role)) {
        //**************************************************
            return Response.SUCCESS;
        }

        return Response.FAILED_TO_DELETE_USER;
    }

    //#endregion

}