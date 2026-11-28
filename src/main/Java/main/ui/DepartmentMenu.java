package main.ui;

import main.model.Module;
import main.model.ModuleResult;
import main.model.PostgraduateProgramme;
import main.model.Programme;
import main.model.Registration;
import main.model.Student;
import main.model.UndergraduateProgramme;
import main.rules.ProgressionStatus;
import main.service.ProgrammeService;
import main.service.ProgressionService;
import main.service.RegistrationService;
import main.service.StudentService;
import main.service.TranscriptService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Command-line menu for department users.
 *
 * <p>A department sets up students, programmes and modules, registers
 * students on programmes, and holds the exam board at the end of each year
 * to review students who do not meet the minimum standard
 * (UL Handbook 2026/27, AR 1.5.3 and SP 3.3.2).</p>
 *
 * <p>Invalid input (duplicate code, unknown student, already registered
 * and so on) is reported with the message from the service, and the menu
 * carries on.</p>
 */
public class DepartmentMenu {

    private final InputHelper input;
    private final ProgressionService progressionService;
    private final TranscriptService transcriptService;
    private final StudentService studentService;
    private final ProgrammeService programmeService;
    private final RegistrationService registrationService;
    private final String departmentId;

    /**
     * Creates the department menu for the user who logged in.
     *
     * @param input               the shared input helper created by Main
     * @param progressionService  checks students against the progression rules
     * @param transcriptService   gives students' QCA and standing results
     * @param studentService      adds and finds students
     * @param programmeService    adds and finds programmes and modules
     * @param registrationService registers students and updates registrations
     * @param departmentId        the ID of the logged-in department user
     */
    public DepartmentMenu(InputHelper input,
                          ProgressionService progressionService,
                          TranscriptService transcriptService,
                          StudentService studentService,
                          ProgrammeService programmeService,
                          RegistrationService registrationService,
                          String departmentId) {

        this.input = Objects.requireNonNull(input, "Input helper must not be null");
        this.progressionService = Objects.requireNonNull(
                progressionService, "Progression service must not be null");
        this.transcriptService = Objects.requireNonNull(
                transcriptService, "Transcript service must not be null");
        this.studentService = Objects.requireNonNull(
                studentService, "Student service must not be null");
        this.programmeService = Objects.requireNonNull(
                programmeService, "Programme service must not be null");
        this.registrationService = Objects.requireNonNull(
                registrationService, "Registration service must not be null");
        this.departmentId = Objects.requireNonNull(
                departmentId, "Department ID must not be null");
    }

    /**
     * Shows the department menu until the user logs out.
     */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== Department Menu (" + departmentId + ") ===");
            System.out.println("1. Add a student");
            System.out.println("2. Add a programme");
            System.out.println("3. Add a module");
            System.out.println("4. Register a student on a programme");
            System.out.println("5. Check a student's progression status");
            System.out.println("6. List students who do not progress");
            System.out.println("7. Run exam board");
            System.out.println("0. Log out");

