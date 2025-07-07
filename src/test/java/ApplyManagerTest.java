import com.example.application.PublicEnums.QualificationLevel;
import com.example.application.PublicEnums.Response;
import com.example.application.microservices.ApplyManager;
import com.example.application.models.Student;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApplyManagerTest {

    @Test
    public void testValidateApplication_Success() {
        Student student = new Student("John", "Doe", "johndoe@example.com", "ABCD123", QualificationLevel.SecondYear, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateApplication_FirstYearStudent() {
        Student student = new Student("Alice", "Smith", "alice@example.com", "EFGH456", QualificationLevel.FirstYear, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.QUALIFICATION_LEVEL_TOO_LOW, result);
    }

    @Test
    public void testValidateApplication_HonoursStudent() {
        Student student = new Student("Emily", "Johnson", "emily@example.com", "IJKL789", QualificationLevel.Honours, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateApplication_PhDStudent() {
        Student student = new Student("David", "Wilson", "david@example.com", "MNOPQR123", QualificationLevel.PhD, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateApplication_NullEmail() {
        // Test with a student with a null email.
        Student student = new Student("Null", "Email", null, "XYZ123", QualificationLevel.SecondYear, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.SUCCESS, result);
    }

    @Test
    public void testValidateApplication_NullStudentID() {
        // Test with a student with a null student ID.
        Student student = new Student("Null", "StudentID", "nullstudent@example.com", null, QualificationLevel.SecondYear, null, null);

        Response result = ApplyManager.validateApplication(student);

        assertEquals(Response.SUCCESS, result);
    }
}
