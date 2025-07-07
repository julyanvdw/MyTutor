package com.example.application.views;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import com.vaadin.flow.data.value.ValueChangeMode;

import com.example.application.PublicEnums.Role;

import com.example.application.models.Course;
import com.example.application.models.Lecturer;
import com.example.application.models.Student;

import com.example.application.microservices.CourseManager;

/**
 * A view that displays a list of active courses as clickable blocks.
 * Clicking on a course block navigates to the corresponding CourseView.
 */
@PageTitle("All Courses | MyTutor")
@Route(value = "courses", layout = MainLayout.class)
public class CoursesView extends VerticalLayout implements BeforeEnterObserver {

    private List<Course> courses;
    private TextField searchField;
    private HorizontalLayout coursesContainer;

    /**
     * Constructs a CoursesView and initializes the layout with course blocks.
     */
    public CoursesView() {
        setPadding(false);

        Role role = (Role) VaadinSession.getCurrent().getAttribute("role");

        if (role != null) {
            courses = initialiseCourses(role);

            coursesContainer = createCoursesContainer();

            if (courses.isEmpty()) {
                configureNoCoursesFound();
                setAlignItems(Alignment.CENTER);
                setJustifyContentMode(JustifyContentMode.CENTER);
                setHeightFull();
                return;
            }

            searchField = createSearchField();

            // Create a horizontal layout for the search field
            HorizontalLayout searchLayout = new HorizontalLayout(searchField);
            searchLayout.setWidth("300px");

            add(searchLayout, coursesContainer);
            updateCourseBlocks(courses);
        }
    }

    /**
     * SECURITY AUTHENTICATION
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if ((String) VaadinSession.getCurrent().getAttribute("firstName") == null || (Role) VaadinSession.getCurrent().getAttribute("role") == null || (Role) VaadinSession.getCurrent().getAttribute("role") == Role.Admin) {
            MainLayout.signOut(true);
        }
    }

    /**
     * Initializes the list of courses based on the user's role.
     *
     * @param role The user's role.
     * @return List of Course objects.
     */
    private List<Course> initialiseCourses(Role role) {
        List<Course> initialisedCourses = new ArrayList<>();

        switch (role) {
            case Admin:
                // If user is an Admin, show ALL Courses.
                initialisedCourses = CourseManager.getCourses();
                break;

            // If user is an Employee (CC or Lecturer),
            // show only Courses that they are associated with - AccessibleCourses table.
            case Employee:
                initialisedCourses = CourseManager.getCoursesFor((Lecturer) VaadinSession.getCurrent().getAttribute("personObject"));
                break; 

            // If the user is an Accepted Student (Tutor or TA),
            // show only the Course they are working for or have worked in the past
            case Student:
                initialisedCourses = CourseManager.getCoursesFor((Student) VaadinSession.getCurrent().getAttribute("personObject"));
                break;

            case NoRole:
            default:
                // If the user is a Non-Accepted Student (Idle, Applied, or Rejected)
                // show a message that tells them that no Courses are there.
                break;
        }

        return initialisedCourses;
    }

    /**
     * Creates the search field with appropriate settings.
     *
     * @return A TextField for searching by course code.
     */
    private TextField createSearchField() {
        TextField textField = new TextField("Search by Course Code");
        textField.setWidthFull();
        textField.getStyle().set("padding-left", "20px");
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        textField.addValueChangeListener(event -> filterCourses(event.getValue()));
        return textField;
    }

    /**
     * Creates the container for course blocks.
     *
     * @return A HorizontalLayout to contain course blocks.
     */
    private HorizontalLayout createCoursesContainer() {
        HorizontalLayout container = new HorizontalLayout();
        container.addClassName("course-block-container");
        container.setSizeFull();
        return container;
    }

    /**
     * Filters a list of courses based on a given course code and updates the course blocks accordingly.
     * 
     * @param courseCode String that represents the code of a course to be filtered.
     */
    private void filterCourses(String courseCode) {

        // If the search field is empty, show all courses
        if (courseCode == null || courseCode.isEmpty()) {
            updateCourseBlocks(courses);

        } else {
            // Filter courses based on the entered courseCode
            List<Course> filteredCourses = courses.stream()
                    .filter(course -> course.getCourseCode().toLowerCase().contains(courseCode.toLowerCase()))
                    .collect(Collectors.toList());

            updateCourseBlocks(filteredCourses);
        }
    }

    /**
     * Updates the course blocks by removing existing blocks, adding new blocks based on
     * the provided list of courses, and applying a CSS class to the container.
     * 
     * @param courses List of Course objects.
     */
    private void updateCourseBlocks(List<Course> courses) {

        // Clear the existing course blocks
        coursesContainer.removeAll();

        if (courses.isEmpty()) {
            coursesContainer.add("No courses matching \"" + searchField.getValue() + "\" were found...");
        } else {
            coursesContainer.addClassName("course-block-container");

            // Sort courses by year in descending order
            courses.sort(Comparator.comparingInt(Course::getYear).reversed());

            // Create course blocks and add them to the layout
            for (Course course : courses) {
                CourseBlock courseBlock = new CourseBlock(course.getCourseCode(), course.getYear());
                coursesContainer.add(courseBlock);
            }
        }
    }

    /**
     * Configures and adds components to display a message indicating that no courses were found.
     */
    private void configureNoCoursesFound() {
        VerticalLayout noCoursesLayout = new VerticalLayout();
        noCoursesLayout.setAlignItems(Alignment.CENTER);

        H1 description = new H1("Sorry, no courses were found for you!");
        H2 lastly = new H2("You have either not applied, or are still awaiting approval...");
        Text apology = new Text("If this is an error, please contact mytutor.capstone@gmail.com for assistance!");

        noCoursesLayout.getStyle().set("margin", "auto");
        noCoursesLayout.getStyle().set("border", "1.5px solid #ddd");
        noCoursesLayout.getStyle().set("border-radius", "15px");
        noCoursesLayout.getStyle().set("box-shadow", "0px 0px 10px rgba(0, 0, 0, 0.2)");
        noCoursesLayout.setWidth("80");
        noCoursesLayout.setAlignItems(Alignment.CENTER);
        noCoursesLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add components to the main layout
        noCoursesLayout.add(description, lastly, apology);
        add(noCoursesLayout);
    }

    /**
     * A custom button representing a course block.
     */
    public class CourseBlock extends Button {

        /**
         * Constructs a CourseBlock button with the provided course code.
         * 
         * @param courseCode The code of the course associated with the block.
         * @param year       The year of the course.
         */
        public CourseBlock(String courseCode, int year) {
            addClassName("course-block");

            if (year == 0) { //Handles admin viewing
                setText(courseCode);
            } else {
                setText(courseCode + ", " + year);
            }

            if (year == 2023) {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }

            setIcon(VaadinIcon.BOOK.create());

            addClickListener(event -> {
                // Store the selected course code and year in the session
                VaadinSession.getCurrent().setAttribute("selectedCourseCode", courseCode);
                VaadinSession.getCurrent().setAttribute("selectedCourseYear", year);

                // Construct the route URL for the CourseView with the course code and year as a parameter
                String routeUrl = "courses/course-view/" + courseCode + "/" + year;

                // Navigate to the constructed route URL
                UI.getCurrent().navigate(routeUrl);
            });
        }
    }
}