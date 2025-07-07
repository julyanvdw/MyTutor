package com.example.application.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.EmailField;

import com.example.application.Dialogs;
import com.example.application.PublicEnums.Response;
import com.example.application.microservices.SignInManager;

/**
 * The welcome view of the MyTutor application, where users can sign in or sign up.
 */
@PageTitle("Sign In | MyTutor")
@Route("") // Maps the view to the root URL of the application
@RouteAlias("access-denied")
@AnonymousAllowed
public class WelcomeView extends VerticalLayout implements BeforeEnterObserver {

    private EmailField emailField;
    private PasswordField passwordField;

    /**
     * Creates and configures the welcome view UI components.
     */
    public WelcomeView() {
        configureLayout();
    }

    /**
     * SECURITY AUTHENTICATION
     * @param event
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if the URL contains "access-denied"
        String path = event.getLocation().getPath();
        if (path.contains("access-denied")) {
            Dialogs.showDialog("Sorry! Access Denied...");
        }
    }

    /**
     * Configures the layout of the welcome view.
     */
    private void configureLayout() {
        add(createLogo());
        add(createHeading());
        add(createInputFields());

        // Adding the control buttons
        HorizontalLayout buttons = createButtons();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);
        add(buttons);

        setSizeFull();
        addClassName("welcome-view");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setMinWidth("240px");
        setMinHeight("400px");
        setPadding(false);
    }

    /**
     * Creates an Image component with the image source from resources.
     *
     * @return HorizontalLayout containing the logo image.
     */
    private HorizontalLayout createLogo() {
        Image logoImage = new Image("images/mytutor-logo.png", "MyTutor Logo");
        logoImage.addClassNames("logo-image"); // CSS class for styling

        HorizontalLayout logo = new HorizontalLayout(logoImage);
        logo.setWidthFull();
        logo.setJustifyContentMode(JustifyContentMode.CENTER);
        return logo;
    }

    /**
     * Creates the heading section.
     *
     * @return Div containing the welcome heading.
     */
    private Div createHeading() {
        Div headingContainer = new Div(new H1("Welcome to MyTutor"));
        headingContainer.addClassNames("welcome-heading"); // CSS class for styling
        headingContainer.setWidthFull();
        return headingContainer;
    }

    /**
     * Creates the input fields for email and password.
     * 
     * @return A HorizontalLayout containing the input fields.
     */
    private HorizontalLayout createInputFields() {
        emailField = new EmailField("Email");
        emailField.setPlaceholder("Enter your email here...");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setClearButtonVisible(true);

        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter your password here...");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setPrefixComponent(VaadinIcon.PASSWORD.create());
        passwordField.setClearButtonVisible(true);

        passwordField.setWidthFull();
        emailField.setWidthFull();

        VerticalLayout emailAndPassword = new VerticalLayout(emailField, passwordField);
        emailAndPassword.setSpacing(false);
        emailAndPassword.setWidth("300px");

        HorizontalLayout inputFieldsLayout = new HorizontalLayout(emailAndPassword);
        inputFieldsLayout.setWidthFull();
        inputFieldsLayout.getStyle().set("padding-top", "0px");
        inputFieldsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        return inputFieldsLayout;
    }

    /**
     * Creates the sign-in and sign-up buttons.
     * 
     * @return A HorizontalLayout containing the buttons.
     */
    private HorizontalLayout createButtons() {
        // Create the buttons
        Button signInButton = new Button("Sign In");
        signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button signUpButton = new Button("Sign Up");
        signUpButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        // Place the VerticalLayout with caption and button inside the HorizontalLayout
        HorizontalLayout signInAndUp = new HorizontalLayout(signInButton, signUpButton);
        signInAndUp.setSpacing(true);

        // Center-align the HorizontalLayout within the parent layout
        HorizontalLayout buttonsLayout = new HorizontalLayout(signInAndUp);
        buttonsLayout.setWidthFull();
        buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Adding click listeners to the sign-in and sign-up buttons.
        signInButton.addClickListener(event -> signInClick(emailField.getValue(), passwordField.getValue()));
        signUpButton.addClickListener(event -> signUpClick());

        return buttonsLayout;
    }

    /**
     * Checks if the email or password is empty, invokes the SignInMicroservice to
     * interact with the database, and handles different response cases.
     * 
     * @param email     A string of the user's email address.
     * @param password  A String of the password entered by the user.
     */
    private void signInClick(String email, String password) {
        // Check if the email or password is empty
        if (email.isBlank() || password.isEmpty()) {
            setFieldsInvalid();
            Dialogs.showDialog(Response.EMPTY_FIELD);
            return;
        }

        //*********************************************************/
        // Invoke the SignInMicroservice to interact with database
        Response result = SignInManager.signIn(email, password);
        //*********************************************************/
        
        // Handles different response cases from the SignInManager.signIn() method.
        switch (result) {
            case SUCCESS:          
                UI.getCurrent().navigate(DashboardView.class);
                return;
            case INVALID_EMAIL:
                emailField.setInvalid(true);
                break;            
            case INVALID_CREDENTIALS:
                setFieldsInvalid();
                break;
            default:
                break;
        }

        Dialogs.showDialog(result);
    }

    /**
     * Handles the click event when the sign-up button is clicked.
     */
    private void signUpClick() {
        // Redirect to the ApplicationView for sign-up
        UI.getCurrent().navigate(SignUpView.class);
    }

    /**
     * Set the input fields as invalid.
     */
    private void setFieldsInvalid() {
        emailField.setInvalid(true);
        passwordField.setInvalid(true);
    }
}