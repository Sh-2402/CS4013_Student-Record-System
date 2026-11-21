import main.model.Module;
import main.repository.CsvUtil;
import main.repository.ResultRepository;
import main.repository.StudentRepository;
import main.rules.QcaCalculator;
import main.service.ResultsService;
import main.service.StudentService;
import main.service.TranscriptService;
import main.ui.DepartmentMenu;
import main.ui.FacultyMenu;
import main.ui.InputHelper;
import main.ui.LoginMenu;
import main.ui.StudentMenu;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String MODULES_FILE = "Data/modules.csv";
    private static final String RESULTS_FILE = "Data/results.csv";

    private final InputHelper input;
    private final LoginMenu loginMenu;
    private final ResultsService resultsService;
    private final TranscriptService transcriptService;

    /**
     * Creates the shell and the shared services.
     *
     * <p>A single InputHelper is shared by every menu, because several
     * Scanners reading System.in can lose input.</p>
     */
    public Main() {
        input = new InputHelper();
        loginMenu = new LoginMenu(input);

        StudentService studentService = new StudentService(new StudentRepository());
        Map<String, Module> modules = loadModules();

        ResultRepository resultRepository = new ResultRepository(
                Path.of(RESULTS_FILE), studentService::findById, modules::get);

        // TODO: replace "s -> true" with a registration check once
        // RegistrationService exists (Austin). Students are loaded without
        // a Registration for now, so the default check would reject them all.
        resultsService = new ResultsService(
                resultRepository, studentService::findById, modules::get, s -> true);

        transcriptService = new TranscriptService(
                resultsService, new QcaCalculator(), studentService::findById);
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
                    new DepartmentMenu(input, userId).start();
            default -> System.out.println("Unknown role '" + role
                    + "' for user " + userId + ". Contact an administrator.");
        }
    }

    /**
     * Loads modules.csv into a map keyed by module code.
     *
     * <p>TEMPORARY: replace with ModuleRepository (Austin) once it exists.</p>
     *
     * @return modules by code
     */
    private static Map<String, Module> loadModules() {
        Map<String, Module> modules = new HashMap<>();
        List<String[]> rows = CsvUtil.readAll(MODULES_FILE);

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (row.length != 5) {
                continue;
            }

            try {
                Module module = new Module(row[0].trim(), row[1].trim(),
                        Integer.parseInt(row[2].trim()),
                        Integer.parseInt(row[3].trim()), row[4].trim());
                modules.put(module.getCode(), module);
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping bad module line " + (i + 1)
                        + " in " + MODULES_FILE + ": " + CsvUtil.joinFields(row));
            }
        }

        return modules;
    }
}
