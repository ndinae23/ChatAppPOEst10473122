package quickchat;

import java.io.IOException;
import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);


        // APPLICATION HEADING

        System.out.println("==================================");
        System.out.println("          QUICKCHAT");
        System.out.println("==================================");

        
        // PERSONAL INFORMATION

        System.out.print("Enter first name: ");
        String firstName = input.nextLine().trim();

        System.out.print("Enter last name: ");
        String lastName = input.nextLine().trim();

        
        // USERNAME
        

        System.out.print("Create username: ");
        String username = input.nextLine().trim();

        
        // PASSWORD

        System.out.print("Create password: ");
        String password = input.nextLine();

     
        // CELLPHONE NUMBER
        

        System.out.print(
                "Enter South African cellphone number "
                + "(+27) "
        );

        String cellPhoneNumber = input.nextLine().trim();

        
        // CREATE USER
       

        Login user = new Login(
                firstName,
                lastName,
                username,
                password,
                cellPhoneNumber
        );

        
        // DISPLAY VALIDATION RESULTS
        

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

       
        // REGISTRATION RESULT
     

        System.out.println();
        System.out.println("REGISTRATION STATUS");
        System.out.println("-------------------");

        String registrationResult = user.registerUser();

        System.out.println(registrationResult);

        
        // PREVENT LOGIN IF REGISTRATION FAILED
        

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

        
        // LOGIN
        

        System.out.println();
        System.out.println("==================================");
        System.out.println("              LOGIN");
        System.out.println("==================================");

        System.out.print("Enter username: ");
        String enteredUsername = input.nextLine().trim();

        System.out.print("Enter password: ");
        String enteredPassword = input.nextLine();

        
        // LOGIN RESULT


        System.out.println();

        System.out.println(
                user.returnLoginStatus(
                        enteredUsername,
                        enteredPassword
                )
        );

        if (!user.loginUser(enteredUsername, enteredPassword)) {
            input.close();
            return;
        }

        // PART 3: MAIN MENU

        Message.setCurrentSender(user.getFirstName());

        try {
            int loaded = Message.loadStoredMessages();
            if (loaded > 0) {
                System.out.println(loaded + " stored message(s) loaded from messages.json.");
            }
        } catch (IOException exception) {
            System.out.println("Could not read stored messages: " + exception.getMessage());
        }

        runMainMenu(input);

        input.close();
    }

    // MAIN MENU

    private static void runMainMenu(Scanner input) {
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        while (true) {
            System.out.println();
            System.out.println("==================================");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.println("==================================");
            System.out.print("Choose an option: ");

            switch (input.nextLine().trim()) {
                case "1":
                    sendMessages(input);
                    break;
                case "2":
                    System.out.println();
                    String sent = Message.printMessages(Message.getSentMessages());
                    System.out.println(sent.isEmpty() ? "No messages have been sent yet." : sent);
                    break;
                case "3":
                    System.out.println("Total messages sent: "
                            + Message.returnTotalMessages());
                    System.out.println("Goodbye.");
                    return;
                case "4":
                    storedMessagesMenu(input);
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // OPTION 1: SEND MESSAGES

    private static void sendMessages(Scanner input) {
        int howMany = readPositiveInt(input, "How many messages would you like to enter? ");

        for (int i = 0; i < howMany; i++) {
            int messageNumber = Message.totalMessagesInArrays() + 1;
            System.out.println();
            System.out.println("Message " + (i + 1) + " of " + howMany);

            String recipient;
            do {
                System.out.print("Recipient (+27 and 9 digits): ");
                recipient = input.nextLine().trim();
                Message check = new Message(messageNumber, recipient, "check");
                String result = check.checkRecipientCell();
                System.out.println(result);
                if (!Message.RECIPIENT_SUCCESS.equals(result)) {
                    recipient = null;
                }
            } while (recipient == null);

            Message message;
            do {
                System.out.print("Message (max 250 characters): ");
                message = new Message(messageNumber, recipient, input.nextLine());
                String result = message.validateMessageText();
                System.out.println(result);
                if (!Message.MESSAGE_READY.equals(result)) {
                    message = null;
                }
            } while (message == null);

            System.out.println("Message ID: " + message.getMessageID());
            System.out.println("Message Hash: " + message.getMessageHash());
            System.out.println("1) Send Message  2) Disregard Message  3) Store Message");
            System.out.print("Choose an option: ");

            String action;
            switch (input.nextLine().trim()) {
                case "1": action = "send"; break;
                case "2": action = "disregard"; break;
                case "3": action = "store"; break;
                default:
                    System.out.println("Invalid option. Message disregarded.");
                    action = "disregard";
            }

            System.out.println(message.SentMessage(action));
            if ("send".equals(action) && "Sent".equals(message.getStatus())) {
                System.out.println(message.printMessages());
            }
        }
    }

    // OPTION 4: STORED MESSAGES

    private static void storedMessagesMenu(Scanner input) {
        while (true) {
            System.out.println();
            System.out.println("---------- STORED MESSAGES ----------");
            System.out.println("a) Display the sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages stored for a recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("x) Back to main menu");
            System.out.print("Choose an option: ");

            System.out.println();
            switch (input.nextLine().trim().toLowerCase()) {
                case "a":
                    System.out.println(Message.displaySenderAndRecipient(Message.getStoredMessages()));
                    break;
                case "b":
                    Message longest = Message.findLongestStoredMessage();
                    System.out.println(longest == null
                            ? "No stored messages."
                            : "Longest stored message: " + longest.getMessageText());
                    break;
                case "c":
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.searchByMessageID(input.nextLine()));
                    break;
                case "d":
                    System.out.print("Enter the recipient: ");
                    System.out.println(Message.searchByRecipient(input.nextLine()));
                    break;
                case "e":
                    System.out.print("Enter the message hash: ");
                    String result = Message.deleteMessageByHash(input.nextLine());
                    System.out.println(result);
                    if (result.endsWith("successfully deleted.")) {
                        try {
                            Message.saveStoredMessages();
                        } catch (IOException exception) {
                            System.out.println("Could not update messages.json: "
                                    + exception.getMessage());
                        }
                    }
                    break;
                case "f":
                    System.out.println(Message.report(Message.getStoredMessages()));
                    break;
                case "x":
                    return;
                default:
                    System.out.println("Invalid option. Please choose a, b, c, d, e, f or x.");
            }
        }
    }

    private static int readPositiveInt(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // fall through to the message below
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }
}
