/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package quickchat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Student
 */
class MessageTest {

    private Message message;

    @BeforeEach
    void setUp() {
        Message.resetTotalMessagesSentForTests();

        message = new Message(
                1,
                "+27821234567",
                "Hello World",
                "1234567890"
        );
    }

    // 1. Valid message ID
    @Test
    void testCheckMessageIDValid() {
        assertTrue(message.checkMessageID());
    }

    // 2. Invalid message ID
    @Test
    void testCheckMessageIDInvalid() {
        Message invalid = new Message(
                1, "+27821234567", "Hello", "12345"
        );

        assertFalse(invalid.checkMessageID());
    }

    // 3. Automatically generated message ID
    @Test
    void testGeneratedMessageID() {
        Message generated = new Message(
                1, "+27821234567", "Hello World"
        );

        assertTrue(generated.checkMessageID());
        assertEquals(10, generated.getMessageID().length());
    }

    // 4. Valid South African cellphone number
    @Test
    void testRecipientCellValid() {
        assertEquals(
                Message.RECIPIENT_SUCCESS,
                message.checkRecipientCell()
        );
    }

    // 5. Invalid cellphone number without international code
    @Test
    void testRecipientCellMissingCode() {
        Message invalid = new Message(
                1, "0821234567", "Hello", "1234567890"
        );

        assertEquals(
                Message.RECIPIENT_ERROR,
                invalid.checkRecipientCell()
        );
    }

    // 6. Incorrect cellphone number format
    @Test
    void testRecipientCellInvalidFormat() {
        Message invalid = new Message(
                1, "+27123", "Hello", "1234567890"
        );

        assertEquals(
                Message.RECIPIENT_ERROR,
                invalid.checkRecipientCell()
        );
    }

    // 7. Correct message hash
    @Test
    void testCreateMessageHash() {
        assertEquals(
                "12:1:HELLOWORLD",
                message.getMessageHash()
        );
    }

    // 8. Message hash with punctuation
    @Test
    void testMessageHashWithPunctuation() {
        Message punctuation = new Message(
                5,
                "+27821234567",
                "Hello, wonderful world!",
                "9876543210"
        );

        assertEquals(
                "98:5:HELLOWORLD",
                punctuation.getMessageHash()
        );
    }

    // 9. Empty message hash
    @Test
    void testEmptyMessageHash() {
        Message empty = new Message(
                1, "+27821234567", "", "1234567890"
        );

        assertEquals("", empty.getMessageHash());
    }

    // 10. Message with exactly 250 characters
    @Test
    void testMessageAtMaximumLength() {
        Message valid = new Message(
                1,
                "+27821234567",
                "a".repeat(250),
                "1234567890"
        );

        assertEquals(
                Message.MESSAGE_READY,
                valid.validateMessageText()
        );
    }

    // 11. Message exceeding 250 characters
    @Test
    void testMessageTooLong() {
        Message tooLong = new Message(
                1,
                "+27821234567",
                "a".repeat(260),
                "1234567890"
        );

        assertEquals(
                "Message exceeds 250 characters by 10; please reduce the size.",
                tooLong.validateMessageText()
        );
    }

    // 12. Successfully send a message
    @Test
    void testSendMessageSuccessfully() {
        assertEquals(
                "Message successfully sent.",
                message.SentMessage("send")
        );

        assertEquals("Sent", message.getStatus());
        assertEquals(1, Message.returnTotalMessages());
    }

    // 13. Cannot send a message with an invalid recipient
    @Test
    void testCannotSendInvalidMessage() {
        Message invalid = new Message(
                1, "0821234567", "Hello", "1234567890"
        );

        assertEquals(
                "Message could not be sent. Please check the message ID, recipient, and message length.",
                invalid.SentMessage("send")
        );

        assertEquals("Pending", invalid.getStatus());
        assertEquals(0, Message.returnTotalMessages());
    }

    // 14. Sending messages increments the counter
    @Test
    void testTotalMessagesSent() {
        Message second = new Message(
                2,
                "+27821234567",
                "Good morning",
                "9876543210"
        );

        message.SentMessage("send");
        second.SentMessage("send");

        assertEquals(2, Message.returnTotalMessages());
        assertEquals(2, Message.returnTotalMessagess());
    }

    // 15. Disregard a message
    @Test
    void testDisregardMessage() {
        assertEquals(
                "Press 0 to delete the message.",
                message.SentMessage("disregard")
        );

        assertEquals("Disregarded", message.getStatus());
        assertEquals(0, Message.returnTotalMessages());
    }

    // 16. Discard action
    @Test
    void testDiscardMessage() {
        assertEquals(
                "Press 0 to delete the message.",
                message.SentMessage("discard")
        );

        assertEquals("Disregarded", message.getStatus());
    }

    // 17. Invalid action
    @Test
    void testInvalidAction() {
        assertEquals(
                "Invalid option. Please choose send, disregard, or store.",
                message.SentMessage("invalid")
        );

        assertEquals("Pending", message.getStatus());
    }

    // 18. Null action
    @Test
    void testNullAction() {
        assertEquals(
                "Invalid option. Please choose send, disregard, or store.",
                message.SentMessage((String) null)
        );
    }

