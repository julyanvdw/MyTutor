package com.example.application.views;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

import com.example.application.Dialogs;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;

import com.example.application.microservices.*;
import com.example.application.models.*;

/**
 * The CourseView class represents the view for a specific course.
 * It displays tabs for various sections related to the course.
 */
@PageTitle("Course | MyTutor")
@Route(value = "courses/course-view", layout = MainLayout.class)
@RouteAlias(value = "courses/course-view/:courseCode/:year", layout = MainLayout.class)
public class CourseView extends VerticalLayout implements BeforeEnterObserver {

    private Role role;
    private Role isTutorOrTA; // Used if user is a Student
    private String id;

    private Course currentCourse;
    private String courseCode;
    private int year;

    private Grid<TimeSlot> scheduleGrid;
    private Schedule schedule;
    private Button editScheduleButton;
    private Button cancelButton;
    private boolean isEditClicked = false;

    private VerticalLayout contentContainer;

    private Grid<Tutor> tutorGrid = new Grid<>(Tutor.class);
    private Grid<TA> TAGrid = new Grid<>(TA.class);

    private List<String> sessionColours = new ArrayList<>();

    /**
     * Constructor for the CourseView class.
     * Initializes the UI components and sets up event listeners.
     */
    public CourseView() {
        setHeightFull();

        // Initialise URL parameters
        role = (Role) VaadinSession.getCurrent().getAttribute("role");
        id = (String) VaadinSession.getCurrent().getAttribute("id");

        if (role != null) {
            courseCode = (String) VaadinSession.getCurrent().getAttribute("selectedCourseCode");
            year = (int) VaadinSession.getCurrent().getAttribute("selectedCourseYear");

            if (role == Role.Student) {
                //*********************************************************************************
                isTutorOrTA = StudentManager.isTutorOrTA(courseCode, Integer.toString(year), id);
                //*********************************************************************************
            }

            //*****************************************************
            currentCourse = CourseManager.getCourseFor(courseCode);
            //*****************************************************

            // Initialise Schedule
            schedule = new Schedule(currentCourse);
            initialiseSessionColours();

            // Create a heading element to display the course code
            Div courseHeading = new Div(new H2(courseCode + " - " + currentCourse.getName()));

            // Create tabs
            Tabs tabs = new Tabs();
            Tab scheduleTab = new Tab("Schedule");
            Tab tutorListingTab = new Tab("Tutor Listing");
            Tab taListingTab = new Tab("TA Listing");
            Tab courseInfoTab = new Tab("Course Information");
            tabs.setWidthFull();
            tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);

            // Set up content containers
            contentContainer = new VerticalLayout();
            contentContainer.setSizeFull();
            contentContainer.setPadding(false);

            // Set up initial content
            setContent("Schedule"); // Set default content to Schedule

            // Add tabs to the tab bar
            tabs.add(scheduleTab, tutorListingTab, taListingTab, courseInfoTab);

            // Add event listener to update content when tab selection changes
            tabs.addSelectedChangeListener(event -> {
                Tab selectedTab = event.getSelectedTab();
                if (selectedTab == scheduleTab) {
                    setContent("Schedule");
                } else if (selectedTab == tutorListingTab) {
                    setContent("Tutor Listing");
                } else if (selectedTab == taListingTab) {
                    setContent("TA Listing");
                } else if (selectedTab == courseInfoTab) {
                    setContent("Course Information");
                }
            });
            
            // If user is an EMP or TA, allow them to view per Tutor stats
            if (role.equals(Role.Employee) || isTutorOrTA.equals(Role.TA)) {
                tutorGrid.addItemClickListener(event -> openPerTutorStatDialog(event.getItem()));
            }
            
            // Add components to the layout
            add(courseHeading, tabs, contentContainer);
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
     * Used to update the container underneath Tabs, based on the user's choice of
     * Tab.
     * 
     * @param tabName The string of the tab that the user clicked.
     */
    private void setContent(String tabName) {
        contentContainer.removeAll();

        switch (tabName) {
            case "Schedule":
                // Schedule has been chosen

                configureScheduleGrid();
                //*******************************************************
                schedule = ScheduleManager.getSchedule(courseCode, year);
                //*******************************************************
                populateScheduleGrid();

                contentContainer.add(scheduleGrid);

                // If user is an Employee / TA, allow ability to edit Schedule
                if (role.equals(Role.Employee) || isTutorOrTA.equals(Role.TA)) {
                    // Create the "Edit Schedule" button
                    editScheduleButton = new Button("Edit Schedule");
                    editScheduleButton.setWidth("50%");
                    editScheduleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                    editScheduleButton.setIcon(VaadinIcon.EDIT.create());
                    editScheduleButton.addClickListener(event -> {
                        if (editScheduleButton.getText().contains("Edit")) {

                            //*******************************************************
                            schedule = ScheduleManager.getSchedule(courseCode, year);
                            //*******************************************************
                            populateScheduleGrid();

                            // Enable handling of cell clicks
                            isEditClicked = true;

                            cancelButton.setVisible(true);
                            editScheduleButton.setText("Save Schedule");

                        } else if (editScheduleButton.getText().contains("Save")) {

                            // *******************************************************************************************************
                            Response result = ScheduleManager.updateSchedule(ScheduleManager.getSchedule(courseCode, year), schedule);
                            // *******************************************************************************************************

                            if (result == Response.SUCCESS) {
                                Dialogs.showDialog("Successful Schedule Update!");

                                editScheduleButton.setText("Edit Schedule");
                                cancelButton.setVisible(false);

                                // Disable handling of cell clicks
                                isEditClicked = false;

                            } else {
                                Dialogs.showDialog(
                                        "Sorry! Unsuccessful update of Schedule...Please try again!");
                            }
                        }
                    });

                    cancelButton = new Button("Cancel Button");
                    cancelButton.setVisible(false);
                    cancelButton.setWidth("50%");
                    cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());

                    cancelButton.addClickListener(event -> {

                        //*******************************************************
                        schedule = ScheduleManager.getSchedule(courseCode, year);
                        //*******************************************************
                        populateScheduleGrid();

                        editScheduleButton.setText("Edit Schedule");
                        cancelButton.setVisible(false);

                        // Disable handling of cell clicks
                        isEditClicked = false;
                    });

                    HorizontalLayout scheduleButtonLayout = new HorizontalLayout(editScheduleButton, cancelButton);
                    scheduleButtonLayout.setWidthFull();

                    contentContainer.add(scheduleButtonLayout);
                }

                break;

            case "Tutor Listing":
                // Tutor Listing has been chosen.

                HorizontalLayout topLayout = new HorizontalLayout();
                topLayout.setWidthFull();
                topLayout.setAlignItems(Alignment.BASELINE);
                topLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

                //*****************************************************************************************
                List<Tutor> tutorData = StudentManager.getActiveTutors(courseCode, Integer.toString(year));
                //*****************************************************************************************

                Button tutorCounter = new Button(tutorData.size() + "/" + currentCourse.getTutorCapacity() + " Active Tutors");
                tutorCounter.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                tutorCounter.getStyle().set("cursor", "none");
                tutorCounter.setEnabled(false);

                // Add "See Applications" button above the TA grid
                Button seeApplicationsButton = new Button("See Applications", event -> openApplicationsDialog("Tutor Applications", tutorData.size() - currentCourse.getTutorCapacity()));
                seeApplicationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                topLayout.add(seeApplicationsButton, tutorCounter);

                tutorGrid.setItems(tutorData);
                tutorGrid.setColumns("studentID", "firstName", "lastName", "email", "qualificationLevel");
                tutorGrid.getStyle().set("border-radius", "15px");
                tutorGrid.getStyle().set("overflow", "hidden");
                tutorGrid.setHeightFull();

                // If user is not an EMP or TA, don't show topLayout (applications + counter)
                if (role.equals(Role.Employee) || isTutorOrTA.equals(Role.TA)) {
                    contentContainer.add(topLayout);
                }

                contentContainer.add(tutorGrid);

                break;

            case "TA Listing":
                // TA Listing has been chosen.

                topLayout = new HorizontalLayout();
                topLayout.setWidthFull();
                topLayout.setAlignItems(Alignment.BASELINE);
                topLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

                //********************************************************************************
                List<TA> taData = StudentManager.getActiveTAs(courseCode, Integer.toString(year));
                //********************************************************************************

                Button taCounter = new Button(taData.size() + "/" + currentCourse.getTaCapacity() + " Active TAs");
                taCounter.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                taCounter.getStyle().set("cursor", "none");
                taCounter.setEnabled(false);

                // Add "See Applications" button above the tutor grid
                seeApplicationsButton = new Button("See Applications", event -> openApplicationsDialog("TA Applications", taData.size() - currentCourse.getTaCapacity()));
                seeApplicationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                topLayout.add(seeApplicationsButton, taCounter);

                TAGrid.setItems(taData);
                TAGrid.setColumns("studentID", "firstName", "lastName", "email", "qualificationLevel");
                TAGrid.getStyle().set("border-radius", "15px");
                TAGrid.getStyle().set("overflow", "hidden");
                TAGrid.setHeightFull();

                // If user is not an EMP, don't show topLayout (applications + counter)
                if (role.equals(Role.Employee)) {
                    contentContainer.add(topLayout);
                }
                
                contentContainer.add(TAGrid);
                
                break;

            case "Course Information":
                // Course Information has been chosen.

                VerticalLayout courseDetailsLayout = new VerticalLayout();
                courseDetailsLayout.setSizeFull();
                courseDetailsLayout.setPadding(false);

                // "Get Tutoring Statistics" button
                Button statisticsButton = new Button("Get Tutoring Statistics", event -> openTutoringStatsDialog());
                statisticsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                statisticsButton.setIcon(VaadinIcon.LINE_CHART.create());

                // If user is not an EMP or TA, don't show statisticsButton
                if (role.equals(Role.Employee) || isTutorOrTA.equals(Role.TA)) {
                    courseDetailsLayout.add(statisticsButton);
                }

                Grid<Lecturer> employeeGrid = new Grid<>(Lecturer.class);
                employeeGrid.setSizeFull();

                //************************************************************************************************
                List<Lecturer> employeeData = EmployeeManager.getEmployeesFor(courseCode, Integer.toString(year));
                //************************************************************************************************

                employeeGrid.setItems(employeeData);
                employeeGrid.setColumns("employeeID", "firstName", "lastName", "email", "department", "faculty", "role");
                employeeGrid.getStyle().set("border-radius", "15px");
                employeeGrid.getStyle().set("overflow", "hidden");
                employeeGrid.setHeightFull();

                courseDetailsLayout.add(employeeGrid);
                contentContainer.add(courseDetailsLayout);
                break;
        }
    }

