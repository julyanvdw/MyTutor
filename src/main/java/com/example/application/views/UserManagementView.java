package com.example.application.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;

import com.example.application.Dialogs;
import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;
import com.example.application.microservices.StudentManager;
import com.example.application.microservices.UserManager;
import com.example.application.models.*;

/**
 * User Management View for Admins to view all users of MyTutor.
 * Admins can create user profiles for other Admins, CourseConvenors, and Lecturers.
 */
@PageTitle("User Management | MyTutor")
@Route(value = "user-management", layout = MainLayout.class)
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final VerticalLayout contentContainer;
    private final Button createUserButton = new Button("Create User", event -> openUserFormDialog(null));
    private ComboBox<QualificationLevel> qualificationLevelCombo = null;

    private Grid<Administrator> administratorGrid = new Grid<>(Administrator.class);
    private Grid<Lecturer> employeeGrid = new Grid<>(Lecturer.class);
    private Grid<Student> studentGrid = new Grid<>(Student.class);

    private String currentTab = "Administrator";

    private TextField searchField;

    /**
     * Creates a new UserManagementView.
     */
    public UserManagementView() {
        setHeightFull();
        addClassName("management-view");
        setMinWidth("625px");

        createUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createUserButton.setIcon(VaadinIcon.USERS.create());
        createUserButton.setWidth("25%");
        createUserButton.setMinWidth("260px");
        createUserButton.setHeightFull();

        // Create tabs
        Tabs userTabs = new Tabs(
            new Tab("Administrators"),
            new Tab("Employees"),
            new Tab("Students")
        );
        userTabs.setWidthFull();
        userTabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);

        // Adding a selected change listener to the tabs.
        // Update currentTab with newly-selected tab. Then, calls the setContent() method.
        userTabs.addSelectedChangeListener(event -> {
            currentTab = (event.getSelectedTab().getLabel()).substring(0,(event.getSelectedTab().getLabel()).length() - 1);
            setContent();
        });

        // Add item click listeners to all the grids
        administratorGrid.addItemClickListener(event -> openUserFormDialog(event.getItem()));
        employeeGrid.addItemClickListener(event -> openUserFormDialog(event.getItem()));
        studentGrid.addItemClickListener(event -> openUserFormDialog(event.getItem()));

        searchField = new TextField("Search by User ID");
        searchField.setPlaceholder("User ID");
        searchField.getStyle().setPadding("0px");
        searchField.setWidthFull();
        searchField.setMinWidth("130px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> filterUsers(event.getValue()));

        // Set up content containers
        contentContainer = new VerticalLayout();
        contentContainer.getStyle().setPadding("0px");
        contentContainer.setSizeFull();
        setContent();

        add(userTabs, contentContainer);
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
     * Sets the content based on the selected tab.
     */
    public void setContent() {
        contentContainer.removeAll();

        String createButtonText = "Create a ";
        if ("Administrator".equals(currentTab) || "Employee".equals(currentTab)) {
            createButtonText = "Create an ";
        }
        createButtonText += currentTab;
        createUserButton.setText(createButtonText);

        // Add the "Create User" button and search field
        HorizontalLayout layout = new HorizontalLayout(createUserButton, searchField);
        layout.setWidth("100%");
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.START);

        // Adding Reset System button
        if ("Student".equals(currentTab)) {
            Button resetSystemButton = new Button("Reset System", event -> openResetSystemConfirmDialog());
            resetSystemButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            resetSystemButton.setIcon(VaadinIcon.RECYCLE.create());
            resetSystemButton.setHeightFull();
            resetSystemButton.setWidth("25%");
            resetSystemButton.setMinWidth("170px");
            layout.add(resetSystemButton);
        }

        contentContainer.add(layout);
        configureUserGrid();
    }

    /**
     * Configures the user grid based on the selected tab.
     */
    private void configureUserGrid() {
        switch (currentTab) {
            case "Administrator":
                // **************************************************************************
                 List<Administrator> administrators = UserManager.getAdministrators();
                // **************************************************************************
                administratorGrid.setItems(administrators);
                administratorGrid.setColumns("employeeID", "firstName", "lastName", "email");
                administratorGrid.setHeightFull();
                administratorGrid.getStyle().set("border-radius", "15px");
                administratorGrid.getStyle().set("overflow", "hidden");
                contentContainer.add(administratorGrid);
                break;
            case "Employee":
                // **************************************************************************
                 List<Lecturer> employees = UserManager.getEmployees();
                // **************************************************************************
                employeeGrid.setItems(employees);
                employeeGrid.setColumns("employeeID", "firstName", "lastName", "email", "department", "faculty");
                employeeGrid.setHeightFull();
                employeeGrid.getStyle().set("border-radius", "15px");
                employeeGrid.getStyle().set("overflow", "hidden");
                contentContainer.add(employeeGrid);
                break;
            case "Student":
                // **************************************************************************
                 List<Student> students = UserManager.getStudents();
                // **************************************************************************
                studentGrid.setItems(students);
                studentGrid.setColumns("studentID", "firstName", "lastName", "email", "qualificationLevel");
                studentGrid.setHeightFull();
                studentGrid.getStyle().set("border-radius", "15px");
                studentGrid.getStyle().set("overflow", "hidden");
                contentContainer.add(studentGrid);
            default:
                break;
        }
    }

    /**
     * Opens a dialog for creating or editing profiles.
     *
     * @param selectedPerson The person data to populate the form for editing.
     */
    private void openUserFormDialog(Person selectedPerson) {
        // Opening user form dialog
        Dialog userFormDialog = new Dialog();
        userFormDialog.setWidth("50%");
        userFormDialog.setMinWidth("350px");
        userFormDialog.setHeaderTitle(currentTab + " Form");

        // Create a layout to hold form fields and buttons
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setWidthFull();
        dialogLayout.setPadding(false);

        // Creating a list of TextFields based on the selected person.
        List<TextField> textFields = createTextFields(selectedPerson);
        textFields.forEach(textField -> {
            textField.getStyle().set("padding", "0");
            textField.setWidthFull();
            dialogLayout.add(textField);
        });
        
        // Get QualificationLevel if Student
        if (currentTab.equals("Student")) {
            qualificationLevelCombo = new ComboBox<>("Qualification Level");
            qualificationLevelCombo.setItems(QualificationLevel.values());
            qualificationLevelCombo.getStyle().set("padding", "0");
            qualificationLevelCombo.setWidthFull();

            if (selectedPerson != null) {
                qualificationLevelCombo.setValue(((Student)selectedPerson).getQualificationLevel());
                qualificationLevelCombo.setEnabled(false);
            }

            qualificationLevelCombo.setRequired(true);

            dialogLayout.add(qualificationLevelCombo);
        }

        HorizontalLayout buttonLayout = createButtonLayout(selectedPerson, textFields, userFormDialog);
        dialogLayout.add(buttonLayout);

        userFormDialog.add(dialogLayout);
        userFormDialog.getFooter().add(buttonLayout);
        userFormDialog.open();
    }

    /**
     * Creates form fields for the user profile form.
     *
     * @param selectedPerson The selected person for editing or null for creation.
     * @return A list of text fields for the form.
     */
    private List<TextField> createTextFields(Person selectedPerson) {
        
        List<TextField> textFields = new ArrayList<>();

        switch (currentTab) {
            case "Administrator":
                textFields.add(new TextField("Employee ID"));
                textFields.add(new TextField("First Name"));
                textFields.add(new TextField("Last Name"));
                textFields.add(new TextField("Email"));

                if (selectedPerson != null) {
                    textFields.get(0).setValue(((Administrator)selectedPerson).getEmployeeID());
                }
                break;
            case "Employee":
                textFields.add(new TextField("Employee ID"));
                textFields.add(new TextField("First Name"));
                textFields.add(new TextField("Last Name"));
                textFields.add(new TextField("Email"));
                textFields.add(new TextField("Department"));
                textFields.add(new TextField("Faculty"));

                textFields.get(4).setClearButtonVisible(true);
                textFields.get(5).setClearButtonVisible(true);
                textFields.get(4).setEnabled(false);
                textFields.get(5).setEnabled(false);

                if (selectedPerson != null) {
                    textFields.get(0).setValue(((Lecturer)selectedPerson).getEmployeeID());
                    textFields.get(4).setValue(((Lecturer)selectedPerson).getDepartment());
                    textFields.get(5).setValue(((Lecturer)selectedPerson).getFaculty());
                }
                break;
            case "Student":
                textFields.add(new TextField("Student ID"));
                textFields.add(new TextField("First Name"));
                textFields.add(new TextField("Last Name"));
                textFields.add(new TextField("Email"));

                if (selectedPerson != null) {
                    textFields.get(0).setValue(((Student)selectedPerson).getStudentID());
                }
                break;
            default:
                break;
        }

        if (selectedPerson != null) {
            textFields.get(1).setValue(selectedPerson.getFirstName());
            textFields.get(2).setValue(selectedPerson.getLastName());
            textFields.get(3).setValue(selectedPerson.getEmail());
        }

        textFields.get(0).setPrefixComponent(VaadinIcon.USER_CARD.create());
        textFields.get(1).setPrefixComponent(VaadinIcon.USER.create());
        textFields.get(2).setPrefixComponent(VaadinIcon.USER.create());
        textFields.get(3).setPrefixComponent(VaadinIcon.ENVELOPE.create());

        textFields.get(0).setClearButtonVisible(true);
        textFields.get(1).setClearButtonVisible(true);
        textFields.get(2).setClearButtonVisible(true);
        textFields.get(3).setClearButtonVisible(true);

        textFields.get(1).setEnabled(false);
        textFields.get(2).setEnabled(false);

        if (selectedPerson == null) {
            for (TextField tf : textFields) {
                tf.setEnabled(true);
            } 
        }

        for (TextField tf : textFields) {
            tf.setWidthFull();
        }

        return textFields;
    }

    /**
     * Creates a horizontal layout for buttons based on the selected person and a list of text fields.
     * 
     * @param selectedPerson    Person that is being edited, or null if a new person is being created.
     * @param textFields        List of TextFields used in the form for inputting data.
     * @param userFormDialog    Dialog component that represents a dialog box for editing or creating a user form.
     * @return A HorizontalLayout object.
     */
    private HorizontalLayout createButtonLayout(Person selectedPerson, List<TextField> textFields, Dialog userFormDialog) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.setWidthFull();

        if (selectedPerson != null) {
            Button editButton = createEditButton(selectedPerson, textFields, userFormDialog);
            Button deleteButton = createDeleteButton(selectedPerson, userFormDialog);
            Button cancelButton = createCancelButton(userFormDialog);

            textFields.get(0).setReadOnly(true);
            textFields.get(3).setReadOnly(true);

            // Add "Cancel" button to the left and align it to the start
            buttonLayout.add(cancelButton);
            buttonLayout.setJustifyContentMode(JustifyContentMode.START);

            // Create a horizontal layout for the other buttons and align them to the end
            HorizontalLayout otherButtonsLayout = new HorizontalLayout(editButton, deleteButton);
            otherButtonsLayout.setWidthFull();
            otherButtonsLayout.setJustifyContentMode(JustifyContentMode.END);

            buttonLayout.add(otherButtonsLayout);
        } else {
            Button createButton = createCreateButton(textFields, userFormDialog);
            Button cancelButton = createCancelButton(userFormDialog);

            textFields.forEach(textField -> textField.setRequired(true));

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
     * Creates an "Edit" button with functionality for editing and saving changes to a person's information.
     * 
     * @param selectedPerson    Person, which represents the person that is currently selected for editing.
     * @param textFields        List of TextFields that represent the input fields for the person's information.
     * @param userFormDialog    Dialog  that represents a dialog box or popup window for displaying and editing
     * @return A Button object for "Edit".
     */
    private Button createEditButton(Person selectedPerson, List<TextField> textFields, Dialog userFormDialog) {
        Button editButton = new Button("Edit");
        editButton.addClickListener(event -> {
            if ("Edit".equals(editButton.getText())) {
                enableTextFieldsForEditing(textFields);
                if (qualificationLevelCombo != null) qualificationLevelCombo.setEnabled(true);
                editButton.setText("Save");
            } else if ("Save".equals(editButton.getText())) {

                if (emptyFieldCheck(textFields)) {
                    Dialogs.showDialog(Response.EMPTY_FIELD);
                    return;
                }

                editClick(selectedPerson, textFields);
                userFormDialog.close();
                setContent();
                editButton.setText("Edit");
            }
        });

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.setIcon(new Icon("lumo", "edit"));
        editButton.setMinWidth("100px");

        return editButton;
    }

    /**
     * Creates a "Delete" button that deletes the selected person, closes a dialog, and updates the content.
     * 
     * @param selectedPerson    Person that represents the person to be deleted.
     * @param userFormDialog    Dialog object that contains a form for editing user information.
     * @return A Button object for "Delete".
     */
    private Button createDeleteButton(Person selectedPerson, Dialog userFormDialog) {
        Button deleteButton = new Button("Delete", event -> {
            
            //***************************************************************
            Response result = UserManager.delete(selectedPerson, currentTab);
            //***************************************************************
            
            if (result == Response.SUCCESS) {
                Dialogs.showDialog("Selected person was successfully deleted!");
            } else {
                Dialogs.showDialog("Sorry! Selected person was not deleted...Please try again!");
            }

            userFormDialog.close();
            setContent();
        });

        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        deleteButton.setMinWidth("100px");

        return deleteButton;
    }

    /**
     * Creates a "Cancel" button to close a dialog when clicked.
     * 
     * @param userFormDialog    Dialog object where the button will be added.
     * @return A Button object of "Cancel".
     */
    private Button createCancelButton(Dialog userFormDialog) {
        Button cancelButton = new Button("Cancel", event -> userFormDialog.close());
        cancelButton.setWidth("30%");
        cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        return cancelButton;
    }

    /**
     * Creates a "Create" button to create a new person.
     * 
     * @param textFields        List of TextField objects that represent the input fields in the user form.
     * @param userFormDialog    Dialog object that represents a form for creating a new user.
     * @return A Button object of "Create".
     */
    private Button createCreateButton(List<TextField> textFields, Dialog userFormDialog) {
        Button createButton = new Button("Create", event -> {

            if (emptyFieldCheck(textFields)) {
                Dialogs.showDialog(Response.EMPTY_FIELD);
                return;
            }

            createClick(textFields);
            userFormDialog.close();
            setContent();
        });

        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setIcon(new Icon("lumo", "plus"));
        createButton.setWidth("30%");

        return createButton;
    }

    /**
     * Enables editing for a list of text fields, excluding "ID" or "Email" fields.
     * 
     * @param textFields A list of TextField objects.
     */
    private void enableTextFieldsForEditing(List<TextField> textFields) {
        for (TextField textField : textFields) {
            if (!textField.getLabel().contains("ID") && !textField.getLabel().equals("Email")) {
                textField.setEnabled(true);
                textField.setRequired(true);
            }
        }
    }

    /**
     * Takes a selected person and a list of text fields as parameters,
     * creates a new person object based on the current tab,
     * and then calls a microservice to update the selected person with the new person object.
     * 
     * @param selectedPerson    Person - the person that is currently selected and needs to be edited.
     * @param textFields        List of TextField objects containing values to be used for initializing the new person object.
     */
    public void editClick(Person selectedPerson, List<TextField> textFields) {
        Person newPerson = createNewPerson(selectedPerson, textFields);

        //**************************************************************************
        Response result = UserManager.update(selectedPerson, newPerson, currentTab);
        //**************************************************************************

        if (result == Response.SUCCESS) {
            Dialogs.showDialog("Selected person was successfully updated!");
        } else {
            Dialogs.showDialog("Sorry! Selected person was not updated...Please try again!");
        }
    }

    /**
     * Creates a new person object based on the current tab and values from a
     * list of text fields, and then calls the create microservice of UserManager.
     * 
     * @param textFields    List of TextField objects containing values to be used for initializing the new person object.
     */
    public void createClick(List<TextField> textFields) {
        Person newPerson = createNewPerson(null, textFields);

        // *********************************************************
        Response result = UserManager.create(newPerson, currentTab);
        // *********************************************************
        
        Dialogs.showDialog(result);
    }

    /**
     * Creates a new instance of a Person object based on the selected tab and inputed values in TextFields.
     * 
     * @param selectedPerson Person that is currently selected or being edited.
     * @param textFields List of TextField objects that contain the values entered by the user.
     * @return A new instance of the Person class or one of its subclasses
     */
    private Person createNewPerson(Person selectedPerson, List<TextField> textFields) {
        
        Person newPerson = null;

        switch (currentTab) {
            case "Administrator":
                newPerson = new Administrator(
                        textFields.get(1).getValue(),
                        textFields.get(2).getValue(),
                        textFields.get(3).getValue(),
                        textFields.get(0).getValue()
                );
                break;
            case "Employee":
                newPerson = new Lecturer(
                        textFields.get(1).getValue(),
                        textFields.get(2).getValue(),
                        textFields.get(3).getValue(),
                        textFields.get(0).getValue(),
                        textFields.get(4).getValue(),
                        textFields.get(5).getValue()
                );
                break;
            case "Student":
                newPerson = new Student(
                        textFields.get(1).getValue(),
                        textFields.get(2).getValue(),
                        textFields.get(3).getValue(),
                        textFields.get(0).getValue(),
                        qualificationLevelCombo.getValue(),
                        ApplicationStatus.IDLE,
                        new ArrayList<>() //??
                );
                break;
        }

        return newPerson;
    }

    /**
     * Checks if any of the text fields or the qualification level combo box is empty and
     * returns true if any of them are empty.
     * 
     * @param textFields    List of TextField objects that need to be checked for empty values.
     * @return A Boolean value, specifically the value of foundEmpty.
     */
    private boolean emptyFieldCheck(List<TextField> textFields) {
        Boolean foundEmpty = false;

        for (TextField textField : textFields) {
            if (textField.isEmpty()) {
                textField.setInvalid(true);
                foundEmpty = true;
            }
        }

        if (qualificationLevelCombo != null) {
            if (qualificationLevelCombo.isEmpty()) {
                qualificationLevelCombo.setInvalid(true);
                foundEmpty = true;
            }
        }

        return foundEmpty;
    }

    /**
     * Filters users based on their user ID and updates the corresponding grid with the
     * filtered results.
     * 
     * @param userId String that represents the user ID that will be used to filter the users in the grid.
     */
    private void filterUsers(String userId) {
        switch (currentTab) {
            case "Administrator":
                // Filter administrators based on the user ID
                List<Administrator> filteredAdmins = getFilteredAdministrators(userId);
                administratorGrid.setItems(filteredAdmins);
                break;
            case "Employee":
                // Filter employees based on the user ID
                List<Lecturer> filteredEmployees = getFilteredEmployees(userId);
                employeeGrid.setItems(filteredEmployees);
                break;
            case "Student":
                // Filter students based on the user ID
                List<Student> filteredStudents = getFilteredStudents(userId);
                studentGrid.setItems(filteredStudents);
                break;
            default:
                break;
        }
    }

    /**
     * Returns a filtered list of administrators based on a given user ID.
     * 
     * @param userId String that represents the employee ID that we want to filter the administrators by.
     * @return List of Administrator objects that match the given userId.
     */
    private List<Administrator> getFilteredAdministrators(String userId) {
        List<Administrator> administrators = UserManager.getAdministrators();
        return administrators.stream()
                .filter(administrator -> administrator.getEmployeeID().toLowerCase().contains(userId.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a filtered list of employees based on a given user ID.
     * 
     * @param userId String that represents the employee ID that we want to filter the employees by.
     * @return Filtered list of Lecturer objects.
     */
    private List<Lecturer> getFilteredEmployees(String userId) {
        List<Lecturer> employees = UserManager.getEmployees();
        return employees.stream()
                .filter(lecturer -> lecturer.getEmployeeID().toLowerCase().contains(userId.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a filtered list of students based on a given student ID.
     * 
     * @param userId String that represents the user ID of a student.
     * @return Filtered list of students based on the provided userId.
     */
    private List<Student> getFilteredStudents(String userId) {
        List<Student> students = UserManager.getStudents();
        return students.stream()
                .filter(student -> student.getStudentID().toLowerCase().contains(userId.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Opens a confirmation dialog box asking the user if they want to reset the system,
     * and if confirmed, it invokes the StudentManager microservice.
     */
    private void openResetSystemConfirmDialog() {
        ConfirmDialog resetSystemConfirmDialog = new ConfirmDialog();
        resetSystemConfirmDialog.setHeader("Resetting System");
        resetSystemConfirmDialog.setText("This should only be done at the start of the year/semester, and resets all Student's application statuses to idle. \n\nAre you sure you want to reset the system?");

        resetSystemConfirmDialog.setCancelable(true);
        resetSystemConfirmDialog.addCancelListener(event -> resetSystemConfirmDialog.close());

        resetSystemConfirmDialog.setRejectable(false);

        Button resetSystemButton = new Button("Reset System");
        resetSystemButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        resetSystemButton.addClickListener(event -> {
            System.out.println("RESET THE SYSTEM");

            //*********************************************
            Response result = StudentManager.resetSystem();
            //*********************************************

            if (result == Response.SUCCESS) {
                Dialogs.showDialog("Successfully reset the system! All students now have an application status of 'Idle'.");
            } else {
                Dialogs.showDialog("Sorry! Something went wrong resetting the system...Please try again!");
            }
        });
        resetSystemConfirmDialog.setConfirmButton(resetSystemButton);

        resetSystemConfirmDialog.open();
    }
}