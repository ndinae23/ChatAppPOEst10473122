/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package quickchat;

/**
 *
 * @author Student
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;


public class Message {

    public static final String MESSAGE_READY = "Message ready to send.";
    public static final String MESSAGE_TOO_LONG =
            "Message exceeds 250 characters by %d; please reduce the size.";
    public static final String RECIPIENT_SUCCESS =
            "Cell phone number successfully captured.";
    public static final String RECIPIENT_ERROR =
            "Cell phone number is incorrectly formatted or does not contain "
            + "an international code. Please correct the number and try again.";

    private static final Pattern SA_CELL_PATTERN = Pattern.compile("^\\+27\\d{9}$");
    private static final Path JSON_FILE = Path.of("messages.json");
    private static int totalMessagesSent = 0;

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private String status;

    /** Creates a message with a randomly generated ten-digit ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(messageNumber, recipient, messageText, generateMessageID());
    }

    /**
     * Constructor with an explicit ID. This overload makes message hashes
     * deterministic and therefore straightforward to unit-test.
     */
    public Message(int messageNumber, String recipient, String messageText, String messageID) {
        this.messageNumber = messageNumber;
        this.recipient = recipient == null ? "" : recipient.trim();
        this.messageText = messageText == null ? "" : messageText.trim();
        this.messageID = messageID == null ? "" : messageID;
        this.messageHash = createMessageHash();
        this.status = "Pending";
    }

    private static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return Long.toString(id);
    }

    /** A valid ID is exactly ten digits (and therefore not longer than ten characters). */
    public boolean checkMessageID() {
        return messageID.matches("\\d{10}");
    }

    /** Reuses the South African international format established in Part 1: +27 and nine digits. */
    public String checkRecipientCell() {
        return SA_CELL_PATTERN.matcher(recipient).matches()
                ? RECIPIENT_SUCCESS : RECIPIENT_ERROR;
    }

    /** Returns the required hash: first two ID digits:number:first word + last word, uppercase. */
    public String createMessageHash() {
        if (messageID.length() < 2 || messageText.isBlank()) {
            return "";
        }
        String[] words = messageText.trim().split("\\s+");
        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        return messageID.substring(0, 2) + ":" + messageNumber + ":"
                + (firstWord + lastWord).toUpperCase();
    }

    private String cleanWord(String word) {
        return word.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");
    }

    /** Returns the success or length-error message for the message text. */
    public String validateMessageText() {
        if (messageText.length() <= 250) {
            return MESSAGE_READY;
        }
        return String.format(MESSAGE_TOO_LONG, messageText.length() - 250);
    }

    /**
     * Applies a selected action. Accepted values: send, disregard, store.
     * This overload is intended for both the console app and unit tests.
     */
    public String SentMessage(String action) {
        if (action == null) {
            return "Invalid option. Please choose send, disregard, or store.";
        }
        switch (action.trim().toLowerCase()) {
            case "send":
            case "send message":
                if (!checkMessageID() || !RECIPIENT_SUCCESS.equals(checkRecipientCell())
                        || !MESSAGE_READY.equals(validateMessageText())) {
                    return "Message could not be sent. Please check the message ID, recipient, and message length.";
                }
                status = "Sent";
                totalMessagesSent++;
                return "Message successfully sent.";
            case "disregard":
            case "discard":
            case "disregard message":
                status = "Disregarded";
                return "Press 0 to delete the message.";
            case "store":
            case "store message":
                if (!checkMessageID() || !RECIPIENT_SUCCESS.equals(checkRecipientCell())
                        || !MESSAGE_READY.equals(validateMessageText())) {
                    return "Message could not be stored. Please check the message ID, recipient, and message length.";
                }
                try {
                    storeMessage();
                    status = "Stored";
                    return "Message successfully stored.";
                } catch (IOException exception) {
                    return "Message could not be stored: " + exception.getMessage();
                }
            default:
                return "Invalid option. Please choose send, disregard, or store.";
        }
    }

    /** Interactive version of SentMessage() required by the task specification. */
    public String SentMessage() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose an action: 1) Send Message  2) Disregard Message  3) Store Message");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": return SentMessage("send");
            case "2": return SentMessage("disregard");
            case "3": return SentMessage("store");
            default: return "Invalid option. Please choose 1, 2, or 3.";
        }
    }

    /**
     * Stores this message as a JSON object in messages.json.
     * JSON string escaping is handled before writing to prevent malformed JSON.
     */
    public void storeMessage() throws IOException {
        String existing = Files.exists(JSON_FILE)
                ? Files.readString(JSON_FILE, StandardCharsets.UTF_8).trim() : "";
        String object = "  {"
                + "\"messageID\":\"" + jsonEscape(messageID) + "\","
                + "\"messageHash\":\"" + jsonEscape(messageHash) + "\","
                + "\"messageNumber\":" + messageNumber + ","
                + "\"recipient\":\"" + jsonEscape(recipient) + "\","
                + "\"message\":\"" + jsonEscape(messageText) + "\","
                + "\"status\":\"Stored\""
                + "}";
        String updated;
        if (existing.isBlank() || existing.equals("[]")) {
            updated = "[\n" + object + "\n]\n";
        } else if (existing.endsWith("]")) {
            String contents = existing.substring(0, existing.length() - 1).trim();
            updated = contents + ",\n" + object + "\n]\n";
        } else {
            throw new IOException("Existing messages.json is not a JSON array.");
        }
        Files.writeString(JSON_FILE, updated, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String jsonEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Returns the full details of messages sent during the current run. */
    public static String printMessages(List<Message> messages) {
        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            if ("Sent".equals(message.status)) {
                result.append(message.details()).append(System.lineSeparator());
            }
        }
        return result.toString().trim();
    }

    /** Instance-compatible method returning this message's details. */
    public String printMessages() {
        return details();
    }

    private String details() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + messageText;
    }

    public static int returnTotalMessagess() {
        return totalMessagesSent;
    }

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
    public String getStatus() { return status; }

    /** Resets the sent counter for isolated unit tests. */
    static void resetTotalMessagesSentForTests() {
        totalMessagesSent = 0;
    }
}
    

