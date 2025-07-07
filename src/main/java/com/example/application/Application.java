package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;

import com.vaadin.flow.theme.Theme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.server.PWA;

/**
 * This class initializes and starts the Spring Boot application. It serves as
 * the main entry point for the application.
 *
 * The class implements the {@link AppShellConfigurator} interface, which allows
 * configuring the application shell and related settings.
 *
 * The {@code @SpringBootApplication} annotation indicates that this class is the
 * main configuration class for the Spring Boot application and enables various
 * configurations and component scanning.
 *
 * The {@code @Theme} annotation sets the default theme for the Vaadin UI. The
 * theme named "myapp" will be used throughout the application.
 *
 * The {@code @ComponentScan} annotation specifies the base package(s) for
 * component scanning. In this case, it scans the "com.example.application.models"
 * package for Spring components.
 */
@SpringBootApplication
@Theme(value = "mytutor")
@PWA(name = "MyTutor", shortName = "MyTutor", startPath = "", backgroundColor = "#227aef", display = "standalone", offlinePath="offline.html", offlineResources = { "./images/offline.png"})
public class Application implements AppShellConfigurator {
    /**
     * The main method to start the Spring Boot application.
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}