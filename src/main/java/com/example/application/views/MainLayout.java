package com.example.application.views;

import com.vaadin.flow.router.PageTitle;

import com.vaadin.flow.server.VaadinSession;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.PublicEnums.ApplicationStatus;
import com.example.application.PublicEnums.Role;

/**
 * The main layout for the application, acting as a top-level placeholder for   other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;
    private String firstName;
    private Role role;
    private ApplicationStatus applicationStatus;

    private Image logoImage = new Image("images/side-nav-logo-LIGHT.png", "MyTutor Logo");
    private boolean currentThemeIsDark = false;

    /**
     * Constructs the MainLayout and initializes its contents.
     */
    public MainLayout() {
        initialiseUserDetails();

        // Set the primary section to DRAWER and add content to drawer and header
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    /**
     * Initializes user details from the session.
     */
    private void initialiseUserDetails() {
        VaadinSession session = VaadinSession.getCurrent();
        firstName = (String) session.getAttribute("firstName");
        role = (Role) session.getAttribute("role");
        
        if (role == Role.Student) {
            applicationStatus = (ApplicationStatus) session.getAttribute("applicationStatus");
        }
    }

    /**
     * Adds the header content to the layout.
     */
    private void addHeaderContent() {
        // Create a drawer toggle button
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        // Create a view title element
        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Add the toggle button and view title to the navbar
        addToNavbar(true, toggle, viewTitle);
    }

    /**
     * Adds the drawer content to the layout.
     */
    private void addDrawerContent() {
        // Create the logo image
        logoImage.addClassNames("side-logo-image");
        Header header = new Header(logoImage);

        // Create divider
        Hr divider1 = new Hr();
        divider1.setClassName("divider");

        // Create a welcome message for the user
        H2 welcomeMessage = new H2("Welcome, " + firstName);
        welcomeMessage.setClassName("side-nav-text");

        // Create divider
        Hr divider2 = new Hr();
        divider2.setClassName("divider");

        // Create a user role display
        H3 userRole = new H3("Role: " + role);
        userRole.setClassName("side-nav-text");

        // Create divider
        Hr divider3 = new Hr();
        divider3.setClassName("divider");

        // Create a navigation menu with tab options
        Scroller scroller = new Scroller(createNavigation());

        // Create Dark Mode Toggle
        Button darkModeButton = new Button();
        darkModeButton.addClickListener(event -> setTheme());
        darkModeButton.setIcon(VaadinIcon.ADJUST.create());
        darkModeButton.getStyle().set("font-size", "20px");
        darkModeButton.setHeight("50px");

        // Create a sign out button
        Button signOutButton = new Button("Sign Out", VaadinIcon.SIGN_OUT.create());
        signOutButton.getStyle().set("font-size", "20px");
        signOutButton.setHeight("50px");
        signOutButton.addClickListener(event -> signOut(false));

        // Add header, dividers, user info, navigation, sign out button, and footer to the drawer
        addToDrawer(header, divider1, welcomeMessage, divider2, userRole, divider3, scroller, darkModeButton, signOutButton, createFooter());
    }

    /**
     * The function toggles the theme between dark and light and updates the logo image source
     * accordingly.
     */
    private void setTheme() {
        currentThemeIsDark = !currentThemeIsDark;
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, currentThemeIsDark ? Lumo.DARK : Lumo.LIGHT);

        // Set the logo image source based on the theme
        if (!currentThemeIsDark) {
            logoImage.setSrc("images/side-nav-logo-LIGHT.png");
        } else {
            logoImage.setSrc("images/side-nav-logo-DARK.png");
        }
    }

    /**
     * Handles user sign-out.
     */
    public static void signOut(boolean accessDenied) {

        // Navigate to the WelcomeView

        if (accessDenied) {
            UI.getCurrent().getPage().setLocation("/access-denied");
        } else {
            UI.getCurrent().getPage().setLocation("/");
        }

        // Invalidate the session
        VaadinSession.getCurrent().getSession().invalidate();
    }

    /**
     * Creates the navigation menu for the drawer.
     */
    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("My Profile", ProfileView.class, LineAwesomeIcon.USER.create()));

        if (role == Role.Employee || role == Role.Student) {
            nav.addItem(new SideNavItem("Courses", CoursesView.class, LineAwesomeIcon.SCHOOL_SOLID.create()));
        }

        if (role == Role.Admin) {
            nav.addItem(new SideNavItem("User Management", UserManagementView.class, VaadinIcon.WARNING.create()));
            nav.addItem(new SideNavItem("Course Management", CourseManagementView.class, VaadinIcon.WARNING.create()));
        }

        if (role == Role.Student && applicationStatus != ApplicationStatus.ACCEPTED) {
            nav.addItem(new SideNavItem("Application", ApplicationView.class, VaadinIcon.PENCIL.create()));
        }

        return nav;
    }

    /**
     * Creates the footer layout.
     */
    private Footer createFooter() {
        Footer layout = new Footer();

        // Add footer content as needed

        return layout;
    }

    /**
     * Retrieves the title of the current page from the PageTitle annotation.
     */
    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    /**
     * Updates the view title with the current page title after navigation.
     */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }
}