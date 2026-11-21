import main.model.Registration;
import main.model.Student;
import main.repository.ModuleRepository;
import main.repository.ProgrammeRepository;
import main.repository.RegistrationRepository;
import main.repository.ResultRepository;
import main.repository.StudentRepository;
import main.rules.QcaCalculator;
import main.service.ProgrammeService;
import main.service.ProgressionService;
import main.service.RegistrationService;
import main.service.ResultsService;
import main.service.StudentService;
import main.service.TranscriptService;
import main.ui.DepartmentMenu;
import main.ui.FacultyMenu;
import main.ui.InputHelper;
import main.ui.LoginMenu;
import main.ui.StudentMenu;

import java.nio.file.Path;

/**
 * Entry point for the UL Student Records System.
 *
 * <p>Main builds the command-line shell. It creates one shared
 * {@link InputHelper} and the services every menu needs, asks the user to
 * log in, and opens the menu for their role (student, faculty or
 * department). When the user logs out they return to the start screen.</p>
 *
 * <p>Run the application from the project root folder so that the
 * {@code Data/} CSV files can be found.</p>
 */
public class Main {

    /** Role names as stored in Data/users.csv. */
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_FACULTY = "FACULTY";
    private static final String ROLE_DEPARTMENT = "DEPARTMENT";

    private static final String STUDENTS_FILE = "Data/students.csv";
    private static final String PROGRAMMES_FILE = "Data/programme.csv";
    private static final String MODULES_FILE = "Data/modules.csv";
    private static final String REGISTRATIONS_FILE = "Data/registrations.csv";
    private static final String RESULTS_FILE = "Data/results.csv";

    private final InputHelper input;
    private final LoginMenu loginMenu;
    private final StudentService studentService;
    private final ProgrammeService programmeService;
    private final RegistrationService registrationService;
    private final ResultsService resultsService;
    private final TranscriptService transcriptService;
    private final ProgressionService progressionService;

    /**
     * Creates the shell and the shared services.
     *
     * <p>A single InputHelper is shared by every menu, because several
     * Scanners reading System.in can lose input.</p>
     */
    public Main() {
        input = new InputHelper();
        loginMenu = new LoginMenu(input);

        // One StudentRepository is shared, so a student added in the
        // department menu is seen everywhere straight away.
        StudentRepository studentRepository = new StudentRepository(STUDENTS_FILE);
        studentService = new StudentService(studentRepository);

        programmeService = new ProgrammeService(
                new ProgrammeRepository(PROGRAMMES_FILE), new ModuleRepository(MODULES_FILE));

        registrationService = new RegistrationService(
                new RegistrationRepository(REGISTRATIONS_FILE), studentService, programmeService);

        ResultRepository resultRepository = new ResultRepository(
                Path.of(RESULTS_FILE), studentService::findById, programmeService::findModule);

        // Results can only be recorded for students with an active registration.
        resultsService = new ResultsService(resultRepository,
                studentService::findById, programmeService::findModule, this::isRegistered);

        transcriptService = new TranscriptService(
                resultsService, new QcaCalculator(), studentService::findById);

        progressionService = new ProgressionService(
                studentRepository, transcriptService, registrationService, programmeService);
    }

    /**
     * Starts the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new Main().run();
    }

    /**
     * Shows the start screen until the user chooses to exit.
     */
    public void run() {
        System.out.println("=================================");
        System.out.println("   UL Student Records System");
        System.out.println("=================================");

        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("1. Log in");
            System.out.println("2. Exit");

            int choice = input.readChoice(1, 2);

            if (choice == 1) {
                String[] user = loginMenu.login();

                if (user != null) {
                    openMenuForRole(user[0], user[1]);
                }
            } else {
                running = false;
            }
        }

        System.out.println("Goodbye.");
    }

    /**
     * Opens the menu for a logged-in user's role. Each menu is created for
     * the user who logged in, so it only works with that user's ID.
     *
     * @param userId the ID the user logged in with
     * @param role   the user's role from users.csv
     */
    private void openMenuForRole(String userId, String role) {
        switch (role) {
            case ROLE_STUDENT ->
                    new StudentMenu(input, transcriptService, userId).start();
            case ROLE_FACULTY ->
                    new FacultyMenu(input, resultsService, transcriptService, userId).start();
            case ROLE_DEPARTMENT ->
                    new DepartmentMenu(input, progressionService, transcriptService, studentService,
                            programmeService, registrationService, userId).start();
            default -> System.out.println("Unknown role '" + role
                    + "' for user " + userId + ". Contact an administrator.");
        }
    }

    /**
     * Returns whether a student has a registration that is not withdrawn.
     *
     * @param student the student
     * @return true if the student can have results recorded
     */
    private boolean isRegistered(Student student) {
        Registration registration = registrationService.findRegistration(student.getId());
        return registration != null && !"WITHDRAWN".equals(registration.getStatus());
    }
}
