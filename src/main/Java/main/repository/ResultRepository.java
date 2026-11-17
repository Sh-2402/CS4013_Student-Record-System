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
 * Saves and loads module results in results.csv.
 *
 * File format (one result per line, after a header row):
 * studentId,moduleCode,year,semester,grade
 *
 * All reading and writing goes through CsvUtil, so this class never
 * touches the file directly (except to check it exists and to create the
 * folder). CsvUtil has no "append" method, so save() reads every row,
 * adds the new one and writes the whole file back. That is fine for the
 * size of file used in this project.
 *
 * A CSV line only holds IDs, but a ModuleResult needs real Student and
 * Module objects. So this class is given two lookup functions, one for
 * students and one for modules, and uses them to rebuild each result.
 *
 * Every attempt is stored, including repeats. Deciding which grade
 * stands is done later, in TranscriptService.
 *
 * Lines that cannot be read (wrong number of columns, unknown student or
 * module, bad grade, year or semester) are skipped with a warning on
 * System.err rather than stopping the whole load. They are kept in the
 * file when it is rewritten, so nothing is lost.
 *
 * Load order: results are rebuilt in the constructor, so the student and
 * module lookups must already be able to find every student and module.
 * Create the student and module repositories first (in Main), then this one.
 */
public class ResultRepository {

    private static final String HEADER =
            "studentId,moduleCode,year,semester,grade";

    private final Path file;
    private final Function<String, Student> studentLookup;
    private final Function<String, Module> moduleLookup;
    private final List<ModuleResult> results = new ArrayList<>();

    /**
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

    /** Returns every result in the file. */
    public List<ModuleResult> findAll() {
        return new ArrayList<>(results);
    }

    /** Returns all results for one student. */
    public List<ModuleResult> findByStudent(String studentId) {
        List<ModuleResult> found = new ArrayList<>();

        for (ModuleResult r : results) {
            if (r.getStudent().getId().equals(studentId)) {
                found.add(r);
            }
        }
        return found;
    }

    /** Returns one student's results for one academic year. */
    public List<ModuleResult> findByStudent(String studentId, int year) {
        List<ModuleResult> found = new ArrayList<>();

        for (ModuleResult r : findByStudent(studentId)) {
            if (r.getYear() == year) {
                found.add(r);
            }
        }
        return found;
    }

    /** Returns one student's results for one year and semester. */
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

