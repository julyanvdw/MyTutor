import com.example.application.microservices.SignUpManager;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.example.application.models.Student;
import com.example.application.PublicEnums.Response;


public class SignUpManagerTest {
    
    @Test
    public void testValidateSignUp_ValidInput() {
        Student newStudent = new Student("John", "Doe", "johndoe@example.com", "ABCDEF123", null, null, null);
        String password = "password123";
        String confirmPassword = "password123";

        Response result = SignUpManager.validateSignUp(newStudent, password, confirmPassword);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateSignUp_PasswordMismatch() {
        Student newStudent = new Student("Alice", "Smith", "alice@example.com", "EFGH456", null, null, null);
        String password = "password123";
        String confirmPassword = "password456";  // Mismatched password

        Response result = SignUpManager.validateSignUp(newStudent, password, confirmPassword);

        assertEquals(Response.PASSWORD_MISMATCH, result);
    }

    @Test
    public void testValidateSignUp_InvalidEmailFormat() {
        Student newStudent = new Student("Bob", "Johnson", "invalidemail", "IJKL789", null, null, null);
        String password = "password123";
        String confirmPassword = "password123";

        Response result = SignUpManager.validateSignUp(newStudent, password, confirmPassword);

        assertEquals(Response.INVALID_EMAIL, result);
    }

    @Test
    public void testValidateSignUp_InvalidStudentNumberFormat() {
        Student newStudent = new Student("Eve", "Wilson", "eve@example.com", "Invalid1234", null, null, null);
        String password = "password123";
        String confirmPassword = "password123";

        Response result = SignUpManager.validateSignUp(newStudent, password, confirmPassword);

        assertEquals(Response.INVALID_STUDENTNUMBER, result);
    }
}