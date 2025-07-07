package com.example.application;

/**
 * This class defines public enums used in the application.
 */
public class PublicEnums {

    /**
     * Enumeration of possible responses.
     */
    public enum Response {
        SUCCESS,
        INVALID_CREDENTIALS,
        UNSUCCESSFUL,

        INVALID_EMAIL,
        INVALID_STUDENTNUMBER, 
        INVALID_EMPID,
        INVALID_GRADE, 
        INVALID_YEAR,
        PASSWORD_MISMATCH,
        EMPTY_FIELD,
        
        USER_ALREADY_EXISTS,
        DUPLICATE_CHECK, //?

        PASSWORD_EMAIL_FAILED,
        QUALIFICATION_LEVEL_TOO_LOW,
        ACADEMIC_STANDING_NOT_SATISFACTORY,
        SIGN_UP_NOT_SUCCESSFUL,
        INVALID_TUTORING_CAPACITY,
        CANNOT_DELETE_SELF,
        FAILED_TO_DELETE_USER,
        APPLICATION_UNSUCCESSFUL,
        COULD_NOT_CREATE_TABLE,
        UPDATE_UNSUCCESSFUL;
    }

    /**
     * Enumeration of possible tutor application states
     */
    public enum ApplicationStatus {
        IDLE,
        APPLIED,
        ACCEPTED,
        REJECTED;

        public static ApplicationStatus fromString(String status) {
            switch (status) {
                case "IDLE":
                    return IDLE;
                case "APPLIED":
                    return APPLIED;
                case "ACCEPTED":
                    return ACCEPTED;
                case "REJECTED":
                    return REJECTED;
                default:
                    throw new IllegalArgumentException("Invalid application status: " + status);
            }
        }
    
    }

    /**
     * Enumeration for different years of study.
     */
    public enum YearOfStudy {
        FIRST(1), 
        SECOND(2), 
        THIRD(3), 
        FOURTH_AND_UP(4);

        private final int value;

        /**
         * Constructor for the YearOfStudy enum. Allows each enum constant to have an associated value.
         * 
         * @param value Integer which is assigned to the value field of the enum.
         */
        private YearOfStudy(int value) {
            this.value = value;
        }

        /**
         * The getValue() function returns the value of a YearOfStudy enum.
         * 
         * @return The value of the enum.
         */
        public int getValue() {
            return value;
        }

        /**
         * Converts a YearOfStudy enum to its corresponding string representation.
         * 
         * @return The string representation of a YearOfStudy enum.
         */
        @Override
        public String toString() {
            switch (value) {
                case 1:
                    return "1st";
                case 2:
                    return "2nd";
                case 3:
                    return "3rd";
                case 4:
                    return "4th+";
                default:
                    return value + "th";
            }
        }

        /**
         * Allows the converion of a string representation to a YearOfStudy.
         * 
         * @param value The string representation.
         * @return The associated YearOfStudy from value.
         */
        public static YearOfStudy fromString(String value) {
            switch (value) {
                case "1st":
                    return FIRST;
                case "2nd":
                    return SECOND;
                case "3rd":
                    return THIRD;
                case "4th+":
                    return FOURTH_AND_UP;
                default:
                    throw new IllegalArgumentException("Invalid years of study: " + value);
            }
        }
    }
    
    /**
     * Enumeration for defining different levels of qualifications.
     * Used to represent the qualification level of a student.
     */
    public enum QualificationLevel {
        None,
        FirstYear,
        SecondYear,
        ThirdYear,
        FourthYear,
        Honours,
        Masters,
        PhD;

        /**
         * The function converts a string value to a corresponding QualificationLevel enum value.
         * 
         * @param value The value parameter is a string that represents a qualification level.
         * @return The method is returning a QualificationLevel enum value based on the input string
         * value.
         */
        public static QualificationLevel fromString(String value) {
            switch (value) {
                case "FirstYear":
                    return FirstYear;
                case "SecondYear":
                    return SecondYear;
                case "ThirdYear":
                    return ThirdYear;
                case "FourthYear":
                    return FourthYear;
                case "Honours":
                    return Honours;
                case "Masters":
                    return Masters;
                case "PhD":
                    return PhD;
                default:
                    throw new IllegalArgumentException("Invalid qualification level: " + value);
            }
        }
    }

    /**
     * Enumeration for different roles.
     */
    public enum Role {
        NoRole,
        Admin,
        CourseConvenor,
        Lecturer,
        Student,
        Tutor, 
        TA,
        Employee; 
    }
}