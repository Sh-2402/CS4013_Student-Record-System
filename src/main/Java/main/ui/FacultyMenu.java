package main.ui;

import main.model.Grade;
import main.model.ModuleResult;
import main.service.ResultsService;
import main.service.TranscriptService;

import java.util.Objects;

/**
 * Menu shown to a logged-in faculty member.
 *
 * Options:
 * - submit a result for a module they teach
 * - look up any student's transcript
 *
 * Invalid input (unknown student, wrong module, duplicate result, a grade
 * a lecturer cannot award and so on) is reported with the message from
 * the service, and the menu carries on.
 */
public class FacultyMenu {

    private final InputHelper input;
    private final ResultsService resultsService;
    private final TranscriptService transcriptService;
    private final String facultyId;

    /**
     * Sets up the menu for the faculty member who just logged in.
     * 
     * @param input shared input helper (one for the whole program)
     * @param resultsService used to record results
     * @param transcriptService used to look up transcripts
     * @param facultyId ID of the faculty member who logged in
     */
    public FacultyMenu(InputHelper input,
                       ResultsService resultsService,
                       TranscriptService transcriptService,
                       String facultyId) {

        this.input = Objects.requireNonNull(input, "Input must not be null");
        this.resultsService = Objects.requireNonNull(
                resultsService, "Results service must not be null");
        this.transcriptService = Objects.requireNonNull(
                transcriptService, "Transcript service must not be null");
        this.facultyId = Objects.requireNonNull(
                facultyId, "Faculty ID must not be null");
    }

    /** Shows the menu until the faculty member chooses to log out. */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== Faculty Menu (" + facultyId + ") ===");
            System.out.println("1. Submit a module result");
            System.out.println("2. Look up a student's transcript");
            System.out.println("0. Log out");

            int choice = input.readChoice(0, 2);

            if (choice == 1) {
                submitResult();
            } else if (choice == 2) {
                lookUpTranscript();
            } else {
                running = false;
            }
        }
    }
    // Asks for the results details and saves it, or shows the error if something is wrong.
    private void submitResult() {
        String studentId = input.readText("Student ID: ").trim();
        String moduleCode = input.readText("Module code: ").trim();
        int year = input.readInt("Academic year start (e.g. 2026): ");
        int semester = input.readInt("Semester (1 or 2): ");
        String gradeText = input.readText("Grade (e.g. A1, B2, C3, D1, F, NG, I): ")
                .trim().toUpperCase();

        Grade grade = parseGrade(gradeText);
        if (grade == null) {
            System.out.println("Error: unknown grade '" + gradeText + "'");
            return;
        }

        try {
            ModuleResult saved = resultsService.recordResult(
                    facultyId, studentId, moduleCode, year, semester, grade);

            System.out.println("Result recorded: " + saved
                    + " for student " + studentId);

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** Returns the grade with this name, or null if there is none. */
    private Grade parseGrade(String text) {
        try {
            return Grade.valueOf(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    // Prints any student's transcript ( lecturers aren't limited to one student)
    private void lookUpTranscript() {
        String studentId = input.readText("Student ID: ").trim();

        try {
            System.out.println();
            System.out.print(transcriptService.buildTranscript(studentId));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}