            int choice = input.readChoice(0, 7);

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> addProgramme();
                case 3 -> addModule();
                case 4 -> registerStudent();
                case 5 -> checkProgression();
                case 6 -> listNotProgressing();
                case 7 -> runExamBoard();
                default -> running = false;
            }
        }
    }

    // ---------- Set-up options ----------

    private void addStudent() {
        String id = input.readText("Student ID: ").trim();
        String name = input.readText("Name: ").trim();
        String email = input.readText("Email: ").trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            System.out.println("Error: ID, name and email must all be filled in");
            return;
        }

        try {
            studentService.addStudent(new Student(name, id, email, null));
            System.out.println("Student " + id + " added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addProgramme() {
        String code = input.readText("Programme code: ").trim();
        String title = input.readText("Title: ").trim();
        String department = input.readText("Department: ").trim();

        System.out.println("Type: 1. Undergraduate  2. Postgraduate");
        int type = input.readChoice(1, 2);

        Programme programme = (type == 1)
                ? new UndergraduateProgramme(code, title, department)
                : new PostgraduateProgramme(code, title, department);

        try {
            programmeService.addProgramme(programme);
            System.out.println("Programme " + code + " added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addModule() {
        String code = input.readText("Module code: ").trim();
        String title = input.readText("Title: ").trim();
        int credits = input.readInt("Credits: ");
        int semester = input.readInt("Semester (1 or 2): ");
        String lecturerId = input.readText("Lecturer ID: ").trim();

        try {
            // The Module constructor rejects bad credits or semesters.
            programmeService.addModule(new Module(code, title, credits, semester, lecturerId));
            System.out.println("Module " + code + " added.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerStudent() {
        String studentId = input.readText("Student ID: ").trim();
        String programmeCode = input.readText("Programme code: ").trim();

        try {
            registrationService.register(studentId, programmeCode);
            System.out.println("Student " + studentId + " registered on " + programmeCode + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- Progression options ----------

    private void checkProgression() {
        String studentId = input.readText("Student ID: ").trim();

        try {
            ProgressionStatus status = progressionService.evaluate(studentId);

            // Nothing to assess yet, for example a first year before their first exams.
            if (transcriptService.getStandingResults(studentId).isEmpty()) {
                System.out.println("No results recorded yet for " + studentId
                        + ", so progression cannot be assessed.");
                return;
            }

            System.out.println("Overall QCA: " + formatQca(transcriptService.overallQca(studentId)));
            System.out.println("Status: " + status + " - " + status.getDescription());
            printModulesToReview(studentId);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listNotProgressing() {
        List<Student> students = progressionService.listNotProgressing();

        if (students.isEmpty()) {
            System.out.println("All registered students meet the minimum standard.");
            return;
        }

        System.out.println("Students who do not meet the minimum standard:");
        for (Student student : students) {
            System.out.println("  " + describe(student));
        }
    }

    /**
     * Reviews each student who does not meet the minimum standard and
     * records the board's decision on their registration (SP 3.3.6.2).
     */
    private void runExamBoard() {
        List<Student> students = progressionService.listNotProgressing();

        if (students.isEmpty()) {
            System.out.println("All registered students meet the minimum standard. Nothing to review.");
            return;
        }

        System.out.println("Exam board: " + students.size() + " student(s) to review.");

        for (Student student : students) {
            System.out.println();
            System.out.println(describe(student));
            printModulesToReview(student.getId());

            System.out.println("Decision: 1. Repeat the year  2. Withdraw  3. No change");
            int decision = input.readChoice(1, 3);

            if (decision == 1) {
                changeStatus(student.getId(), "REPEATING");
            } else if (decision == 2) {
                changeStatus(student.getId(), "WITHDRAWN");
            } else {
                System.out.println("No change made.");
            }
        }

        System.out.println();
        System.out.println("Exam board finished.");
    }

    // ---------- Helpers ----------

    /** Updates a student's registration status and saves it. */
    private void changeStatus(String studentId, String newStatus) {
        try {
            Registration registration = registrationService.findRegistration(studentId);

            if (registration == null) {
                System.out.println("Error: student " + studentId + " is not registered");
                return;
            }

            registration.changeStatus(newStatus);
            registrationService.updateRegistration(registration);
            System.out.println("Status for " + studentId + " set to " + newStatus + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** One line about a student: ID, name, programme, year and QCA. */
    private String describe(Student student) {
        String id = student.getId();
        Registration registration = registrationService.findRegistration(id);

        String programme = (registration == null)
                ? "not registered"
                : registration.getProgrammeCode() + " year " + registration.getCurrentYear()
                        + " (" + registration.getStatus() + ")";

        return id + "  " + student.getName() + "  " + programme
                + "  QCA " + formatQca(transcriptService.overallQca(id));
    }

    /** Lists the student's failed (deficient) and D1 modules, if any. */
    private void printModulesToReview(String studentId) {
        for (ModuleResult result : transcriptService.getStandingResults(studentId)) {
            if (result.isDeficient() || result.isCompensatingFail()) {
                System.out.println("    " + result.getModule().getCode() + "  "
                        + result.getModule().getTitle() + "  " + result.getGrade());
            }
        }
    }

    private String formatQca(double qca) {
        return String.format(Locale.ROOT, "%.2f", qca);
    }
}
