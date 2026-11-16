package main.ui;

import java.util.Objects;

/**
 * Command-line menu for students.
 *
 * <p>PLACEHOLDER: created by Shourjyo so that Main runs end to end.
 * Owner: Bhumika, who will replace the option bodies. Please keep the
 * constructor and {@code show(String)} signature, because Main calls them.</p>
 */
public class StudentMenu {

    private final InputHelper input;

    /**
     * Creates the student menu.
     *
     * @param input the shared input helper created by Main
     */
    public StudentMenu(InputHelper input) {
        this.input = Objects.requireNonNull(input, "Input helper must not be null");
    }

    /**
     * Shows the student menu until the user logs out.
     *
     * @param studentId the ID of the logged-in student
     */
    public void show(String studentId) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("--- Student Menu (" + studentId + ") ---");
            System.out.println("1. View my transcript");
            System.out.println("2. Log out");

            int choice = input.readChoice(1, 2);

            if (choice == 1) {
                // TODO (Bhumika): show this student's transcript via TranscriptService
                System.out.println("Transcript view is not implemented yet.");
            } else {
                loggedIn = false;
            }
        }

        System.out.println("Logged out.");
    }
}
