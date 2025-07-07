import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.example.application.microservices.SignInManager;
import com.example.application.PublicEnums.Response;


public class SignInManagerTest {

    @Test
    public void testInvalidEmailFormat() {
        // Perform the sign-in with an invalid email format and check the response.
        Response response = SignInManager.validateEmailFormat("invalid-email");
        assertEquals(Response.INVALID_EMAIL, response);
    }

    @Test
    public void testValidEmailFormat() {
        // Perform the sign-in with an invalid email format and check the response.
        Response response = SignInManager.validateEmailFormat("test@test.com");
        assertEquals(Response.SUCCESS, response);
    }
}
