package main.service;

import static org.junit.jupiter.api.Assertions.*;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Registration;
import main.model.Student;
import main.repository.ResultRepository;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResultsServiceTest {

    private static final String ANN = "101";
    private static final String BOB = "102";

    @TempDir
    Path tempDir;

    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();

    private ResultRepository repository;
    private ResultsService service;

    @BeforeEach
    void setUp() {
        students.put(ANN, new Student("Ann", ANN, "ann@ul.ie", null));
        students.put(BOB, new Student("Bob", BOB, "bob@ul.ie", null));

        modules.put("CS1", new Module("CS1", "First Module", 6, 1, "L1"));
        modules.put("CS2", new Module("CS2", "Second Module", 6, 2, "L1"));
        modules.put("CS3", new Module("CS3", "Third Module", 6, 1, "L1"));

        repository = new ResultRepository(
                tempDir.resolve("results.csv"), students::get, modules::get);

        // Everyone counts as registered unless a test says otherwise.
        service = new ResultsService(
                repository, students::get, modules::get, s -> true);
    }

    /** Runs the action and returns the message of the exception it throws. */
    private String messageOf(org.junit.jupiter.api.function.Executable action) {
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, action);
        return e.getMessage();
    }

    // ---------- Recording a valid result ----------

    @Test
    void validResultIsSavedAndReturned() {
        ModuleResult r = service.recordResult(ANN, "CS1", 2026, 1, Grade.A2);

        assertEquals(ANN, r.getStudent().getId());
        assertEquals("CS1", r.getModule().getCode());
        assertEquals(2026, r.getYear());
        assertEquals(1, r.getSemester());
        assertEquals(Grade.A2, r.getGrade());

        assertEquals(1, repository.findAll().size());
        assertSame(r, repository.findAll().get(0));
    }

    @Test
    void differentStudentsCanHaveResultsForTheSameModule() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.A1);
        service.recordResult(BOB, "CS1", 2026, 1, Grade.C3);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void sameStudentCanHaveDifferentModulesInTheSameSemester() {
        modules.put("CS3", new Module("CS3", "Third Module", 6, 1, "L1"));

        service.recordResult(ANN, "CS1", 2026, 1, Grade.A1);
        service.recordResult(ANN, "CS3", 2026, 1, Grade.B1);

        assertEquals(2, service.getResults(ANN, 2026, 1).size());
    }

    // ---------- Repeats ----------

    @Test
    void repeatInALaterYearIsAllowed() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.F);
        service.recordResult(ANN, "CS1", 2027, 1, Grade.C3);

        assertEquals(2, service.getResults(ANN).size());
    }

    // ---------- Validation: grade ----------

    @Test
    void nullGradeIsRejected() {
        String msg = messageOf(
                () -> service.recordResult(ANN, "CS1", 2026, 1, null));

        assertTrue(msg.contains("Grade"));
        assertTrue(repository.findAll().isEmpty());
    }

    // ---------- Validation: student ----------

    @Test
    void unknownStudentIsRejected() {
        String msg = messageOf(
                () -> service.recordResult("999", "CS1", 2026, 1, Grade.A1));

        assertTrue(msg.contains("999"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void unregisteredStudentIsRejected() {
        ResultsService strict = new ResultsService(
                repository, students::get, modules::get, s -> false);

        String msg = messageOf(
                () -> strict.recordResult(ANN, "CS1", 2026, 1, Grade.A1));

        assertTrue(msg.contains("not registered"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void onlyTheStudentWhoIsNotRegisteredIsRejected() {
        ResultsService onlyAnn = new ResultsService(
                repository, students::get, modules::get,
                s -> s.getId().equals(ANN));

        assertDoesNotThrow(
                () -> onlyAnn.recordResult(ANN, "CS1", 2026, 1, Grade.A1));
        assertThrows(IllegalArgumentException.class,
                () -> onlyAnn.recordResult(BOB, "CS1", 2026, 1, Grade.A1));
    }

    @Test
    void defaultConstructorTreatsNullRegistrationAsNotRegistered() {
        // The students in this test have a null registration.
        ResultsService defaultService = new ResultsService(
                repository, students::get, modules::get);

        String msg = messageOf(() -> defaultService.recordResult(
                ANN, "CS1", 2026, 1, Grade.A1));

        assertTrue(msg.contains("not registered"));
    }

    @Test
    void defaultConstructorAcceptsStudentWithARegistration() {
        Student carol = new Student("Carol", "103", "carol@ul.ie",
                new Registration("103", "LM118", 2026, 1, "ACTIVE"));
        students.put("103", carol);

        ResultsService defaultService = new ResultsService(
                repository, students::get, modules::get);

        ModuleResult r = defaultService.recordResult(
                "103", "CS1", 2026, 1, Grade.B1);

        assertSame(carol, r.getStudent());
        assertEquals(1, repository.findAll().size());
    }

    // ---------- Validation: module and semester ----------

    @Test
    void unknownModuleIsRejected() {
        String msg = messageOf(
                () -> service.recordResult(ANN, "NOPE", 2026, 1, Grade.A1));

        assertTrue(msg.contains("NOPE"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void semesterMustMatchTheModulesSemester() {
        // CS1 runs in semester 1, CS2 runs in semester 2.
        String msg = messageOf(
                () -> service.recordResult(ANN, "CS1", 2026, 2, Grade.A1));

        assertTrue(msg.contains("CS1"));
        assertTrue(msg.contains("semester 1"));
        assertTrue(repository.findAll().isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> service.recordResult(ANN, "CS2", 2026, 1, Grade.A1));
    }

    @Test
    void semesterOutsideOneOrTwoIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordResult(ANN, "CS1", 2026, 0, Grade.A1));
        assertThrows(IllegalArgumentException.class,
                () -> service.recordResult(ANN, "CS1", 2026, 3, Grade.A1));
    }

    @Test
    void invalidYearIsRejectedAndNothingIsSaved() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordResult(ANN, "CS1", 0, 1, Grade.A1));

        assertTrue(repository.findAll().isEmpty());
    }

    // ---------- Validation: duplicates ----------

    @Test
    void duplicateResultIsRejected() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.A1);

        String msg = messageOf(
                () -> service.recordResult(ANN, "CS1", 2026, 1, Grade.B1));

        assertTrue(msg.contains("already recorded"));
        assertEquals(1, repository.findAll().size());
        assertEquals(Grade.A1, repository.findAll().get(0).getGrade());
    }

    // ---------- Lecturer check ----------

    @Test
    void lecturerCanRecordResultForTheirOwnModule() {
        // Both test modules are taught by lecturer "L1".
        ModuleResult r = service.recordResult(
                "L1", ANN, "CS1", 2026, 1, Grade.A2);

        assertEquals(Grade.A2, r.getGrade());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void lecturerCannotRecordResultForAnotherLecturersModule() {
        String msg = messageOf(() -> service.recordResult(
                "L2", ANN, "CS1", 2026, 1, Grade.A2));

        assertTrue(msg.contains("not taught by lecturer L2"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void lecturerIdMustBeGiven() {
        assertThrows(IllegalArgumentException.class, () -> service.recordResult(
                null, ANN, "CS1", 2026, 1, Grade.A2));
        assertThrows(IllegalArgumentException.class, () -> service.recordResult(
                "  ", ANN, "CS1", 2026, 1, Grade.A2));
    }

    @Test
    void lecturerOverloadStillReportsUnknownModule() {
        String msg = messageOf(() -> service.recordResult(
                "L1", ANN, "NOPE", 2026, 1, Grade.A2));

        assertTrue(msg.contains("NOPE"));
    }

    @Test
    void lecturerOverloadStillAppliesTheOtherChecks() {
        service.recordResult("L1", ANN, "CS1", 2026, 1, Grade.A2);

        String msg = messageOf(() -> service.recordResult(
                "L1", ANN, "CS1", 2026, 1, Grade.B1));

        assertTrue(msg.contains("already recorded"));
    }

    // ---------- Repeat rules (credits already held) ----------

    @Test
    void moduleWithCreditsCannotBeAttemptedAgain() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.C3);

        String msg = messageOf(
                () -> service.recordResult(ANN, "CS1", 2027, 1, Grade.A1));

        assertTrue(msg.contains("already has credits for CS1"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void passedPassFailModuleCannotBeAttemptedAgain() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.P);

        assertThrows(IllegalArgumentException.class,
                () -> service.recordResult(ANN, "CS1", 2027, 1, Grade.P));
    }

    @Test
    void failedModuleCanBeRepeated() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.F);

        assertDoesNotThrow(
                () -> service.recordResult(ANN, "CS1", 2027, 1, Grade.C3));
    }

    @Test
    void incompleteAndNotGradedModulesCanBeRepeated() {
        modules.put("CS3", new Module("CS3", "Third Module", 6, 1, "L1"));
        service.recordResult(ANN, "CS1", 2026, 1, Grade.NG);
        service.recordResult(ANN, "CS3", 2026, 1, Grade.I);

        assertDoesNotThrow(
                () -> service.recordResult(ANN, "CS1", 2027, 1, Grade.C3));
        assertDoesNotThrow(
                () -> service.recordResult(ANN, "CS3", 2027, 1, Grade.C3));
    }

    @Test
    void d1CarriesCreditsButCanStillBeRepeated() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.D1);

        assertDoesNotThrow(
                () -> service.recordResult(ANN, "CS1", 2027, 1, Grade.C3));
    }

    @Test
    void repeatIsBlockedOnceTheStandingGradeHasCredits() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.F);
        service.recordResult(ANN, "CS1", 2027, 1, Grade.C3);

        String msg = messageOf(
                () -> service.recordResult(ANN, "CS1", 2028, 1, Grade.C3));

        assertTrue(msg.contains("grade C3"));
    }

    @Test
    void repeatRuleAppliesPerStudent() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.A1);

        assertDoesNotThrow(
                () -> service.recordResult(BOB, "CS1", 2027, 1, Grade.C3));
    }

    // ---------- Administrative grades and withdrawn students ----------

    @Test
    void lecturerCannotAwardAdministrativeGrades() {
        Grade[] notAwarded = {Grade.G, Grade.EX, Grade.R, Grade.RE,
                Grade.W, Grade.X, Grade.Y};

        for (Grade g : notAwarded) {
            String msg = messageOf(() -> service.recordResult(
                    "L1", ANN, "CS1", 2026, 1, g));
            assertTrue(msg.contains("cannot be awarded by a lecturer"),
                    g + " should be refused");
        }
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void lecturerCanAwardOtherGradesIncludingIncomplete() {
        assertDoesNotThrow(() -> service.recordResult(
                "L1", ANN, "CS1", 2026, 1, Grade.I));
    }

    @Test
    void methodWithoutLecturerIdMayRecordAdministrativeGrades() {
        assertDoesNotThrow(
                () -> service.recordResult(ANN, "CS1", 2026, 1, Grade.EX));
    }

    @Test
    void defaultConstructorRejectsWithdrawnStudent() {
        Student dan = new Student("Dan", "104", "dan@ul.ie",
                new Registration("104", "LM118", 2026, 1, "WITHDRAWN"));
        students.put("104", dan);

        ResultsService defaultService = new ResultsService(
                repository, students::get, modules::get);

        String msg = messageOf(() -> defaultService.recordResult(
                "104", "CS1", 2026, 1, Grade.A1));

        assertTrue(msg.contains("not registered"));
    }

    @Test
    void defaultConstructorAcceptsRepeatingStudent() {
        Student eve = new Student("Eve", "105", "eve@ul.ie",
                new Registration("105", "LM118", 2026, 2, "REPEATING"));
        students.put("105", eve);

        ResultsService defaultService = new ResultsService(
                repository, students::get, modules::get);

        assertDoesNotThrow(() -> defaultService.recordResult(
                "105", "CS1", 2026, 1, Grade.B1));
    }

    // ---------- Persistence ----------

    @Test
    void recordedResultIsWrittenToTheCsvFile() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.B3);

        ResultRepository reloaded = new ResultRepository(
                tempDir.resolve("results.csv"), students::get, modules::get);

        assertEquals(1, reloaded.findAll().size());
        assertEquals(Grade.B3, reloaded.findAll().get(0).getGrade());
    }

    // ---------- Finding ----------

    @Test
    void getResultsFiltersByStudentYearAndSemester() {
        service.recordResult(ANN, "CS1", 2026, 1, Grade.A1);
        service.recordResult(ANN, "CS2", 2026, 2, Grade.B1);
        service.recordResult(ANN, "CS3", 2027, 1, Grade.C3);
        service.recordResult(BOB, "CS1", 2026, 1, Grade.F);

        assertEquals(3, service.getResults(ANN).size());
        assertEquals(1, service.getResults(BOB).size());

        assertEquals(2, service.getResults(ANN, 2026).size());
        assertEquals(1, service.getResults(ANN, 2027).size());

        List<ModuleResult> sem2 = service.getResults(ANN, 2026, 2);
        assertEquals(1, sem2.size());
        assertEquals("CS2", sem2.get(0).getModule().getCode());
    }

    @Test
    void getResultsForUnknownStudentIsEmpty() {
        assertTrue(service.getResults("999").isEmpty());
        assertTrue(service.getResults("999", 2026).isEmpty());
        assertTrue(service.getResults("999", 2026, 1).isEmpty());
    }

    // ---------- Constructor validation ----------

    @Test
    void constructorRejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new ResultsService(
                        null, students::get, modules::get, s -> true));
        assertThrows(NullPointerException.class,
                () -> new ResultsService(
                        repository, null, modules::get, s -> true));
        assertThrows(NullPointerException.class,
                () -> new ResultsService(
                        repository, students::get, null, s -> true));
        assertThrows(NullPointerException.class,
                () -> new ResultsService(
                        repository, students::get, modules::get, null));
    }
}