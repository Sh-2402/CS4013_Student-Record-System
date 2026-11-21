package main.repository;

import java.util.ArrayList;
import java.util.List;
import main.model.Registration;

public class RegistrationRepository {

    private final String filePath;

    public RegistrationRepository(String filePath) {
        this.filePath = filePath;
    }

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


    private static final String[] HEADER =
            {"studentId", "programmeCode", "startYear", "currentYear", "status"};

    private final String filePath;

    public RegistrationRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads every registration from the file. Broken lines are skipped.
     *
     * @return all registrations
     */
    public List<Registration> loadAll() {
        List<String[]> rows = CsvUtil.readAll(filePath);
        List<Registration> registrations = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {      // start at 1 to skip the header
            String[] r = rows.get(i);
            if (r.length < 5) {                        // skip blank or broken lines
                continue;
            }

            try {
                registrations.add(new Registration(r[0].trim(), r[1].trim(),
                        Integer.parseInt(r[2].trim()), Integer.parseInt(r[3].trim()), r[4].trim()));
            } catch (IllegalArgumentException e) {     // bad number or status
                System.err.println("Skipping bad line " + (i + 1) + " in " + filePath);
            }
        }
        return registrations;
    }

    /**
     * Adds a new registration to the file.
     *
     * @param r the registration to add
     */
    public void save(Registration r) {
        List<Registration> registrations = loadAll();
        registrations.add(r);
        writeAll(registrations);
    }

    /**
     * Replaces the saved registration for the same student, for example
     * after advanceYear() or changeStatus().
     *
     * @param r the changed registration
     * @throws IllegalArgumentException if the student has no saved registration
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
                "No registration found for student " + r.getStudentId());
    }

    /**
     * Finds a student's registration.
     *
     * @param id the student ID
     * @return the registration, or null if the student is not registered
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
     * Finds every registration on a programme.
     *
     * @param code the programme code
     * @return the registrations on that programme (empty if none)
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

    /** Writes the header and all registrations back to the file. */
    private void writeAll(List<Registration> registrations) {
        List<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (Registration r : registrations) {
            rows.add(new String[]{r.getStudentId(), r.getProgrammeCode(),
                    String.valueOf(r.getStartYear()), String.valueOf(r.getCurrentYear()),
                    r.getStatus()});
        }
        CsvUtil.writeAll(filePath, rows);
    }
}
