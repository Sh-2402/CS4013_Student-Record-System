package main.ui;

import java.util.Objects;

/**
 * Command-line menu for faculty members.
 *
 * <p>PLACEHOLDER: created by Shourjyo so that Main runs end to end.
 * Owner: Bhumika, who will replace the option bodies. Please keep the
 * constructor and {@code show(String)} signature, because Main calls them.</p>
 */
public class FacultyMenu {

    private final InputHelper input;

    /**
     * Creates the faculty menu.
     *
     * @param input the shared input helper created by Main
     */
    public FacultyMenu(InputHelper input) {
        this.input = Objects.requireNonNull(input, "Input helper must not be null");
    }

    /**
     * Shows the faculty menu until the user logs out.
     *
     * @param facultyId the ID of the logged-in faculty member
     */
    public void show(String facultyId) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("--- Faculty Menu (" + facultyId + ") ---");
            System.out.println("1. Look up a student transcript");
            System.out.println("2. Submit module results");
            System.out.println("3. Log out");

            int choice = input.readChoice(1, 3);

            switch (choice) {
                // TODO (Bhumika): look up a transcript via TranscriptService
                case 1 -> System.out.println("Transcript lookup is not implemented yet.");
                // TODO (Bhumika): record results via ResultsService
                case 2 -> System.out.println("Submitting results is not implemented yet.");
                default -> loggedIn = false;
            }
        }

        System.out.println("Logged out.");
    }
}
