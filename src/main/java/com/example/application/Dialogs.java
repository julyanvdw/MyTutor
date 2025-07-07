package com.example.application;

import com.example.application.PublicEnums.Response;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Utility class for displaying various dialogs in the application.
 */
public class Dialogs {
    
    /**
     * Displays a dialog with a custom message.
     *
     * @param message The message to be displayed in the dialog.
     * @return The created Dialog component.
     */
    public static Dialog showDialog(String message) {
        Dialog dialog = new Dialog();
        dialog.getElement().getStyle().set("z-index", "1000");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.add(message); 
        dialog.open();
        return dialog;
    }

    /**
     * Displays a dialog with a message corresponding to a specific response type.
     *
     * @param messageType The Response type indicating the message to be displayed.
     */
    public static void showDialog(Response messageType) {
        String message = "";

        switch (messageType) {
            case SUCCESS:
                message = "Success! You may continue.";
                break;
            case INVALID_EMAIL:
                message = "Sorry! Invalid email format...";
                break;
            case INVALID_STUDENTNUMBER:
                message = "Sorry! Invalid student number format...";
                break;
            case INVALID_EMPID:
                message = "Sorry! Invalid employee ID format...";
                break;
            case INVALID_GRADE:
                message = "Sorry! Invalid grade value...";
                break;
            case INVALID_YEAR:
                message = "Sorry! Invalid year value...";
                break;
            case PASSWORD_MISMATCH:
                message = "Sorry! The passwords you have entered does not match...";
                break;
            case EMPTY_FIELD:
                message = "Sorry! Please fill in all fields...";
                break;
            case INVALID_CREDENTIALS:
                message = "Sorry! Email/Password is incorrect...";
                break;
            case USER_ALREADY_EXISTS:
                message = "Sorry! That user already exists...";
                break;
            case PASSWORD_EMAIL_FAILED:
                message = "Sorry! We were unable to email the password...";
                break;
            case QUALIFICATION_LEVEL_TOO_LOW:
                message = "Sorry! First years cannot apply...";
                break;
            case ACADEMIC_STANDING_NOT_SATISFACTORY:
                message = "Sorry! Your current GPA is unsatisfactory...";
                break;
            case SIGN_UP_NOT_SUCCESSFUL:
                message = "Sorry! Something went wrong with your sign-up...Please try again!";
                break;
            case INVALID_TUTORING_CAPACITY:
                message = "Sorry! Tutoring Capacity must be greater than zero...";
                break;
            case UPDATE_UNSUCCESSFUL:
                message = "Sorry! Updating was not successful...Please try again!";
                break;
            
            default:
                message = "ERROR";
        }

        showDialog(message);
    }
}