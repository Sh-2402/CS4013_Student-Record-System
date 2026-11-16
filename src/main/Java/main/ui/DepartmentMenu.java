package main.ui;

import java.util.Objects;

/**
 * Command-line menu for department users.
 *
 * <p>Departments hold exam boards at the end of each academic year to
 * review student progression (UL Handbook 2026/27, SP 3.3.2). The menu
 * options will call ProgressionService once it has been built.</p>
 */
public class DepartmentMenu {

    private final InputHelper input;

    /**
     * Creates the department menu.
     *
     * @param input the shared input helper created by Main
     */
    public DepartmentMenu(InputHelper input) {
        this.input = Objects.requireNonNull(input, "Input helper must not be null");
    }

    /**
     * Shows the department menu until the user logs out.
     *
     * @param departmentId the ID of the logged-in department user
     */
    public void show(String departmentId) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("--- Department Menu (" + departmentId + ") ---");
            System.out.println("1. Run exam board for a programme");
            System.out.println("2. Check a student's progression status");
            System.out.println("3. List students below the minimum standard");
            System.out.println("4. Log out");

            int choice = input.readChoice(1, 4);

            switch (choice) {
                case 1 -> runExamBoard();
                case 2 -> checkStudentProgression();
                case 3 -> listStudentsBelowStandard();
                default -> loggedIn = false;
            }
        }

        System.out.println("Logged out.");
    }

    /** Reviews every student on a programme. */
    private void runExamBoard() {
        String programmeCode = input.readText("Enter programme code: ").trim();
        // TODO (Shourjyo): call ProgressionService to evaluate every student on the programme
        System.out.println("Exam board for " + programmeCode + " is not implemented yet.");
    }

    /** Shows the progression status of one student. */
    private void checkStudentProgression() {
        String studentId = input.readText("Enter student ID: ").trim();
        // TODO (Shourjyo): call ProgressionService for this student
        System.out.println("Progression check for " + studentId + " is not implemented yet.");
    }

    /** Lists students who do not meet the minimum standard (AR 1.5.3). */
    private void listStudentsBelowStandard() {
        String programmeCode = input.readText("Enter programme code: ").trim();
        // TODO (Shourjyo): call ProgressionService and show only non-progressing students
        System.out.println("Below-standard list for " + programmeCode + " is not implemented yet.");
    }
}
