
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

        for (int i = 1; i < rows.size(); i++) { // start at 1 to skip the header
            String[] r = rows.get(i);
            if (r.length < 5) { // skip blank or broken lines
                continue;
            }

            Registration reg = new Registration(r[0], r[1], Integer.parseInt(r[2]), Integer.parseInt(r[3]), r[4]);
            registrations.add(reg);
        }

        return registrations;
    }

    public Registration findByStudent(String studentId) {
        List<Registration> registrations = loadAll();
        for (Registration reg : registrations) {
            if (reg.getStudentId().equals(studentId)) {
                return reg;
            }
        }
        return null; // Return null if no registration with the given studentId is found
    }

    public void save(Registration r) {
        List<Registration> registrations = loadAll();
        registrations.add(r);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "studentId", "programmeCode", "startYear", "currentYear", "status" }); // header
        for (Registration reg : registrations) {
            rows.add(new String[] { reg.getStudentId(), reg.getProgrammeCode(), String.valueOf(reg.getStartYear()),
                    String.valueOf(reg.getCurrentYear()), reg.getStatus() });
        }
        CsvUtil.writeAll(filePath, rows);
    }

    public List<Registration> findByProgramme(String programmeCode) {
        List<Registration> registrations = loadAll();
        List<Registration> result = new ArrayList<>();
        for (Registration reg : registrations) {
            if (reg.getProgrammeCode().equals(programmeCode)) {
                result.add(reg);
            }
        }
        return result;
    }

    public void update(Registration updated) {
        List<Registration> registrations = loadAll();

        for (int i = 0; i < registrations.size(); i++) {
            if (registrations.get(i).getStudentId().equals(updated.getStudentId())) {
                registrations.set(i, updated);
            }
        }

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "studentId", "programmeCode", "startYear", "currentYear", "status" });
        for (Registration reg : registrations) {
            rows.add(new String[] { reg.getStudentId(), reg.getProgrammeCode(), String.valueOf(reg.getStartYear()),
                    String.valueOf(reg.getCurrentYear()), reg.getStatus() });
        }
        CsvUtil.writeAll(filePath, rows);
    }

}
