package com.example.application.microservices;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.vaadin.flow.server.VaadinSession;

import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;

import com.example.application.database.DatabaseController;

import com.example.application.models.Administrator;
import com.example.application.models.Lecturer;
import com.example.application.models.Person;
import com.example.application.models.Student;

/**
 * Manages user sign-in functionality.
 */
public class SignInManager {

    /**
     * Validates and performs user sign-in.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @return A Response indicating the result of the sign-in attempt.
     */
    public static Response signIn(String email, String password) {
        
        Response validationResponse = validateEmailFormat(email);
        
        if (validationResponse.equals(Response.SUCCESS)) {

            //**********************************************************
            switch (DatabaseController.doesUserExist(email, password)) {
            //**********************************************************
			
				case 0:
					return Response.INVALID_CREDENTIALS;
				
				case 1: //Student
					//*****************************************************
					Student student = DatabaseController.getStudent(email);
					//*****************************************************
					setSession(student, password);
                    break;

				case 2: //Employee
					//***************************************************
					Lecturer emp = DatabaseController.getEmployee(email);
					//***************************************************
					setSession(emp, password);
                    break;

				case 3: //Administrator
					//***************************************************************
					Administrator admin = DatabaseController.getAdministrator(email);
					//***************************************************************
					setSession(admin, password);
                    break;
			}
			
            return Response.SUCCESS;
		}

        return validationResponse;
    }

    /**
     * Validates the user sign-in data.
     *
     * @param email    The user's email.
     * @return A Response indicating the validation result.
     */
    public static Response validateEmailFormat(String email) {
        // Checking if the email is in correct format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher matcher = emailPattern.matcher(email);
        if (!matcher.matches()) {
            return Response.INVALID_EMAIL;
        }

        return Response.SUCCESS;
    }

    /**
     * Sets session attributes based on the user's role and stores them in the VaadinSession.
     * 
     * @param user Person which represents a user in the system.
     */
    public static void setSession(Person user, String password) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();

        
        vaadinSession.setAttribute("password", password);

        vaadinSession.setAttribute("firstName", user.getFirstName());
        System.out.println("firstName: " + user.getFirstName());
        
        vaadinSession.setAttribute("lastName", user.getLastName());
        System.out.println("lastName: " + user.getLastName());
        
        vaadinSession.setAttribute("email", user.getEmail());
        System.out.println("email: " + user.getEmail());
    
        switch (user.getRole()) {
            case Student:
                Student s = (Student) user;
                vaadinSession.setAttribute("personObject", s);

                vaadinSession.setAttribute("id", s.getStudentID());
                System.out.println("id: " + s.getStudentID());
    
                vaadinSession.setAttribute("role", Role.Student);
                System.out.println("role: " + Role.Student);

                vaadinSession.setAttribute("qualificationLevel", s.getQualificationLevel());
                System.out.println("qualificationStatus: " + s.getQualificationLevel());
    
                vaadinSession.setAttribute("applicationStatus", s.getApplicationStatus());
                System.out.println("applicationStatus: " + s.getApplicationStatus());
    
                vaadinSession.setAttribute("completedCourses", s.getCompletedCourses());
                System.out.println("completedCourses: " + s.getCompletedCourses());
                break;
    
            case Lecturer:
                Lecturer l = (Lecturer) user;
                vaadinSession.setAttribute("personObject", l);

                vaadinSession.setAttribute("id", l.getEmployeeID());
                System.out.println("id: " + l.getEmployeeID());
    
                vaadinSession.setAttribute("role", Role.Employee);
                System.out.println("role: " + Role.Employee);
    
                vaadinSession.setAttribute("department", l.getDepartment());
                System.out.println("department: " + l.getDepartment());
    
                vaadinSession.setAttribute("faculty", l.getFaculty());
                System.out.println("faculty: " + l.getFaculty());
                break;
    
            case Admin:
                Administrator a = (Administrator) user;
                vaadinSession.setAttribute("personObject", a);

                vaadinSession.setAttribute("id", a.getEmployeeID());
                System.out.println("id: " + a.getEmployeeID());
    
                vaadinSession.setAttribute("role", Role.Admin);
                System.out.println("role: " + Role.Admin);
                break;
    
            default:
                break;
        }
    }  
}