import com.example.application.PublicEnums.Response;
import com.example.application.microservices.UserManager;
import com.example.application.models.CourseConvenor;
import com.example.application.models.Lecturer;
import com.example.application.models.TA;
import com.example.application.models.Tutor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    @Test
    public void testValidateUserCreation_ValidLecturer() {
        Lecturer lecturer = new Lecturer("John", "Doe", "johndoe@example.com", "123456789", "Department", "Faculty");

        Response result = UserManager.validateUserCreation(lecturer);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateUserCreation_InvalidLecturerEMPID() {
        Lecturer lecturer = new Lecturer("Alice", "Smith", "alice@example.com", "789", "Department", "Faculty");


        Response result = UserManager.validateUserCreation(lecturer);

        assertEquals(Response.INVALID_EMPID, result);
    }

    @Test
    public void testValidateUserCreation_ValidCourseConvenor() {
        CourseConvenor convener = new CourseConvenor("Bob", "Johnson", "bob@example.com", "123456789", "Department", "Faculty");

        Response result = UserManager.validateUserCreation(convener);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateUserCreation_InvalidCourseConvenorEMPID() {
        CourseConvenor convener = new CourseConvenor("Eve", "Wilson", "eve@example.com", "InvalidEMPID", "Department", "Faculty");

        Response result = UserManager.validateUserCreation(convener);

        assertEquals(Response.INVALID_EMPID, result);
    }

    @Test
    public void testValidateUserCreation_ValidTA() {
        TA ta = new TA("Tom", "Smith", "tom@example.com", "STUTST123", null);

        Response result = UserManager.validateUserCreation(ta);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateUserCreation_InvalidTAStudentID() {
        TA ta = new TA("Sarah", "Wilson", "sarah@example.com", "InvalidStudentID", null);

        Response result = UserManager.validateUserCreation(ta);

        assertEquals(Response.INVALID_STUDENTNUMBER, result);
    }

    @Test
    public void testValidateUserCreation_ValidTutor() {
        Tutor tutor = new Tutor("David", "Johnson", "david@example.com", "STUEDF990", null);

        Response result = UserManager.validateUserCreation(tutor);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateUserCreation_InvalidTutorStudentID() {
        Tutor tutor = new Tutor("Grace", "Smith", "grace@example.com", "InvalidStudentID", null);

        Response result = UserManager.validateUserCreation(tutor);

        assertEquals(Response.INVALID_STUDENTNUMBER, result);
    }

    @Test
    public void testGeneratePassword() {
        String password = UserManager.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }
}

