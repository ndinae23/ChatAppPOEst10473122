# QuickChat

A console messaging app written in Java for PROG5121 (POE). It covers three parts:

1. **Part 1 – Registration and login:** username, password and South African cell number validation, then login.
2. **Part 2 – Messages:** create a message with a 10-digit ID and a hash, then send, disregard or store it.
3. **Part 3 – Arrays and stored messages:** keep every message in arrays, load stored messages from JSON, and search, report on and delete them.

## Project structure

```
quickchat/
├── src/quickchat/
│   ├── Login.java        Registration and login validation (Part 1)
│   ├── Message.java      Message logic, arrays, JSON storage, search/report/delete (Parts 2 and 3)
│   └── QuickChat.java    Console app: registration, login, main menu, Stored Messages menu
├── test/quickchat/
│   ├── LoginTest.java    JUnit 5 tests for Login
│   └── MessageTest.java  JUnit 4 tests for Message (including the Part 3 test data)
├── build.xml / nbproject/  NetBeans (Ant) project files
└── pom.xml               Optional Maven build
```

## Requirements

- JDK 24 (the NetBeans project is set to source/target 24)
- Apache NetBeans, or Maven 3.9+ if you use the `pom.xml`

## Running it

**NetBeans:** open the `quickchat` folder as a project, then press **Shift+F6** with `QuickChat.java` selected.

**Maven:**

```
mvn compile exec:java
```

## Running the tests

**NetBeans:** right-click the project and choose **Test** (Alt+F6).

**Maven:**

```
mvn test
```

## Using the app

1. Enter your first name, last name, username, password and cell number. The app tells you if anything is badly formatted.
2. Log in with the username and password you just created.
3. Pick an option from the main menu:

| Option | What it does |
|---|---|
| 1) Send Messages | Enter how many messages you want, then a recipient and text for each. Choose **Send**, **Disregard** or **Store**. |
| 2) Show recently sent messages | Lists the messages sent in this run. |
| 3) Quit | Shows the total sent and exits. |
| 4) Stored Messages | Opens the Part 3 menu below. |

### Stored Messages menu (option 4)

| Key | Feature |
|---|---|
| a | Display the sender and recipient of all stored messages |
| b | Display the longest stored message |
| c | Search for a message ID and show its recipient and message |
| d | Search for all messages sent or stored for a recipient |
| e | Delete a message using its message hash |
| f | Display a report with the full details of all stored messages |
| x | Back to the main menu |

## Validation rules

- **Recipient:** `+27` followed by exactly 9 digits (for example `+27838884567`).
- **Message text:** 250 characters or fewer. Longer messages show how many characters over the limit they are.
- **Message ID:** exactly 10 digits, generated randomly.
- **Message hash:** first two digits of the ID, then the message number, then the first and last words of the message, in capitals (for example `22:1:WHERETIME`). Punctuation is ignored.

## How Part 3 works

- **Arrays:** `Message.java` keeps the Sent, Disregarded, Stored, Message Hash and Message ID arrays. They are filled while the app runs and are never hard-coded.
- **Storage:** stored messages are appended to `messages.json` in the folder you run the app from. When you log in, the app reads that file into the Stored Messages array, skipping any message ID it already has.
- **Deleting:** deleting a message by hash removes it from the arrays and rewrites `messages.json`, so it stays deleted next time.
- **Sender:** new messages use your first name as the sender. In the unit tests the sender is "Developer".

## Unit tests (Part 3)

The tests use Test Data Messages 1–5 from the task and cover:

- Sent Messages array correctly populated
- Display the longest message
- Search for a message ID
- Search all messages for a recipient
- Delete a message using a message hash
- Display report

There are also tests for reading `messages.json` (including quotes inside a message) and for saving then reloading it.

## Notes

- `messages.json` is created the first time you store a message. Delete it to start with an empty Stored Messages array.
- Do not put non-Java files (such as pasted message dumps) inside `src`. They will break the build.
