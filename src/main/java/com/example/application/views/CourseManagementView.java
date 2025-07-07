package com.example.application.views;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import com.example.application.Dialogs;

import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;

import com.example.application.models.Course;
import com.example.application.models.Lecturer;
import com.example.application.microservices.CourseManager;
import com.example.application.microservices.EmployeeManager;
import com.example.application.microservices.UserManager;

/**
 * User Management View for Admins to view all Courses of MyTutor.
 * Admins can create and edit Courses.
 */
@PageTitle("Course Management | MyTutor")
@Route(value = "course-management", layout = MainLayout.class)
public class CourseManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final Grid<Course> courseGrid = new Grid<>(Course.class);
    private Grid<Lecturer> employeesGrid = new Grid<>(Lecturer.class, false);

    private TextField courseCodeField;
    private TextField nameField;
    private IntegerField tutorCapacityField;
    private IntegerField taCapacityField;

    private TextField searchField;

    /**
     * Creates a new CourseManagementView.
     */
    public CourseManagementView() {
        setHeightFull();
        setMinWidth("430px");

        configureCourseGrid();

        // Show the form overlay when "Create User" button is clicked
        Button createCourseButton = new Button("Create a Course", event -> openCourseFormDialog(null));
        createCourseButton.setHeightFull();
        createCourseButton.setWidth("25%");
        createCourseButton.setMinWidth("190px");
        createCourseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createCourseButton.setIcon(VaadinIcon.BOOK.create());

        // Add a search field at the top
        searchField = new TextField("Search by Course Code");
        searchField.setPlaceholder("Course Code");
        searchField.getStyle().set("padding", "0px");
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> filterCourses(event.getValue()));

        HorizontalLayout topLayout = new HorizontalLayout(createCourseButton, searchField);
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.CENTER);
        topLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Show the form overlay when a row is clicked
        courseGrid.addItemClickListener(event -> {
            openCourseFormDialog(event.getItem());
        });

        add(topLayout, courseGrid);
    }

    /**
     * SECURITY AUTHENTICATION
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if ((Role) VaadinSession.getCurrent().getAttribute("role") != Role.Admin) {
            MainLayout.signOut(true);
        }
    }

    /**
     * Configures the course grid with data and click listeners.
     */
    private void configureCourseGrid() {
        // ***********************************************
        List<Course> courses = CourseManager.getCourses();
        // ***********************************************
        courseGrid.setItems(courses);
        courseGrid.setColumns("courseCode", "name", "tutorCapacity", "taCapacity");
        courseGrid.setSizeFull();
        courseGrid.getStyle().set("border-radius", "15px");
        courseGrid.getStyle().set("overflow", "hidden");
    }

    /**
     * Opens a dialog for creating / editing courses.
     * 
     * @param selectedCourse The Course data to populate the form for editing.
     */
    private void openCourseFormDialog(Course selectedCourse) {
        Dialog courseFormDialog = new Dialog();
        courseFormDialog.setMinWidth("500px");
        courseFormDialog.setHeaderTitle("Course Form");

        // Create a layout to hold form fields and buttons
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setWidthFull();
        dialogLayout.setPadding(false);

        // Reset TextFields
        courseCodeField = new TextField("Course Code");
        nameField = new TextField("Name");
        tutorCapacityField = new IntegerField("Tutor Capacity");
        taCapacityField = new IntegerField("TA Capacity");

        courseCodeField.setPrefixComponent(VaadinIcon.ACADEMY_CAP.create());

        // Adding steps the Tutor Capacity & TA Capacity
        tutorCapacityField.setStep(1);
        tutorCapacityField.setStepButtonsVisible(true);
        taCapacityField.setStep(1);
        taCapacityField.setStepButtonsVisible(true);

        courseCodeField.getStyle().set("padding", "0");
        nameField.getStyle().set("padding", "0");
        tutorCapacityField.getStyle().set("padding", "0");
        taCapacityField.getStyle().set("padding", "0");

        courseCodeField.setWidthFull();
        nameField.setWidthFull();
        tutorCapacityField.setWidthFull();
        taCapacityField.setWidthFull();

        nameField.setClearButtonVisible(true);

        if (selectedCourse != null) {
            isEnabled(false);
            courseCodeField.setReadOnly(true);
            tutorCapacityField.setReadOnly(true);
            taCapacityField.setReadOnly(true);
        } else {
            isEnabled(true);
            courseCodeField.setEnabled(true);
            courseCodeField.setRequired(true);

            tutorCapacityField.setRequired(true);
            taCapacityField.setRequired(true);

            courseCodeField.setClearButtonVisible(true);
            tutorCapacityField.setClearButtonVisible(true);
            taCapacityField.setClearButtonVisible(true);
        }

        clearFields();
        populateFields(selectedCourse);

        dialogLayout.add(courseCodeField, nameField, tutorCapacityField, taCapacityField);

        HorizontalLayout buttonLayout = createButtonLayout(selectedCourse, courseFormDialog);

        courseFormDialog.add(dialogLayout);
        courseFormDialog.getFooter().add(buttonLayout);
        courseFormDialog.open();
    }

    /**
     * Creates a horizontal layout for buttons based on the selected course.
     * 
     * @param selectedCourse    Course that is being edited, or null if a new course is being created.
     * @param courseFormDialog  Dialog component that represents a dialog box for editing or creating a course.
     * @return A HorizontalLayout object.
     */
    private HorizontalLayout createButtonLayout(Course selectedCourse, Dialog courseFormDialog) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setWidthFull();

        if (selectedCourse != null) {
            Button editButton = createEditButton(selectedCourse, courseFormDialog);
            Button addEmployeesButton = createAddEmployeesButton(selectedCourse, courseFormDialog);
            Button deleteButton = createDeleteButton(selectedCourse, courseFormDialog);
            Button cancelButton = createCancelButton(courseFormDialog);

            // Add "Cancel" button to the left and align it to the start
            buttonLayout.add(cancelButton);
            buttonLayout.setJustifyContentMode(JustifyContentMode.START);

            // Create a horizontal layout for the other buttons and align them to the end
            HorizontalLayout otherButtonsLayout = new HorizontalLayout(addEmployeesButton, editButton, deleteButton);
            otherButtonsLayout.setWidthFull();
            otherButtonsLayout.setJustifyContentMode(JustifyContentMode.END);

            buttonLayout.add(otherButtonsLayout);
        } else {
            Button createButton = createCreateButton(courseFormDialog);
            Button cancelButton = createCancelButton(courseFormDialog);

            // Add "Cancel" button to the left and align it to the start
            buttonLayout.add(cancelButton);
            buttonLayout.setJustifyContentMode(JustifyContentMode.START);

            // Create a horizontal layout for the "Create" button and align it to the end
            HorizontalLayout createButtonLayout = new HorizontalLayout(createButton);
            createButtonLayout.setWidthFull();
            createButtonLayout.setJustifyContentMode(JustifyContentMode.END);

            buttonLayout.add(createButtonLayout);
        }

        return buttonLayout;
    }

    /**
     * Creates an "Edit" button with functionality for editing and saving changes to a course's information.
     * 
     * @param selectedCourse    Course, which represents the course that is currently selected for editing.
     * @param courseFormDialog  Dialog  that represents a dialog box or popup window for displaying and editing
     * @return A Button object for "Edit".
     */
    private Button createEditButton(Course selectedCourse, Dialog courseFormDialog) {
        Button editButton = new Button("Edit");
        editButton.addClickListener(event -> {
            if ("Edit".equals(editButton.getText())) {
                isEnabled(true);
                editButton.setText("Save");
            } else if ("Save".equals(editButton.getText())) {

                if (emptyFieldCheck()) {
                    Dialogs.showDialog(Response.EMPTY_FIELD);
                    return;
                }

                if (!tutorCapacityCheck()) {
                    Dialogs.showDialog(Response.INVALID_TUTORING_CAPACITY);
                    return;
                }

                editClick(selectedCourse);
                courseFormDialog.close();
                configureCourseGrid();
                editButton.setText("Edit");
            }
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.setIcon(new Icon("lumo", "edit"));

        return editButton;
    }

    /**
     * Creates a "Add Employees" button that opens another dialog that allows Admin to add
     * EmployeeID + Role (either Course Convenor or Lecturer) for the parsed Course.
     * 
     * @param selectedCourse    Course that represents the course to be deleted.
     * @return A Button object for "Add Employees".
     */
    private Button createAddEmployeesButton(Course selectedCourse, Dialog courseFormDialog) {
        Button addEmployeesButton = new Button("Add Employees", event -> {
            
            openAddEmployeesDialog(selectedCourse);

            courseFormDialog.close();
            configureCourseGrid();
        });
        addEmployeesButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.MATERIAL_OUTLINED);
        addEmployeesButton.setIcon(VaadinIcon.WORKPLACE.create());
        return addEmployeesButton;
    }

    /**
     * Opens a dialog for adding employees (Lecturers & Course Convenors) to courses.
     * 
     * @param selectedCourse The Course that the employees will be added to.
     */
    private void openAddEmployeesDialog(Course selectedCourse) {
        Dialog addEmployeesDialog = new Dialog();
        addEmployeesDialog.setWidth("75%");
        addEmployeesDialog.setHeaderTitle("Adding Employees to " + selectedCourse.getCourseCode());

        IntegerField yearField = new IntegerField("Year");
        yearField.getStyle().set("padding-top", "0px");
        yearField.setWidth("25%");
        yearField.setPlaceholder("YYYY");
        yearField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        yearField.setClearButtonVisible(true);
        yearField.setMin(1950);

        configureEmployeesGrid(selectedCourse);

        // Create the buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setWidthFull();

        Button addLecturersButton = new Button("Add as Lecturers", event -> {
            // Check if year was entered
            if (yearField.isEmpty()) {
                yearField.setInvalid(true);
                Dialogs.showDialog("Sorry! Please enter a year for the employees to be added...");
                return;
            }

            // Check if any employees were selected
            if (employeesGrid.getSelectedItems().isEmpty()) {
                Dialogs.showDialog("Sorry! No employees were selected...");
                return;
            }

            //*********************************************************************************************************************************************
            Lecturer duplicateCheck = EmployeeManager.doesEmployeeExist(employeesGrid.getSelectedItems(), selectedCourse, yearField.getValue());
            //*********************************************************************************************************************************************

            if (duplicateCheck != null) {
                Dialogs.showDialog("Sorry! " + duplicateCheck.getEmployeeID() + " is already assigned to " + selectedCourse.getCourseCode() + ", " + yearField.getValue());
                addEmployeesDialog.close();
                return;
            }

            //**********************************************************************************************************************************
            Response result = EmployeeManager.addEmpToCourse(employeesGrid.getSelectedItems(), selectedCourse, Role.Lecturer, yearField.getValue());
            //**********************************************************************************************************************************

            if (result == Response.SUCCESS) {
                addEmployeesDialog.close();
                Dialogs.showDialog("Success! Selected employees have been added to " + selectedCourse.getCourseCode() + ", " + yearField.getValue() + " as Lecturers!");
            } else {
                Dialogs.showDialog("Sorry! Something went wrong adding selected employees as Lecturers...Please try again!");
            }
        });
        addLecturersButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addLecturersButton.setIcon(VaadinIcon.FOLDER_ADD.create());
        addLecturersButton.setWidth("30%");

        Button addCourseConvenorsButton = new Button("Add as Course Convenors", event -> {
            // Check if year was entered
            if (yearField.isEmpty()) {
                yearField.setInvalid(true);
                Dialogs.showDialog("Sorry! Please enter a year for the employees to be added...");
                return;
            }

            // Check if any employees were selected
            if (employeesGrid.getSelectedItems().isEmpty()) {
                Dialogs.showDialog("Sorry! No employees were selected...");
                return;
            }

            //***************************************************************************************************************************************************
            Lecturer duplicateCheck = EmployeeManager.doesEmployeeExist(employeesGrid.getSelectedItems(), selectedCourse, yearField.getValue());
            //***************************************************************************************************************************************************

            if (duplicateCheck != null) {
                Dialogs.showDialog("Sorry! " + duplicateCheck.getEmployeeID() + " is already assigned to " + selectedCourse.getCourseCode() + ", " + yearField.getValue());
                addEmployeesDialog.close();
                return;
            }

            //****************************************************************************************************************************************
            Response result = EmployeeManager.addEmpToCourse(employeesGrid.getSelectedItems(), selectedCourse, Role.CourseConvenor, yearField.getValue());
            //****************************************************************************************************************************************

            if (result == Response.SUCCESS) {
                Dialogs.showDialog("Success! Selected employees have been added to " + selectedCourse.getCourseCode() + ", " + yearField.getValue() + " as CourseConvenors!");
            } else {
                Dialogs.showDialog("Sorry! Something went wrong adding selected employees as Course Convenors...Please try again!");
            }

            addEmployeesDialog.close();
        });
        addCourseConvenorsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCourseConvenorsButton.setIcon(VaadinIcon.FOLDER_ADD.create());
        addCourseConvenorsButton.setWidth("30%");

        Button cancelButton = createCancelButton(addEmployeesDialog);
        cancelButton.setText("Cancel");
        cancelButton.setWidth("30%");

        buttonLayout.add(addLecturersButton, addCourseConvenorsButton, cancelButton);

        addEmployeesDialog.add(yearField, employeesGrid);
        addEmployeesDialog.getFooter().add(buttonLayout);
        addEmployeesDialog.open();
    }

    /**
     * Configures a grid to display employee information.
     * Retrieves a list of employees from the UserManager microservice.
     */
    private void configureEmployeesGrid(Course selectedCourse) {
        employeesGrid = new Grid<>(Lecturer.class, false);
        employeesGrid.setHeight("350px");
        employeesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        employeesGrid.setColumns("employeeID", "firstName", "lastName", "email", "department", "faculty");
        
        //*******************************************************
        List<Lecturer> allEmployees = UserManager.getEmployees();
        //*******************************************************

        // Filter not needed (replaced by Duplicate Check):
            // List<Lecturer> currentEmployees = EmployeeManager.getEmployeesFor(selectedCourse);
            
            // Set<String> currentEmployeeIds = currentEmployees.stream()
            // .map(Lecturer::getEmployeeID)
            // .collect(Collectors.toSet());

            // Filters employees that are in allEmployees but not in currentEmployees
            // List<Lecturer> employeesToDisplay = allEmployees.stream()
            //                                     .filter(employee -> !currentEmployeeIds.contains(employee.getEmployeeID()))
            //                                     .collect(Collectors.toList());

        employeesGrid.setItems(allEmployees);
    }

    /**
     * Creates a "Delete" button that deletes the selected course, closes a dialog, and updates the content.
     * 
     * @param selectedCourse    Course that represents the course to be deleted.
     * @param courseFormDialog  Dialog object that contains a form for editing course information.
     * @return A Button object for "Delete".
     */
    private Button createDeleteButton(Course selectedCourse, Dialog courseFormDialog) {
        Button deleteButton = new Button("Delete", event -> {
            //*****************************************************/
            Response result = CourseManager.delete(selectedCourse);
            //*****************************************************/
            
            if (result == Response.SUCCESS) {
                Dialogs.showDialog("Successfully deleted selected course!");
            } else {
                Dialogs.showDialog("Sorry! Unscucessful deletion of selected course...Please try again!");
            }

            courseFormDialog.close();
            configureCourseGrid();
        });

        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setIcon(VaadinIcon.TRASH.create());

        return deleteButton;
    }

    /**
     * Creates a "Cancel" button to close a dialog when clicked.
     * 
     * @param courseFormDialog    Dialog object where the button will be added.
     * @return A Button object of "Cancel".
     */
    private Button createCancelButton(Dialog courseFormDialog) {
        Button cancelButton = new Button("Cancel", event -> courseFormDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());

        return cancelButton;
    }

    /**
     * Creates a "Create" button to create a new course.
     * 
     * @param courseFormDialog    Dialog object that represents a form for creating a new course.
     * @return A Button object of "Create".
     */
    private Button createCreateButton(Dialog courseFormDialog) {
        Button createButton = new Button("Create", event -> {

            if (emptyFieldCheck()) {
                Dialogs.showDialog(Response.EMPTY_FIELD);
                return;
            }

            if (!tutorCapacityCheck()) {
                tutorCapacityField.setInvalid(true);
                Dialogs.showDialog(Response.INVALID_TUTORING_CAPACITY);
                return;
            }

            if (!taCapacityFieldCheck()) {
                taCapacityField.setInvalid(true);
                Dialogs.showDialog(Response.INVALID_TUTORING_CAPACITY); //?? create new response
                return;
            }

            createClick();
            courseFormDialog.close();
            configureCourseGrid();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setIcon(new Icon("lumo", "plus"));

        return createButton;
    }

    /**
     * Toggles the editing for a list of text fields.
     */
    private void isEnabled(boolean val) {
        nameField.setEnabled(val);
        nameField.setRequired(true);
    }

    /**
     * Takes a selected course and creates a new course object.
     * and then calls a microservice to update the selected course with the new course object.
     * 
     * @param selecedCourse     Course - the course that is currently selected and needs to be edited.
     */
    public void editClick(Course selectedCourse) {
        Course newCourse = createNewCourse(selectedCourse);

        // ***************************************************************
        Response result = CourseManager.update(selectedCourse, newCourse);
        // ***************************************************************

        if (result == Response.SUCCESS) {
            Dialogs.showDialog("Successfully updated selected course!");
        } else {
            Dialogs.showDialog("Sorry! Unsuccessful update of selected course...Please try again!");
        }
    }

    /**
     * Creates a new course object, and then calls the create microservice of CourseManager.
     */
    public void createClick() {
        Course newCourse = createNewCourse(null);

        // ***********************************************
        Response result = CourseManager.create(newCourse);
        // ***********************************************

        if (result == Response.SUCCESS) {
            Dialogs.showDialog("Successfully created new course!");
        } else {
            Dialogs.showDialog("Sorry! Unsuccessul creation of new course...Please try again!");
        }
    }

    /**
     * Creates a new instance of a Coruse object with inputed values in TextFields.
     * 
     * @param selectedCourse    Course that is currently selected or being edited.
     * @return A new instance of the Course class.
     */
    private Course createNewCourse(Course selectedCourse) {
        
        return new Course(
            courseCodeField.getValue(),
            nameField.getValue(),
            tutorCapacityField.getValue(),
            taCapacityField.getValue()
        );

    }

    /**
     * Clears the values of all fields.
     */
    private void clearFields() {
        courseCodeField.clear();
        nameField.clear();
        tutorCapacityField.clear();
        taCapacityField.clear();
    }

    /**
     * Populates fields with values from a selected course object.
     * 
     * @param selectedCourse Course that is currently selected.
     */
    private void populateFields(Course selectedCourse) {
        if (selectedCourse != null) {
            courseCodeField.setValue(selectedCourse.getCourseCode());
            nameField.setValue(selectedCourse.getName());
            tutorCapacityField.setValue(selectedCourse.getTutorCapacity());
            taCapacityField.setValue(selectedCourse.getTaCapacity());
        }
    }

    /**
     * Checks for empty fields in the form.
     *
     * @return True if there are empty fields, false otherwise.
     */
    private boolean emptyFieldCheck() {
        boolean hasEmptyField = false;

        if (courseCodeField.isEmpty()) {
            courseCodeField.setInvalid(true);
            hasEmptyField = true;
        }

        if (nameField.isEmpty()) {
            nameField.setInvalid(true);
            hasEmptyField = true;
        }

        if (tutorCapacityField.isEmpty()) {
            tutorCapacityField.setInvalid(true);
            hasEmptyField = true;
        }

        if (taCapacityField.isEmpty()) {
            taCapacityField.setInvalid(true);
            hasEmptyField = true;
        }

        return hasEmptyField;
    }

    /**
     * Checks the validity of the value of tutorCapacityField.
     * 
     * @return Boolean - True if valid (greater than 0), False if invalid.
     */
    private boolean tutorCapacityCheck() {
        // can't be 0 or neg
        if (tutorCapacityField.getValue() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Checks the validity of the value of taCapacityField.
     * 
     * @return Boolean - True if valid (greater than 0), False if invalid.
     */
    private boolean taCapacityFieldCheck() {
        // can't be 0 or neg
        if (taCapacityField.getValue() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Filters the course grid based on the search query.
     *
     * @param query The search query entered by the user.
     */
    private void filterCourses(String query) {
        List<Course> filteredCourses = CourseManager.getCourses().stream()
                .filter(course -> course.getCourseCode().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        courseGrid.setItems(filteredCourses);
    }
}