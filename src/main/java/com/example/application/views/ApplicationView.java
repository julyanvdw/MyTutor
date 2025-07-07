package com.example.application.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.example.application.microservices.ApplyManager;
import com.example.application.Dialogs;
import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.Response;
import com.example.application.PublicEnums.Role;

/**
 * This class represents the Application view of the MyTutor application.
 * Students that are signed-up must apply here to indicate that they 
 * want to be considered for the upcoming Tutor/TA acceptances.
 */
@PageTitle("Apply | MyTutor")
@Route(value = "apply", layout = MainLayout.class)
public class ApplicationView extends VerticalLayout implements BeforeEnterObserver {

    /**
     * Constructs a ApplicationView instance and sets up the content layout.
     */
    public ApplicationView() {

        // Create a bordered layout
        VerticalLayout borderedLayout = new VerticalLayout();

        // Get current user if they are applied
        ApplicationStatus applicationStatus = (ApplicationStatus) VaadinSession.getCurrent().getAttribute("applicationStatus");

        if (applicationStatus == ApplicationStatus.APPLIED) {
            borderedLayout = configureAlreadyApplied();
        } else {
            borderedLayout = configureNoApplication();
        }

        borderedLayout.getStyle().set("margin", "auto");
        borderedLayout.getStyle().set("border", "1.5px solid #ddd");
        borderedLayout.getStyle().set("border-radius", "15px");
        borderedLayout.getStyle().set("box-shadow", "0px 0px 10px rgba(0, 0, 0, 0.2)");
        borderedLayout.setWidth("65%");
        borderedLayout.setAlignItems(Alignment.CENTER);
        borderedLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        add(borderedLayout);

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
    }

    /**
     * SECURITY AUTHENTICATION
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if ((String) VaadinSession.getCurrent().getAttribute("firstName") == null || (Role) VaadinSession.getCurrent().getAttribute("role") != Role.Student) {
            MainLayout.signOut(true);
        }
    }

    /**
     * For when Student has not applied yet, configures layout for making an Application.
     */
    private VerticalLayout configureNoApplication() {
        VerticalLayout layout = new VerticalLayout();

        // Create a welcome message
        H1 heading = new H1("MyTutor Application");
        // Image formImage = new Image("images/form.png", "Application Form");

        // Create a form for application
        FormLayout applicationForm = new FormLayout();
        applicationForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        TextArea motivationTextArea = new TextArea("Motivation");
        motivationTextArea.setPlaceholder("Tell us why you want to be a Tutor/TA and why you should be accepted...\nShare your motivation, skills, and accomplishments that make you a great candidate.");
        motivationTextArea.setWidth("100%");
        motivationTextArea.setHeight("250px");

        applicationForm.add(motivationTextArea);

        // Create a button for submission
        Button applyButton = new Button("Apply Now!");
        applyButton.addClickListener(e -> applyClick(motivationTextArea.getValue()));
        applyButton.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant.LUMO_PRIMARY);

        // Create a horizontal layout to center the button
        HorizontalLayout buttonLayout = new HorizontalLayout(applyButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Add components to the main layout
        layout.add(heading, applicationForm, buttonLayout);
        return layout;
    }

    /**
     * For when Student has already applied, configures layout to inform them of this.
     */
    private VerticalLayout configureAlreadyApplied() {
        VerticalLayout layout = new VerticalLayout();

        H1 heading = new H1("MyTutor Application");

        Image formImage = new Image("images/form.png", "Application Form");

        H2 description = new H2("You have already applied! Awaiting approval...");
        Text lastly = new Text("You don't need to perform any actions!");

        // Add components to the main layout
        layout.add(heading, formImage, description, lastly);
        return layout;
    }

    /**
     * Retrieves the current student from the VaadinSession and uses it to
     * submit an application with a given motivation.
     * 
     * @param motivation String that represents the motivation for the student's application.
     */
    private void applyClick(String motivation) {

        //************************************************************************************************************
        Response result = ApplyManager.apply((String) VaadinSession.getCurrent().getAttribute("email"), (String) VaadinSession.getCurrent().getAttribute("id"), motivation);
        //************************************************************************************************************

        switch (result) {
            case SUCCESS:
                Dialogs.showDialog("Application submitted successfully!");
                VaadinSession.getCurrent().setAttribute("applicationStatus", ApplicationStatus.APPLIED);
                return;
            case QUALIFICATION_LEVEL_TOO_LOW: // if user is a first-year
            case ACADEMIC_STANDING_NOT_SATISFACTORY: // if user's GPA < 60
            default:
                break;
        }

        Dialogs.showDialog(result);
    }
}