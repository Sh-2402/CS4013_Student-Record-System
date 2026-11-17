package main.ui;

import static org.junit.jupiter.api.Assertions.*;

import main.model.Grade;
import main.model.Module;
import main.model.Student;
import main.repository.ResultRepository;
import main.rules.QcaCalculator;
import main.service.ResultsService;
import main.service.TranscriptService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FacultyMenuTest {

    private static final String LECTURER = "L1";

    @TempDir
    Path tempDir;

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();

    private ResultRepository repository;
    private ResultsService resultsService;
    private TranscriptService transcriptService;

    @BeforeEach
    void setUp() {
        students.put("101", new Student("Ann", "101", "ann@ul.ie", null));

        modules.put("CS1", new Module("CS1", "First Module", 6, 1, LECTURER));
        modules.put("CS9", new Module("CS9", "Other Module", 6, 1, "L2"));

        repository = new ResultRepository(
                tempDir.resolve("results.csv"), students::get, modules::get);

        resultsService = new ResultsService(
                repository, students::get, modules::get, s -> true);

        transcriptService = new TranscriptService(
                resultsService, new QcaCalculator(), students::get);
    }

    /** Runs the menu with the given keystrokes and returns what it printed. */
    private String run(String keys) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(
                    keys.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

            new FacultyMenu(new InputHelper(), resultsService,
                    transcriptService, LECTURER).start();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void logOutEndsTheMenu() {
        String out = run("0\n");

        assertTrue(out.contains("Faculty Menu (L1)"));
    }

    @Test
    void invalidChoiceIsRejectedAndMenuContinues() {
        String out = run("9\n0\n");

        assertTrue(out.contains("Invalid choice"));
    }

    @Test
    void submittingAValidResultSavesIt() {
        String out = run("1\n101\nCS1\n2026\n1\na2\n0\n");

        assertTrue(out.contains("Result recorded"));
        assertEquals(1, repository.findAll().size());
        assertEquals(Grade.A2, repository.findAll().get(0).getGrade());
    }

    @Test
    void lecturerCannotSubmitForAnotherLecturersModule() {
        String out = run("1\n101\nCS9\n2026\n1\nA1\n0\n");

        assertTrue(out.contains("not taught by lecturer L1"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void unknownGradeIsReportedAndNothingIsSaved() {
        String out = run("1\n101\nCS1\n2026\n1\nZ9\n0\n");

        assertTrue(out.contains("unknown grade 'Z9'"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void unknownStudentIsReported() {
        String out = run("1\n999\nCS1\n2026\n1\nA1\n0\n");

        assertTrue(out.contains("No student with ID 999"));
    }

    @Test
    void wrongSemesterIsReported() {
        String out = run("1\n101\nCS1\n2026\n2\nA1\n0\n");

        assertTrue(out.contains("runs in semester 1"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void duplicateResultIsReported() {
        String out = run("1\n101\nCS1\n2026\n1\nA1\n"
                + "1\n101\nCS1\n2026\n1\nB1\n0\n");

        assertTrue(out.contains("already recorded"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void textInsteadOfAYearAsksAgain() {
        String out = run("1\n101\nCS1\nabc\n2026\n1\nA1\n0\n");

        assertTrue(out.contains("Invalid input"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void facultyCanLookUpAnyStudentsTranscript() {
        resultsService.recordResult("L1", "101", "CS1", 2026, 1, Grade.A1);

        String out = run("2\n101\n0\n");

        assertTrue(out.contains("TRANSCRIPT"));
        assertTrue(out.contains("Ann"));
        assertTrue(out.contains("CS1"));
        assertTrue(out.contains("Overall QCA: 4.00"));
    }

    @Test
    void lookingUpAnUnknownStudentIsReported() {
        String out = run("2\n999\n0\n");

        assertTrue(out.contains("No student with ID 999"));
    }

    @Test
    void constructorRejectsNulls() {
        InputHelper helper = new InputHelper();

        assertThrows(NullPointerException.class, () -> new FacultyMenu(
                null, resultsService, transcriptService, LECTURER));
        assertThrows(NullPointerException.class, () -> new FacultyMenu(
                helper, null, transcriptService, LECTURER));
        assertThrows(NullPointerException.class, () -> new FacultyMenu(
                helper, resultsService, null, LECTURER));
        assertThrows(NullPointerException.class, () -> new FacultyMenu(
                helper, resultsService, transcriptService, null));
    }
}