    /**
     * Opens a dialog displaying the applications for tutors or TAs.
     * 
     * @param dialogTitle The title of the dialog.
     */
    private void openApplicationsDialog(String dialogTitle, int capacityAvailable) {
        Dialog applicationsDialog = new Dialog();
        applicationsDialog.setHeaderTitle(dialogTitle);
        applicationsDialog.setWidth("850px");
        applicationsDialog.setHeightFull();

        Grid<Student> applicationsGrid = new Grid<>(Student.class);
        applicationsGrid.setHeightFull();
        applicationsGrid.setSelectionMode(SelectionMode.MULTI);

        //********************************************************************************
        List<Student> pendingApplicants = StudentManager.getPendingApplicants(courseCode);
        //********************************************************************************
        applicationsGrid.setItems(pendingApplicants);
        applicationsGrid.setColumns("studentID", "firstName", "lastName", "email", "qualificationLevel");
        applicationsGrid.getStyle().set("border-radius", "15px");
        applicationsGrid.getStyle().set("overflow", "hidden");

        applicationsGrid.addItemClickListener(event -> openMotivationDialog(event.getItem().getStudentID()));

        // Add the applications grid to the dialog
        applicationsDialog.add(applicationsGrid);
        
        // Accepting of Tutors/TAs
        Button acceptButton = new Button("Accept Selected", event -> {
            // Get the selected items from the applicationsGrid
            Set<Student> selectedStudents = applicationsGrid.getSelectedItems();
            System.out.println(selectedStudents.size());

            // None-Selected Check
            if (selectedStudents.isEmpty()) {
                Dialogs.showDialog("No applicants were selected...");
                return;
            }

            // Set what they are being accepted as
            Role applicantRole = null;
            if (dialogTitle.contains("Tutor")) {
                applicantRole = Role.Tutor;
            } else {
                applicantRole = Role.TA;
            }

            //*****************************************************************************************************************
            Response result = ApplyManager.acceptStudents(courseCode, Integer.toString(year), selectedStudents, applicantRole);
            //*****************************************************************************************************************

            if (result == Response.SUCCESS) {
                Dialogs.showDialog("Successful acceptance of selected applicants!");
                
                //*************************************************************************
                applicationsGrid.setItems(StudentManager.getPendingApplicants(courseCode));
                //*************************************************************************
                applicationsDialog.close();

                if (dialogTitle.contains("Tutor")) {
                    setContent("Tutor Listing");
                } else {
                    setContent("TA Listing");
                }

            } else {
                Dialogs.showDialog("Sorry! Something went wrong in accepting selected applicants...Please try again!");
                applicationsGrid.deselectAll();
            }
        });
        acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        acceptButton.setIcon(VaadinIcon.CHECK.create());

        HorizontalLayout buttonLayout = new HorizontalLayout(acceptButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonLayout.setWidthFull();

        // Check if Capacity Available, or if no Applicants Found
        if (capacityAvailable == 0 || pendingApplicants.isEmpty()) {
            acceptButton.setEnabled(false);
            acceptButton.getStyle().set("background-color", "gray");
        }

        applicationsDialog.getFooter().add(buttonLayout);
        applicationsDialog.open();
    }

    /**
     * Creates a dialog box to display a student's motivation and grade for a
     * specific course.
     * 
     * @param studentID String that represetns the unique identifier for a student.
     */
    private void openMotivationDialog(String studentID) {
        Dialog motivationDialog = new Dialog();
        motivationDialog.setWidth("500px");
        motivationDialog.setHeaderTitle(studentID + "'s Motivation");

        NumberField gradeField = new NumberField("Grade for " + courseCode);
        gradeField.setWidth("50%");
        gradeField.setSuffixComponent(new Div(new Span("%")));
        gradeField.setReadOnly(true);

        //*****************************************************************************************
        gradeField.setValue(Double.parseDouble(StudentManager.getGradeFor(courseCode, studentID)));
        //*****************************************************************************************

        // Create a TextArea to display the student's motivation
        TextArea motivationTextArea = new TextArea();
        motivationTextArea.setReadOnly(true);
        motivationTextArea.setWidthFull();
        motivationTextArea.setHeight("300px");

        //*******************************************************************
        motivationTextArea.setValue(StudentManager.getMotivation(studentID));
        //*******************************************************************

        Button closeButton = new Button("Close", event -> motivationDialog.close());
        closeButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        closeButton.setWidth("100%");

        VerticalLayout layout = new VerticalLayout(gradeField, motivationTextArea);
        layout.setPadding(false);
        layout.setSpacing(true);

        motivationDialog.add(layout);
        motivationDialog.getFooter().add(closeButton);
        motivationDialog.open();
    }

    /**
     * Creates a grid for displaying a schedule, with columns for hours and each day
     * of the week,
     * and adds a click listener to open a dialog with information about the clicked
     * timeslot and day.
     */
    private void configureScheduleGrid() {
        // Create a grid for the schedule
        scheduleGrid = new Grid<>();
        scheduleGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        scheduleGrid.addClassName("schedule");
        scheduleGrid.getStyle().set("border-radius", "15px");
        scheduleGrid.getStyle().set("overflow", "hidden");
        scheduleGrid.setHeightFull();

        // Create a column for the hours
        scheduleGrid.addColumn(TimeSlot::getTimeslot).setHeader("Time");

        // Add columns for each day of the week
        for (String day : Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")) {
            scheduleGrid.addComponentColumn(timeSlot -> {
                List<Button> buttons = timeSlot.getButtonsByDay().getOrDefault(day, Collections.emptyList());
                return createButtonGroup(buttons);
            }).setHeader(day);
        }

        // Create data for the grid
        List<TimeSlot> data = getCleanScheduleData();

        // Add click listener to the schedule grid
        scheduleGrid.addItemClickListener(this::handleScheduleCellClick);

        // Set the data provider for the grid
        scheduleGrid.setItems(data);
    }

    /**
     * Populates ScheduleGrid with tutoring session buttons based on fetched
     * Schedule.
     */
    private void populateScheduleGrid() {
        List<TimeSlot> data = getCleanScheduleData();

        // If Schedule is currently empty, just set it to cleanScheduleData
        if (schedule.getTutoringSessions().isEmpty()) {
            scheduleGrid.setItems(data);
            return;
        }

        Iterator<String> colorIterator = sessionColours.iterator();

        for (TutoringSession ts : schedule.getTutoringSessions()) {
            String randomColour = colorIterator.hasNext() ? colorIterator.next() : "#000000"; // Use black if colors run out

            int rowIndex = (int) ts.getStartTimeAsDouble() - 8;
            TimeSlot currentTimeSlot = data.get(rowIndex);

            // Create a button for the TutoringSession
            Button button = viewSessionButton(ts, true);
            button.getStyle().set("background-color", randomColour);

            currentTimeSlot.addButton(ts.getDay(), button);

            int duration = (int) (Math.ceil(ts.getEndTimeAsDouble()) - ts.getStartTimeAsDouble()) - 1;

            for (int i = 0; i < duration; i++) {

                rowIndex++;
                currentTimeSlot = data.get(rowIndex);

                // Create a new button for each time slot
                button = viewSessionButton(ts, false);
                button.getStyle().set("background-color", randomColour);

                currentTimeSlot.addButton(ts.getDay(), button);
            }
        }

        scheduleGrid.setItems(data);
    }

    /**
     * Creates a button with the capacity of a tutoring session and adds a click
     * listener
     * to handle button clicks.
     * 
     * @param ts TutoringSession, which contains information about a tutoring
     *           session.
     * @return Button object.
     */
    private Button viewSessionButton(TutoringSession ts, boolean isFirstHour) {
        Button button = new Button("");
        button.addClickListener(event -> openTutoringSessionDialog(ts));

        if (isFirstHour) {

            // Check if current user is a Tutor who is signed up for this TutoringSession
            if (role.equals(Role.Student) && isTutorOrTA.equals(Role.Tutor)) {

                boolean isCurrentUserSignedUp = ts.getSignedUpTutors()
                                                .stream()
                                                .anyMatch(tutor -> tutor.getStudentID().equals(id));

                if (isCurrentUserSignedUp) {
                    button.setText("SIGNED UP");
                    return button;
                }

            }

            if (ts.getSignedUpTutors() == null) {
                button.setText("Available: " + ts.getTutoringCapacity());
            } else {
                button.setText("Available: " + (ts.getTutoringCapacity() - ts.getSignedUpTutors().size()));
            }
        }

        return button;
    }

    /**
     * Creates a list of TimeSlot objects representing time slots from 8:00 to
     * 19:00.
     * Used to generate data to configure ScheduleGrid, i.e. an empty schedule.
     * 
     * @return List of TimeSlot objects.
     */
    private List<TimeSlot> getCleanScheduleData() {
        List<TimeSlot> data = new ArrayList<>();
        for (int i = 8; i <= 19; i++) {
            String timeslot = String.format("%02d:00", i);
            data.add(new TimeSlot(timeslot));
        }
        return data;
    }

    /**
     * Creates a button group component using a list of buttons, applying styling
     * and adding the buttons to the group.
     * 
     * @param buttons A list of Button objects that will be added to the button
     *                group.
     * @return Component, specifically a Div component that contains a group of
     *         buttons.
     */
    private Component createButtonGroup(List<Button> buttons) {
        Div buttonGroup = new Div();

        // Use Flexbox to make buttons wrap and stack vertically
        buttonGroup.getStyle().set("display", "flex");
        buttonGroup.getStyle().set("flex-wrap", "wrap");
        buttonGroup.getStyle().set("flex-direction", "column");

        for (Button button : buttons) {
            button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            button.setHeightFull();
            buttonGroup.add(button);
        }

        buttonGroup.setWidth("75%");
        buttonGroup.setHeightFull();

        return buttonGroup;
    }

    /**
     * Handles a cell click event by displaying a dialog with information about the
     * clicked timeslot and day.
     * 
     * @param event ItemClickEvent<TimeSlot>, the event that occurred when a cell in
     *              a grid was clicked.
     */
    private void handleScheduleCellClick(ItemClickEvent<TimeSlot> event) {
        if (isEditClicked) {
            TimeSlot gridItem = event.getItem();
            String timeslot = gridItem.getTimeslot();
            String day = event.getColumn().getHeaderText();

            // Ignore the click if user clicked on the TimeSlot column
            if (day == "Time")
                return;

            // Open the "Create Tutoring Session" dialog with the day and timeslot as parameters
            openCreateTutoringSessionDialog(day, timeslot);
        }
    }

    /**
     * Creates and opens a dialog box for creating a tutoring session,
     * allowing the user to input details.
     * 
     * @param day      String that represents the day of the tutoring session.
     * @param timeslot String that represents the time slot for the tutoring
     *                 session.
     */
    private void openCreateTutoringSessionDialog(String day, String timeslot) {
        // Create a dialog for creating a tutoring session
        Dialog createSessionDialog = new Dialog();
        createSessionDialog.setHeaderTitle("Create a Tutoring Session");

        VerticalLayout createSessionContainer = new VerticalLayout();
        createSessionContainer.setPadding(false);
        createSessionContainer.setWidth("550px");

        // #region TutoringSession Fields
        TextField dayField = new TextField("Day");
        dayField.getStyle().set("padding", "0");
        dayField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        dayField.setWidthFull();
        dayField.setValue(day);
        dayField.setReadOnly(true);

        TimePicker startTimePicker = new TimePicker("Start Time");
        startTimePicker.getStyle().set("padding", "0");
        startTimePicker.setWidthFull();
        startTimePicker.setValue(LocalTime.parse(timeslot));
        startTimePicker.setReadOnly(true);

        TimePicker endTimePicker = new TimePicker("End Time");
        endTimePicker.setPlaceholder("Time when this session ends...");
        endTimePicker.getStyle().set("padding", "0");
        endTimePicker.setRequired(true);
        endTimePicker.setWidthFull();
        endTimePicker.setStep(Duration.ofMinutes(30));
        endTimePicker.setMin(LocalTime.parse(timeslot).plusHours(1));
        endTimePicker.setMax(LocalTime.parse("20:00"));

        TextField locationField = new TextField("Location");
        locationField.setPlaceholder("Venue");
        locationField.getStyle().set("padding", "0");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.setRequired(true);
        locationField.setWidthFull();

        TextField whatsappLinkTextField = new TextField("Whatsapp Group Link");
        whatsappLinkTextField.setPlaceholder("Link to the WhatsApp group that tutors of this session should join...");
        whatsappLinkTextField.getStyle().set("padding", "0");
        whatsappLinkTextField.setPrefixComponent(VaadinIcon.LINK.create());
        whatsappLinkTextField.setWidthFull();

        IntegerField tutorCapacityField = new IntegerField("Tutor Capacity");
        tutorCapacityField.setPlaceholder("Number of tutors to work this session...");
        tutorCapacityField.getStyle().set("padding", "0");
        tutorCapacityField.setStep(1);
        tutorCapacityField.setStepButtonsVisible(true);
        tutorCapacityField.setRequired(true);
        tutorCapacityField.setMin(1);
        tutorCapacityField.setWidthFull();
        // #endregion

        createSessionContainer.add(dayField, startTimePicker, endTimePicker, locationField, whatsappLinkTextField, tutorCapacityField);

        // Creating a new TutoringSesssion
        Button createButton = new Button("Create", event -> {
            // Handles the (local) creation of the tutoring session here

            // #region Empty-Field Check

            // Check if the "End Time" field is empty
            if (endTimePicker.isEmpty()) {
                endTimePicker.setInvalid(true);
                Dialogs.showDialog("Sorry! Please enter the end time...");
                return;
            }

            // Check if the "Location" field is empty
            if (locationField.isEmpty()) {
                locationField.setInvalid(true);
                Dialogs.showDialog("Sorry! Please enter the location...");
                return;
            }

            // Check if the "Tutor Capacity" field is empty
            if (tutorCapacityField.isEmpty()) {
                tutorCapacityField.setInvalid(true);
                Dialogs.showDialog("Sorry! Please enter the tutor capacity...");
                return;
            }

            // #endregion

            // Validate WhatsappLink if one has been entered
            if (!whatsappLinkTextField.isEmpty() && !validateWhatsappLink(whatsappLinkTextField.getValue())) {
                whatsappLinkTextField.setInvalid(true);
                Dialogs.showDialog("Sorry! Invalid WhatsApp link...");
                return;
            }

            // Create new TutoringSession to be added to the local copy - not saved to DB
            // yet
            TutoringSession newTS = new TutoringSession(
                    startTimePicker.getValue(),
                    endTimePicker.getValue(),
                    dayField.getValue(),
                    locationField.getValue(),
                    whatsappLinkTextField.getValue(),
                    tutorCapacityField.getValue()
            );

            schedule.addTutoringSession(newTS);
            populateScheduleGrid();
            createSessionDialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setIcon(VaadinIcon.PLUS.create());

        // Cancelling out of the TutoringSession Form
        Button cancelButton = new Button("Cancel", event -> createSessionDialog.close());
        cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, createButton);
        buttonLayout.setWidthFull();
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Add components to the dialog
        createSessionDialog.add(createSessionContainer);
        createSessionDialog.getFooter().add(buttonLayout);

        // Open the dialog
        createSessionDialog.open();
    }

    /**
     * Creates and opens a dialog for viewing and editing a tutoring session,
     * allowing the user to modify various fields and save the changes.
     * 
     * @param tutoringSession The `tutoringSession` parameter is an object of the
     *                        `TutoringSession`
     *                        class. It represents a specific tutoring session and
     *                        contains information such as the day, start
     *                        time, end time, location, WhatsApp group link, and
     *                        tutor capacity for that session.
     */
    private void openTutoringSessionDialog(TutoringSession tutoringSession) {
        // Create a dialog for editing a tutoring session
        Dialog sessionDialog = new Dialog();
        sessionDialog.setHeaderTitle("Tutoring Session Form");

        VerticalLayout sessionContainer = new VerticalLayout();
        sessionContainer.setPadding(false);
        sessionContainer.setWidth("750px");

        // #region TutoringSession Fields
        TextField dayField = new TextField("Day");
        dayField.getStyle().set("padding", "0");
        dayField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        dayField.setWidthFull();
        dayField.setValue(tutoringSession.getDay());

        TimePicker startTimePicker = new TimePicker("Start Time");
        startTimePicker.getStyle().set("padding", "0");
        startTimePicker.setWidthFull();
        startTimePicker.setStep(Duration.ofMinutes(30));
        startTimePicker.setValue(tutoringSession.getStartTimeAsLocalTime());
        startTimePicker.setMin(LocalTime.parse("08:00"));
        startTimePicker.setMax(LocalTime.parse("19:00"));

        TimePicker endTimePicker = new TimePicker("End Time");
        endTimePicker.getStyle().set("padding", "0");
        endTimePicker.setWidthFull();
        endTimePicker.setStep(Duration.ofMinutes(30));
        endTimePicker.setValue(tutoringSession.getEndTimeAsLocalTime());
        endTimePicker.setMin(startTimePicker.getValue().plusHours(1));
        endTimePicker.setMax(LocalTime.parse("20:00"));

        TextField locationField = new TextField("Location");
        locationField.getStyle().set("padding", "0");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.setWidthFull();
        locationField.setValue(tutoringSession.getLocation());

        TextField whatsappLinkTextField = new TextField("Whatsapp Group Link");
        whatsappLinkTextField.getStyle().set("padding", "0");
        whatsappLinkTextField.setPrefixComponent(VaadinIcon.LINK.create());
        whatsappLinkTextField.setWidthFull();
        whatsappLinkTextField.setValue(tutoringSession.getWhatsappLink());

        IntegerField tutorCapacityField = new IntegerField("Tutor Capacity");
        tutorCapacityField.getStyle().set("padding", "0");
        tutorCapacityField.setStep(1);
        tutorCapacityField.setStepButtonsVisible(true);
        tutorCapacityField.setMin(1);
        tutorCapacityField.setWidthFull();
        tutorCapacityField.setValue(tutoringSession.getTutoringCapacity());

        // Create a grid for signed-up tutors
        Grid<Tutor> tutorsGrid = new Grid<>(Tutor.class);
        tutorsGrid.setHeight("250px");
        tutorsGrid.setWidthFull();

        // if no tutors have signed up
        if (tutoringSession.getSignedUpTutors() == null) {
            tutorsGrid.setItems(new ArrayList<Tutor>());
        } else {
            tutorsGrid.setItems(tutoringSession.getSignedUpTutors()); // Populate with signed-up tutors
        }

        tutorsGrid.setColumns("studentID", "firstName", "lastName", "email", "qualificationLevel"); // Customize the
                                                                                                    // columns as needed

        // Add a component column for the "Remove" button - only available to Employees or TAs
        // if (role.equals(Role.Employee) || role.equals(Role.Admin)) {
        //     tutorsGrid.addComponentColumn(tutor -> {
        //         Button removeButton = new Button("Remove");
        //         removeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR,
        //                 ButtonVariant.LUMO_PRIMARY);
        //         removeButton.setIcon(VaadinIcon.TRASH.create());
        //         removeButton.addClickListener(event -> {
        //             // Removing a Tutoring
        //             tutoringSession.removeTutor(tutor);
        //             tutorsGrid.setItems(tutoringSession.getSignedUpTutors());
        //         });
        //         return removeButton;
        //     }).setHeader("Actions");
        // }

        // #endregion

        sessionContainer.add(dayField, startTimePicker, endTimePicker, locationField, whatsappLinkTextField, tutorCapacityField, tutorsGrid);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.setWidthFull();

        Button cancelButton = new Button("Cancel", event -> sessionDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        cancelButton.setIcon(VaadinIcon.ARROW_BACKWARD.create());
        buttonLayout.add(cancelButton);

        // EDIT MODE
        if (isEditClicked) {
            dayField.setRequired(true);
            startTimePicker.setRequired(true);
            endTimePicker.setRequired(true);
            locationField.setRequired(true);
            tutorCapacityField.setRequired(true);

            // Editing a TutoringSession
            Button saveButton = new Button("Save", event -> {
                // Handle saving changes to the tutoring session here

                // #region Empty-Field Check

                // Check if the "Day" field is empty
                if (dayField.isEmpty()) {
                    dayField.setInvalid(true);
                    Dialogs.showDialog("Sorry! Please enter the day...");
                    return;
                }

                // Check if the "Start Time" field is empty
                if (startTimePicker.isEmpty()) {
                    startTimePicker.setInvalid(true);
                    Dialogs.showDialog("Sorry! Please enter the start time...");
                    return;
                }

                // Check if the "End Time" field is empty
                if (endTimePicker.isEmpty()) {
                    endTimePicker.setInvalid(true);
                    Dialogs.showDialog("Sorry! Please enter the end time...");
                    return;
                }

                // Check if the "Location" field is empty
                if (locationField.isEmpty()) {
                    locationField.setInvalid(true);
                    Dialogs.showDialog("Sorry! Please enter the location...");
                    return;
                }

                // Check if the "Tutor Capacity" field is empty
                if (tutorCapacityField.isEmpty()) {
                    tutorCapacityField.setInvalid(true);
                    Dialogs.showDialog("Sorry! Please enter the tutor capacity...");
                    return;
                }

                // #endregion

                // Validate WhatsappLink if one has been entered
                if (!whatsappLinkTextField.isEmpty() && !validateWhatsappLink(whatsappLinkTextField.getValue())) {
                    whatsappLinkTextField.setInvalid(true);
                    Dialogs.showDialog("Sorry! Invalid WhatsApp link...");
                    return;
                }

                // Create new TutoringSession to be added to the local copy - not saved to DB yet
                TutoringSession updatedTutoringSession = new TutoringSession(
                        startTimePicker.getValue(),
                        endTimePicker.getValue(),
                        dayField.getValue(),
                        locationField.getValue(),
                        whatsappLinkTextField.getValue(),
                        tutorCapacityField.getValue()
                );

                updatedTutoringSession.setSessionID(tutoringSession.getSessionID());
                updatedTutoringSession.setSignedUpTutors(tutoringSession.getSignedUpTutors());

                // Replace the original tutoringSession in the schedule with updatedTutoringSession
                for (int i = 0; i < schedule.getTutoringSessions().size(); i++) {
                    if (schedule.getTutoringSessions().get(i).equals(tutoringSession)) {
                        schedule.getTutoringSessions().set(i, updatedTutoringSession);
                        break;
                    }
                }

                // Update the grid with the modified session
                populateScheduleGrid();
                sessionDialog.close();
            });
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            saveButton.setIcon(VaadinIcon.DOWNLOAD.create());

            // Deleting the selected TutoringSession locally - not changed in DB
            Button deleteButton = new Button("Delete", event -> {

                // Remove TutoringSession from current Schedule
                schedule.removeTutoringSession(tutoringSession);

                // Update the grid with the modified session
                populateScheduleGrid();
                sessionDialog.close();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            deleteButton.setIcon(VaadinIcon.DEL.create());

            HorizontalLayout otherButtons = new HorizontalLayout(saveButton, deleteButton);
            buttonLayout.add(otherButtons);

            // VIEW MODE
        } else {
            dayField.setReadOnly(true);
            startTimePicker.setReadOnly(true);
            endTimePicker.setReadOnly(true);
            locationField.setReadOnly(true);
            whatsappLinkTextField.setReadOnly(true);
            tutorCapacityField.setReadOnly(true);
            tutorsGrid.setEnabled(false);

            // Check if current user is a Tutor who is signed up for this TutoringSession
            if (role.equals(Role.Student) && isTutorOrTA.equals(Role.Tutor)) {

                boolean isCurrentUserSignedUp = tutoringSession.getSignedUpTutors()
                        .stream()
                        .anyMatch(tutor -> tutor.getStudentID().equals(id));

                if (!isCurrentUserSignedUp) {
                    // If there is space to sign up, make the sign-up button visible
                    if (tutoringSession.getSignedUpTutors().size() < tutoringSession.getTutoringCapacity()) {
                        // Create a signup button
                        Button signUpButton = new Button("Sign Up", event -> {

                            //********************************************************************************
                            Response result = ScheduleManager.tutorSignUp(tutoringSession.getSessionID(), id);
                            //********************************************************************************

                            if (result == Response.SUCCESS) {
                                Dialogs.showDialog("Successfully signed up to the session!");
                                sessionDialog.close();

                                //******************************************************
                                schedule = ScheduleManager.getSchedule(courseCode, year);
                                //******************************************************
                                populateScheduleGrid();

                            } else {
                                Dialogs.showDialog("Sorry! Something went wrong signing up to the session...Please try again!");
                                sessionDialog.close();
                            }
                        });
                        signUpButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
                        signUpButton.setWidthFull();
                        sessionContainer.add(signUpButton);
                    }

                } else {
                    // Current user is signed up to this TutoringSession
                    VerticalLayout signedUpLayout = new VerticalLayout();
                    signedUpLayout.setPadding(false);
                    signedUpLayout.setAlignItems(Alignment.CENTER);
                    signedUpLayout.setWidthFull();

                    // Create a button for the user to leave the TutoringSession
                    Button leaveButton = new Button("Leave", event -> {

                        //*******************************************************************************
                        Response result = ScheduleManager.tutorLeave(tutoringSession.getSessionID(), id);
                        //*******************************************************************************

                        if (result == Response.SUCCESS) {
                            // Remove the current user from signed-up tutors
                            Dialogs.showDialog("You have left the session.");
                            sessionDialog.close();

                            //*******************************************************
                            schedule = ScheduleManager.getSchedule(courseCode, year);
                            //*******************************************************
                            populateScheduleGrid();

                        } else {
                            Dialogs.showDialog("Sorry! Something went wrong leaving the session...Please try again!");
                        }

                    });

                    leaveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
                    leaveButton.setWidthFull();
                    signedUpLayout.add(leaveButton);

                    // #region TutoringSession Check-In

                    // Get the current day of the week
                    DayOfWeek currentDayOfWeek = LocalDate.now().getDayOfWeek();

                    // Check if it's the same day as the tutoring session
                    if (currentDayOfWeek == DayOfWeek.valueOf(tutoringSession.getDay().toUpperCase())) {
                        // Get the current time
                        LocalTime currentTime = LocalTime.now();

                        // Calculate the end time of the session plus 15 minutes
                        LocalTime sessionEndTimePlusFifteenMin = tutoringSession.getEndTimeAsLocalTime().plusMinutes(15);


                        // Check if the current time is within the grace period
                        if (currentTime.isAfter(tutoringSession.getEndTimeAsLocalTime()) && currentTime.isBefore(sessionEndTimePlusFifteenMin)) {

                            // Display a Check-In button
                            Button checkInButton = new Button("Check-In", event -> {

                                //*************************************************************************************************************************
                                Response result = ScheduleManager.tutorCheckIn(tutoringSession.getSessionID(), courseCode, id, LocalDate.now().toString());
                                //*************************************************************************************************************************

                                if (result == Response.SUCCESS) {
                                    Dialogs.showDialog("Your attendance has been successfully recorded!");
                                    sessionDialog.close();
                                } else {
                                    Dialogs.showDialog("Sorry! Something went wrong recording your attendance...Please try again!");
                                }
                            });
                            
                            checkInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
                            checkInButton.setWidthFull();
                            
                            //*************************************************************************************************
                            if (ScheduleManager.hasCheckedIn(tutoringSession.getSessionID(), id, LocalDate.now().toString())) {
                            //*************************************************************************************************
                                checkInButton.setEnabled(false);
                                checkInButton.getStyle().set("background-color", "gray");
                            }

                            signedUpLayout.add(checkInButton);
                        }
                    }

                    // #endregion

                    // Add the signedUpLayout to the sessionContainer
                    sessionContainer.add(signedUpLayout);
                }
            }

            // If user is an EMP or TA, allow the viewing of per sessions stats
            if (role.equals(Role.Employee) || isTutorOrTA.equals(Role.TA)) {
                Button viewSessionStatsButton = new Button("View Session Stats", event -> openPerSessionStatDialog(tutoringSession.getSessionID()));
                viewSessionStatsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                viewSessionStatsButton.setIcon(VaadinIcon.LINE_CHART.create());
                buttonLayout.add(viewSessionStatsButton);
            }
        }

        // Add components to the dialog
        sessionDialog.add(sessionContainer);
        sessionDialog.getFooter().add(buttonLayout);

        // Open the dialog
        sessionDialog.open();
    }

    /**
     * Checks if a given link is a valid WhatsApp group link.
     * 
     * @param link String that represents a WhatsApp group link.
     * @return Boolean value. True if the input string matches the regular
     *         expression pattern
     *         for a valid WhatsApp link, and false otherwise.
     */
    private boolean validateWhatsappLink(String link) {
        String WHATSAPP_LINK_REGEX = "https://chat.whatsapp.com/[A-Za-z0-9]+";

        // Compile the regular expression pattern
        Pattern pattern = Pattern.compile(WHATSAPP_LINK_REGEX);

        // Match the input string against the pattern
        Matcher matcher = pattern.matcher(link);

        // Return true if a match is found (i.e., it's a valid WhatsApp link)
        return matcher.matches();
    }

    /**
     * Generates a random RGB color code that has a brightness value above a
     * specified
     * minimum threshold.
     * 
     * @return Randomly generated hexadecimal color code that has a
     *         brightness value above the specified minimum brightness threshold.
     */
    private void initialiseSessionColours() {
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            // Generate random RGB values
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);

            // Calculate the brightness of the color
            double brightness = (red * 299 + green * 587 + blue * 114) / 1000;

            // If the brightness is too light, make the color darker
            if (brightness > 192) {
                red = Math.max(0, red - 50);
                green = Math.max(0, green - 50);
                blue = Math.max(0, blue - 50);
            }

            // Convert RGB back to hexadecimal format
            String hexColour = String.format("#%02x%02x%02x", red, green, blue);
            sessionColours.add(hexColour);
        }
    }

    /**
     * Opens a dialog box to display tutoring statistics for the selected course.
     */
    private void openTutoringStatsDialog() {
        Dialog perCourseStatsDialog = new Dialog();
        perCourseStatsDialog.setHeaderTitle("Tutoring Statistics for " + courseCode);

        VerticalLayout statLayout = new VerticalLayout();
        statLayout.setAlignItems(Alignment.CENTER);
        statLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
         
        IntegerField stat = new IntegerField("Number of Tutor Attendances:");
        stat.setReadOnly(true);
        stat.setWidthFull();
        stat.setHelperText("This is the overall number of tutor attendances for " + courseCode + ".");

        //*********************************************************
        stat.setValue(CourseManager.getCourseStatsFor(courseCode));
        //*********************************************************

        perCourseStatsDialog.add(stat);
        perCourseStatsDialog.open();
    } 

    /**
     * Creates a dialog box displaying the tutoring statistics for a selected tutor.
     * 
     * @param selectedTutor Tutor class. It represents the tutor for whom we want to display the tutoring statistics.
     */
    private void openPerTutorStatDialog(Tutor selectedTutor) { 
        Dialog perTutorStatDialog = new Dialog();
        perTutorStatDialog.setHeaderTitle(selectedTutor.getStudentID() + "'s Tutoring Statistics for " + courseCode);

        VerticalLayout statLayout = new VerticalLayout();
        statLayout.setAlignItems(Alignment.CENTER);
        statLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
         
        IntegerField stat = new IntegerField("Number of Tutoring Session Attendances:");
        stat.setReadOnly(true);
        stat.setWidthFull();
        stat.setHelperText("This is the number of sessions attended by " + selectedTutor.getStudentID() + " for " + courseCode + ".");

        //***************************************************************************************
        stat.setValue(StudentManager.getTutorStatsFor(courseCode, selectedTutor.getStudentID()));
        //***************************************************************************************

        perTutorStatDialog.add(stat);
        perTutorStatDialog.open();
    }

    /**
     * Creates a dialog box that displays tutoring statistics for a specific session,
     * including the number of tutor attendances.
     * 
     * @param sessionID Integer that represents the unique identifier for a tutoring session. 
     */
    private void openPerSessionStatDialog(int sessionID) {
        Dialog perSessionStatDialog = new Dialog();
        perSessionStatDialog.setHeaderTitle("Tutoring Statistics for this Session");

        VerticalLayout statLayout = new VerticalLayout();
        statLayout.setAlignItems(Alignment.CENTER);
        statLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        IntegerField stat = new IntegerField("Number of Tutor Attendances:");
        stat.setReadOnly(true);
        stat.setWidthFull();
        stat.setHelperText("This is the number of attendances across all tutors for this session.");

        //*******************************************************************
        stat.setValue(ScheduleManager.getTutoringSessionStatsFor(sessionID));
        //*******************************************************************

        perSessionStatDialog.add(stat);
        perSessionStatDialog.open(); 
    }

    /**
     * The TimeSlot class represents a time slot and stores buttons organized by
     * day.
     */
    private static class TimeSlot {
        private String timeslot;
        private Map<String, List<Button>> buttonsByDay; // Store buttons by day

        /**
         * Constructor that takes in a paramter called timeslot and assings it to timeslot attribute.
         * Also, initialises a new HashMap called buttonsByDay.
         * 
         * @param timeslot
         */
        public TimeSlot(String timeslot) {
            this.timeslot = timeslot;
            this.buttonsByDay = new HashMap<>();
        }

        /**
         * The function "getTimeslot" returns the value of the timeslot variable.
         * 
         * @return The method is returning a String value.
         */
        public String getTimeslot() {
            return timeslot;
        }

        /**
         * The function returns a map that contains a list of buttons for each day.
         * 
         * @return A Map object with String keys and List<Button> values is being returned.
         */
        public Map<String, List<Button>> getButtonsByDay() {
            return buttonsByDay;
        }

        /**
         * The function adds a button to a list of buttons associated with a specific day.
         * 
         * @param day A string representing the day for which the button is being added.
         * @param button The parameter "button" is of type Button. It represents the button that needs
         * to be added to the list of buttons for a specific day.
         */
        public void addButton(String day, Button button) {
            buttonsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(button);
        }
    }
}