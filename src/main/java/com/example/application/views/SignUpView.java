package com.example.application.views;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.PageTitle;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.Dialogs;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.models.CompletedCourse;
import com.example.application.models.Student;
import com.example.application.microservices.CourseManager;
import com.example.application.microservices.SignInManager;
import com.example.application.microservices.SignUpManager;

/**
 * View for the Sign Up form in the MyTutor application.
 * It contains fields for entering personal and course-related information, such
 * as name, email, student number, course details, and then entering the courses that
 * they have completed. If successful, it creates their profile.
 */
@PageTitle("Sign Up | MyTutor")
@Route("sign-up")
@AnonymousAllowed
public class SignUpView extends VerticalLayout {

    // User Details Fields
    private TextField firstNameField;
    private TextField lastNameField;
    private ComboBox<QualificationLevel> qualificationLevelCombo;
    private EmailField emailField;
    private TextField studentNumberField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    
    // Completed Courses Fields
    private ComboBox<String> coursesCombo;
    private NumberField gradeField;
    private IntegerField yearTakenField;
     private Grid<CompletedCourse> completedCoursesGrid = new Grid<>(CompletedCourse.class);

    // List to store selected courses and grades
    private List<CompletedCourse> completedCourses = new ArrayList<>();
   
    /**
     * Creates a new instance of the Sign Up View.
     */
    public SignUpView() {
        configureLayout();
    }

    /**
     * Configures the layout of the application form.
     */
    private void configureLayout() {
        setSizeFull();
        setMinWidth("800px");
        setMinHeight("675px");
        getStyle().set("transform", "scale(0.8)");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Adding the sign-up heading
        H2 heading = new H2("MyTutor Sign Up");
        add(heading);

        // Create a bordered content layout
        VerticalLayout contentLayout = createBorderedLayout();
        addFormFields(contentLayout);
        add(contentLayout);

        addButtons();
    }

