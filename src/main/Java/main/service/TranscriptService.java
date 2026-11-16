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
 * Works out QCA values and builds transcripts for a student.
 *
 * results.csv holds every attempt at every module, including repeats.
 * QcaCalculator expects only the grade that stands for each module, so
 * this service first works that out (SP 3.2.6):
 *
 * - The first attempt is the original.
 * - Every later attempt is treated as an annual repeat, so its grade is
 *   capped at C3 (Grade.capAtC3).
 * - The higher of the original and the capped repeat stands
 *   (Grade.higherOf). An I grade during a repeat leaves the original.
 *
 * Assumptions:
 * - Link-in repeats (SP 3.2.6.2) and repeats of a full semester or year
 *   (Table 4.1, "No cap" participation) are not capped by the handbook,
 *   but are not told apart from annual repeats here, so they are capped
 *   at C3 too.
 * - The standing grade is counted in the year and semester of the
 *   ORIGINAL attempt. Table 2.2 describes grade R as "grade for repeat
 *   inserted in previous attempt", so an improved grade replaces the
 *   original in the original semester, and that semester's QCA changes
 *   with it. The transcript marks such modules "(repeated)".
 */
public class TranscriptService {

    private final ResultsService resultsService;
    private final QcaCalculator calculator;
    private final Function<String, Student> studentLookup;

    public TranscriptService(ResultsService resultsService,
                             QcaCalculator calculator,
                             Function<String, Student> studentLookup) {

        this.resultsService = Objects.requireNonNull(resultsService);
        this.calculator = Objects.requireNonNull(calculator);
        this.studentLookup = Objects.requireNonNull(studentLookup);
    }

    // ---------- Public API ----------

    /** QCA for one student in one academic year and semester. */
    public double semesterQca(String studentId, int year, int semester) {
        return calculator.semesterQca(
                getStandingResults(studentId), year, semester);
    }

    /** Overall QCA for one student, using the grades that stand. */
    public double overallQca(String studentId) {
        return calculator.overallQca(getStandingResults(studentId));
    }

    /**
     * Returns one result per module: the grade that stands after
     * the repeat rules are applied.
     */
    public List<ModuleResult> getStandingResults(String studentId) {
        List<ModuleResult> standing = new ArrayList<>();

        for (Standing s : standingEntries(studentId)) {
            standing.add(s.result);
        }
        return standing;
    }

    /**
     * Builds a printable transcript grouped by year and semester.
     *
     * @throws IllegalArgumentException if the student does not exist
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

        // year -> semester -> entries (TreeMap keeps them in order)
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

    

