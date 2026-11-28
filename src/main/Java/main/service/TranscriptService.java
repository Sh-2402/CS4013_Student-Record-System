package main.service;

import main.model.Grade;
import main.model.ModuleResult;
import main.model.Student;
import main.rules.QcaCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Works out a student's QCA and builds their transcript.
 * results.csv keeps every attempt, but QcaCalculator only wants the grade
 * that counts for each module, so this class sorts out repeats first
 * (SP 3.2.6): repeats are capped at C3 and the better grade is kept.
 *
 * Assumptions:
 * - Every repeat is treated as an annual repeat and capped at C3, even
 *   link-in repeats, which the handbook doesn't cap, because the program
 *   can't tell them apart.
 * - The grade that counts stays in the year and semester of the first
 *   attempt (like grade R in Table 2.2), so that semester's QCA can change.
 *   These modules are marked "(repeated)" on the transcript.
 */
public class TranscriptService {

    private final ResultsService resultsService;
    private final QcaCalculator calculator;
    private final Function<String, Student> studentLookup;
     
     /**
     * Sets up the service with the things it needs.
     *
     * @param resultsService where the student's results come from
     * @param calculator does the QCA maths
     * @param studentLookup finds a student by their ID
     */
    public TranscriptService(ResultsService resultsService,
                             QcaCalculator calculator,
                             Function<String, Student> studentLookup) {

        this.resultsService = Objects.requireNonNull(resultsService);
        this.calculator = Objects.requireNonNull(calculator);
        this.studentLookup = Objects.requireNonNull(studentLookup);
    }

    // ---------- Public API ----------

    /**
     * Gets a student's QCA for one semester.
     *
     * @param studentId the student's ID
     * @param year the starting year of the academic year, e.g. 2026
     * @param semester the semester (1 or 2)
     * @return the QCA for that semester
     */
    public double semesterQca(String studentId, int year, int semester) {
        return calculator.semesterQca(
                getStandingResults(studentId), year, semester);
    }

    /**
     * Gets a student's overall QCA, after repeats are sorted out.
     *
     * @param studentId the student's ID
     * @return the student's QCA across all their results
     */
    public double overallQca(String studentId) {
        return calculator.overallQca(getStandingResults(studentId));
    }

    /**
     * Gets one result per module, with the grade that counts after repeats.
     *
     * @param studentId the student's ID
     * @return one result per module, with the grade that counts after repeats
     */
    public List<ModuleResult> getStandingResults(String studentId) {
        List<ModuleResult> standing = new ArrayList<>();

        for (Standing s : standingEntries(studentId)) {
            standing.add(s.result);
        }
        return standing;
    }

    /**
     * Builds the transcript as text, sorted by year and semester.
     *
     * @param studentId the student's ID
     * @return the transcript as text, ready to print
     * @throws IllegalArgumentException if the student isn't found
     */
    public String buildTranscript(String studentId) {

        Student student = studentLookup.apply(studentId);
        if (student == null) {
            throw new IllegalArgumentException(
                    "No student with ID " + studentId);
        }

        List<Standing> entries = standingEntries(studentId);

        StringBuilder sb = new StringBuilder();
        sb.append("TRANSCRIPT\n");
        sb.append("Student: ").append(student.getName())
          .append(" (ID ").append(student.getId()).append(")\n");

        if (entries.isEmpty()) {
            sb.append("\nNo results recorded.\n");
            return sb.toString();
        }

        // Group by year, then semester (TreeMap keeps them in order)
        Map<Integer, Map<Integer, List<Standing>>> grouped = new TreeMap<>();
        for (Standing s : entries) {
            grouped.computeIfAbsent(s.result.getYear(),
                        y -> new TreeMap<>())
                   .computeIfAbsent(s.result.getSemester(),
                        sem -> new ArrayList<>())
                   .add(s);
        }

        int creditsEarned = 0;

        for (Map.Entry<Integer, Map<Integer, List<Standing>>> year
                : grouped.entrySet()) {

            for (Map.Entry<Integer, List<Standing>> sem
                    : year.getValue().entrySet()) {

                sb.append(String.format("%n%d/%02d - Semester %d%n",
                        year.getKey(), (year.getKey() + 1) % 100,
                        sem.getKey()));

                List<ModuleResult> semesterResults = new ArrayList<>();

                for (Standing s : sem.getValue()) {
                    ModuleResult r = s.result;
                    semesterResults.add(r);
                    creditsEarned += r.getCreditsEarned();

                    sb.append(String.format("  %-8s %-42s %2d cr  %-3s%s%n",
                            r.getModule().getCode(),
                            r.getModule().getTitle(),
                            r.getModule().getCredits(),
                            r.getGrade(),
                            s.repeated ? "  (repeated)" : ""));
                }

                sb.append(String.format(Locale.ROOT,
                        "  Semester QCA: %.2f%n",
                        calculator.semesterQca(semesterResults)));
            }
            
                sb.append(String.format(Locale.ROOT,
                    "  Year QCA: %.2f%n",
                    calculator.yearQca(getStandingResults(studentId),
                            year.getKey())));
        }

        sb.append(String.format(Locale.ROOT,
                "%nOverall QCA: %.2f%n",
                calculator.overallQca(getStandingResults(studentId))));
        sb.append("Credits earned: ").append(creditsEarned).append("\n");

        return sb.toString();
    }

    // ---------- Repeat handling ----------

    /** A standing result, plus whether the module was attempted again. */
    private static class Standing {
        private final ModuleResult result;
        private final boolean repeated;

        private Standing(ModuleResult result, boolean repeated) {
            this.result = result;
            this.repeated = repeated;
        }
    }

    /** Groups attempts by module and works out the standing grade. */
    private List<Standing> standingEntries(String studentId) {

        Map<String, List<ModuleResult>> byModule = new LinkedHashMap<>();

        for (ModuleResult r : resultsService.getResults(studentId)) {
            byModule.computeIfAbsent(r.getModule().getCode(),
                    code -> new ArrayList<>()).add(r);
        }

        List<Standing> entries = new ArrayList<>();
        for (List<ModuleResult> attempts : byModule.values()) {
            entries.add(resolve(attempts));
        }
        return entries;
    }

    /** Applies the repeat rules to all attempts at one module. */
    private Standing resolve(List<ModuleResult> attempts) {

        List<ModuleResult> ordered = new ArrayList<>(attempts);
        ordered.sort(Comparator
                .comparingInt(ModuleResult::getYear)
                .thenComparingInt(ModuleResult::getSemester));

        ModuleResult original = ordered.get(0);
        Grade standing = original.getGrade();

        for (int i = 1; i < ordered.size(); i++) {
            Grade cappedRepeat = ordered.get(i).getGrade().capAtC3();
            standing = Grade.higherOf(standing, cappedRepeat);
        }

        // The standing grade stays in the original year and semester.
        ModuleResult current = (standing == original.getGrade())
                ? original
                : new ModuleResult(
                        original.getStudent(), original.getModule(),
                        original.getYear(), original.getSemester(),
                        standing);

        return new Standing(current, ordered.size() > 1);
    }
}

    