    /**
     * Creates a layout with a border to contain the form content.
     *
     * @return The bordered layout for the form content.
     */
    private VerticalLayout createBorderedLayout() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setSpacing(false);
        contentLayout.setPadding(false);
        contentLayout.getStyle().set("border", "1.5px solid #ddd");
        contentLayout.getStyle().set("border-radius", "15px");
        contentLayout.getStyle().set("box-shadow", "0px 0px 10px rgba(0, 0, 0, 0.2)");
        contentLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        contentLayout.setAlignItems(Alignment.CENTER);
        return contentLayout;
    }

    /**
     * Adds form fields to the given parent layout.
     *
     * @param parentLayout The layout to which form fields are added.
     */
    private void addFormFields(VerticalLayout parentLayout) {
        parentLayout.add(createUserDetailsLayout());
        parentLayout.add(createCompletedCoursesLayout());
    }

    /**
     * Adds Submit and Cancel buttons to the layout.
     */
    private void addButtons() {
        Button submitButton = new Button("Submit", event -> submitClick());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> cancelClick());

        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton, cancelButton);
        buttonLayout.setSpacing(true);

        add(buttonLayout);
    }

    /**
     * Creates a layout containing fields for entering user details.
     *
     * @return The layout containing user detail input fields.
     */
    private VerticalLayout createUserDetailsLayout() {
        // Create user detail input fields here
        
        // Create a TextField for the firstname
        firstNameField = new TextField("First Name");
        firstNameField.setWidth("35%");
        firstNameField.setPlaceholder("Enter your first name here...");
        firstNameField.setRequired(true);
        firstNameField.setPrefixComponent(VaadinIcon.USER.create());
        firstNameField.setClearButtonVisible(true);

        // Create a TextField for the surname
        lastNameField = new TextField("Last Name");
        lastNameField.setWidth("35%");
        lastNameField.setPlaceholder("Enter your last name here...");
        lastNameField.setRequired(true);
        lastNameField.setPrefixComponent(VaadinIcon.USER.create());
        lastNameField.setClearButtonVisible(true);

        // Create a ComboBox for years (manually selectable)
        qualificationLevelCombo = new ComboBox<>();
        qualificationLevelCombo.setLabel("Qualification Level");
        qualificationLevelCombo.setClearButtonVisible(true);

        // Get the values from the QualificationLevel enum and set them
        qualificationLevelCombo.setItems(QualificationLevel.values());

        qualificationLevelCombo.setPlaceholder("Level");
        qualificationLevelCombo.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());
        qualificationLevelCombo.setWidth("30%");
        qualificationLevelCombo.setRequired(true);

        // Create a HorizontalLayout to hold the firstname, surname, and qualification level
        HorizontalLayout firstLayout = new HorizontalLayout(firstNameField, lastNameField, qualificationLevelCombo);
        firstLayout.setWidth("100%");

        // Create a TextField for email address
        emailField = new EmailField("Email");
        emailField.setWidth("70%");
        emailField.setPlaceholder("Enter your email here...");
        emailField.setRequired(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setClearButtonVisible(true);

        // Create a TextField for student number
        studentNumberField = new TextField("Student Number");
        studentNumberField.setWidth("30%");
        studentNumberField.setPlaceholder("ABCXYZ123");
        studentNumberField.setRequired(true);
        studentNumberField.setPrefixComponent(VaadinIcon.USER_CARD.create());
        studentNumberField.setClearButtonVisible(true);

        // Create a HorizontalLayout to hold the email address and student number
        HorizontalLayout secondLayout = new HorizontalLayout(emailField, studentNumberField);
        secondLayout.setWidth("100%");

        // Create a PasswordField
        passwordField = new PasswordField("Password");
        passwordField.setWidth("50%");
        passwordField.setPlaceholder("Enter your password here...");
        passwordField.setRequired(true);
        passwordField.setRevealButtonVisible(true); // Show/hide password toggle
        passwordField.setPrefixComponent(VaadinIcon.PASSWORD.create());
        passwordField.setClearButtonVisible(true);

        // Create a PasswordField for confirmation
        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidth("50%");
        confirmPasswordField.setPlaceholder("Confirm your password...");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setRevealButtonVisible(true); // Show/hide password toggle
        confirmPasswordField.setPrefixComponent(VaadinIcon.PASSWORD.create());
        confirmPasswordField.setClearButtonVisible(true);

        // Create a layout with the password fields
        HorizontalLayout passwordLayout = new HorizontalLayout(passwordField, confirmPasswordField);
        passwordLayout.setWidth("100%");

        // Create a VerticalLayout to hold all user-details form layouts
        VerticalLayout layout = new VerticalLayout(firstLayout, secondLayout, passwordLayout);
        layout.setSpacing(false);
        layout.getStyle().set("padding-top", "0px");
        layout.getStyle().set("padding-bottom", "0px");
        layout.setWidth("100%");
        return layout;
    }

    /**
     * Creates a layout containing fields for entering completed courses,
     *
     * @return The layout containing course-related input fields.
     */
    private VerticalLayout createCompletedCoursesLayout() {
        // Create completed course input fields here

        // Combo box for Course Code
        coursesCombo = new ComboBox<>();
        coursesCombo.setWidth("40%");
        coursesCombo.setLabel("Add a Course you have Completed");

        // Get the values from the Courses enum and set them as items in the ComboBox
        updateCoursesCombo();

        coursesCombo.setPlaceholder("Course Code");
        coursesCombo.setPrefixComponent(LineAwesomeIcon.SCHOOL_SOLID.create());
        coursesCombo.setClearButtonVisible(true);

        // Add a grade field
        gradeField = new NumberField("Grade [%]");
        gradeField.setWidth("20%");
        gradeField.setSuffixComponent(new Div(new Span("%")));
        gradeField.setPlaceholder("##");
        gradeField.setClearButtonVisible(true);
        gradeField.setMin(50);
        gradeField.setMax(100);

        // Add a year field
        yearTakenField = new IntegerField("Year Taken");
        yearTakenField.setWidth("20%");
        yearTakenField.setPlaceholder("YYYY");
        yearTakenField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        yearTakenField.setClearButtonVisible(true);
        yearTakenField.setMin(1950);
        yearTakenField.setMax(2023);

        // Add a button to add selected courses to the list
        Button addCourseButton = new Button("Add Course");
        addCourseButton.setWidth("20%");
        addCourseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCourseButton.addClickListener(event -> addCourseToDisplay());

        HorizontalLayout completedCourses = new HorizontalLayout(coursesCombo, gradeField, yearTakenField, addCourseButton);
        completedCourses.setAlignItems(Alignment.BASELINE);
        completedCourses.setWidth("100%");

        // Configure the completedCoursesGrid
        completedCoursesGrid.getStyle().set("border-radius", "15px");
        completedCoursesGrid.getStyle().set("overflow", "hidden");
        completedCoursesGrid.getStyle().set("margin-top", "16px");
        completedCoursesGrid.setWidth("100%");
        completedCoursesGrid.setHeight("300px");
        completedCoursesGrid.setColumns("courseCode", "grade", "year");
        completedCoursesGrid.getColumnByKey("courseCode").setHeader("Course Code");
        completedCoursesGrid.getColumnByKey("grade").setHeader("Grade [%]");
        completedCoursesGrid.getColumnByKey("year").setHeader("Year Taken [YYYY]");

        // Add a column for removing courses
        completedCoursesGrid.addComponentColumn(this::createRemoveButton).setHeader("Actions");

        // Disable multi-select on the grid
        completedCoursesGrid.setSelectionMode(Grid.SelectionMode.NONE);

        // Create a VerticalLayout to hold the course selection and display
        VerticalLayout layout = new VerticalLayout(completedCourses, completedCoursesGrid);
        layout.setSpacing(false);
        layout.getStyle().set("padding-top", "0px");
        layout.setWidth("100%");
        return layout;
    }

    /**
     * Creates a remove button with a specific theme variant and click listener to remove
     * a course from a grid.
     * 
     * @param course    A CompletedCourse, that will be used to remove the course
     *                  from a grid or any other data structure.
     * @return The method is returning a Button object.
     */
    private Button createRemoveButton(CompletedCourse course) {
        Button removeButton = new Button("Remove");
        removeButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                                        ButtonVariant.LUMO_ERROR,
                                        ButtonVariant.LUMO_PRIMARY);
        removeButton.setIcon(VaadinIcon.TRASH.create());
        removeButton.addClickListener(event -> removeCourseFromGrid(course));
        return removeButton;
    }

    /**
     * Removes a completed course from the grid, updates the grid's items, and updates a combo
     * box with the updated list of courses.
     * 
     * @param course    A CompletedCourse; represents the course that needs
     *                  to be removed from the completedCourses list and the grid.
     */
    private void removeCourseFromGrid(CompletedCourse course) {
        completedCourses.remove(course);
        completedCoursesGrid.setItems(completedCourses);
        updateCoursesCombo();
    }

    /**
     * Updates a combo box with a list of courses,
     * excluding any courses that have alread been completed.
     */
    private void updateCoursesCombo() {
        
        //*******************************************************
        List<String> allCourses = CourseManager.getCourseCodes();
        //*******************************************************
        String[] selectedCourseCodes;
        
        // Check if completedCourses is null or empty
        if (completedCourses == null || completedCourses.isEmpty()) {
            selectedCourseCodes = new String[0]; // Initialize as an empty array
        } else {
            selectedCourseCodes = new String[completedCourses.size()];

            // Populate selectedCourseCodes from completedCourses
            for (int i = 0; i < completedCourses.size(); i++) {
                selectedCourseCodes[i] = completedCourses.get(i).getCourseCode();
            }
        }

        String[] out = new String[allCourses.size() - selectedCourseCodes.length];
        int outIndex = 0;

        for (String course : allCourses) {
            // Check if the courseCode is not in selectedCourseCodes
            if (!Arrays.asList(selectedCourseCodes).contains(course)) {
                out[outIndex] = course;
                outIndex++;
            }
        }

        // Check if out is empty
        if (out.length == 0) {
            coursesCombo.setEnabled(false);
        } else {
            coursesCombo.setItems(out);
            coursesCombo.setEnabled(true);
        }
    }

    /**
     * Adds the selected course to the display.
     */
    private void addCourseToDisplay() {

        // Empty check
        if (coursesCombo.isEmpty() || gradeField.isEmpty() || yearTakenField.isEmpty()) {
            
            if (coursesCombo.isEmpty()) {
                coursesCombo.setInvalid(true);
            }

            if (gradeField.isEmpty()) {
                gradeField.setInvalid(true);
            }

            if (yearTakenField.isEmpty()) {
                yearTakenField.setInvalid(true);
            }
            
            Dialogs.showDialog(Response.EMPTY_FIELD);
            return;
        }

        String selectedCourse = coursesCombo.getValue();
        double grade = gradeField.getValue();
        int year = yearTakenField.getValue();

        // Validating grade
        if (grade < 50.0 || grade > 100.0) {
            Dialogs.showDialog(Response.INVALID_GRADE);
            gradeField.setInvalid(true);
            return;
        }
        
        // Validating year taken
        if (year < 1950 || year > 2022) {
            Dialogs.showDialog(Response.INVALID_YEAR);
            yearTakenField.setInvalid(true);
            return;
        }

        if (selectedCourse != null) {
            CompletedCourse course = new CompletedCourse(selectedCourse, grade, year);
            completedCourses.add(course);

            // Update the CompletedCourses grid
            completedCoursesGrid.setItems(completedCourses);

            // Update the Courses ComboBox
            updateCoursesCombo();
        }

        coursesCombo.setValue(null);
        gradeField.setValue(null);
        gradeField.setInvalid(false);
        yearTakenField.setValue(null);
        yearTakenField.setInvalid(false);
    }

    /**
     * Handles the event when the "Submit" button is clicked.
     * Displays a success dialog and navigates to the welcome view.
     */
    private void submitClick() {

        if (emptyFieldCheck()) {
            Dialogs.showDialog(Response.EMPTY_FIELD);
            return;
        }

        if (completedCourses.isEmpty()) {
            Dialogs.showDialog("Sorry! Please add at least one course...");
            return;
        }

        Student newStudent = new Student(
            firstNameField.getValue(),
            lastNameField.getValue(),
            emailField.getValue(),
            studentNumberField.getValue().toUpperCase(), 
            qualificationLevelCombo.getValue(),
            ApplicationStatus.IDLE,
            completedCourses
        );

        //************************************************************************************************************/
        // Invokes the apply method in the SignUpManager Microservice
        Response result = SignUpManager.signUp(newStudent, passwordField.getValue(), confirmPasswordField.getValue()); 
        //************************************************************************************************************/

        switch (result) {
            case SUCCESS:
                Dialogs.showDialog("Successful! Signing in now...");
                
                //********************************************************************************************/
                // Invoke the SignInMicroservice to interact with database
                Response signInResult = SignInManager.signIn(emailField.getValue(), passwordField.getValue());
                //********************************************************************************************/
                
                if (signInResult == Response.SUCCESS) {
                    UI.getCurrent().navigate(DashboardView.class);
                } else {
                    Dialogs.showDialog("Sorry! Something went wrong...");
                    UI.getCurrent().navigate(WelcomeView.class);
                }
                
                return;
            case SIGN_UP_NOT_SUCCESSFUL:
                Dialogs.showDialog(result);
                UI.getCurrent().navigate(WelcomeView.class);
                return;
            case USER_ALREADY_EXISTS:
                clearFields();
                break;
            case INVALID_EMAIL:
                emailField.setInvalid(true);
                break;
            case INVALID_STUDENTNUMBER:
                studentNumberField.setInvalid(true);
                break;
            case PASSWORD_MISMATCH:
                passwordField.setValue("");
                passwordField.setInvalid(true);
                confirmPasswordField.setValue("");
                confirmPasswordField.setInvalid(true);
                break;
            default:
                break;
        }

        Dialogs.showDialog(result);
    }

    /**
     * Handles the event when the "Cancel" button is clicked.
     * Navigates to the welcome view.
     */
    private void cancelClick() {
        UI.getCurrent().navigate(WelcomeView.class);
    }

    /**
     * Checks for empty fields in the form.
     *
     * @return True if there are empty fields, false otherwise.
     */
    private boolean emptyFieldCheck() {
        boolean hasEmptyField = false;

        if (firstNameField.getValue().isEmpty()) {
            firstNameField.setInvalid(true);
            hasEmptyField = true;
        }

        if (lastNameField.getValue().isEmpty()) {
            lastNameField.setInvalid(true);
            hasEmptyField = true;
        }

        if (qualificationLevelCombo.isEmpty()) {
            qualificationLevelCombo.setInvalid(true);
            hasEmptyField = true;
        }

        if (emailField.getValue().isEmpty()) {
            emailField.setInvalid(true);
            hasEmptyField = true;
        }

        if (studentNumberField.getValue().isEmpty()) {
            studentNumberField.setInvalid(true);
            hasEmptyField = true;
        }

        if (passwordField.getValue().isEmpty()) {
            passwordField.setInvalid(true);
            hasEmptyField = true;
        }

        if (confirmPasswordField.getValue().isEmpty()) {
            confirmPasswordField.setInvalid(true);
            hasEmptyField = true;
        }

        return hasEmptyField;
    }

    /**
     * Clears all input fields and updates the courses combo box.
     */
    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        qualificationLevelCombo.clear();
        emailField.clear();
        studentNumberField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        completedCourses.clear();
        completedCoursesGrid.setItems(completedCourses);
        updateCoursesCombo();
    }
}