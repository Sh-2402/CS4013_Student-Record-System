package main.service;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;
import main.repository.ResultRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Saves new module results and gets them back.
 * Before saving, it checks the student is registered, the module exists,
 * the semester is right, the result isn't a duplicate, and the module
 * is allowed to be repeated. If a check fails it throws an
 * IllegalArgumentException, so the menu can show the message.
 *
 * A module can only be taken again if the grade that stands has no
 * credits, or is D1 (AR 1.3.4, AR 1.4.17).
 *
 * Assumptions:
 * - A repeat is saved under a later year, since the same year and
 *   semester can't be saved twice (in reality repeats are in the summer,
 *   AR 1.4.16).
 * - Each module runs in one semester only.
 * - The grade isn't checked against the module type, because Module
 *   has no pass/fail flag.
 * - Using the lecturer version, a lecturer can only give results for
 *   their own modules, and can't give G, EX, R, RE, W, X or Y
 *   (SP 3.2.2.5). The version without a lecturer ID skips these checks
 *   (for admins and tests).
 */
public class ResultsService {

    /** Administrative grades that a lecturer may not award. */
    private static final Set<Grade> NOT_AWARDED_BY_LECTURER = EnumSet.of(
            Grade.G, Grade.EX, Grade.R, Grade.RE,
            Grade.W, Grade.X, Grade.Y);

    private final ResultRepository repository;
    private final Function<String, Student> studentLookup;
    private final Function<String, Module> moduleLookup;
    private final Predicate<Student> isRegistered;

    /**
     * Sets up the service with where results are stored and how to look 
     * up students and modules.
     * 
     * @param repository where results are stored
     * @param studentLookup finds a Student by ID (null if none)
     * @param moduleLookup finds a Module by code (null if none)
     * @param isRegistered says whether a student is registered on a
     *                     programme (will use RegistrationService)
     */
    public ResultsService(
            ResultRepository repository,
            Function<String, Student> studentLookup,
            Function<String, Module> moduleLookup,
            Predicate<Student> isRegistered) {

        this.repository = Objects.requireNonNull(repository, "Repository must not be null");
        this.studentLookup = Objects.requireNonNull(studentLookup, "Student lookup must not be null");
        this.moduleLookup = Objects.requireNonNull(moduleLookup, "Module lookup must not be null");
        this.isRegistered = Objects.requireNonNull(isRegistered, "isRegistered predicate must not be null");
    }

    // ---------- Recording ----------

    /**
     * Checks a result is valid and then saves it.
     *
     * @param studentId the student's ID
     * @param moduleCode the module code, e.g. CS4221
     * @param year the starting year of the academic year, e.g. 2026
     * @param semester the semester (1 or 2)
     * @param grade the grade to record
     * @return the saved result
     * @throws IllegalArgumentException if any check fails
     */
    public ModuleResult recordResult(String studentId, String moduleCode,
                                     int year, int semester, Grade grade) {

        if (grade == null) {
            throw new IllegalArgumentException("Grade must be given");
        }

        Student student = studentLookup.apply(studentId);
        if (student == null) {
            throw new IllegalArgumentException(
                    "No student with ID " + studentId);
        }

        if (!isRegistered.test(student)) {
            throw new IllegalArgumentException(
                    "Student " + studentId
                    + " is not registered on a programme");
        }

        Module module = moduleLookup.apply(moduleCode);
        if (module == null) {
            throw new IllegalArgumentException(
                    "No module with code " + moduleCode);
        }

        if (module.getSemester() != semester) {
            throw new IllegalArgumentException(
                    moduleCode + " runs in semester "
                    + module.getSemester() + ", not " + semester);
        }

        for (ModuleResult existing
                : repository.findByStudent(studentId, year, semester)) {

            if (existing.getModule().getCode().equals(moduleCode)) {
                throw new IllegalArgumentException(
                        "A result for " + moduleCode + " in " + year
                        + " semester " + semester
                        + " is already recorded for this student");
            }
        }

        // A module that already has credits cannot be attempted again,
        // except D1, which carries credits but can be repeated.
        List<ModuleResult> earlier = new ArrayList<>();
        for (ModuleResult r : repository.findByStudent(studentId)) {
            if (r.getModule().getCode().equals(moduleCode)) {
                earlier.add(r);
            }
        }

        if (!earlier.isEmpty()) {
            Grade standing = standingGrade(earlier);

            if (standing.isCreditsAwarded() && standing != Grade.D1) {
                throw new IllegalArgumentException(
                        "Student " + studentId + " already has credits for "
                        + moduleCode + " (grade " + standing
                        + "), so it cannot be attempted again");
            }
        }

        // ModuleResult itself rejects a bad year or semester
        ModuleResult result =
                new ModuleResult(student, module, year, semester, grade);

        repository.save(result);
        return result;
    }

    /**
     * Same as the other recordResult, but first checks the lecturer
     * teaches the module and isn't giving an admin grade.
     *
     * @param lecturerId the ID of the lecturer giving the result
     * @param studentId the student's ID
     * @param moduleCode the module code, e.g. CS4221
     * @param year the starting year of the academic year, e.g. 2026
     * @param semester the semester (1 or 2)
     * @param grade the grade to record
     * @return the saved result
     * @throws IllegalArgumentException if the lecturer doesn't teach the
     *module, the grade is an admin grade, or any other check fails
     */
    public ModuleResult recordResult(String lecturerId, String studentId,
                                     String moduleCode, int year,
                                     int semester, Grade grade) {

        if (lecturerId == null || lecturerId.isBlank()) {
            throw new IllegalArgumentException("Lecturer ID must be given");
        }

        Module module = moduleLookup.apply(moduleCode);
        if (module != null
                && !lecturerId.equals(module.getLecturerId())) {
            throw new IllegalArgumentException(
                    "Module " + moduleCode
                    + " is not taught by lecturer " + lecturerId);
        }

        if (NOT_AWARDED_BY_LECTURER.contains(grade)) {
            throw new IllegalArgumentException(
                    "Grade " + grade + " cannot be awarded by a lecturer "
                    + "(it is an administrative grade)");
        }

        // An unknown module falls through to the normal check below.
        return recordResult(studentId, moduleCode, year, semester, grade);
    }

    /**
     * Finds which grade stands after all attempts at one module.
     * Repeats are capped at C3 and the better grade is kept (SP 3.2.6),
     * the same rule as TranscriptService.
     *
     * @param attempts all earlier attempts at the module
     * @return the grade that stands
     */
    private Grade standingGrade(List<ModuleResult> attempts) {
        List<ModuleResult> ordered = new ArrayList<>(attempts);
        ordered.sort(Comparator
                .comparingInt(ModuleResult::getYear)
                .thenComparingInt(ModuleResult::getSemester));

        Grade standing = ordered.get(0).getGrade();

        for (int i = 1; i < ordered.size(); i++) {
            standing = Grade.higherOf(
                    standing, ordered.get(i).getGrade().capAtC3());
        }
        return standing;
    }

    // ---------- Finding ----------

    /** Gets every result for a student, including repeats.
     * 
     * @param studentId the student's ID.
     * @return all the student's results
     */
    public List<ModuleResult> getResults(String studentId) {
        return repository.findByStudent(studentId);
    }
}

