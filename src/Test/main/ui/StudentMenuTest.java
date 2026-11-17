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

class StudentMenuTest {

    @TempDir
    Path tempDir;

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();

    private ResultsService resultsService;
    private TranscriptService transcriptService;

    @BeforeEach
    void setUp() {
        students.put("101", new Student("Ann", "101", "ann@ul.ie", null));
        students.put("102", new Student("Bob", "102", "bob@ul.ie", null));

        modules.put("CS1", new Module("CS1", "First Module", 6, 1, "L1"));
        modules.put("CS2", new Module("CS2", "Second Module", 6, 1, "L1"));

        ResultRepository repository = new ResultRepository(
                tempDir.resolve("results.csv"), students::get, modules::get);

        resultsService = new ResultsService(
                repository, students::get, modules::get, s -> true);

        transcriptService = new TranscriptService(
                resultsService, new QcaCalculator(), students::get);

        resultsService.recordResult("101", "CS1", 2026, 1, Grade.A1);
        resultsService.recordResult("101", "CS2", 2026, 1, Grade.C3);
        resultsService.recordResult("102", "CS1", 2026, 1, Grade.F);
    }

    /** Runs the menu for one student and returns what it printed. */
    private String run(String studentId, String keys) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(
                    keys.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));

            new StudentMenu(new InputHelper(), transcriptService,
                    studentId).start();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void logOutEndsTheMenu() {
        String out = run("101", "0\n");

        assertTrue(out.contains("Student Menu (101)"));
    }

    @Test
    void invalidChoiceIsRejectedAndMenuContinues() {
        String out = run("101", "7\n0\n");

        assertTrue(out.contains("Invalid choice"));
    }

    @Test
    void studentSeesTheirOwnTranscript() {
        String out = run("101", "1\n0\n");

        assertTrue(out.contains("TRANSCRIPT"));
        assertTrue(out.contains("Ann"));
        assertTrue(out.contains("CS1"));
        assertTrue(out.contains("Overall QCA: 3.00"));
    }

    @Test
    void studentDoesNotSeeAnotherStudentsResults() {
        String out = run("101", "1\n0\n");

        assertFalse(out.contains("Bob"));
        assertFalse(out.contains("(ID 102)"));
    }

    @Test
    void otherStudentSeesTheirOwnResults() {
        String out = run("102", "1\n0\n");

        assertTrue(out.contains("Bob"));
        assertTrue(out.contains("Overall QCA: 0.00"));
    }

    @Test
    void semesterQcaIsShown() {
        String out = run("101", "2\n2026\n1\n0\n");

        assertTrue(out.contains("QCA for 2026 semester 1: 3.00"));
    }

    @Test
    void invalidSemesterIsReported() {
        String out = run("101", "2\n2026\n3\n0\n");

        assertTrue(out.contains("Error: Semester must be either 1 or 2"));
    }

    @Test
    void overallQcaIsShown() {
        String out = run("101", "3\n0\n");

        assertTrue(out.contains("Overall QCA: 3.00"));
    }

    @Test
    void studentWithNoResultsSeesAMessage() {
        students.put("103", new Student("Cara", "103", "c@ul.ie", null));

        String out = run("103", "1\n0\n");

        assertTrue(out.contains("No results recorded."));
    }

    @Test
    void constructorRejectsNulls() {
        InputHelper helper = new InputHelper();

        assertThrows(NullPointerException.class,
                () -> new StudentMenu(null, transcriptService, "101"));
        assertThrows(NullPointerException.class,
                () -> new StudentMenu(helper, null, "101"));
        assertThrows(NullPointerException.class,
                () -> new StudentMenu(helper, transcriptService, null));
    }
}