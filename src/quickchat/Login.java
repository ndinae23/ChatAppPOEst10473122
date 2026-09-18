package quickchat;

import java.util.regex.Pattern;

public class Login {

    
    // CONSTANT MESSAGES

    public static final String USERNAME_SUCCESS =
            "Username successfully captured.";

    public static final String USERNAME_ERROR =
            "Username is not correctly formatted; please ensure that "
            + "your username contains an underscore and is no more "
            + "than five characters in length.";

    public static final String PASSWORD_SUCCESS =
            "Password successfully captured.";

    public static final String PASSWORD_ERROR =
            "Password is not correctly formatted; please ensure that "
            + "the password contains at least eight characters, "
            + "a capital letter, a number, and a special character.";

    public static final String CELL_SUCCESS =
            "Cell number successfully captured.";

    public static final String CELL_ERROR =
            "Cell number is incorrectly formatted or does not contain "
            + "an international code; please correct the number and "
            + "try again.";

    public static final String REGISTRATION_SUCCESS =
            "User successfully registered.";

    public static final String LOGIN_ERROR =
            "Username or password incorrect, please try again.";

    
    // ATTRIBUTES

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhoneNumber;


    private static final Pattern SA_CELL_PATTERN =
            Pattern.compile("^\\+27\\d{9}$");

    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------

    public Login(String firstName,
                 String lastName,
                 String username,
                 String password,
                 String cellPhoneNumber) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
    }

    // ---------------------------------------------------------
    // USERNAME VALIDATION
    // ---------------------------------------------------------

    public boolean checkUserName() {

        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    // ---------------------------------------------------------
    // PASSWORD VALIDATION
    // ---------------------------------------------------------

    public boolean checkPasswordComplexity() {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasCapitalLetter = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (char character : password.toCharArray()) {

            if (Character.isUpperCase(character)) {
                hasCapitalLetter = true;
            }

            if (Character.isDigit(character)) {
                hasNumber = true;
            }

            if (!Character.isLetterOrDigit(character)) {
                hasSpecialCharacter = true;
            }
        }

        return hasCapitalLetter
                && hasNumber
                && hasSpecialCharacter;
    }

    // ---------------------------------------------------------
    // CELLPHONE VALIDATION
    // ---------------------------------------------------------

    public boolean checkCellPhoneNumber() {

        if (cellPhoneNumber == null) {
            return false;
        }

        return SA_CELL_PATTERN
                .matcher(cellPhoneNumber)
                .matches();
    }

    // ---------------------------------------------------------
    // USERNAME MESSAGE
    // ---------------------------------------------------------

    public String getUsernameMessage() {

        if (checkUserName()) {
            return USERNAME_SUCCESS;
        }

        return USERNAME_ERROR;
    }

    // ---------------------------------------------------------
    // PASSWORD MESSAGE
    // ---------------------------------------------------------

    public String getPasswordMessage() {

        if (checkPasswordComplexity()) {
            return PASSWORD_SUCCESS;
        }

        return PASSWORD_ERROR;
    }

    // ---------------------------------------------------------
    // CELLPHONE MESSAGE
    // ---------------------------------------------------------

    public String getCellPhoneMessage() {

        if (checkCellPhoneNumber()) {
            return CELL_SUCCESS;
        }

        return CELL_ERROR;
    }

    // ---------------------------------------------------------
    // REGISTRATION
    // ---------------------------------------------------------

    public String registerUser() {

        if (!checkUserName()) {
            return USERNAME_ERROR;
        }

        if (!checkPasswordComplexity()) {
            return PASSWORD_ERROR;
        }

        if (!checkCellPhoneNumber()) {
            return CELL_ERROR;
        }

        return REGISTRATION_SUCCESS;
    }

    // ---------------------------------------------------------
    // LOGIN AUTHENTICATION
    // ---------------------------------------------------------

    public boolean loginUser(String enteredUsername,
                             String enteredPassword) {

        if (enteredUsername == null || enteredPassword == null) {
            return false;
        }

        return enteredUsername.equals(username)
                && enteredPassword.equals(password);
    }

    // ---------------------------------------------------------
    // LOGIN STATUS
    // ---------------------------------------------------------

    public String returnLoginStatus(String enteredUsername,
                                    String enteredPassword) {

        if (loginUser(enteredUsername, enteredPassword)) {

            return "Welcome " + firstName + ", " + lastName
                    + " it is great to see you again.";
        }

        return LOGIN_ERROR;
    }

    // ---------------------------------------------------------
    // GETTERS
    // ---------------------------------------------------------

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }
}