    // 19. Store a message in JSON
    @Test
    void testStoreMessageSuccessfully() throws IOException {
        Path file = Path.of("messages.json");
        String previous = Files.exists(file)
                ? Files.readString(file, StandardCharsets.UTF_8)
                : null;

        try {
            Files.deleteIfExists(file);

            assertEquals(
                    "Message successfully stored.",
                    message.SentMessage("store")
            );

            assertEquals("Stored", message.getStatus());
            assertTrue(Files.exists(file));

            String json = Files.readString(
                    file, StandardCharsets.UTF_8
            );

            assertTrue(json.contains("\"messageID\":\"1234567890\""));
            assertTrue(json.contains("\"messageHash\":\"12:1:HELLOWORLD\""));
            assertTrue(json.contains("\"messageNumber\":1"));
            assertTrue(json.contains("\"recipient\":\"+27821234567\""));
            assertTrue(json.contains("\"message\":\"Hello World\""));
            assertTrue(json.contains("\"status\":\"Stored\""));
        } finally {
            restoreFile(file, previous);
        }
    }

    // 20. Store multiple messages
    @Test
    void testStoreMultipleMessages() throws IOException {
        Path file = Path.of("messages.json");
        String previous = Files.exists(file)
                ? Files.readString(file, StandardCharsets.UTF_8)
                : null;

        try {
            Files.deleteIfExists(file);
            message.storeMessage();

            Message second = new Message(
                    2,
                    "+27821234567",
                    "Second message",
                    "9876543210"
            );

            second.storeMessage();

            String json = Files.readString(
                    file, StandardCharsets.UTF_8
            );

            assertTrue(json.startsWith("["));
            assertTrue(json.trim().endsWith("]"));
            assertTrue(json.contains("\"messageID\":\"1234567890\""));
            assertTrue(json.contains("\"messageID\":\"9876543210\""));
        } finally {
            restoreFile(file, previous);
        }
    }

    // 21. Reject invalid existing JSON
    @Test
    void testStoreMessageInvalidExistingJson() throws IOException {
        Path file = Path.of("messages.json");
        String previous = Files.exists(file)
                ? Files.readString(file, StandardCharsets.UTF_8)
                : null;

        try {
            Files.writeString(file, "invalid JSON");

            assertThrows(
                    IOException.class,
                    () -> message.storeMessage()
            );
        } finally {
            restoreFile(file, previous);
        }
    }

    // 22. Escape special characters in JSON
    @Test
    void testStoreMessageEscapesSpecialCharacters() throws IOException {
        Path file = Path.of("messages.json");
        String previous = Files.exists(file)
                ? Files.readString(file, StandardCharsets.UTF_8)
                : null;

        try {
            Files.deleteIfExists(file);

            Message special = new Message(
                    1,
                    "+27821234567",
                    "Hello \"World\"\nNext line\tDone",
                    "1234567890"
            );

            special.storeMessage();

            String json = Files.readString(
                    file, StandardCharsets.UTF_8
            );

            assertTrue(json.contains("Hello \\\"World\\\"\\nNext line\\tDone"));
        } finally {
            restoreFile(file, previous);
        }
    }

    // 23. Print an individual message's details
    @Test
    void testPrintIndividualMessage() {
        String details = message.printMessages();

        assertTrue(details.contains("Message ID: 1234567890"));
        assertTrue(details.contains("Message Hash: 12:1:HELLOWORLD"));
        assertTrue(details.contains("Recipient: +27821234567"));
        assertTrue(details.contains("Message: Hello World"));
    }

    // 24. Print only sent messages
    @Test
    void testPrintSentMessagesOnly() {
        Message second = new Message(
                2,
                "+27821234567",
                "Another message",
                "9876543210"
        );

        message.SentMessage("send");
        second.SentMessage("disregard");

        String output = Message.printMessages(
                List.of(message, second)
        );

        assertTrue(output.contains("1234567890"));
        assertFalse(output.contains("9876543210"));
    }

    // 25. Empty message list
    @Test
    void testPrintEmptyMessages() {
        assertEquals("", Message.printMessages(List.of()));
    }

    // 26. Getters and whitespace trimming
    @Test
    void testGettersAndTrimming() {
        Message trimmed = new Message(
                7,
                " +27821234567 ",
                " Hello World ",
                "1234567890"
        );

        assertEquals("1234567890", trimmed.getMessageID());
        assertEquals(7, trimmed.getMessageNumber());
        assertEquals("+27821234567", trimmed.getRecipient());
        assertEquals("Hello World", trimmed.getMessageText());
        assertEquals("12:7:HELLOWORLD", trimmed.getMessageHash());
        assertEquals("Pending", trimmed.getStatus());
    }

    // 27. Interactive menu: send option
    @Test
    void testInteractiveSendOption() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "1\n".getBytes(StandardCharsets.UTF_8)
        );

        var originalInput = System.in;

        try {
            System.setIn(input);

            assertEquals(
                    "Message successfully sent.",
                    message.SentMessage()
            );
        } finally {
            System.setIn(originalInput);
        }

        assertEquals("Sent", message.getStatus());
    }

    // 28. Interactive menu: invalid option
    @Test
    void testInteractiveInvalidOption() {
        ByteArrayInputStream input = new ByteArrayInputStream(
                "9\n".getBytes(StandardCharsets.UTF_8)
        );

        var originalInput = System.in;

        try {
            System.setIn(input);

            assertEquals(
                    "Invalid option. Please choose 1, 2, or 3.",
                    message.SentMessage()
            );
        } finally {
            System.setIn(originalInput);
        }
    }

    // Helper to restore messages.json after file tests
    private void restoreFile(Path file, String previous)
            throws IOException {

        if (previous == null) {
            Files.deleteIfExists(file);
        } else {
            Files.writeString(
                    file,
                    previous,
                    StandardCharsets.UTF_8
            );
        }
    }
}