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
 * Records and retrieves module results.
 *
 * Before a result is saved, this service checks that:
 * - the student exists and is registered on a programme
 * - the module exists
 * - the semester matches the semester the module runs in
 * - the same student/module/year/semester has not already been recorded
 * - a new attempt at a module is only allowed if the grade that currently
 *   stands for it is one that can be repeated
 *
 * Invalid requests throw IllegalArgumentException with a clear message,
 * so a menu can catch it and show the message to the user.
 *
 * Repeat attempts: the same module can be recorded again in a later year,
 * but only when the standing grade carries no credits (F, NG, N, I, I2, M
 * and similar) or is D1, a compensating fail. A module that already has
 * credits cannot be selected again (AR 1.3.4), and annual repeats are
 * for F, NG, D1 and I grades (AR 1.4.17). Deciding which grade stands is
 * done in TranscriptService.
 *
 * Assumptions (to be listed in the project documentation):
 * - A repeat is recorded under a later academic year than the original,
 *   because the same student/module/year/semester cannot be recorded twice.
 *   (Annual repeats are really held in the summer of the same academic
 *   year, AR 1.4.16, so this is a simplification.)
 * - A module runs in one semester only, so the semester must match
 *   Module.getSemester().
 * - The grade is not checked against the module type (for example P or N
 *   in a quality-graded module), because Module has no pass/fail flag.
 * - A lecturer may only submit results for modules they teach
 *   (Module.getLecturerId()), using the overload that takes a lecturer ID.
 * - A lecturer cannot award the administrative grades G, EX, R, RE, W, X
 *   or Y (Table 4.1, SP 3.2.2.5). For example, EX is decided by the
 *   Student Status Committee (SP 3.4.8). Such grades can still appear in
 *   results.csv.
 *   The overload without a lecturer ID does neither of these two checks,
 *   for use by administrators and tests.
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

    /**
     * Same as above, but treats a student as registered when they have a
     * Registration whose status is not WITHDRAWN. ACTIVE and REPEATING
     * students can receive results.
     */
    public ResultsService(
            ResultRepository repository,
            Function<String, Student> studentLookup,
            Function<String, Module> moduleLookup) {

        this(repository, studentLookup, moduleLookup,
                s -> s.getRegistration() != null
                        && !"WITHDRAWN".equals(
                                s.getRegistration().getStatus()));
    }

    // ---------- Recording ----------

    /**
     * Validates and saves a result.
     *
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
     * Same as the version without a lecturer ID, but first checks that
     * the lecturer teaches the module.
     *
     * @param lecturerId ID of the faculty member submitting the result
     * @return the saved result
     * @throws IllegalArgumentException if the module is not taught by this
     *         lecturer, if the grade is one a lecturer cannot award, or if
     *         any other check fails
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
     * Works out the grade that currently stands for earlier attempts at
     * one module: later attempts are capped at C3 and the higher grade
     * stands (SP 3.2.6). TranscriptService applies the same rule.
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

    /** All results for one student (every attempt). */
    public List<ModuleResult> getResults(String studentId) {
        return repository.findByStudent(studentId);
    }

    /** One student's results for one academic year. */
    public List<ModuleResult> getResults(String studentId, int year) {
        return repository.findByStudent(studentId, year);
    }

    /** One student's results for one year and semester. */
    public List<ModuleResult> getResults(
            String studentId, int year, int semester) {

        return repository.findByStudent(studentId, year, semester);
    }
}

