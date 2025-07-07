package com.example.application.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

import com.example.application.Dialogs;

import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;
import com.example.application.microservices.CourseManager;
import com.example.application.microservices.SignInManager;
import com.example.application.microservices.UserManager;
import com.example.application.models.CompletedCourse;
import com.example.application.models.Lecturer;
import com.example.application.models.Person;
import com.example.application.models.Student;

/**
 * The ProfileView class represents the user's profile page.
 */
@PageTitle("My Profile | MyTutor")
@Route(value = "profile", layout = MainLayout.class)
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private Person currentPerson;
    private String password;
    private Role role;
    private List<CompletedCourse> completedCourses;
    private List<CompletedCourse> tempCompletedCourses;
    private QualificationLevel qualificationLevel;

    private TextField idField;
    private EmailField emailField;
    private TextField firstNameField;
    private TextField lastNameField;

    // Employee TextFields
    private TextField departmentField;
    private TextField facultyField;

    // Student CompletedCourses
    private ComboBox<QualificationLevel> qualificationLevelCombo;
    private VerticalLayout completedCoursesLayout;
    private ComboBox<String> coursesCombo;
    private NumberField gradeField;
    private IntegerField yearTakenField;
    private Accordion completedCoursesAccordion;
    private AccordionPanel completedCoursesAccordionPanel;
    private Grid<CompletedCourse> completedCoursesGrid = new Grid<>(CompletedCourse.class);

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    private Button editButton;
    private Button cancelButton;
    private Button changePasswordButton;

    /**
     * Constructs a ProfileView and initializes the UI components.
     */
    public ProfileView() {
        setMinWidth("220px");

        currentPerson = (Person) VaadinSession.getCurrent().getAttribute("personObject");
        password = (String) VaadinSession.getCurrent().getAttribute("password");
        role = (Role) VaadinSession.getCurrent().getAttribute("role");
        completedCourses = getCompletedCourses();

        tempCompletedCourses = new ArrayList<>(completedCourses.size());
        for (CompletedCourse course : completedCourses) {
            tempCompletedCourses.add(course);
        }

        getStyle().set("padding-top", "0px");

        // Create a FlexLayout to organize the components
        FlexLayout mainLayout = new FlexLayout();
        mainLayout.setFlexDirection(FlexDirection.COLUMN);
        mainLayout.setFlexWrap(FlexWrap.WRAP);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setWidthFull();

        FormLayout formLayout = new FormLayout();

        if (role != null) {
            idField = new TextField("ID");
            idField.setValue((String) VaadinSession.getCurrent().getAttribute("id"));
            idField.setReadOnly(true);

            emailField = new EmailField("Email");
            emailField.setValue((String) VaadinSession.getCurrent().getAttribute("email"));
            emailField.setReadOnly(true);

            firstNameField = new TextField("First Name");
            firstNameField.setValue((String) VaadinSession.getCurrent().getAttribute("firstName"));
            firstNameField.setEnabled(false);

            lastNameField = new TextField("Last Name");
            lastNameField.setValue((String) VaadinSession.getCurrent().getAttribute("lastName"));
            lastNameField.setEnabled(false);

            editButton = createEditButton();
            cancelButton = createCancelButton();
            changePasswordButton = createChangePasswordButton();

            HorizontalLayout buttonLayout = new HorizontalLayout(editButton, cancelButton);
            buttonLayout.setSpacing(true);
            buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            buttonLayout.setWidthFull();

            HorizontalLayout changePasswordButtonLayout = new HorizontalLayout(changePasswordButton);
            changePasswordButtonLayout.setSpacing(true);
            changePasswordButtonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            changePasswordButtonLayout.setWidthFull();

            VerticalLayout buttonContainer = new VerticalLayout(buttonLayout, changePasswordButtonLayout);
            buttonContainer.getStyle().set("padding-top", "64px");
            buttonContainer.setWidthFull();
            
            // Set alignment to the bottom for buttonContainer
            buttonContainer.getStyle().set("margin-top", "auto");

            // Adding components
            formLayout.add(idField, emailField, firstNameField, lastNameField);

            if ((role.name()).equals("Employee")) {
                departmentField = new TextField("Department");
                departmentField.setValue((String) VaadinSession.getCurrent().getAttribute("department"));
                departmentField.setEnabled(false);

                facultyField = new TextField("Faculty");
                facultyField.setValue((String) VaadinSession.getCurrent().getAttribute("faculty"));
                facultyField.setEnabled(false);

                formLayout.add(departmentField, facultyField);
            }

            if ((role.name()).equals("Student") || (role.name()).equals("Tutor") || (role.name()).equals("TA")) {
                
                //#region //!! QualificationLevelComboBox
                qualificationLevel = (QualificationLevel) VaadinSession.getCurrent().getAttribute("qualificationLevel");

                // Create a ComboBox for years (manually selectable)
                qualificationLevelCombo = new ComboBox<>("Qualification Level");
                qualificationLevelCombo.getStyle().set("padding-bottom", "32px");
                qualificationLevelCombo.setWidthFull();

                // Get the values from the QualificationLevel enum and set them
                qualificationLevelCombo.setItems(QualificationLevel.values());
                qualificationLevelCombo.setValue(qualificationLevel);
                qualificationLevelCombo.setEnabled(false);
                //#endregion

                formLayout.add(qualificationLevelCombo);
                mainLayout.add(formLayout);

                //#region //!! CompletedCourses
                completedCourses = getCompletedCourses();

                completedCoursesAccordion = new Accordion();
                completedCoursesAccordion.setWidthFull();

                // Combo box for Course Code
                coursesCombo = new ComboBox<>();
                coursesCombo.setWidth("25%");
                coursesCombo.setLabel("Add a Course you have Completed");

                // Get the values from the Courses enum and set them as items in the ComboBox
                updateCoursesCombo();

                coursesCombo.setPlaceholder("Course Code");
                coursesCombo.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());
                coursesCombo.setClearButtonVisible(true);

                // Add a grade field
                gradeField = new NumberField("Grade [%]");
                gradeField.setWidth("25%");
                gradeField.setSuffixComponent(new Div(new Span("%")));
                gradeField.setPlaceholder("##");
                gradeField.setClearButtonVisible(true);
                gradeField.setMin(50);
                gradeField.setMax(100);

                // Add a year field
                yearTakenField = new IntegerField("Year Taken");
                yearTakenField.setWidth("25%");
                yearTakenField.setPlaceholder("YYYY");
                yearTakenField.setPrefixComponent(VaadinIcon.CALENDAR.create());
                yearTakenField.setClearButtonVisible(true);
                yearTakenField.setMin(1950);
                yearTakenField.setMax(2023);

                // Add a button to add selected courses to the list
                Button addCourseButton = new Button("Add Course");
                addCourseButton.setWidth("25%");
                addCourseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                addCourseButton.addClickListener(event -> addCourseToDisplay());

                HorizontalLayout addCourseLayout = new HorizontalLayout(coursesCombo, gradeField, yearTakenField, addCourseButton);;
                addCourseLayout.setAlignItems(Alignment.BASELINE);
                addCourseLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
                addCourseLayout.setWidthFull();

                // Configure the completedCoursesGrid
                completedCoursesGrid.setItems(completedCourses);
                completedCoursesGrid.getStyle().set("margin-top", "16px");
                completedCoursesGrid.setHeight("200px");
                completedCoursesGrid.setColumns("courseCode", "grade", "year");
                completedCoursesGrid.getColumnByKey("courseCode").setHeader("Course Code");
                completedCoursesGrid.getColumnByKey("grade").setHeader("Grade [%]");
                completedCoursesGrid.getColumnByKey("year").setHeader("Year Taken [YYYY]");

                // Add a column for removing courses
                completedCoursesGrid.addComponentColumn(this::createRemoveButton).setHeader("Actions");

                // Disable multi-select on the grid
                completedCoursesGrid.setSelectionMode(Grid.SelectionMode.NONE);

                // Create a VerticalLayout to hold the course selection and display
                completedCoursesLayout = new VerticalLayout(addCourseLayout, completedCoursesGrid);
                completedCoursesLayout.setSpacing(false);
                completedCoursesLayout.getStyle().set("padding-top", "0px");
                completedCoursesLayout.setWidthFull();

                completedCoursesAccordionPanel = completedCoursesAccordion.add("Change your Completed Courses", completedCoursesLayout);
                completedCoursesAccordionPanel.setWidthFull();
                completedCoursesAccordionPanel.setEnabled(false);
                completedCoursesAccordionPanel.addThemeVariants(DetailsVariant.FILLED);
                //#endregion

                mainLayout.add(completedCoursesAccordionPanel);

            } else {
                mainLayout.add(formLayout);
            }

            mainLayout.add(buttonContainer);
            add(mainLayout);
        }
    }

    /**
     * SECURITY AUTHENTICATION
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if ((Role) VaadinSession.getCurrent().getAttribute("role") == null) {
            MainLayout.signOut(true);
        }
    }

    /**
     * Retrieves the list of completed courses from the current Vaadin session attribute,
     * casting it to the appropriate type if it exists, and returning an empty list if it doesn't.
     * 
     * @return List of CompletedCourse objects.
     */
    @SuppressWarnings("unchecked")
    public static List<CompletedCourse> getCompletedCourses() {
        Object attribute = VaadinSession.getCurrent().getAttribute("completedCourses");
        
        if (attribute instanceof List<?>) {
            return (List<CompletedCourse>) attribute;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Creates and configures an Edit button with a click listener that toggles between
     * enabling text fields for editing and saving the changes made in the text fields.
     * 
     * @return Button object for Edit.
     */
    private Button createEditButton() {
        editButton = new Button("Edit");

        editButton.addClickListener(event -> {
            if ("Edit".equals(editButton.getText())) {
                toggleTextFieldsForEditing(true);
                cancelButton.setVisible(true);
                editButton.setText("Save");
            
            } else if ("Save".equals(editButton.getText())) {

                if (emptyFieldCheck()) {
                    Dialogs.showDialog(Response.EMPTY_FIELD);
                    return;
                }

                editClick();
                editButton.setText("Edit");
                cancelButton.setVisible(false);
                toggleTextFieldsForEditing(false);
            }
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.setIcon(new Icon("lumo", "edit"));
        editButton.setWidth("15%");
        editButton.setMinWidth("110px");

        return editButton;
    }

    /**
     * Updates a person's profile and displays a success or error message.
     */
    private void editClick() {
        Person newPerson = createNewPerson();

        //**************************************************************************
        Response result = UserManager.update(currentPerson, newPerson, role.name());
        //**************************************************************************

        if (result == Response.SUCCESS) {
            Dialogs.showDialog("Your profile was successfully updated!");
            
            //********************************************
            SignInManager.setSession(newPerson, password);
            //********************************************

        } else {
            Dialogs.showDialog("Sorry! Something went wrong in updating your profile...Please try again!");
        }
    }

    /**
     * Creates a new person object and sets its properties based on the values of various
     * fields and the selected role.
     * 
     * @return Person object.
     */
    private Person createNewPerson() {
        Person newPerson = currentPerson;

        newPerson.setFirstName(firstNameField.getValue());
        newPerson.setLastName(lastNameField.getValue());

        switch (role) {
            case Employee:
                ((Lecturer) newPerson).setDepartment(departmentField.getValue());
                ((Lecturer) newPerson).setFaculty(facultyField.getValue());
                break;

            case Student:
                ((Student) newPerson).setQualificationLevel(qualificationLevelCombo.getValue());
                ((Student) newPerson).setCompletedCourses(completedCourses);
                break;

            default:
                break;
        }

        return newPerson;
    }

    /**
     * Creates a Cancel button with a specified label, width, and icon, and sets an event
     * listener to toggle text fields for editing.
     * 
     * @return Button object for Cancel.
     */
    private Button createCancelButton() {
        cancelButton = new Button("Cancel", event -> {
            toggleTextFieldsForEditing(false);

            // Reset back to original values
            idField.setValue((String) VaadinSession.getCurrent().getAttribute("id"));
            emailField.setValue((String) VaadinSession.getCurrent().getAttribute("email"));
            firstNameField.setValue((String) VaadinSession.getCurrent().getAttribute("firstName"));
            lastNameField.setValue((String) VaadinSession.getCurrent().getAttribute("lastName"));
        
            if ((role.name()).equals("Employee")) {
                departmentField.setValue((String) VaadinSession.getCurrent().getAttribute("department"));
                facultyField.setValue((String) VaadinSession.getCurrent().getAttribute("faculty"));
            }

            if ((role.name()).equals("Student") || (role.name()).equals("Tutor") || (role.name()).equals("TA")) {
                qualificationLevelCombo.setValue((QualificationLevel) VaadinSession.getCurrent().getAttribute("qualificationLevel"));

                System.out.println(tempCompletedCourses);

                completedCoursesGrid.setItems(tempCompletedCourses);
                updateCoursesCombo();
            }

            editButton.setText("Edit");
            cancelButton.setVisible(false);
        });
        cancelButton.setVisible(false);
        cancelButton.setWidth("15%");
        cancelButton.setMinWidth("110px");
        cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        return cancelButton;
    }

    /**
     * Creates a button with the label "Change your Password", an icon, and a width of
     * 25%, and returns the button.
     * 
     * @return Button object.
     */
    private Button createChangePasswordButton() {
        changePasswordButton = new Button("Change your Password", event -> {
            openPasswordChangingDialog();
        });
        changePasswordButton.setWidth("25%");
        changePasswordButton.setMinWidth("220px");
        changePasswordButton.setIcon(VaadinIcon.PASSWORD.create());

        return changePasswordButton;
    }

    /**
     * Opens a dialog box for the user to change their password, displaying current password,
     * new password, and confirm new password fields.
     */
    private void openPasswordChangingDialog() {
        // Opening user form dialog
        Dialog passwordChangingDialog = new Dialog();
        passwordChangingDialog.setHeaderTitle("Change your Password");

        // Create a layout to hold form fields and buttons
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);

        currentPasswordField = new PasswordField("Current Password");
        currentPasswordField.setWidthFull();
        currentPasswordField.getStyle().set("padding-top", "0px");
        currentPasswordField.setValue(password);
        currentPasswordField.setReadOnly(true);
        
        newPasswordField = new PasswordField("New Password");
        newPasswordField.getStyle().set("padding-top", "0px");
        newPasswordField.setWidthFull();

        confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.getStyle().set("padding-top", "0px");
        confirmPasswordField.setWidthFull();

        dialogLayout.add(currentPasswordField, newPasswordField, confirmPasswordField);

        HorizontalLayout buttonLayout = createPasswordChangingButtonLayout(passwordChangingDialog);
        dialogLayout.add(buttonLayout);

        passwordChangingDialog.add(dialogLayout);
        passwordChangingDialog.getFooter().add(buttonLayout);
        passwordChangingDialog.open();
    }

    /**
     * Ccreates a horizontal layout containing "Save" and "Cancel" buttons for changing a password.
     * 
     * @param passwordChangingDialog Dialog component used to display a dialog box for changing the password.
     * @return HorizontalLayout object.
     */
    private HorizontalLayout createPasswordChangingButtonLayout(Dialog passwordChangingDialog) {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button saveButton = new Button("Save", event -> changePassword(passwordChangingDialog));
        saveButton.setIcon(VaadinIcon.KEY.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelPasswordChangingButton = new Button("Cancel", event -> passwordChangingDialog.close());
        cancelPasswordChangingButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        cancelPasswordChangingButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        buttonLayout.add(cancelPasswordChangingButton, saveButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.setWidthFull();
        return buttonLayout;
    }

    /**
     * Validates and changes the user's password,
     * and updates the session if the password change is successful.
     * 
     * @param passwordChangingDialog Dialog box that allows the user to change their password.
     */
    private void changePassword(Dialog passwordChangingDialog) {
        
        // Validate password change
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        if (newPassword.equals(password)) {
            Dialogs.showDialog("Your new password is the same as your old one!");
            newPasswordField.setInvalid(true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Dialogs.showDialog(Response.PASSWORD_MISMATCH);
            newPasswordField.setInvalid(true);
            confirmPasswordField.setInvalid(true);
            return;
        }

        //*************************************************************************************
        Response result = UserManager.changePassword(emailField.getValue(), role, newPassword);
        //*************************************************************************************

        Dialogs.showDialog(result);

        if (result == Response.SUCCESS) {
            passwordChangingDialog.close();
            
            //***************************************************
            SignInManager.setSession(currentPerson, newPassword);
            //***************************************************
            password = newPassword;
        }
    }

    /**
     * Enables text fields for editing.
     */
    private void toggleTextFieldsForEditing(boolean val) {
        firstNameField.setEnabled(val);
        lastNameField.setEnabled(val);

        if ((role.name()).equals("Employee")) {
            departmentField.setEnabled(val);
            facultyField.setEnabled(val);
        }

        if ((role.name()).equals("Student")) {
            qualificationLevelCombo.setEnabled(val);
            completedCoursesAccordionPanel.setEnabled(val);
        }
    }

    /**
     * Checks if fields are empty and marks any found invalid.
     * 
     * @return Boolean value, which indicates whether any of the field are empty.
     * If any of the fields are empty, the method returns true. Otherwise, false.
     */
    private boolean emptyFieldCheck() {
        boolean foundEmpty = false;

        if (firstNameField.isEmpty()) {
            foundEmpty = true;
            firstNameField.setInvalid(true);
        }

        if (lastNameField.isEmpty()) {
            foundEmpty = true;
            lastNameField.setInvalid(true);
        }

        return foundEmpty;
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
}