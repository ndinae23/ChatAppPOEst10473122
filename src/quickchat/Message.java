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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // ---- Part 3: arrays (populated at runtime, never hard-coded) ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();

    /** Name shown as the sender of new messages (set to the logged-in user). */
    private static String currentSender = "Developer";

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private String status;
    private String sender;

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
        this.sender = currentSender;
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
                addMessage(this, "Sent");
                totalMessagesSent++;
                return "Message successfully sent.";
            case "disregard":
            case "discard":
            case "disregard message":
                addMessage(this, "Disregarded");
                return "Press 0 to delete the message.";
            case "store":
            case "store message":
                if (!checkMessageID() || !RECIPIENT_SUCCESS.equals(checkRecipientCell())
                        || !MESSAGE_READY.equals(validateMessageText())) {
                    return "Message could not be stored. Please check the message ID, recipient, and message length.";
                }
                try {
                    storeMessage();
                    addMessage(this, "Stored");
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
        String object = "  " + toJson();
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

    private String toJson() {
        return "{"
                + "\"messageID\":\"" + jsonEscape(messageID) + "\","
                + "\"messageHash\":\"" + jsonEscape(messageHash) + "\","
                + "\"messageNumber\":" + messageNumber + ","
                + "\"sender\":\"" + jsonEscape(sender) + "\","
                + "\"recipient\":\"" + jsonEscape(recipient) + "\","
                + "\"message\":\"" + jsonEscape(messageText) + "\","
                + "\"status\":\"Stored\""
                + "}";
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

    // =====================================================================
    //  PART 3 - ARRAYS, STORED MESSAGES AND SEARCH / REPORT FEATURES
    // =====================================================================

    /** Sets the sender name used for messages created from now on. */
    public static void setCurrentSender(String name) {
        currentSender = (name == null || name.isBlank()) ? "Developer" : name.trim();
    }

    /**
     * Adds a message to the correct array for its flag (Sent, Disregard or
     * Stored) and records its hash and ID in the hash / ID arrays.
     * No validation happens here, so it can also load the test data.
     */
    public static void addMessage(Message message, String flag) {
        if (message == null || flag == null) {
            return;
        }
        if (sentMessages.contains(message) || disregardedMessages.contains(message)
                || storedMessages.contains(message)) {
            return; // already recorded
        }
        switch (flag.trim().toLowerCase()) {
            case "sent":
                message.status = "Sent";
                sentMessages.add(message);
                break;
            case "disregard":
            case "disregarded":
                message.status = "Disregarded";
                disregardedMessages.add(message);
                break;
            case "stored":
            case "store":
                message.status = "Stored";
                storedMessages.add(message);
                break;
            default:
                return;
        }
        messageHashes.add(message.messageHash);
        messageIDs.add(message.messageID);
    }

    // ---- array accessors (copies, so callers cannot corrupt the arrays) ----

    public static List<Message> getSentMessages() { return new ArrayList<>(sentMessages); }
    public static List<Message> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public static List<Message> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public static List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public static List<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    /** Message texts held in the Sent Messages array. */
    public static List<String> getSentMessageTexts() {
        return texts(sentMessages);
    }

    /** Total number of messages across the sent, disregarded and stored arrays. */
    public static int totalMessagesInArrays() {
        return sentMessages.size() + disregardedMessages.size() + storedMessages.size();
    }

    private static List<String> texts(List<Message> messages) {
        List<String> result = new ArrayList<>();
        for (Message message : messages) {
            result.add(message.messageText);
        }
        return result;
    }

    // ---- Stored Messages menu features (a - f) ----

    /** (a) Sender and recipient of every message in the given list. */
    public static String displaySenderAndRecipient(List<Message> messages) {
        if (messages.isEmpty()) {
            return "No messages found.";
        }
        StringBuilder result = new StringBuilder();
        for (Message message : messages) {
            result.append("Sender: ").append(message.sender)
                  .append(" | Recipient: ").append(message.recipient)
                  .append(System.lineSeparator());
        }
        return result.toString().trim();
    }

    /** (b) The longest stored message, or null if nothing is stored. */
    public static Message findLongestStoredMessage() {
        Message longest = null;
        for (Message message : storedMessages) {
            if (longest == null || message.messageText.length() > longest.messageText.length()) {
                longest = message;
            }
        }
        return longest;
    }

    /** (c) Looks a message up by ID across the sent, disregarded and stored arrays. */
    public static Message findMessageByID(String id) {
        if (id == null) {
            return null;
        }
        String wanted = id.trim();
        for (List<Message> list : List.of(sentMessages, storedMessages, disregardedMessages)) {
            for (Message message : list) {
                if (message.messageID.equals(wanted)) {
                    return message;
                }
            }
        }
        return null;
    }

    /** (c) Display text for an ID search: recipient and message. */
    public static String searchByMessageID(String id) {
        Message found = findMessageByID(id);
        if (found == null) {
            return "No message found with ID " + (id == null ? "" : id.trim()) + ".";
        }
        return "Recipient: " + found.recipient + System.lineSeparator()
                + "Message: " + found.messageText;
    }

    /** (d) All sent or stored messages for one recipient. */
    public static List<Message> findMessagesByRecipient(String recipient) {
        List<Message> result = new ArrayList<>();
        if (recipient == null) {
            return result;
        }
        String wanted = recipient.trim();
        for (List<Message> list : List.of(sentMessages, storedMessages)) {
            for (Message message : list) {
                if (message.recipient.equals(wanted)) {
                    result.add(message);
                }
            }
        }
        return result;
    }

    /** (d) Display text for a recipient search. */
    public static String searchByRecipient(String recipient) {
        List<Message> found = findMessagesByRecipient(recipient);
        if (found.isEmpty()) {
            return "No messages found for recipient " + (recipient == null ? "" : recipient.trim()) + ".";
        }
        StringBuilder result = new StringBuilder();
        for (Message message : found) {
            result.append(message.messageText).append(System.lineSeparator());
        }
        return result.toString().trim();
    }

    /**
     * (e) Deletes the first message with the given hash from whichever array
     * holds it (and from the hash / ID arrays).
     */
    public static String deleteMessageByHash(String hash) {
        if (hash == null || hash.isBlank()) {
            return "Please enter a message hash.";
        }
        String wanted = hash.trim();
        for (List<Message> list : List.of(sentMessages, storedMessages, disregardedMessages)) {
            for (int i = 0; i < list.size(); i++) {
                Message message = list.get(i);
                if (message.messageHash.equalsIgnoreCase(wanted)) {
                    list.remove(i);
                    messageHashes.remove(message.messageHash);
                    messageIDs.remove(message.messageID);
                    return "Message: \"" + message.messageText + "\" successfully deleted.";
                }
            }
        }
        return "No message found with hash " + wanted + ".";
    }

    /** (f) Report listing the full details of every message in the given list. */
    public static String report(List<Message> messages) {
        if (messages.isEmpty()) {
            return "No messages found.";
        }
        StringBuilder result = new StringBuilder();
        int count = 1;
        for (Message message : messages) {
            result.append("--- Message ").append(count++).append(" ---").append(System.lineSeparator())
                  .append("Message Hash: ").append(message.messageHash).append(System.lineSeparator())
                  .append("Message ID: ").append(message.messageID).append(System.lineSeparator())
                  .append("Sender: ").append(message.sender).append(System.lineSeparator())
                  .append("Recipient: ").append(message.recipient).append(System.lineSeparator())
                  .append("Message: ").append(message.messageText).append(System.lineSeparator());
        }
        return result.toString().trim();
    }

    // ---- JSON <-> Stored Messages array ----

    /** Reads messages.json into the Stored Messages array (skips IDs already loaded). */
    public static int loadStoredMessages() throws IOException {
        return loadStoredMessages(JSON_FILE);
    }

    /** Reads the given JSON file into the Stored Messages array; returns how many were added. */
    public static int loadStoredMessages(Path file) throws IOException {
        if (!Files.exists(file)) {
            return 0;
        }
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) {
            return 0;
        }
        int added = 0;
        for (Map<String, String> record : new JsonReader(json).readArray()) {
            String id = record.getOrDefault("messageID", "");
            if (findMessageByID(id) != null) {
                continue;
            }
            int number;
            try {
                number = Integer.parseInt(record.getOrDefault("messageNumber", ""));
            } catch (NumberFormatException exception) {
                number = totalMessagesInArrays() + 1;
            }
            Message message = new Message(number, record.get("recipient"),
                    record.get("message"), id);
            message.sender = record.getOrDefault("sender", "Developer");
            addMessage(message, "Stored");
            added++;
        }
        return added;
    }

    /** Rewrites messages.json from the Stored Messages array (used after a delete). */
    public static void saveStoredMessages() throws IOException {
        saveStoredMessages(JSON_FILE);
    }

    public static void saveStoredMessages(Path file) throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < storedMessages.size(); i++) {
            json.append(i == 0 ? "\n  " : ",\n  ").append(storedMessages.get(i).toJson());
        }
        json.append(storedMessages.isEmpty() ? "]\n" : "\n]\n");
        Files.writeString(file, json.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /** Small dependency-free reader for a JSON array of flat objects. */
    private static final class JsonReader {
        private final String text;
        private int pos = 0;

        JsonReader(String text) {
            this.text = text;
        }

        List<Map<String, String>> readArray() throws IOException {
            List<Map<String, String>> list = new ArrayList<>();
            skipWhitespace();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(readObject());
                skipWhitespace();
                char c = next();
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw error("Expected ',' or ']'");
                }
            }
        }

        private Map<String, String> readObject() throws IOException {
            Map<String, String> map = new HashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                map.put(key, peek() == '"' ? readString() : readBare());
                skipWhitespace();
                char c = next();
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw error("Expected ',' or '}'");
                }
            }
        }

        private String readString() throws IOException {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char escaped = next();
                switch (escaped) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 > text.length()) {
                            throw error("Bad unicode escape");
                        }
                        sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: sb.append(escaped); // \" \\ \/
                }
            }
        }

        private String readBare() {
            int start = pos;
            while (pos < text.length() && ",}] \t\r\n".indexOf(text.charAt(pos)) < 0) {
                pos++;
            }
            return text.substring(start, pos);
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private char peek() throws IOException {
            if (pos >= text.length()) {
                throw error("Unexpected end of file");
            }
            return text.charAt(pos);
        }

        private char next() throws IOException {
            char c = peek();
            pos++;
            return c;
        }

        private void expect(char expected) throws IOException {
            if (next() != expected) {
                throw error("Expected '" + expected + "'");
            }
        }

        private IOException error(String message) {
            return new IOException("Invalid messages.json at position " + pos + ": " + message);
        }
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
    public String getSender() { return sender; }

    /** Resets the sent counter for isolated unit tests. */
    static void resetTotalMessagesSentForTests() {
        totalMessagesSent = 0;
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        currentSender = "Developer";
    }
}
    

