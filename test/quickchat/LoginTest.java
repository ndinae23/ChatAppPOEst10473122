package quickchat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    // =========================================================
    // USERNAME TESTS
    // =========================================================

    @Test
    public void testUsernameCorrectlyFormatted() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertTrue(login.checkUserName());
    }


    @Test
    public void testUsernameIncorrectlyFormatted() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyle!!!!!!!",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertFalse(login.checkUserName());
    }


    @Test
    public void testUsernameSuccessMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Username successfully captured.",
                login.getUsernameMessage()
        );
    }


    @Test
    public void testUsernameErrorMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyle!!!!!!!",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Username is not correctly formatted; "
                + "please ensure that your username contains "
                + "an underscore and is no more than five "
                + "characters in length.",
                login.getUsernameMessage()
        );
    }


    // =========================================================
    // PASSWORD TESTS
    // =========================================================

    @Test
    public void testPasswordMeetsComplexityRequirements() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertTrue(
                login.checkPasswordComplexity()
        );
    }


    @Test
    public void testPasswordDoesNotMeetComplexityRequirements() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "password",
                "+27838968976"
        );

        assertFalse(
                login.checkPasswordComplexity()
        );
    }


    @Test
    public void testPasswordSuccessMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Password successfully captured.",
                login.getPasswordMessage()
        );
    }


    @Test
    public void testPasswordErrorMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "password",
                "+27838968976"
        );

        assertEquals(
                "Password is not correctly formatted; "
                + "please ensure that the password contains "
                + "at least eight characters, a capital letter, "
                + "a number, and a special character.",
                login.getPasswordMessage()
        );
    }


    // =========================================================
    // CELLPHONE TESTS
    // =========================================================

    @Test
    public void testCellPhoneCorrectlyFormatted() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertTrue(
                login.checkCellPhoneNumber()
        );
    }


    @Test
    public void testCellPhoneIncorrectlyFormatted() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "08966553"
        );

        assertFalse(
                login.checkCellPhoneNumber()
        );
    }


    @Test
    public void testCellPhoneSuccessMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Cell number successfully captured.",
                login.getCellPhoneMessage()
        );
    }


    @Test
    public void testCellPhoneErrorMessage() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "08966553"
        );

        assertEquals(
                "Cell number is incorrectly formatted or does "
                + "not contain an international code; please "
                + "correct the number and try again.",
                login.getCellPhoneMessage()
        );
    }


    // =========================================================
    // REGISTRATION TESTS
    // =========================================================

    @Test
    public void testSuccessfulRegistration() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "User successfully registered.",
                login.registerUser()
        );
    }


    @Test
    public void testRegistrationWithInvalidUsername() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyle!!!!!!!",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                Login.USERNAME_ERROR,
                login.registerUser()
        );
    }


    @Test
    public void testRegistrationWithInvalidPassword() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "password",
                "+27838968976"
        );

        assertEquals(
                Login.PASSWORD_ERROR,
                login.registerUser()
        );
    }


    // =========================================================
    // LOGIN BOOLEAN TESTS
    // =========================================================

    @Test
    public void testLoginSuccessful() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertTrue(
                login.loginUser(
                        "kyl_1",
                        "Ch&&sec@ke99!"
                )
        );
    }


    @Test
    public void testLoginFailed() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertFalse(
                login.loginUser(
                        "wrongUser",
                        "wrongPassword"
                )
        );
    }


    // =========================================================
    // LOGIN STATUS TESTS
    // =========================================================

    @Test
    public void testSuccessfulLoginStatus() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Welcome Kyle, Smith it is great to see you again.",
                login.returnLoginStatus(
                        "kyl_1",
                        "Ch&&sec@ke99!"
                )
        );
    }


    @Test
    public void testFailedLoginStatus() {

        Login login = new Login(
                "Kyle",
                "Smith",
                "kyl_1",
                "Ch&&sec@ke99!",
                "+27838968976"
        );

        assertEquals(
                "Username or password incorrect, please try again.",
                login.returnLoginStatus(
                        "wrongUser",
                        "wrongPassword"
                )
        );
    }
}