
package main.repository;

import java.util.ArrayList;
import java.util.List;
import main.model.Registration;

/**
 * Stores student registrations in registrations.csv.
 *
 * File format: studentId,programmeCode,startYear,currentYear,status
 */
public class RegistrationRepository {

    private static final String[] HEADER = {
        "studentId", "programmeCode", "startYear", "currentYear", "status"
    };

    private final String filePath;

    public RegistrationRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads every registration from the file.
     * Invalid records are skipped.
     */
    public List<Registration> loadAll() {
        List<String[]> rows = CsvUtil.readAll(filePath);
        List<Registration> registrations = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);

            if (r.length != 5) {
                System.err.println("Skipping invalid line " + (i + 1));
                continue;
            }

            try {
                registrations.add(new Registration(
                    r[0].trim(),
                    r[1].trim(),
                    Integer.parseInt(r[2].trim()),
                    Integer.parseInt(r[3].trim()),
                    r[4].trim()
                ));
            } catch (IllegalArgumentException e) {
                System.err.println(
                    "Skipping bad line " + (i + 1) + " in " + filePath
                );
            }
        }

        return registrations;
    }

    /**
     * Adds a new registration to the file.
     */
    public void save(Registration r) {
        List<Registration> registrations = loadAll();
        registrations.add(r);
        writeAll(registrations);
    }

    /**
     * Updates an existing student's registration.
     */
    public void update(Registration r) {
        List<Registration> registrations = loadAll();

        for (int i = 0; i < registrations.size(); i++) {
            if (registrations.get(i).getStudentId().equals(r.getStudentId())) {
                registrations.set(i, r);
                writeAll(registrations);
                return;
            }
        }

        throw new IllegalArgumentException(
            "No registration found for student " + r.getStudentId()
        );
    }

    /**
     * Finds a registration using the student ID.
     */
    public Registration findByStudent(String id) {
        for (Registration r : loadAll()) {
            if (r.getStudentId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Finds all registrations for a particular programme.
     */
    public List<Registration> findByProgramme(String code) {
        List<Registration> onProgramme = new ArrayList<>();

        for (Registration r : loadAll()) {
            if (r.getProgrammeCode().equals(code)) {
                onProgramme.add(r);
            }
        }

        return onProgramme;
    }

    /**
     * Writes all registrations to the CSV file.
     */
    private void writeAll(List<Registration> registrations) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (Registration r : registrations) {
            rows.add(new String[] {
                r.getStudentId(),
                r.getProgrammeCode(),
                String.valueOf(r.getStartYear()),
                String.valueOf(r.getCurrentYear()),
                r.getStatus()
            });
        }

        CsvUtil.writeAll(filePath, rows);
    }
}
