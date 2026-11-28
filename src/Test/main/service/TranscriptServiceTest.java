package main.service;

import static org.junit.jupiter.api.Assertions.*;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;
import main.repository.ResultRepository;
import main.rules.QcaCalculator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TranscriptServiceTest {

    private static final double DELTA = 0.001;
    private static final String ID = "101";

    @TempDir
    Path tempDir;

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();

    private Student ann;
    private ResultRepository repository;
    private TranscriptService service;

    @BeforeEach
    void setUp() {
        ann = new Student("Ann", ID, "ann@ul.ie", null);
        students.put(ID, ann);

        modules.put("CS1", new Module("CS1", "First Module", 6, 1, "L1"));
        modules.put("CS2", new Module("CS2", "Second Module", 6, 1, "L1"));
        modules.put("CS3", new Module("CS3", "Third Module", 6, 2, "L1"));

        repository = new ResultRepository(
                tempDir.resolve("results.csv"), students::get, modules::get);

        ResultsService resultsService = new ResultsService(
                repository, students::get, modules::get, s -> true);

        service = new TranscriptService(
                resultsService, new QcaCalculator(), students::get);
    }

    private void add(String code, int year, int semester, Grade grade) {
        repository.save(new ModuleResult(
                ann, modules.get(code), year, semester, grade));
    }

    /** Returns the grade that stands for a module, or null if none. */
    private Grade standingGrade(String code) {
        for (ModuleResult r : service.getStandingResults(ID)) {
            if (r.getModule().getCode().equals(code)) {
                return r.getGrade();
            }
        }
        return null;
    }

    // ---------- Single attempts ----------

    @Test
    void singleAttemptIsUnchanged() {
        add("CS1", 2026, 1, Grade.A1);

        assertEquals(Grade.A1, standingGrade("CS1"));
        assertEquals(4.0, service.overallQca(ID), DELTA);
    }

    @Test
    void semesterQcaOnlyUsesThatSemester() {
        add("CS1", 2026, 1, Grade.A1);
        add("CS2", 2026, 1, Grade.C3);
        add("CS3", 2026, 2, Grade.B1);

        assertEquals(3.0, service.semesterQca(ID, 2026, 1), DELTA);
        assertEquals(3.2, service.semesterQca(ID, 2026, 2), DELTA);
        assertEquals(0.0, service.semesterQca(ID, 2027, 1), DELTA);
    }

    @Test
    void overallQcaCoversAllSemesters() {
        add("CS1", 2026, 1, Grade.A1);
        add("CS3", 2026, 2, Grade.C3);

        assertEquals(3.0, service.overallQca(ID), DELTA);
    }

    // ---------- Repeat rules ----------

    @Test
    void repeatGradeIsCappedAtC3() {
        add("CS1", 2026, 1, Grade.F);
        add("CS1", 2027, 1, Grade.A1);

        assertEquals(Grade.C3, standingGrade("CS1"));
        assertEquals(2.0, service.overallQca(ID), DELTA);
    }

    @Test
    void betterOriginalBeatsCappedRepeat() {
        add("CS1", 2026, 1, Grade.B1);
        add("CS1", 2027, 1, Grade.A1);

        assertEquals(Grade.B1, standingGrade("CS1"));
        assertEquals(3.2, service.overallQca(ID), DELTA);
    }

    @Test
    void d1ImprovedByRepeatToC3() {
        add("CS1", 2026, 1, Grade.D1);
        add("CS1", 2027, 1, Grade.B2);

        assertEquals(Grade.C3, standingGrade("CS1"));
    }

    @Test
    void worseRepeatLeavesOriginalStanding() {
        add("CS1", 2026, 1, Grade.D1);
        add("CS1", 2027, 1, Grade.F);

        assertEquals(Grade.D1, standingGrade("CS1"));
    }

    @Test
    void incompleteDuringRepeatKeepsOriginalGrade() {
        add("CS1", 2026, 1, Grade.F);
        add("CS1", 2027, 1, Grade.I);

        assertEquals(Grade.F, standingGrade("CS1"));
    }

    @Test
    void passOnRepeatReplacesFailInPassFailModule() {
        add("CS1", 2026, 1, Grade.N);
        add("CS1", 2027, 1, Grade.P);

        assertEquals(Grade.P, standingGrade("CS1"));
        assertTrue(service.buildTranscript(ID).contains("Credits earned: 6"));
    }

    @Test
    void repeatedModuleIsNotCountedTwice() {
        // CS1 stands at C3 (2.0), CS2 is A1 (4.0): (12 + 24) / 12 = 3.0.
        // If the F were also counted, QCA would be 36 / 18 = 2.0.
        add("CS1", 2026, 1, Grade.F);
        add("CS2", 2026, 1, Grade.A1);
        add("CS1", 2027, 1, Grade.C3);

        assertEquals(2, service.getStandingResults(ID).size());
        assertEquals(3.0, service.overallQca(ID), DELTA);
    }

    @Test
    void improvedRepeatKeepsModuleInOriginalSemester() {
        add("CS1", 2026, 1, Grade.F);
        add("CS1", 2027, 1, Grade.C3);

        // TranscriptService keeps the standing grade in the original
        // attempt's year and semester.
        assertEquals(2.0, service.semesterQca(ID, 2026, 1), DELTA);
        assertEquals(0.0, service.semesterQca(ID, 2027, 1), DELTA);
    }

    @Test
    void threeAttemptsUseBestCappedGrade() {
        add("CS1", 2026, 1, Grade.F);
        add("CS1", 2027, 1, Grade.NG);
        add("CS1", 2028, 1, Grade.A2);

        assertEquals(Grade.C3, standingGrade("CS1"));
    }

    // ---------- Non-QCA grades ----------

    @Test
    void passFailModuleDoesNotAffectQca() {
        add("CS1", 2026, 1, Grade.A1);
        add("CS2", 2026, 1, Grade.P);

        assertEquals(4.0, service.overallQca(ID), DELTA);
    }

    // ---------- No results ----------

    @Test
    void studentWithNoResultsHasZeroQca() {
        assertEquals(0.0, service.overallQca(ID), DELTA);
        assertEquals(0.0, service.semesterQca(ID, 2026, 1), DELTA);
        assertTrue(service.getStandingResults(ID).isEmpty());
    }

    // ---------- Transcript text ----------

    @Test
    void transcriptShowsStudentModulesAndQca() {
        add("CS1", 2026, 1, Grade.A1);
        add("CS2", 2026, 1, Grade.C3);

        String t = service.buildTranscript(ID);

        assertTrue(t.contains("Ann"));
        assertTrue(t.contains("101"));
        assertTrue(t.contains("2026/27 - Semester 1"));
        assertTrue(t.contains("CS1"));
        assertTrue(t.contains("First Module"));
        assertTrue(t.contains("A1"));
        assertTrue(t.contains("Semester QCA: 3.00"));
        assertTrue(t.contains("Overall QCA: 3.00"));
        assertTrue(t.contains("Credits earned: 12"));
    }

    @Test
    void transcriptGroupsByYearInOrder() {
        add("CS3", 2027, 2, Grade.B1);
        add("CS1", 2026, 1, Grade.A1);

        String t = service.buildTranscript(ID);

        assertTrue(t.indexOf("2026/27") < t.indexOf("2027/28"));
        assertTrue(t.contains("2027/28 - Semester 2"));
    }

    @Test
    void transcriptMarksRepeatedModulesInOriginalSemester() {
        add("CS1", 2026, 1, Grade.F);
        add("CS1", 2027, 1, Grade.C3);

        String t = service.buildTranscript(ID);

        assertTrue(t.contains("(repeated)"));
        assertTrue(t.contains("2026/27 - Semester 1"));
        assertFalse(t.contains("2027/28"));
    }

    @Test
    void transcriptDoesNotMarkSingleAttempts() {
        add("CS1", 2026, 1, Grade.A1);

        assertFalse(service.buildTranscript(ID).contains("(repeated)"));
    }

    @Test
    void failedModuleEarnsNoCredits() {
        add("CS1", 2026, 1, Grade.F);

        assertTrue(service.buildTranscript(ID).contains("Credits earned: 0"));
    }

    @Test
    void passFailModuleEarnsCreditsButNotQca() {
        add("CS1", 2026, 1, Grade.A1);
        add("CS2", 2026, 1, Grade.P);

        String t = service.buildTranscript(ID);

        assertTrue(t.contains("Overall QCA: 4.00"));
        assertTrue(t.contains("Credits earned: 12"));
    }

    @Test
    void transcriptForStudentWithNoResultsSaysSo() {
        String t = service.buildTranscript(ID);

        assertTrue(t.contains("Ann"));
        assertTrue(t.contains("No results recorded."));
    }

    @Test
    void transcriptForUnknownStudentIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buildTranscript("999"));
    }

    @Test 
    void transcriptShowsYearQcaForEachYear(){
        add("CS1", 2026, 1, Grade.A1);
        add("CS3", 2026, 2, Grade.C3);
        add("CS2", 2027, 1, Grade.F);

        String t = service.buildTranscript(ID);

        assertTrue(t.contains("Year QCA: 3.00"));
        assertTrue(t.contains("Year QCA: 0.00"));
        assertTrue(t.indexOf("Year QCA: 3.00") < t.indexOf("2027/28"));
        assertTrue(t.contains("Overall QCA: 2.00"));
    }
}