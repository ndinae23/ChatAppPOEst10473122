/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package quickchat;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 * @author Student
 */
public class MessageTest {

    private Message message;

    @Before
    public void setUp() {
        Message.resetTotalMessagesSentForTests();

        message = new Message(
                1,
                "+27821234567",
                "Hello World",
                "1234567890"
        );
    }

    @Test
    public void testValidMessageID() {
        assertTrue(message.checkMessageID());
    }

    @Test
    public void testInvalidMessageID() {
        Message invalid = new Message(
                1, "+27821234567", "Hello", "12345"
        );

        assertFalse(invalid.checkMessageID());
    }

    @Test
    public void testGeneratedMessageID() {
        Message generated = new Message(
                1, "+27821234567", "Hello"
        );

        assertTrue(generated.checkMessageID());
        assertEquals(10, generated.getMessageID().length());
    }

    @Test
    public void testValidRecipient() {
        assertEquals(
                Message.RECIPIENT_SUCCESS,
                message.checkRecipientCell()
        );
    }

    @Test
    public void testInvalidRecipient() {
        Message invalid = new Message(
                1, "0821234567", "Hello", "1234567890"
        );

        assertEquals(
                Message.RECIPIENT_ERROR,
                invalid.checkRecipientCell()
        );
    }

    @Test
    public void testMessageHash() {
        assertEquals("12:1:HELLOWORLD", message.getMessageHash());
    }

    @Test
    public void testMessageHashWithPunctuation() {
        Message punctuation = new Message(
                5,
                "+27821234567",
                "Hello, wonderful world!",
                "9876543210"
        );

        assertEquals("98:5:HELLOWORLD", punctuation.getMessageHash());
    }

    @Test
    public void testEmptyMessageHash() {
        Message empty = new Message(
                1, "+27821234567", "", "1234567890"
        );

        assertEquals("", empty.getMessageHash());
    }

    @Test
    public void testMessageAt250Characters() {
        Message valid = new Message(
                1,
                "+27821234567",
                repeatCharacter('a', 250),
                "1234567890"
        );

        assertEquals(
                Message.MESSAGE_READY,
                valid.validateMessageText()
        );
    }

    @Test
    public void testMessageOver250Characters() {
        Message tooLong = new Message(
                1,
                "+27821234567",
                repeatCharacter('a', 260),
                "1234567890"
        );

        assertEquals(
                "Message exceeds 250 characters by 10; please reduce the size.",
                tooLong.validateMessageText()
        );
    }

    @Test
    public void testSendMessageSuccessfully() {
        assertEquals(
                "Message successfully sent.",
                message.SentMessage("send")
        );

        assertEquals("Sent", message.getStatus());
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testCannotSendInvalidRecipient() {
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

    @Test
    public void testCannotSendInvalidMessageID() {
        Message invalid = new Message(
                1, "+27821234567", "Hello", "123"
        );

        assertTrue(invalid.SentMessage("send").startsWith(
                "Message could not be sent."
        ));
        assertEquals("Pending", invalid.getStatus());
    }

    @Test
    public void testCannotSendLongMessage() {
        Message invalid = new Message(
                1,
                "+27821234567",
                repeatCharacter('x', 251),
                "1234567890"
        );

        assertTrue(invalid.SentMessage("send").startsWith(
                "Message could not be sent."
        ));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void testTotalMessagesSent() {
        Message second = new Message(
                2, "+27821234567", "Good morning", "9876543210"
        );

        message.SentMessage("send");
        second.SentMessage("send");

        assertEquals(2, Message.returnTotalMessages());
        assertEquals(2, Message.returnTotalMessagess());
    }

    @Test
    public void testDisregardMessage() {
        assertEquals(
                "Press 0 to delete the message.",
                message.SentMessage("disregard")
        );

        assertEquals("Disregarded", message.getStatus());
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    public void testDiscardAlias() {
        assertEquals(
                "Press 0 to delete the message.",
                message.SentMessage("discard")
        );

        assertEquals("Disregarded", message.getStatus());
    }

    @Test
    public void testStoreAction() throws Exception {
        // This test checks the store action's response and status.
        // It saves and restores messages.json to avoid losing existing data.
        java.nio.file.Path file = java.nio.file.Path.of("messages.json");
        boolean existed = java.nio.file.Files.exists(file);
        String previous = existed
                ? java.nio.file.Files.readString(file)
                : null;

        try {
            java.nio.file.Files.deleteIfExists(file);

            assertEquals(
                    "Message successfully stored.",
                    message.SentMessage("store")
            );

            assertEquals("Stored", message.getStatus());

            String json = java.nio.file.Files.readString(file);
            assertTrue(json.contains("\"messageID\":\"1234567890\""));
            assertTrue(json.contains("\"message\":\"Hello World\""));
        } finally {
            if (existed) {
                java.nio.file.Files.writeString(file, previous);
            } else {
                java.nio.file.Files.deleteIfExists(file);
            }
        }
    }

    @Test
    public void testInvalidAction() {
        assertEquals(
                "Invalid option. Please choose send, disregard, or store.",
                message.SentMessage("invalid")
        );

        assertEquals("Pending", message.getStatus());
    }

    @Test
    public void testNullAction() {
        assertEquals(
                "Invalid option. Please choose send, disregard, or store.",
                message.SentMessage((String) null)
        );
    }

    @Test
    public void testPrintIndividualMessage() {
        String details = message.printMessages();

        assertTrue(details.contains("Message ID: 1234567890"));
        assertTrue(details.contains("Message Hash: 12:1:HELLOWORLD"));
        assertTrue(details.contains("Recipient: +27821234567"));
        assertTrue(details.contains("Message: Hello World"));
    }

    @Test
    public void testPrintOnlySentMessages() {
        Message second = new Message(
                2, "+27821234567", "Another message", "9876543210"
        );

        message.SentMessage("send");
        second.SentMessage("disregard");

        String result = Message.printMessages(Arrays.asList(message, second));

        assertTrue(result.contains("1234567890"));
        assertFalse(result.contains("9876543210"));
    }

    @Test
    public void testPrintEmptyList() {
        assertEquals("", Message.printMessages(Collections.emptyList()));
    }

    @Test
    public void testGettersAndTrimming() {
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

    @Test
    public void testInteractiveSendOption() {
        java.io.InputStream original = System.in;

        try {
            System.setIn(new ByteArrayInputStream(
                    "1\n".getBytes(StandardCharsets.UTF_8)
            ));

            assertEquals(
                    "Message successfully sent.",
                    message.SentMessage()
            );
        } finally {
            System.setIn(original);
        }

        assertEquals("Sent", message.getStatus());
    }

    private String repeatCharacter(char character, int count) {
        char[] characters = new char[count];
        java.util.Arrays.fill(characters, character);
        return new String(characters);
    }
}