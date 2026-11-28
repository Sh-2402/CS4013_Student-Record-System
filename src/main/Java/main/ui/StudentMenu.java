package main.ui;

import main.model.ModuleResult;
import main.service.TranscriptService;

import java.util.Locale;
import java.util.Objects;

/**
 * Menu shown to a logged-in student.
 *
 * A student can only see their own results: the student ID is fixed when
 * the menu is created (from the login) and is never typed in.
 *
 * Options: view transcript, semester QCA, overall QCA.
 */
public class StudentMenu {

    private final InputHelper input;
    private final TranscriptService transcriptService;
    private final String studentId;

    /**
     * Sets up the menu for the student who just logged in.
     * 
     * @param input shared input helper (use one InputHelper for the whole
     *              program, because each one opens its own Scanner)
     * @param transcriptService source of transcripts and QCA values
     * @param studentId ID of the student who logged in
     */
    public StudentMenu(InputHelper input,
                       TranscriptService transcriptService,
                       String studentId) {

        this.input = Objects.requireNonNull(input, "Input must not be null");
        this.transcriptService = Objects.requireNonNull(
                transcriptService, "Transcript service must not be null");
        this.studentId = Objects.requireNonNull(
                studentId, "Student ID must not be null");
    }

    /** Shows the menu until the student chooses to log out. */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== Student Menu (" + studentId + ") ===");
            System.out.println("1. View my transcript");
            System.out.println("2. View my semester QCA");
            System.out.println("3. View my overall QCA");
            System.out.println("0. Log out");

            int choice = input.readChoice(0, 3);

            if (choice == 1) {
                showTranscript();
            } else if (choice == 2) {
                showSemesterQca();
            } else if (choice == 3) {
                showOverallQca();
            } else {
                running = false;
            }
        }
    }
    // Prints the student's full transcript 
    private void showTranscript() {
        try {
            System.out.println();
            System.out.print(transcriptService.buildTranscript(studentId));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    // Asks for a year and semester, then prints the QCA for it
    private void showSemesterQca() {
        int year = input.readInt("Academic year start (e.g. 2026): ");
        int semester = input.readInt("Semester (1 or 2): ");

        try {
            double qca = transcriptService.semesterQca(
                    studentId, year, semester);

            if (!hasResultsIn(year, semester)) {
                System.out.println("No results recorded for " + year
                        + " semester " + semester + ".");
                return;
            }

            System.out.println(String.format(Locale.ROOT,
                    "QCA for %d semester %d: %.2f", year, semester, qca));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    // Prints the overall QCA, or a message if there are no results yet.
    private void showOverallQca() {
        if (transcriptService.getStandingResults(studentId).isEmpty()) {
            System.out.println("No results recorded.");
            return;
        }

        System.out.println(String.format(Locale.ROOT,
                "Overall QCA: %.2f", transcriptService.overallQca(studentId)));
    }

    /** Returns true if the student has a standing result in this semester. */
    private boolean hasResultsIn(int year, int semester) {
        for (ModuleResult r : transcriptService.getStandingResults(studentId)) {
            if (r.getYear() == year && r.getSemester() == semester) {
                return true;
            }
        }
        return false;
    }
}
