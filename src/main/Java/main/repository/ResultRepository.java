package main.repository;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The only class that reads and writes results.csv.
 * Each line is one result: studentId,moduleCode,year,semester,grade.
 * When the program starts, every line is turned into a ModuleResult.
 * A line only has IDs (like 101 and CS4221), so two lookup functions
 * are used to turn those IDs into real Student and Module objects.
 * A bad line (unknown student or module, wrong grade, missing column)
 * is skipped with a warning, so one mistake doesn't crash the program.
 * Saving adds the new result and writes the whole file again.
 * Every attempt is stored, including repeats; TranscriptService decides
 * which grade counts.
 */
public class ResultRepository {

    private static final String HEADER =
            "studentId,moduleCode,year,semester,grade";

    private final Path file;
    private final Function<String, Student> studentLookup;
    private final Function<String, Module> moduleLookup;
    private final List<ModuleResult> results = new ArrayList<>();

    /**
     * Sets up the repository and loads all results from the file.
     * 
     * @param file path to results.csv (for example data/results.csv)
     * @param studentLookup function to look up a Student by ID (returns null if not found)
     * @param moduleLookup function to look up a Module by code (returns null if not found)
     */
    public ResultRepository(Path file,
                            Function<String, Student> studentLookup,
                            Function<String, Module> moduleLookup) {

        this.file = Objects.requireNonNull(file, "File must not be null");
        this.studentLookup = Objects.requireNonNull(
                studentLookup, "Student lookup must not be null");
        this.moduleLookup = Objects.requireNonNull(
                moduleLookup, "Module lookup must not be null");

        load();
    }

    // ---------- Loading ----------

    /** Reads results.csv. Creates it (header only) if it does not exist. */
    private void load() {
        results.clear();

        if (!Files.exists(file)) {
            writeRows(new ArrayList<>());
            return;
        }

        List<String[]> rows = CsvUtil.readAll(file.toString());

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);

            if (isBlankRow(row)) {
                continue;
            }

            // Skip the header row, but do not lose a real result if
            // the file was written by hand without one.
            if (i == 0 && isHeader(row)) {
                continue;
            }

            ModuleResult result = parseRow(row);

            if (result == null) {
                System.err.println("Skipping bad line " + (i + 1)
                        + " in " + file + ": " + CsvUtil.joinFields(row));
            } else {
                results.add(result);
            }
        }
    }

    /** Turns one CSV row into a ModuleResult, or null if it is invalid. */
    private ModuleResult parseRow(String[] parts) {

        if (parts.length != 5) {
            return null;
        }

        try {
            Student student = studentLookup.apply(parts[0].trim());
            Module module = moduleLookup.apply(parts[1].trim());

            if (student == null || module == null) {
                return null;
            }

            int year = Integer.parseInt(parts[2].trim());
            int semester = Integer.parseInt(parts[3].trim());
            Grade grade = Grade.valueOf(parts[4].trim().toUpperCase());

            return new ModuleResult(student, module, year, semester, grade);

        } catch (IllegalArgumentException e) {
            // covers bad numbers, unknown grade names, invalid year/semester
            return null;
        }
    }

    private static boolean isBlankRow(String[] row) {
        return row.length == 0
                || (row.length == 1 && row[0].trim().isEmpty());
    }

    private static boolean isHeader(String[] row) {
        return CsvUtil.joinFields(row).trim().equalsIgnoreCase(HEADER);
    }

    // ---------- Saving ----------

    /**
     * Adds a result to memory and to results.csv.
     *
     * @param result the result to save
     */
    public void save(ModuleResult result) {
        Objects.requireNonNull(result, "Result must not be null");

        // Keep every existing data row (without the header or blank lines).
        List<String[]> dataRows = new ArrayList<>();

        if (Files.exists(file)) {
            List<String[]> rows = CsvUtil.readAll(file.toString());

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);

                if (isBlankRow(row) || (i == 0 && isHeader(row))) {
                    continue;
                }
                dataRows.add(row);
            }
        }

        dataRows.add(new String[] {
                result.getStudent().getId(),
                result.getModule().getCode(),
                String.valueOf(result.getYear()),
                String.valueOf(result.getSemester()),
                result.getGrade().name()
        });

        writeRows(dataRows);
        results.add(result);
    }

    /** Writes the header plus the given data rows, creating the folder if needed. */
    private void writeRows(List<String[]> dataRows) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create folder for " + file, e);
        }

        List<String[]> all = new ArrayList<>();
        all.add(CsvUtil.splitLine(HEADER));
        all.addAll(dataRows);

        CsvUtil.writeAll(file.toString(), all);
    }

    // ---------- Finding ----------

    /**
     * Gets every result in the file.
     *
     * @return a copy of all results
     */
    public List<ModuleResult> findAll() {
        return new ArrayList<>(results);
    }

    /**
     * Gets all results for one student.
     *
     * @param studentId the student's ID
     * @return the student's results, including repeats
     */
    public List<ModuleResult> findByStudent(String studentId) {
        List<ModuleResult> found = new ArrayList<>();

        for (ModuleResult r : results) {
            if (r.getStudent().getId().equals(studentId)) {
                found.add(r);
            }
        }
        return found;
    }

    /**
     * Gets one student's results for one academic year.
     *
     * @param studentId the student's ID
     * @param year the starting year of the academic year, e.g. 2026
     * @return the student's results for that year
     */
    public List<ModuleResult> findByStudent(String studentId, int year) {
        List<ModuleResult> found = new ArrayList<>();

        for (ModuleResult r : findByStudent(studentId)) {
            if (r.getYear() == year) {
                found.add(r);
            }
        }
        return found;
    }

    /**
     * Gets one student's results for one year and semester.
     *
     * @param studentId the student's ID
     * @param year the starting year of the academic year, e.g. 2026
     * @param semester the semester (1 or 2)
     * @return the student's results for that semester
     */
    public List<ModuleResult> findByStudent(
            String studentId, int year, int semester) {

        List<ModuleResult> found = new ArrayList<>();

        for (ModuleResult r : findByStudent(studentId, year)) {
            if (r.getSemester() == semester) {
                found.add(r);
            }
        }
        return found;
    }
}

