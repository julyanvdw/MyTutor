# MyTutor - Tutor Support Platform

## Overview  
**MyTutor** is a web-based application developed to enhance the management of Tutors and Teaching Assistants (TAs) at the University of Cape Town (UCT). The platform streamlines the application process, scheduling, and administrative workflows. Built using **Java**, **Vaadin**, and **Spring Boot**, MyTutor delivers a secure, modular, and scalable solution for students, lecturers, and course administrators.

---

## Key Features  
- **Role-Based Access and Authentication**  
  Secure login and permissions for administrators, lecturers, tutors, and students.  
- **Application Management**  
  Intuitive interface allowing students to apply for Tutor and TA positions.  
- **Scheduling Functionality**  
  Efficient management of tutor assignments and session scheduling.  
- **Administrative Oversight**  
  Enables course convenors to monitor tutor performance and allocate responsibilities.  
- **Modular and Scalable Architecture**  
  Designed for easy expansion and integration with existing UCT systems.  
- **User Experience Options**  
  Support for light and dark mode themes to enhance usability.

---

## Technology Stack  
- **Java 17** — Backend development and business logic  
- **Spring Boot** — Web application framework  
- **Vaadin** — Java-based frontend UI framework  
- **MySQL** — Relational database management system  
- **Maven** — Dependency and build management  
- **Git & GitLab** — Version control and collaboration  
- **RESTful APIs** — Communication between frontend and backend components

---

## About Vaadin  
[Vaadin](https://vaadin.com/) is a Java framework that facilitates building modern, interactive web user interfaces entirely in Java, eliminating the need for separate frontend technologies such as JavaScript frameworks. It is particularly well-suited for enterprise applications where maintainability and security are priorities.

### Reasons for Choosing Vaadin  
- Enables full-stack Java development without additional frontend frameworks.  
- Manages UI state server-side, reducing security vulnerabilities.  
- Provides a comprehensive set of pre-built UI components to accelerate development.  
- Seamless integration with Spring Boot backend services.  
- Encourages object-oriented programming principles, improving code maintainability and extensibility.

---

## Project Team  
This project was completed as part of the **2023 Undergraduate Computer Science Capstone** at UCT:  
- **Julyan van der Westhuizen (VWSJUL003)** - Team Lead  
- **Cassandra Wallace (WLLCAS004)**  
- **Ethan Wilson (WLSETH003)**

---

## Repository  
Alternative repository location:  
[MyTutor GitLab Repository](https://gitlab.cs.uct.ac.za/capstone-elite/mytutor)

---

## Installation and Setup  

### Prerequisites  
- Java 17 (JDK) installed  
- Maven installed

### Important Notes  
- This application is no longer deployed on Google Cloud. To evaluate the system, it must be run locally.

### Test User Accounts  
| Role               | Email                     | Password |  
|--------------------|---------------------------|----------|  
| Administrator      | testadmin@test.com        | 1234     |  
| Course Convenor    | johnbright@gmail.com      | 1234     |  
| Teaching Assistant | smttay001@myuct.ac.za     | 1234     |  
| Tutor              | blesam001@myuct.ac.za     | 1234     |  
| Student            | Register via login page   | N/A      |

**Role Capabilities:**  
- Course convenors can approve tutor and TA applications.  
- Teaching Assistants can manage tutor approvals, schedules, and view statistics.  
- Tutors may view schedules and sign up for sessions.  
- Students can apply for tutor positions in courses they have completed.

---

### Running the Application  
1. Clone the repository.  
2. Ensure you're in the mytutor directory.
3. Run 'mvn' command. 

Note, to clean the complication run: 'mvn clean install'. This will clean the project and re-run the tests - preparing it for the next run.


