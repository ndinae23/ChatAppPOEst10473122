package quickchat;

import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);


        // APPLICATION HEADING

        System.out.println("==================================");
        System.out.println("          QUICKCHAT");
        System.out.println("==================================");

        // -----------------------------------------------------
        // PERSONAL INFORMATION
        // -----------------------------------------------------

        System.out.print("Enter first name: ");
        String firstName = input.nextLine().trim();

        System.out.print("Enter last name: ");
        String lastName = input.nextLine().trim();

        // -----------------------------------------------------
        // USERNAME
        // -----------------------------------------------------

        System.out.print("Create username: ");
        String username = input.nextLine().trim();

        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        System.out.print("Create password: ");
        String password = input.nextLine();

        // -----------------------------------------------------
        // CELLPHONE NUMBER
        // -----------------------------------------------------

        System.out.print(
                "Enter South African cellphone number "
                + "(+27) "
        );

        String cellPhoneNumber = input.nextLine().trim();

        // -----------------------------------------------------
        // CREATE USER
        // -----------------------------------------------------

        Login user = new Login(
                firstName,
                lastName,
                username,
                password,
                cellPhoneNumber
        );

        // -----------------------------------------------------
        // DISPLAY VALIDATION RESULTS
        // -----------------------------------------------------

        System.out.println();
        System.out.println(user.getUsernameMessage());
        System.out.println(user.getPasswordMessage());

        if (user.checkCellPhoneNumber()) {

           
            System.out.println(
                    "Cell phone number successfully added."
            );

        } else {

            System.out.println(
                    "Cell phone number incorrectly formatted "
                    + "or does not contain international code."
            );
        }

        // -----------------------------------------------------
        // REGISTRATION RESULT
        // -----------------------------------------------------

        System.out.println();
        System.out.println("REGISTRATION STATUS");
        System.out.println("-------------------");

        String registrationResult = user.registerUser();

        System.out.println(registrationResult);

        // -----------------------------------------------------
        // PREVENT LOGIN IF REGISTRATION FAILED
        // -----------------------------------------------------

        if (!user.checkUserName()
                || !user.checkPasswordComplexity()
                || !user.checkCellPhoneNumber()) {

            System.out.println();
            System.out.println(
                    "Registration was unsuccessful. "
                    + "Please correct your details and try again."
            );

            input.close();
            return;
        }

        // -----------------------------------------------------
        // LOGIN
        // -----------------------------------------------------

        System.out.println();
        System.out.println("==================================");
        System.out.println("              LOGIN");
        System.out.println("==================================");

        System.out.print("Enter username: ");
        String enteredUsername = input.nextLine().trim();

        System.out.print("Enter password: ");
        String enteredPassword = input.nextLine();

        // -----------------------------------------------------
        // LOGIN RESULT
        // -----------------------------------------------------

        System.out.println();

        System.out.println(
                user.returnLoginStatus(
                        enteredUsername,
                        enteredPassword
                )
        );

        input.close();
    }
}