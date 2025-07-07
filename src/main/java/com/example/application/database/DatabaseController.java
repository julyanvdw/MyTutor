package com.example.application.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.time.Year;

import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Role;

import com.example.application.models.*;

/**
 * The DatabaseController class is responsible for managing interactions with a
 * database. The DatabaseController class is the only class managing the interactions between the 
 * database and the rest of the program as per convention. 
 */
public class DatabaseController {

    // #region // !! SETTING UP and AUX METHODS

    // Declaring and initializing constants for the database connectiondetails.
    private static final String DATABASE_URL = "jdbc:mysql://nightmare.cs.uct.ac.za/wlseth003";
    private static final String DATABASE_USER = "wlseth003";
    private static final String DATABASE_PASSWORD = "ijeiM4Bu";

    /**
     * Function returns a connection to a database using the specified URL,
     * username, and password.
     * 
     * @return The method is returning a Connection object.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }

    /**
     * Returns the current year as a string.
     * 
     * @return The current year as a string.
     */
    public static String getCurrentYear() {
        Year currentYear = Year.now();
        return currentYear.toString();
    }

    /**
     * Function "resets" the system by setting all the Students' application statuses to "IDLE"
     * and thereby allows them to re-enter the application process
     * @return
     */
    public static boolean resetSystem() {
        try (Connection connection = getConnection()) {

            String query = "UPDATE Students SET applicationStatus = 'IDLE'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // #endregion

    // #region // !! SIGNING-UP

    /**
     * The function `studentSignUp` inserts a new student into a database table
     * called "Students" with
     * the provided student information and password.
     * 
     * @param newStudent The newStudent parameter is an object of the Student class.
     *                   It contains
     *                   information about the student such as their student ID,
     *                   first name, last name, email,
     *                   qualification level, and application status.
     * @param password   The password parameter is a String that represents the
     *                   password for the new
     *                   student.
     * @return The method is returning a boolean value. It returns true if the
     *         student sign up was
     *         successful and false if there was an error or the sign up was
     *         unsuccessful.
     */
    public static boolean studentSignUp(Student newStudent, String password) {

        try (Connection connection = getConnection()) {

            boolean success = true;

            String query = "INSERT INTO Students (studentID, firstName, lastName, email, password, qualificationLevel, applicationStatus) VALUES (?,?,?,?,?,?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newStudent.getStudentID());
            preparedStatement.setString(2, newStudent.getFirstName());
            preparedStatement.setString(3, newStudent.getLastName());
            preparedStatement.setString(4, newStudent.getEmail());
            preparedStatement.setString(5, password);
            preparedStatement.setString(6, newStudent.getQualificationLevel().toString());
            preparedStatement.setString(7, newStudent.getApplicationStatus().toString());

            int rowsAffected = preparedStatement.executeUpdate();
            int rowsInserted = 0;

            // Adding to the "completedCourses" table

            for (CompletedCourse c : newStudent.getCompletedCourses()) {
                String q = "INSERT INTO CompletedCourses (courseCode, studentID, grade, year) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(q);

                ps.setString(1, c.getCourseCode().toString());
                ps.setString(2, newStudent.getStudentID());
                ps.setString(3, Double.toString(c.getGrade()));
                ps.setString(4, Integer.toString(c.getYear()));

                rowsInserted += ps.executeUpdate();
            }

            if (rowsAffected <= 0) {
                success = false;
            }

            if (rowsInserted <= 0) {
                success = false;
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function checks if a student exists in the database based on their email.
     * 
     * @param student The "student" parameter is an object of the "Student" class,
     *                which contains
     *                information about a student, including their email address.
     * @return The method returns a boolean value. It returns true if the email of
     *         the given student
     *         exists in the "Students" table, and false otherwise.
     */
    public static boolean doesStudentExist(Student student) {

        try (Connection connection = getConnection()) {

            String query = "SELECT COUNT(*) FROM Students WHERE email =?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, student.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // if count > 0, email exists
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // #endregion

    // #region // !! SIGNING-IN

    /**
     * Checks if a user with the given email and password exists in the
     * Students, Employees, or Administrators table in a database and returns an
     * integer value
     * indicating the user type (1 for student, 2 for employee, 3 for administrator)
     * or 0 if the user
     * does not exist.
     * 
     * @param email    String that represents the email address of the user.
     * @param password String that represents the user's password.
     * @return An integer value.
     *         0: User not found (or something went wrong)
     *         1: User found; is a Student (Base, Tutor, or TA)
     *         2: User found; is an Employee (Lecturer, CourseConvenor)
     *         3: User found; is an Administrator
     */
    public static int doesUserExist(String email, String password) {

        try (Connection connection = getConnection()) {

            // Check Students
            String query = "SELECT * FROM Students WHERE email = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return 1;

            // Check Employees
            query = "SELECT * FROM Employees WHERE email = ? AND password = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return 2;

            // Check Administrator
            query = "SELECT * FROM Administrators WHERE email = ? AND password = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return 3;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Retrieves a student from a database based on their email.
     * 
     * @param email String of the unique identifier for a student; used to search in
     *              the database.
     * @return A Student object if a student with the given email is found. If no
     *         student is found, it returns null.
     */
    public static Student getStudent(String email) {

        Student student = null;

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Students WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                student = new Student(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("studentID"),
                        QualificationLevel.valueOf(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        getCompletedCourses(resultSet.getString("studentID")));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return student;
    }

    /**
     * Retrieves an employee from a database based on their email.
     * 
     * @param email String of the unique identifier for an employee; used to search
     *              in the database.
     * @return A Lecturer object if a employee with the given email is found. If no
     *         employee is found, it returns null.
     */
    public static Lecturer getEmployee(String email) {

        Lecturer lecturer = null;

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Employees WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                lecturer = new Lecturer(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"),
                        resultSet.getString("department"),
                        resultSet.getString("faculty"));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lecturer;

    }

    /**
     * Retrieves an admin from a database based on their email.
     * 
     * @param email String of the unique identifier for an admin; used to search in
     *              the database.
     * @return An Administrator object with the given email is found. If none is
     *         found, it returns null.
     */
    public static Administrator getAdministrator(String email) {

        Administrator admin = null;

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Administrators WHERE email = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                admin = new Administrator(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return admin;
    }

    // #endregion

    // #region // !! PROFILE

    /**
     * Retrieves the password associated with a given email and role from
     * the corresponding database table.
     * 
     * @param email String that represents the email address of the user for whom
     *              you want to retrieve the password.
     * @param role  Role enum that represents the role of the user for whom we want
     *              to retrieve the password.
     * @return String value If a matching password is found in the database, it will
     *         be returned.
     *         Otherwise, an empty string will be returned.
     *         !! DEPRECATED
     */
    public static String getPassword(String email, Role role) {

        String query = "";

        try (Connection connection = getConnection()) {

            switch (role) {
                case Student:
                    // Check Students
                    query = "SELECT password FROM Students WHERE email = ?";
                    break;

                case Employee:
                    // Check Students
                    query = "SELECT password FROM Employees WHERE email = ?";

                case Admin:
                    // Check Administrator
                    query = "SELECT password FROM Administrators WHERE email = ?";

                default:
                    break;
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getString("password");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * The function `updatePassword` updates the password for a user with the
     * specified email and role
     * in the corresponding database table.
     * 
     * @param email       The email parameter is a string that represents the email
     *                    address of the user whose
     *                    password needs to be updated.
     * @param role        The role parameter is an enum type called Role. It
     *                    represents the role of the user
     *                    whose password needs to be updated. The possible values
     *                    for the Role enum are Student, Employee,
     *                    and Admin.
     * @param newPassword The `newPassword` parameter is a String that represents
     *                    the new password that
     *                    you want to update for the user.
     * @return The method is returning a boolean value. It returns true if the
     *         password update was
     *         successful (i.e., if the number of rows affected is greater than 0),
     *         and false otherwise.
     */
    public static boolean updatePassword(String email, Role role, String newPassword) {

        String query = "";
        int rA = 0;

        try (Connection connection = getConnection()) {

            System.out.println("ROLE IN DB: " + role.name());
            switch (role) {
                case Student:
                    // Check Students
                    query = "UPDATE Students SET password = ? WHERE email = ?";
                    break;

                case Employee:
                    // Check Students
                    query = "UPDATE Employees SET password = ? WHERE email = ?";
                    break;

                case Admin:
                    // Check Administrator
                    query = "UPDATE Administrators SET password = ? WHERE email = ?";
                    break;

                default:
                    break;
            }

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, email);

            int rowsAffected = preparedStatement.executeUpdate();
            rA = rowsAffected;

            connection.close();
            return rA > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * This method returns the motivation as recorded by the student when making an application
     * 
     * @param studentID of the student
     * 
     * @return returns the String motivation
     */
    public static String getMotivation(String studentID) {

        try (Connection connection = getConnection()) {

            String query = "SELECT motivation FROM Applications WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getString("motivation");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Motivation not found";
    }

    /**
     * This method retrieves the grade for a specified student, for a given course
     * 
     * @param courseCode as a String
     * @param studentID as a String 
     * @return a String representing the grade 
     */
    public static String getGradeFor(String courseCode, String studentID) {
        try (Connection connection = getConnection()) {

            String query = "SELECT grade FROM CompletedCourses WHERE studentID = ? AND courseCode = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getString("grade");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "-1";
    }
    // #endregion

    // #region // !! ADMIN USER FUNCTIONALITIES - Displaying, Creating, Updating,

    /**
     * Retrieves a list of Lecturer objects from a database table called Employees.
     * 
     * @return List of Lecturer objects.
     */
    public static List<Lecturer> getAllEmployees() {

        List<Lecturer> employeeList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Employees";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Lecturer employee = new Lecturer(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"),
                        resultSet.getString("department"),
                        resultSet.getString("faculty"));

                employeeList.add(employee);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeList;
    }

    /**
     * The function getAllLecturers retrieves a list of all lecturers from a
     * database.
     * 
     * @return The method is returning a List of Lecturer objects.
     */
    public static List<Lecturer> getAllLecturers() {

        List<Lecturer> lecturerList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Employees LEFT JOIN AccessibleCourses ON Employees.employeeID = AccessibleCourses.empID WHERE role = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "Lecturer");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Lecturer lecturer = new Lecturer(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"),
                        resultSet.getString("department"),
                        resultSet.getString("faculty"));

                lecturerList.add(lecturer);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lecturerList;
    }

    /**
     * The function getAllCourseConveners retrieves a list of all course conveners
     * from a database.
     * 
     * @return The method is returning a List of CourseConvenor objects.
     */
    public static List<CourseConvenor> getAllCourseConveners() {

        List<CourseConvenor> convenerList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Employees LEFT JOIN AccessibleCourses ON Employees.employeeID = AccessibleCourses.empID WHERE role = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "CourseConvener");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                CourseConvenor courseConvener = new CourseConvenor(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"),
                        resultSet.getString("department"),
                        resultSet.getString("faculty"));

                convenerList.add(courseConvener);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return convenerList;
    }

    /**
     * The function getAllAdmins retrieves a list of all administrators from a
     * database table called
     * Administrators.
     * 
     * @return The method is returning a List of Administrator objects.
     */
    public static List<Administrator> getAllAdmins() {

        List<Administrator> adminList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Administrators";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Administrator admin = new Administrator(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("employeeID"));

                adminList.add(admin);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return adminList;
    }

    /**
     * The function getAllTutors retrieves a list of all tutors from a database.
     * 
     * @return The method is returning a List of Tutor objects.
     */
    public static List<Tutor> getAllTutors() {

        List<Tutor> tutorList = new ArrayList<>();
        List<CompletedCourse> courses = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Students INNER JOIN Tutors ON Students.StudentID = Tutors.stuID";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Tutor tutor = new Tutor(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("studentID"),
                        QualificationLevel.fromString(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        courses);

                tutorList.add(tutor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tutorList;
    }

    /**
     * The function getAllTAs retrieves a list of all TAs from a database and
     * returns it.
     * 
     * @return The method is returning a List of Tutor objects.
     */
    public static List<TA> getAllTAs() {

        List<TA> taList = new ArrayList<>();
        List<CompletedCourse> courses = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Students INNER JOIN TAs ON Students.StudentID = TAs.studentID";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                TA ta = new TA(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("studentID"),
                        QualificationLevel.valueOf(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        courses);

                taList.add(ta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taList;
    }

    /**
     * The function getAllStudents retrieves a list of all Students from a database
     * and returns it.
     * 
     * @return The method is returning a List of Student objects.
     */
    public static List<Student> getAllStudents() {

        List<Student> studentList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Students";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Student student = new Student(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        resultSet.getString("studentID"),
                        QualificationLevel.valueOf(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        null);

                studentList.add(student);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentList;
    }

    /**
     * The function `createUser` inserts a new user into the database based on their
     * role
     * (Administrator, Course Convenor, Lecturer, Tutor, or TA) and returns true if
     * the insertion was
     * successful.
     * 
     * @param newUser     The `newUser` parameter is an object of type `Person`
     *                    which represents the user
     *                    to be created. It contains information such as the user's
     *                    first name, last name, email, and
     *                    other relevant details.
     * @param role        The "role" parameter is a string that specifies the role
     *                    of the user being created.
     *                    It can have the values "Administrator", "Course Convenor",
     *                    "Lecturer", "Tutor", or "TA".
     * @param genPassword The `genPassword` parameter is a String that represents
     *                    the generated
     *                    password for the new user.
     * @return The method is returning a boolean value. It returns true if the user
     *         creation was
     *         successful (i.e., if the number of rows affected by the SQL statement
     *         is greater than 0), and
     *         false otherwise.
     */
    public static boolean createUser(Person newUser, String role, String genPassword) {
        int rA = 0;

        try (Connection connection = getConnection()) {

            if (role.equals("Administrator")) {

                String query = "INSERT INTO Administrators (employeeID, firstName, lastName, email, password) VALUES (?,?,?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Administrator) newUser).getEmployeeID());
                preparedStatement.setString(2, newUser.getFirstName());
                preparedStatement.setString(3, newUser.getLastName());
                preparedStatement.setString(4, newUser.getEmail());
                preparedStatement.setString(5, genPassword);

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

            } else if (role.equals("Course Convenor") || role.equals("Lecturer") || role.equals("Employee")) {

                String query = "INSERT INTO Employees (employeeID, firstName, lastName, email, password, department, faculty) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Lecturer) newUser).getEmployeeID());
                preparedStatement.setString(2, newUser.getFirstName());
                preparedStatement.setString(3, newUser.getLastName());
                preparedStatement.setString(4, newUser.getEmail());
                preparedStatement.setString(5, genPassword);
                preparedStatement.setString(6, ((Lecturer) newUser).getDepartment());
                preparedStatement.setString(7, ((Lecturer) newUser).getFaculty());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

            } else

            if (role.equals("Tutor") || role.equals("TA") || role.equals("Student")) {

                String query = "INSERT INTO Students (studentID, firstName, lastName, email, password, qualificationLevel, applicationStatus) VALUES (?,?,?,?,?,?,?)";

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) newUser).getStudentID());
                preparedStatement.setString(2, newUser.getFirstName());
                preparedStatement.setString(3, newUser.getLastName());
                preparedStatement.setString(4, newUser.getEmail());
                preparedStatement.setString(5, genPassword);
                preparedStatement.setString(6, ((Student) newUser).getQualificationLevel().toString());
                preparedStatement.setString(7, ApplicationStatus.ACCEPTED.toString());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;
            }

            return rA > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * The function `doesUserExist` checks if a user with a specific role exists in
     * the database based
     * on their email.
     * 
     * @param user The "user" parameter is an instance of the Person class, which
     *             represents a user in
     *             the system. It contains information about the user, such as their
     *             email address.
     * @param role The role parameter is a String that represents the role of the
     *             user. It can have
     *             values such as "Administrator", "Course Convenor", "Lecturer",
     *             "Tutor", or "TA".
     * @return The method returns a boolean value. It returns true if the user
     *         exists in the specified
     *         role (Administrator, Course Convenor, Lecturer, Tutor, or TA) and
     *         false otherwise.
     */
    public static boolean doesUserExist(Person user, String role) {
        int counter = 0;
        try (Connection connection = getConnection()) {
            if (role.equals("Administrator")) {

                Administrator admin = (Administrator) user;

                String query = "SELECT COUNT(*) FROM Administrators WHERE email = ? OR employeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, admin.getEmail());
                preparedStatement.setString(2, admin.getEmployeeID());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    counter = count;
                }
                return counter > 0;

            } else if (role.equals("Employee")) {

                Lecturer emp = (Lecturer) user;

                String query = "SELECT COUNT(*) FROM Employees WHERE email = ? OR employeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, emp.getEmail());
                preparedStatement.setString(2, emp.getEmployeeID());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    counter = count;
                }

                System.out.println("Counter: " + counter);

                return counter > 0;

            } else if (role.equals("Student")) {

                Student student = (Student) user;

                String query = "SELECT COUNT(*) FROM Students WHERE email = ? OR studentID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, student.getEmail());
                preparedStatement.setString(2, student.getStudentID());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    counter = count;
                }
                return counter > 0;

            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;
    }

    /**
     * This Java function deletes a user from the database based on their role.
     * 
     * @param user The "user" parameter is an object of type Person, which
     *             represents a user in the
     *             system. The specific type of user can be one of the following:
     *             Administrator, CourseConvenor,
     *             Lecturer, Student, Tutor, or TA. The role parameter is a string
     *             that specifies the role of
     * @param role The "role" parameter is a String that represents the role of the
     *             person to be
     *             deleted. It can have the following values: "Administrator",
     *             "Course Convenor", "Lecturer",
     *             "Tutor", or "TA".
     * @return The method is returning a boolean value. It returns true if the
     *         delete operation was
     *         successful (i.e., if rows were affected), and false otherwise.
     */
    public static boolean deleteUser(Person user, String role) {
        int rA = 0;
        try (Connection connection = getConnection()) {

            if (role.equals("Administrator")) {

                String query = "DELETE FROM Administrators WHERE employeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Administrator) user).getEmployeeID());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

            } else if (role.equals("Course Convenor") || role.equals("Lecturer") || role.equals("Employee")) {

                // Deletes from accessibleCourses first
                String q = "DELETE FROM AccessibleCourses WHERE empID = ?";
                PreparedStatement p = connection.prepareStatement(q);
                p.setString(1, ((Lecturer) user).getEmployeeID());

                int rowsAffected = p.executeUpdate();
                rA = rowsAffected;

                // then deletes from the actual emp table
                String query = "DELETE FROM Employees WHERE employeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Lecturer) user).getEmployeeID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

            } else if (role.equals("Tutor") || role.equals("TA") || role.equals("Student")) {

                // Delete from COMPLETED COURSES if this student has an entry there
                String query = "DELETE FROM CompletedCourses WHERE studentID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                int rowsAffected = preparedStatement.executeUpdate();
                rA += rowsAffected;

                // Delete from APPLCATIONS if this student has an entry there
                query = "DELETE FROM Applications WHERE studentID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

                // Delete from TUTORS if this student has an entry there
                query = "DELETE FROM Tutors WHERE stuID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

                // Delete from TAs if this student has an entry there
                query = "DELETE FROM TAs WHERE studID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

                // Delete from Tutoring sessions if this student has an entry there
                query = "DELETE FROM TutoringSessionTutors WHERE studentID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

                // Delete from Students

                query = "DELETE FROM Students WHERE studentID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

            }

            connection.close();
            return rA > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * The function updates the information of a person in the database based on
     * their role.
     * 
     * @param user    The "user" parameter is an object of type Person, which
     *                represents the current user
     *                that needs to be updated in the database.
     * @param newUser The `newUser` parameter is an instance of the `Person` class,
     *                which represents
     *                the updated information for the user. It contains the new
     *                first name and last name for the user.
     * @param role    The "role" parameter is a String that represents the role of
     *                the user. It can have
     *                values such as "Administrator", "Course Convenor", "Lecturer",
     *                "Tutor", or "TA".
     * @return The method is returning a boolean value. It returns true if the
     *         update operation was
     *         successful (i.e., if the number of rows affected is greater than 0),
     *         and false otherwise.
     */
    public static boolean updateUser(Person user, Person newUser, String role) {

        int rA = 0;
        try (Connection connection = getConnection()) {

            if (role.equals("Administrator") || role.equals("Admin")) {

                String query = "UPDATE Administrators SET firstName = ?, lastName = ? WHERE EmployeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, newUser.getFirstName());
                preparedStatement.setString(2, newUser.getLastName());
                preparedStatement.setString(3, ((Administrator) user).getEmployeeID());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

            } else if (role.equals("Course Convenor") || role.equals("Lecturer") || role.equals("Employee")) {

                String query = "UPDATE Employees SET firstName = ?, lastName = ?, department = ?, faculty = ? WHERE EmployeeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, newUser.getFirstName());
                preparedStatement.setString(2, newUser.getLastName());
                preparedStatement.setString(3, ((Lecturer) newUser).getDepartment());
                preparedStatement.setString(4, ((Lecturer) newUser).getFaculty());
                preparedStatement.setString(5, ((Lecturer) user).getEmployeeID());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

            } else if (role.equals("Student") || role.equals("Tutor") || role.equals("TA")) {

                String query = "UPDATE Students SET firstName = ?, lastName = ?, qualificationLevel = ? WHERE StudentID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, newUser.getFirstName());
                preparedStatement.setString(2, newUser.getLastName());
                preparedStatement.setString(3, ((Student) newUser).getQualificationLevel().name());
                preparedStatement.setString(4, ((Student) user).getStudentID());

                int rowsAffected = preparedStatement.executeUpdate();
                rA = rowsAffected;

                // updating the completed courses
                // first delete
                query = "DELETE FROM CompletedCourses WHERE studentID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, ((Student) user).getStudentID());

                rowsAffected += preparedStatement.executeUpdate();
                rA += rowsAffected;

                // then re-add
                for (CompletedCourse c : ((Student) newUser).getCompletedCourses()) {
                    String q = "INSERT INTO CompletedCourses (courseCode, studentID, grade, year) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = connection.prepareStatement(q);

                    ps.setString(1, c.getCourseCode().toString());
                    ps.setString(2, ((Student) newUser).getStudentID());
                    ps.setString(3, Double.toString(c.getGrade()));
                    ps.setString(4, Integer.toString(c.getYear()));

                    rowsAffected = ps.executeUpdate();
                }

                rA += rowsAffected;
            }

            connection.close();
            return rA > 0;

        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    // #endregion

    // #region // !! APPLICATION

    /**
     * Inserts a new application into a database table and returns true if the
     * insertion was successful.
     * 
     * @param studentID  String of the unique identifier for a student; used to
     *                   search in the database.
     * @param motivation String that represents the motivation or reason for the
     *                   student's application.
     * @return A boolean value - True if the application submission was successful
     *         (i.e., if the number of rows affected by the SQL query is greater
     *         than 0), and false otherwise.
     */
    public static boolean submitApplication(String studentID, String motivation) {
        int rA = 0;

        try (Connection connection = getConnection()) {
            String query = "INSERT INTO Applications (studentID, motivation) VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, motivation);

            int rowsAffected = preparedStatement.executeUpdate();
            rA = rowsAffected;

            return rA > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * The function sets the application status of a student to "ACCEPTED" in the database.
     * 
     * @param studentID The studentID parameter is a unique identifier for a student in the database.
     * It is used to specify which student's application status should be updated.
     */
    private static void setACCEPTEDapplicationStatus(String studentID) {
        try (Connection connection = getConnection()) {
            String query = "UPDATE Students SET applicationStatus = ? WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, ApplicationStatus.ACCEPTED.name());
            preparedStatement.setString(2, studentID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * The addTutor function inserts a new tutor into the Tutors table with the specified student ID,
     * course code, and year.
     * 
     * @param studentID The student ID of the tutor being added.
     * @param courseCode The course code is a unique identifier for a specific course. It is typically
     * a combination of letters and numbers that represents a particular subject or topic. For example,
     * "CS101" could be the course code for an introductory computer science course.
     * @param year The "year" parameter represents the year in which the student is acting as a tutor
     * for the specified course.
     */
    private static void addTutor(String studentID, String courseCode, String year) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO Tutors (stuID, courseCode, year) VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, year);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The addTA function inserts a new TA (Teaching Assistant) record into the TAs table with the
     * provided student ID, course code, and year.
     * 
     * @param studentID The student ID of the TA being added.
     * @param courseCode The courseCode parameter represents the code or identifier of the course for
     * which the student is being added as a TA.
     * @param year The "year" parameter represents the year in which the student is assigned as a
     * teaching assistant for a specific course.
     */
    private static void addTA(String studentID, String courseCode, String year) {
        try (Connection connection = getConnection()) {
            String query = "INSERT INTO TAs (studID, courseCode, year) VALUES (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, year);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The function removes an application from the database based on the provided student ID.
     * 
     * @param studentID The studentID parameter is a String that represents the unique identifier of
     * the student whose application needs to be removed.
     */
    private static void removeApplication(String studentID) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM Applications WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The function accepts selected students for a specific course and role, updates their application
     * status, adds them as a tutor or TA, and removes their application.
     * 
     * @param courseCode The course code is a String that represents the code of the course for which
     * students are being accepted. It is used to add the student to the appropriate role (Tutor or TA)
     * for that course.
     * @param year The "year" parameter represents the academic year for which the students are being
     * accepted. It could be a string or an integer value representing the year.
     * @param selectedStudents A set of Student objects representing the students who have been
     * selected for acceptance.
     * @param applicantRole The applicantRole parameter is of type Role, which is an enumeration
     * representing the role of the student applicant. It can have two possible values: Role.Tutor or
     * Role.TA.
     * @return The method is returning a boolean value of true.
     */
    public static boolean acceptStudents(String courseCode, String year, Set<Student> selectedStudents,
            Role applicantRole) {

        for (Student student : selectedStudents) {

            // updates the application status in students table
            setACCEPTEDapplicationStatus(student.getStudentID());

            // adds the student to either ta or tutor
            if (applicantRole == Role.Tutor) {
                addTutor(student.getStudentID(), courseCode, year);
            } else if (applicantRole == Role.TA) {
                addTA(student.getStudentID(), courseCode, year);
            }

            // removes the application from the applications table
            removeApplication(student.getStudentID());

        }
        return true;
    }

    /**
     * Updates the application status of a student in the database and returns true
     * ifsuccessful.
     * 
     * @param studentID String of the unique identifier for a student; used to
     *                  search in the database.
     * @return A boolean value - True true if the update operation was successful
     *         and at least one row was affected, and false otherwise.
     */
    public static boolean updateApplicationStatus(String studentID) {
        int rA = 0;

        try (Connection connection = getConnection()) {
            String query = "UPDATE Students SET applicationStatus = ? WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, ApplicationStatus.APPLIED.name());
            preparedStatement.setString(2, studentID);

            int rowsAffected = preparedStatement.executeUpdate();
            rA = rowsAffected;

            return rA > 0;
        } catch (SQLException e) {
            e.printStackTrace();

            return false;
        }

    }

    /**
     * Retrieves the application status of a student from the database based on
     * their student ID.
     * 
     * @param studentID String of the unique identifier for a student; used to
     *                  search in the database.
     * @return An ApplicationStatus object.
     */
    public static ApplicationStatus getApplicationStatus(String studentID) {

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Students WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return ApplicationStatus.valueOf(resultSet.getString("applicationStatus"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of completed courses of a student from the database based on
     * their student ID.
     * 
     * @param studentID String of the unique identifier for a student; used to
     *                  search in the database.
     * @return An ApplicationStatus object.
     */
    private static List<CompletedCourse> getCompletedCourses(String studentID) {

        List<CompletedCourse> completedCourses = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM CompletedCourses WHERE studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                CompletedCourse completedCourse = new CompletedCourse(
                        resultSet.getString("courseCode"),
                        resultSet.getDouble("grade"),
                        resultSet.getInt("year"));

                completedCourses.add(completedCourse);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return completedCourses;
    }

    // #endregion

    // #region // !! ADMIN COURSE FUNCTIONALITIES - Displaying, Creating, Updating,
    // Deleting

    /**
     * This method gets all the course codes as a list of strings from the DB
     * 
     * @return
     */
    public static List<String> getCourseCodes() {

        List<String> courseCodesList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Courses";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                courseCodesList.add(resultSet.getString("courseCode"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseCodesList;
    }

    /**
     * Retrieves all courses from a database and returns them as a list.
     * 
     * @return List of Course objects.
     */
    public static List<Course> getAllCourses() {

        List<Course> courseList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Courses";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Course course = new Course(
                        resultSet.getString("courseCode"),
                        resultSet.getString("courseName"),
                        Integer.parseInt(resultSet.getString("tutorCapacity")),
                        Integer.parseInt(resultSet.getString("TACapacity")));

                courseList.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courseList;
    }

    /**
     * Creates a new course in a database table and returns true if the operation is
     * successful.
     * 
     * @param newCourse The parameter `newCourse` is an object of the `Course`
     *                  class. It contains the
     *                  information about the course that needs to be created, such
     *                  as the course code, course name,
     *                  tutor capacity, and TA capacity.
     * @return The method is returning a boolean value. It returns true if the
     *         course was successfully
     *         created and inserted into the database, and false otherwise.
     */
    public static boolean createCourse(Course newCourse) {
        int rA = 0;

        try (Connection connection = getConnection()) {

            String query = "INSERT INTO Courses (courseCode, courseName, tutorCapacity, TACApacity) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newCourse.getCourseCode());
            preparedStatement.setString(2, newCourse.getName());
            preparedStatement.setString(3, Integer.toString(newCourse.getTutorCapacity()));
            preparedStatement.setString(4, Integer.toString(newCourse.getTaCapacity()));

            int rowsAffected = preparedStatement.executeUpdate();
            rA = rowsAffected;

            return rA > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function `deleteCourse` deletes a course and all related records from multiple tables in a
     * database.
     * 
     * @param course The "course" parameter is an object of the Course class. It represents the course
     * that needs to be deleted from the database.
     * @return The method is returning a boolean value. It returns true if any rows were affected
     * during the deletion process, indicating that the course and its related records were
     * successfully deleted. It returns false if no rows were affected, indicating that the deletion
     * was unsuccessful.
     */
    public static boolean deleteCourse(Course course) {

        String courseCode = course.getCourseCode();

        int rowsAffected = 0;

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit to ensure all deletes happen atomically
            // Step 0: Delete records from AccessibleCourses
            String query = "DELETE FROM AccessibleCourses WHERE courseCode = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 0: Delete records from CompletedCourses
            query = "DELETE FROM CompletedCourses WHERE courseCode = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 0: Delete records from CompletedCourses
            query = "DELETE FROM TAs WHERE courseCode = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 1: Delete records from TutoringSessionTutors
            query = "DELETE FROM TutoringSessionTutors WHERE tutSessionID IN (SELECT ts.tutSessionID FROM TutoringSessions ts WHERE ts.scheduleID IN (SELECT scheduleID FROM Schedules WHERE courseID = ?))";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 2: Delete records from Tutors
            query = "DELETE FROM Tutors WHERE courseCode = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 3: Delete records from TutoringSessions
            query = "DELETE FROM TutoringSessions WHERE scheduleID IN (SELECT scheduleID FROM Schedules WHERE courseID = ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 4: Delete records from Schedules
            query = "DELETE FROM Schedules WHERE courseID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // Step 5: Delete the course itself
            query = "DELETE FROM Courses WHERE courseCode = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            rowsAffected += preparedStatement.executeUpdate();

            // If all deletions were successful, commit the transaction
            connection.commit();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Updates a course in a database with the provided new course information.
     * 
     * @param course    Course that you want to update.
     * @param newCourse Course that represents the updated course information.
     * @return A boolean value. It returns true if the update operation was
     *         successful and at least one row was affected in the database. It
     *         returns false if the update
     *         operation was not successful or no rows were affected.
     */
    public static boolean updateCourse(Course course, Course newCourse) {

        int rA = 0;

        try (Connection connection = getConnection()) {

            String query = "UPDATE Courses SET courseCode = ?, courseName = ?, tutorCapacity = ?, TACapacity = ? WHERE courseCode = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newCourse.getCourseCode());
            preparedStatement.setString(2, newCourse.getName());
            preparedStatement.setString(3, Integer.toString(newCourse.getTutorCapacity()));
            preparedStatement.setString(4, Integer.toString(newCourse.getTaCapacity()));
            preparedStatement.setString(5, course.getCourseCode());

            int rowsAffected = preparedStatement.executeUpdate();
            rA = rowsAffected;

            return rA > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            ;
        }

        return false;
    }

    /**
     * Adds a set of lecturers to a course with a specified role and year by
     * inserting
     * records into the AccessibleCourses table in a database.
     * 
     * @param selectedEmps Set of Lecturer objects representing the employees
     *                     selected to be added to the course.
     * @param course       Course to which the employees will be added.
     * @param role         Role, which is an enum representing the role of the
     *                     employee in the course.
     * @param year         Integer of the Year in which the employee is being added
     *                     to the course.
     * @return Boolean value. It returns true if the number of rows inserted
     *         into the database is greater than the number of selected employees,
     *         indicating that all selected
     *         employees were successfully added to the course. Otherwise, it
     *         returns false.
     */
    public static boolean addEmpToCourse(Set<Lecturer> selectedEmps, Course course, Role role, int year) {

        int rowsInserted = 0;

        try (Connection connection = getConnection()) {

            for (Lecturer emp : selectedEmps) {

                String q = "INSERT INTO AccessibleCourses (empID, courseCode, role, year) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(q);

                ps.setString(1, emp.getEmployeeID());
                ps.setString(2, course.getCourseCode());
                ps.setString(3, role.name());
                ps.setString(4, Integer.toString(year));

                rowsInserted += ps.executeUpdate();
            }

            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function checks if a lecturer has access to a specific course in a given year.
     * 
     * @param lectuer The parameter "lectuer" is of type Lecturer, which is a class representing a
     * lecturer or employee. It likely has properties such as employeeID, name, email, etc.
     * @param course The "course" parameter is an object of the Course class. It represents a specific
     * course and contains information such as the course code, course name, and other details related
     * to the course.
     * @param year The "year" parameter represents the year for which you want to check if the employee
     * exists for a specific course.
     * @return The method returns a boolean value. It returns true if the employee exists in the
     * AccessibleCourses table for the given lecturer, course, and year. It returns false otherwise.
     */
    public static boolean doesEmployeeExist(Lecturer lectuer, Course course, int year) {
        try (Connection connection = getConnection()) {

            String query = "SELECT COUNT(*) FROM AccessibleCourses WHERE empID = ? AND courseCode = ? AND year = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, lectuer.getEmployeeID());
            preparedStatement.setString(2, course.getCourseCode());
            preparedStatement.setString(3, Integer.toString(year));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                System.out.println(count);
                return count > 0; // if count > 0, email exists
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // #endregion

    // #region // !! PER COURSE FUNCTIONALITIES (COURSE VIEW)

    /**
     * Retrieves a list of courses accessible by a given employee ID from a
     * database.
     * 
     * @param employeeID String that represents the ID of an employee.
     * @return List of Course objects.
     */
    public static List<Course> getCoursesForEmployee(String employeeID) {

        List<Course> courseList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            String query = "SELECT Courses.courseCode, Courses.courseName, Courses.tutorCapacity, Courses.TACapacity, AccessibleCourses.year "
                    +
                    "FROM Courses " +
                    "INNER JOIN AccessibleCourses ON Courses.courseCode = AccessibleCourses.courseCode " +
                    "WHERE AccessibleCourses.empID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, employeeID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Course course = new Course(
                        resultSet.getString("courseCode"),
                        resultSet.getString("courseName"),
                        resultSet.getInt("tutorCapacity"),
                        resultSet.getInt("TACapacity"));

                course.setYear(resultSet.getInt("year"));
                courseList.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseList;
    }

    /**
     * Retrieves a list of courses accessible by a given student ID from a database.
     * 
     * @param studentID String that represents the ID of a student.
     * @return List of Course objects.
     */
    public static List<Course> getCoursesForStudent(String studentID) {
        List<Course> courseList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            // Query for TAs
            String taQuery = "SELECT Courses.courseCode, Courses.courseName, Courses.tutorCapacity, Courses.TACapacity, TAs.year "
                    +
                    "FROM Courses " +
                    "INNER JOIN TAs ON Courses.courseCode = TAs.courseCode " +
                    "WHERE TAs.studID = ?";

            PreparedStatement taStatement = connection.prepareStatement(taQuery);
            taStatement.setString(1, studentID);
            ResultSet taResultSet = taStatement.executeQuery();

            // Query for Tutors
            String tutorQuery = "SELECT Courses.courseCode, Courses.courseName, Courses.tutorCapacity, Courses.TACapacity, Tutors.year "
                    +
                    "FROM Courses " +
                    "INNER JOIN Tutors ON Courses.courseCode = Tutors.courseCode " +
                    "WHERE Tutors.stuID = ?";

            PreparedStatement tutorStatement = connection.prepareStatement(tutorQuery);
            tutorStatement.setString(1, studentID);
            ResultSet tutorResultSet = tutorStatement.executeQuery();

            // Process TAs
            while (taResultSet.next()) {
                Course course = new Course(
                        taResultSet.getString("courseCode"),
                        taResultSet.getString("courseName"),
                        taResultSet.getInt("tutorCapacity"),
                        taResultSet.getInt("TACapacity"));

                course.setYear(taResultSet.getInt("year"));
                courseList.add(course);
            }

            // Process Tutors
            while (tutorResultSet.next()) {
                Course course = new Course(
                        tutorResultSet.getString("courseCode"),
                        tutorResultSet.getString("courseName"),
                        tutorResultSet.getInt("tutorCapacity"),
                        tutorResultSet.getInt("TACapacity"));

                course.setYear(tutorResultSet.getInt("year"));
                courseList.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courseList;
    }

    /**
     * This method returns a course object given a specific course code
     * 
     * @param courseCode
     * @return
     */
    public static Course getCourseFor(String courseCode) {
        // atm it is implemented to just fetch the data from the courses table and send
        // back a course object
        // we can extend this later to include more complicted SQL queries to get more
        // data.

        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Courses WHERE courseCode = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            Course course = null;

            if (resultSet.next()) {
                int tutorCapacity = Integer.parseInt(resultSet.getString("tutorCapacity"));
                int taCapacity = Integer.parseInt(resultSet.getString("TACapacity"));
                course = new Course(courseCode, resultSet.getString("courseName"), tutorCapacity, taCapacity);
            }

            return course;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // if no course is found
        return null;
    }

    // #endregion

    // #region // !! SCHEDULE FUNCTIONALITIES

    /**
     * This method adds a courses schedule to the relevant tables
     * TODOL test this method
     * 
     * @param schedule
     * @return boolean
     */
    public static int createSchedule(Schedule schedule) {

        int scheduleID = -1;

        try (Connection connection = getConnection()) {

            // step 1 - create en entry into the schedules table

            String query = "INSERT INTO Schedules (courseID, year) VALUES (?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, schedule.getCourse().getCourseCode());
            preparedStatement.setString(2, getCurrentYear());

            preparedStatement.executeUpdate();

            System.out.println("TEST");

            // step 1.5 - figure out what the scheduleID is (since it's auto-incremented)
            query = "SELECT * FROM Schedules WHERE courseID = ? AND year = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, schedule.getCourse().getCourseCode());
            preparedStatement.setString(2, getCurrentYear());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                scheduleID = resultSet.getInt("scheduleID");
                return scheduleID;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return scheduleID;

    }

    /**
     * The function retrieves the schedule ID for a given course code and year from a database table.
     * 
     * @param courseCode The course code is a string that represents the code or identifier for a
     * specific course. It is used to uniquely identify a course in the database.
     * @param year The "year" parameter is an integer representing the year for which you want to
     * retrieve the schedule ID.
     * @return The method is returning an integer value, which is the scheduleID. If the scheduleID is
     * found in the database, it will be returned. If the scheduleID is not found, it will return -1.
     */
    private static int getScheduleID(String courseCode, int year) {

        try (Connection connection = getConnection()) {

            String query = "SELECT scheduleID FROM Schedules WHERE courseID = ? AND year = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, courseCode);
            preparedStatement.setString(2, Integer.toString(year));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int scheduleID = resultSet.getInt("scheduleID");
                return scheduleID;
            } else {
                System.out.println("Could not find the scheduleID");
                return -1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * The function retrieves a list of assigned tutors for a given tutoring session ID.
     * 
     * @param sessionID The sessionID parameter is an integer that represents the ID of a tutoring
     * session.
     * @return The method is returning an ArrayList of Tutor objects.
     */
    private static ArrayList<Tutor> getAssignedTutors(int sessionID) {
        ArrayList<Tutor> asssignedTutors = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT S.email FROM TutoringSessionTutors TT JOIN Students S ON TT.studentID = S.studentID WHERE TT.tutSessionID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Integer.toString(sessionID));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String email = resultSet.getString("email");
                Student student = getStudent(email);

                Tutor tutor = new Tutor(
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail(),
                        student.getStudentID(),
                        student.getQualificationLevel(),
                        null,
                        null);

                asssignedTutors.add(tutor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return asssignedTutors;
    }

    /**
     * The function retrieves tutoring sessions from the database based on a given schedule ID and
     * returns them as an ArrayList of TutoringSession objects.
     * 
     * @param scheduleID The `scheduleID` parameter is an integer that represents the ID of a schedule.
     * It is used to retrieve tutoring sessions associated with that particular schedule from the
     * database.
     * @return The method is returning an ArrayList of TutoringSession objects.
     */
    private static ArrayList<TutoringSession> getTutoringSessions(int scheduleID) {
        ArrayList<TutoringSession> tutoringSessions = new ArrayList<>();

        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM TutoringSessions WHERE scheduleID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Integer.toString(scheduleID));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int sessionID = resultSet.getInt("tutSessionID");
                String day = resultSet.getString("day");
                String startTime = resultSet.getString("startTime");
                String endTime = resultSet.getString("endTime");
                String location = resultSet.getString("location");
                String whatsappLink = resultSet.getString("whatsappLink");
                int tutoringCapacity = resultSet.getInt("tutoringCapacity");

                // Create TutoringSession objects and add them to the list
                TutoringSession tutoringSession = new TutoringSession(
                        Double.parseDouble(startTime),
                        Double.parseDouble(endTime),
                        day,
                        location,
                        whatsappLink,
                        tutoringCapacity);

                // add the assigned tutors to this session
                ArrayList<Tutor> tutors = getAssignedTutors(sessionID);
                tutoringSession.setSignedUpTutors(tutors);
                tutoringSession.setSessionID(sessionID);

                tutoringSessions.add(tutoringSession);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tutoringSessions;
    }

    /**
     * The function retrieves a schedule for a given course code and year, creating a new schedule
     * entry if necessary, and populates it with tutoring sessions.
     * 
     * @param courseCode The course code is a string that represents the code or identifier for a
     * specific course. It is used to uniquely identify a course in the system.
     * @param year The "year" parameter represents the year for which the schedule is being retrieved.
     * It is an integer value that specifies the year.
     * @return The method is returning a Schedule object.
     */
    public static Schedule getSchedule(String courseCode, int year) {

        Schedule schedule = new Schedule();

        try (Connection connection = getConnection()) {

            int scheduleID = getScheduleID(courseCode, year);
            Course course = getCourseFor(courseCode);

            schedule.setCourse(course);

            if ((scheduleID == -1) || (course == null)) {
                // create a new entry into the scheules table
                scheduleID = createSchedule(schedule);
            }

            schedule.setScheduleID(scheduleID);

            // find all the tuturing sessions and add them to the course
            ArrayList<TutoringSession> sessions = getTutoringSessions(scheduleID);

            // add the sessoins to the schedule object
            schedule.setSlots(sessions);

            return schedule;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return schedule;
    }

    /**
     * The function adds a tutor to a tutoring session by inserting a new record into the
     * TutoringSessionTutors table.
     * 
     * @param tutoringSessionID The tutoringSessionID parameter is an integer that represents the ID of
     * the tutoring session to which you want to add a tutor.
     * @param studentID The studentID parameter is a String that represents the ID of the student who
     * will be added as a tutor to the tutoring session.
     * @return The method is returning a boolean value. It returns true if the tutor was successfully
     * added to the tutoring session, and false otherwise.
     */
    public static boolean addTutorToSession(int tutoringSessionID, String studentID) {
        try (Connection connection = getConnection()) {

            String query = "INSERT INTO TutoringSessionTutors (tutSessionID, studentID) VALUES (?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Integer.toString(tutoringSessionID));
            preparedStatement.setString(2, studentID);

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function removes a tutor from a tutoring session by deleting the corresponding entry in the
     * TutoringSessionTutors table.
     * 
     * @param tutoringSessionID The tutoringSessionID parameter is an integer that represents the ID of
     * the tutoring session from which you want to remove a tutor.
     * @param studentID The studentID parameter is a String that represents the ID of the student who
     * needs to be removed from the tutoring session.
     * @return The method is returning a boolean value. It returns true if the tutor was successfully
     * removed from the tutoring session, and false otherwise.
     */
    public static boolean removeTutorFromSession(int tutoringSessionID, String studentID) {
        try (Connection connection = getConnection()) {

            String query = "DELETE FROM TutoringSessionTutors WHERE tutSessionID = ? AND studentID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Integer.toString(tutoringSessionID));
            preparedStatement.setString(2, studentID);

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function checks if a student has already checked in for a tutoring session on a specific
     * date.
     * 
     * @param tutoringSessionID The tutoringSessionID parameter is an integer that represents the ID of
     * the tutoring session.
     * @param studentID The studentID parameter is a String that represents the ID of the student.
     * @param date The "date" parameter represents the date of the tutoring session.
     * @return The method is returning a boolean value. It returns true if there is a record in the
     * Attendance table that matches the given tutoringSessionID, studentID, and date. Otherwise, it
     * returns false.
     */
    public static boolean hasTutorAlreadyCheckedIn(int tutoringSessionID, String studentID, String date) {
        try (Connection connection = getConnection()) {

            String query = "SELECT * FROM Attendance WHERE studentID = ? AND date = ? AND tutoringSessionID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, Integer.toString(tutoringSessionID));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function `tutorCheckIn` checks if a tutor has already checked in for a tutoring session on a
     * specific date, and if not, inserts the attendance record into the database.
     * 
     * @param tutoringSessionID The tutoring session ID is a unique identifier for a specific tutoring
     * session. It is used to track attendance for that particular session.
     * @param courseCode The course code is a unique identifier for a specific course. It is typically
     * a combination of letters and numbers that represents a particular subject or topic. For example,
     * "CS101" could be the course code for an introductory computer science course.
     * @param studentID The student's ID, which is a unique identifier for each student.
     * @param date The "date" parameter is a string representing the date of the tutoring session. It
     * should be in a specific format, such as "YYYY-MM-DD".
     * @return The method is returning a boolean value. It returns true if the insertion into the
     * Attendance table was successful (i.e., rowsAffected > 0), and false otherwise.
     */
    public static boolean tutorCheckIn(int tutoringSessionID, String courseCode, String studentID, String date) {
        try (Connection connection = getConnection()) {

            // first check if they have not already checked in for that day
            if (hasTutorAlreadyCheckedIn(tutoringSessionID, studentID, date)) {
                return false; // ie: the tutorCheckIn failed
            }

            // else, continue with inserting into attendance
            String query = "INSERT INTO Attendance (studentID, date, tutoringSessionID, courseCode) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, Integer.toString(tutoringSessionID));
            preparedStatement.setString(4, courseCode);

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * The function `updateSchedule` updates a schedule by inserting new tutoring sessions, updating
     * existing tutoring sessions, and deleting old tutoring sessions.
     * 
     * @param oldSchedule The oldSchedule parameter is an object of the Schedule class representing the
     * existing schedule that needs to be updated.
     * @param newSchedule The newSchedule parameter is an object of the Schedule class, which
     * represents the updated schedule that needs to be applied. It contains a list of TutoringSession
     * objects, representing the new tutoring sessions.
     * @return The method is returning a boolean value. If the schedule update is successful, it
     * returns true. If there is an SQLException, it returns false.
     */
    public static boolean updateSchedule(Schedule oldSchedule, Schedule newSchedule) {

        try (Connection connection = getConnection()) {

            List<TutoringSession> oldSessions = oldSchedule.getTutoringSessions();
            List<TutoringSession> newSessions = newSchedule.getTutoringSessions();

            int scheduleID = oldSchedule.getScheduleID();

            // Identify new, updated, and deleted sessions
            for (TutoringSession newSession : newSessions) {
                if (!oldSessions.contains(newSession)) {
                    System.out.println("INSERTING " + newSession.getTutoringCapacity());

                    // New session
                    String q = "INSERT INTO TutoringSessions (scheduleID, day, startTime, endTime, tutoringCapacity, location, whatsappLink) VALUES (?, ?, ?, ?, ?, ?,?)";
                    PreparedStatement ps = connection.prepareStatement(q);

                    ps.setInt(1, scheduleID);
                    ps.setString(2, newSession.getDay());
                    ps.setString(3, Double.toString(newSession.getStartTimeAsDouble()));
                    ps.setString(4, Double.toString(newSession.getEndTimeAsDouble()));
                    ps.setInt(5, newSession.getTutoringCapacity());
                    ps.setString(6, newSession.getLocation());
                    ps.setString(7, newSession.getWhatsappLink());

                    ps.executeUpdate();

                } else {
                    System.out.println("UPDATING " + newSession.getTutoringCapacity());

                    String query = "UPDATE TutoringSessions SET day = ?, startTime = ?, endTime = ?, tutoringCapacity = ?, location = ?, whatsappLink = ? WHERE tutSessionID = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, newSession.getDay());
                    preparedStatement.setString(2,
                            Double.toString(newSession.getStartTimeAsDouble()));
                    preparedStatement.setString(3,
                            Double.toString(newSession.getEndTimeAsDouble()));
                    preparedStatement.setInt(4, newSession.getTutoringCapacity());
                    preparedStatement.setString(5, newSession.getLocation());
                    preparedStatement.setString(6, newSession.getWhatsappLink());
                    preparedStatement.setInt(7, newSession.getSessionID());

                    // for also update the assignedTytor

                    preparedStatement.executeUpdate();
                }
            }

            for (TutoringSession oldSession : oldSessions) {
                if (!newSessions.contains(oldSession)) {
                    System.out.println("DELETING " + oldSession.getTutoringCapacity());

                    // Deleted session
                    String q = "DELETE FROM TutoringSessions WHERE tutSessionID = ? AND scheduleID = ?";
                    PreparedStatement ps = connection.prepareStatement(q);
                    ps.setInt(1, oldSession.getSessionID());
                    ps.setInt(2, scheduleID);
                    ps.executeUpdate();

                    q = "DELETE FROM TutoringSessionTutors WHERE tutSessionID = ?";
                    ps = connection.prepareStatement(q);
                    ps.setInt(1, oldSession.getSessionID());
                    ps.executeUpdate();

                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // #endregion

    // #region // !! USER PER COURSE FUNCTIONALITIES

    /**
     * Retrieves a list of lecturers who are assigned to a specific course.
     * NOTE: sepearate lecture / convenor objects are created and downcased to fit
     * into a list<lectuer> since lectuer is the super type of cc
     * This means that the role is accessible via te upcasting when accessing the
     * returned objects in the list
     * 
     * @param course Course that represents a specific course for which we want to
     *               retrieve the list of employees.
     * @return List of Lecturer objects.
     * 
     * 
     * 
     */
    public static List<Lecturer> getEmployeesFor(String courseCode, String year) {

        List<Lecturer> employeeList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT DISTINCT Employees.firstName, Employees.lastName, Employees.email, Employees.employeeID, Employees.department, Employees.faculty, AccessibleCourses.role "
                    +
                    "FROM Employees " +
                    "INNER JOIN AccessibleCourses ON Employees.employeeID = AccessibleCourses.empID " +
                    "WHERE AccessibleCourses.courseCode = ? AND AccessibleCourses.year = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            preparedStatement.setString(2, year);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String role = resultSet.getString("role");

                Lecturer employee = null;

                if (role.equals("Lecturer")) {
                    employee = new Lecturer(
                            resultSet.getString("firstName"),
                            resultSet.getString("lastName"),
                            resultSet.getString("email"),
                            resultSet.getString("employeeID"),
                            resultSet.getString("department"),
                            resultSet.getString("faculty"));

                } else if (role.equals("CourseConvenor")) {
                    employee = new CourseConvenor(
                            resultSet.getString("firstName"),
                            resultSet.getString("lastName"),
                            resultSet.getString("email"),
                            resultSet.getString("employeeID"),
                            resultSet.getString("department"),
                            resultSet.getString("faculty"));
                }

                employeeList.add(employee);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeList;
    }

    /**
     * The function checks if a student is a tutor or a teaching assistant for a specific course and
     * year.
     * 
     * @param courseCode The course code is a unique identifier for a specific course. It is typically
     * a combination of letters and numbers that represents a particular subject or topic. For example,
     * "CS101" could be the course code for an introductory computer science course.
     * @param year The "year" parameter represents the academic year in which the course is being
     * taken. It could be a string representing the year, such as "2021-2022" or "2022".
     * @param studentID The studentID parameter is the unique identifier for the student. It is used to
     * check if the student is a tutor or a teaching assistant (TA) for a specific course and year.
     * @return The method is returning a Role enum value. The possible return values are Role.Tutor,
     * Role.TA, or Role.NoRole.
     */
    public static Role isTutorOrTA(String courseCode, String year, String studentID) {

        try (Connection connection = getConnection()) {

            // check the tutors table
            String query = "SELECT * FROM Tutors WHERE stuID = ? AND courseCode = ? AND year = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, year);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Role.Tutor;
            }

            query = "SELECT * FROM TAs WHERE studID = ? AND courseCode = ? AND year = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            preparedStatement.setString(3, year);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return Role.TA;
            }

            return Role.NoRole;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Role.NoRole;
    }

    /**
     * The function retrieves a list of active tutors based on the given course code and year.
     * 
     * @param courseCode The course code is a string that represents the code of the course for which
     * you want to retrieve active tutors. It is used as a filter in the SQL query to fetch tutors who
     * are associated with this specific course.
     * @param year The "year" parameter in the method represents the year in which the tutors are
     * active. It is used to filter the tutors based on their active year.
     * @return The method is returning a List of Tutor objects.
     */
    public static List<Tutor> getActiveTutors(String courseCode, String year) {

        List<Tutor> tutorList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT t.stuID, s.firstname, s.lastName, s.email, s.qualificationLevel, s.applicationStatus FROM Tutors t JOIN Students s ON t.stuID = s.studentID WHERE t.courseCode = ? AND t.year = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            preparedStatement.setString(2, year);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String studentID = resultSet.getString("stuID");
                List<CompletedCourse> courses = getCompletedCourses(studentID);

                Tutor tutor = new Tutor(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        studentID,
                        QualificationLevel.fromString(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        courses);

                tutorList.add(tutor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tutorList;
    }

    /**
     * The function retrieves a list of active TAs (Teaching Assistants) for a specific course and year
     * from a database.
     * 
     * @param courseCode The course code is a string that represents the code of the course for which
     * you want to retrieve the active TAs. It is used in the SQL query to filter the TAs based on the
     * course they are assigned to.
     * @param year The "year" parameter is used to specify the year for which you want to retrieve
     * active TAs. It is a String parameter that represents the year value.
     * @return The method is returning a List of TA objects.
     */
    public static List<TA> getActiveTAs(String courseCode, String year) {

        List<TA> taList = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT t.studID, s.firstname, s.lastName, s.email, s.qualificationLevel, s.applicationStatus FROM TAs t JOIN Students s ON t.studID = s.studentID WHERE t.courseCode = ? AND t.year = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            preparedStatement.setString(2, year);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                String studentID = resultSet.getString("studID");
                List<CompletedCourse> courses = getCompletedCourses(studentID);

                TA ta = new TA(
                        resultSet.getString("firstName"),
                        resultSet.getString("lastName"),
                        resultSet.getString("email"),
                        studentID,
                        QualificationLevel.fromString(resultSet.getString("qualificationLevel")),
                        ApplicationStatus.valueOf(resultSet.getString("applicationStatus")),
                        courses);

                taList.add(ta);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taList;
    }

    /**
     * The function retrieves a list of pending applicants for a specific course by querying the
     * database for students who have applied and have previously completed the parsed course.
     * 
     * @param courseCode The parameter `courseCode` is a String that represents the code of a course.
     * It is used in the SQL query to filter the results and retrieve the pending applicants for that
     * specific course.
     * @return The method is returning a List of Student objects.
     */
    public static List<Student> getPendingApplicants(String courseCode) {
        List<Student> students = new ArrayList<>();

        try (Connection connection = getConnection()) {

            String query = "SELECT S.email FROM Students S WHERE S.studentID IN ( SELECT A.studentID FROM Applications A JOIN CompletedCourses C ON A.studentID = C.studentID WHERE S.applicationStatus = 'APPLIED' AND C.courseCode = ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                students.add(getStudent(resultSet.getString("email")));
            }

            return students;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    // #endregion

    // #region // !! STATISTICS

    /**
     * The function retrieves the number of attendance entries for a specific student in a specific
     * course.
     * 
     * @param courseCode The courseCode parameter is a String that represents the code of the course
     * for which you want to get the tutor statistics.
     * @param studentID The studentID parameter is a String that represents the unique identifier of a
     * student.
     * @return The method is returning the number of entries in the Attendance table for a specific
     * student and course combination.
     */
    public static int getTutorStatsFor(String courseCode, String studentID) {
        try (Connection connection = getConnection()) {

            String query = "SELECT COUNT(*) AS entryCount FROM Attendance WHERE studentID = ? AND courseCode = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, courseCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("entryCount");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Retrieves the number of entries in the Attendance table for a given tutoring
     * session ID.
     * 
     * @param tutoringSessionID The `tutoringSessionID` parameter is an integer that represents the ID
     * of the tutoring session for which you want to retrieve the statistics.
     * @return The method is returning the count of entries in the Attendance table for a specific
     * tutoring session ID.
     */
    public static int getTutoringSessionStatsFor(int tutoringSessionID) {
        try (Connection connection = getConnection()) {

            String query = "SELECT COUNT(*) AS entryCount FROM Attendance WHERE tutoringSessionID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, Integer.toString(tutoringSessionID));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("entryCount");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Retrieves the number of entries in the Attendance table for a given course code.
     * 
     * @param courseCode String that represents the code of a course. It is used to filter the
     * attendance records in the database and retrieve the count of entries for that specific course.
     * @return Count of entries in the Attendance table for the given courseCode.
     */
    public static int getCourseStatsFor(String courseCode) {
        try (Connection connection = getConnection()) {

            String query = "SELECT COUNT(*) AS entryCount FROM Attendance WHERE courseCode = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, courseCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("entryCount");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // #endregion
}