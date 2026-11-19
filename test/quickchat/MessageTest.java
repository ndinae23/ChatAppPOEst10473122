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
import java.util.List;

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


    // =====================================================================
    //  PART 3 UNIT TESTS (test data from the task specification)
    // =====================================================================

    /** Loads Test Data Messages 1-4 into the arrays by flag (no hard-coding in the app itself). */
    private void loadTestData1To4() {
        Message.addMessage(new Message(1, "+27834557896", "Did you get the cake?", "1000000001"), "Sent");
        Message.addMessage(new Message(2, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "1000000002"), "Stored");
        Message.addMessage(new Message(3, "+27834484567", "Yohoooo, I am at your gate.", "1000000003"), "Disregard");
        Message.addMessage(new Message(4, "0838884567", "It is dinner time!", "0838884567"), "Sent");
    }

    private void loadTestMessage5() {
        Message.addMessage(new Message(5, "+27838884567", "Ok, I am leaving without you.", "1000000005"), "Stored");
    }

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        loadTestData1To4();

        assertEquals(
                Arrays.asList("Did you get the cake?", "It is dinner time!"),
                Message.getSentMessageTexts()
        );
    }

    @Test
    public void testDisregardedAndStoredArraysPopulated() {
        loadTestData1To4();

        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals(1, Message.getStoredMessages().size());
        assertEquals(4, Message.getMessageHashes().size());
        assertEquals(4, Message.getMessageIDs().size());
    }

    @Test
    public void testDisplayLongestMessage() {
        loadTestData1To4();

        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                Message.findLongestStoredMessage().getMessageText()
        );
    }

    @Test
    public void testSearchForMessageID() {
        loadTestData1To4();

        assertEquals(
                "It is dinner time!",
                Message.findMessageByID("0838884567").getMessageText()
        );
    }

    @Test
    public void testSearchForMessageIDNotFound() {
        loadTestData1To4();

        assertNull(Message.findMessageByID("9999999999"));
    }

    @Test
    public void testSearchAllMessagesForRecipient() {
        loadTestData1To4();
        loadTestMessage5();

        List<String> found = new java.util.ArrayList<>();
        for (Message m : Message.findMessagesByRecipient("+27838884567")) {
            found.add(m.getMessageText());
        }

        assertEquals(
                Arrays.asList(
                        "Where are you? You are late! I have asked you to be on time.",
                        "Ok, I am leaving without you."
                ),
                found
        );
    }

    @Test
    public void testDeleteMessageUsingHash() {
        loadTestData1To4();

        String hash = Message.getStoredMessages().get(0).getMessageHash();
        assertEquals("10:2:WHERETIME", hash);

        assertEquals(
                "Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                Message.deleteMessageByHash(hash)
        );

        assertEquals(0, Message.getStoredMessages().size());
        assertFalse(Message.getMessageHashes().contains(hash));
        assertFalse(Message.getMessageIDs().contains("1000000002"));
    }

    @Test
    public void testDeleteMessageHashNotFound() {
        loadTestData1To4();

        assertEquals(
                "No message found with hash 99:9:NOPE.",
                Message.deleteMessageByHash("99:9:NOPE")
        );
        assertEquals(4, Message.totalMessagesInArrays());
    }

    @Test
    public void testDisplayReport() {
        loadTestData1To4();

        String report = Message.report(Message.getSentMessages());

        assertTrue(report.contains("Message Hash: 10:1:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message Hash: 08:4:ITTIME"));
        assertEquals("No messages found.", Message.report(Collections.emptyList()));
    }

    @Test
    public void testDisplaySenderAndRecipient() {
        loadTestData1To4();

        assertEquals(
                "Sender: Developer | Recipient: +27838884567",
                Message.displaySenderAndRecipient(Message.getStoredMessages())
        );
    }

    @Test
    public void testSendActionPopulatesSentArray() {
        message.SentMessage("send");

        assertEquals(1, Message.getSentMessages().size());
        assertEquals(1, Message.getMessageHashes().size());
        assertEquals("1234567890", Message.getMessageIDs().get(0));
    }

    @Test
    public void testStoredMessagesReadFromJsonFile() throws Exception {
        java.nio.file.Path file = java.nio.file.Files.createTempFile("quickchat", ".json");
        try {
            java.nio.file.Files.writeString(file,
                    "[\n  {\"messageID\":\"1000000002\",\"messageHash\":\"10:2:WHERETIME\","
                    + "\"messageNumber\":2,\"recipient\":\"+27838884567\","
                    + "\"message\":\"Say \\\"hi\\\", ok\",\"status\":\"Stored\"}\n]\n");

            assertEquals(1, Message.loadStoredMessages(file));
            assertEquals(0, Message.loadStoredMessages(file)); // no duplicates on reload

            Message loaded = Message.getStoredMessages().get(0);
            assertEquals("Say \"hi\", ok", loaded.getMessageText());
            assertEquals("+27838884567", loaded.getRecipient());
            assertEquals("10:2:SAYOK", loaded.getMessageHash());
        } finally {
            java.nio.file.Files.deleteIfExists(file);
        }
    }

    @Test
    public void testSaveThenLoadRoundTrip() throws Exception {
        java.nio.file.Path file = java.nio.file.Files.createTempFile("quickchat", ".json");
        try {
            loadTestData1To4();
            loadTestMessage5();
            Message.saveStoredMessages(file);

            Message.resetTotalMessagesSentForTests();
            assertEquals(2, Message.loadStoredMessages(file));
            assertEquals(2, Message.getStoredMessages().size());
        } finally {
            java.nio.file.Files.deleteIfExists(file);
        }
    }

    private String repeatCharacter(char character, int count) {
        char[] characters = new char[count];
        java.util.Arrays.fill(characters, character);
        return new String(characters);
    }
}
