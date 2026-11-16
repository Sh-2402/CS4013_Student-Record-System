import main.ui.DepartmentMenu;
import main.ui.FacultyMenu;
import main.ui.InputHelper;
import main.ui.LoginMenu;
import main.ui.StudentMenu;

/**
 * Entry point for the UL Student Records System.
 *
 * <p>Main builds the command-line shell: it creates one shared
 * {@link InputHelper}, asks the user to log in, and sends them to the
 * menu for their role (student, faculty or department). When the user
 * logs out they return to the start screen.</p>
 *
 * <p>Run the application from the project root folder so that the
 * {@code Data/} CSV files can be found.</p>
 */
public class Main {

    /** Role names as stored in Data/users.csv. */
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_FACULTY = "FACULTY";
    private static final String ROLE_DEPARTMENT = "DEPARTMENT";

    private final InputHelper input;
    private final LoginMenu loginMenu;
    private final StudentMenu studentMenu;
    private final FacultyMenu facultyMenu;
    private final DepartmentMenu departmentMenu;

    /**
     * Creates the shell. A single InputHelper is shared by every menu,
     * because several Scanners reading System.in can lose input.
     */
    public Main() {
        input = new InputHelper();
        loginMenu = new LoginMenu(input);
        studentMenu = new StudentMenu(input);
        facultyMenu = new FacultyMenu(input);
        departmentMenu = new DepartmentMenu(input);
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
     * Sends a logged-in user to the menu for their role.
     *
     * @param userId the ID the user logged in with
     * @param role   the user's role from users.csv
     */
    private void openMenuForRole(String userId, String role) {
        switch (role) {
            case ROLE_STUDENT -> studentMenu.show(userId);
            case ROLE_FACULTY -> facultyMenu.show(userId);
            case ROLE_DEPARTMENT -> departmentMenu.show(userId);
            default -> System.out.println("Unknown role '" + role
                    + "' for user " + userId + ". Contact an administrator.");
        }
    }
}
