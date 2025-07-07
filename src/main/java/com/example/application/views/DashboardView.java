package com.example.application.views;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.example.application.PublicEnums.Role;
import com.vaadin.flow.component.html.H1;

/**
 * This class represents the Dashboard view of the MyTutor application.
 * It displays a welcome message to the user on the dashboard page.
 */
@PageTitle("Dashboard | MyTutor")
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    /**
     * Constructs a DashboardView instance and sets up the content layout.
     * The welcome message is displayed in the center of the layout.
     */
    public DashboardView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        // Display the welcome message
        H1 welcomeMessage = new H1("Welcome to MyTutor!");
        add(welcomeMessage);
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
}