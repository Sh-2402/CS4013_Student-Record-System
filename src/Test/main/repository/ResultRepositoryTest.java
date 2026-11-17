package main.repository;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResultRepository.
 *
 * These tests use a temporary CSV file, so the real data/results.csv
 * file in the project is not changed.
 */
class ResultRepositoryTest {

    @TempDir
    Path tempDir;

    private Path resultsFile;
    private Student student1;
    private Student student2;
    private Module module1;
    private Module module2;

    private Map<String, Student> students;
    private Map<String, Module> modules;

    @BeforeEach
    void setUp() {
        resultsFile = tempDir.resolve("results.csv");

        // Registration is not needed by ResultRepository tests.
        student1 = new Student("Test Student One", "S1001",
                "student1@test.com", null);
        student2 = new Student("Test Student Two", "S1002",
                "student2@test.com", null);

        module1 = new Module("CS4013", "Software Development",
                6, 1, "F1001");
        module2 = new Module("CS4023", "Operating Systems",
                6, 2, "F1002");

        students = Map.of(
                student1.getId(), student1,
                student2.getId(), student2
        );

        modules = Map.of(
                module1.getCode(), module1,
                module2.getCode(), module2
        );
    }

    private ResultRepository createRepository() {
        return new ResultRepository(
                resultsFile,
                students::get,
                modules::get
        );
    }

    private ModuleResult result(Student student, Module module,
                                int year, int semester, Grade grade) {
        return new ModuleResult(student, module, year, semester, grade);
    }

    @Test
    void createsCsvFileWithHeaderWhenFileDoesNotExist() throws IOException {
        ResultRepository repository = createRepository();

        assertTrue(Files.exists(resultsFile));
        assertTrue(repository.findAll().isEmpty());
        assertEquals(
                "studentId,moduleCode,year,semester,grade",
                Files.readAllLines(resultsFile).get(0)
        );
    }

    @Test
    void savesResultToCsvAndMemory() throws IOException {
        ResultRepository repository = createRepository();
        ModuleResult expected = result(
                student1, module1, 2026, 1, Grade.A1
        );

        repository.save(expected);

        assertEquals(1, repository.findAll().size());
        assertSame(expected, repository.findAll().get(0));

        List<String> lines = Files.readAllLines(resultsFile);
        assertEquals(2, lines.size());
        assertEquals(
                "S1001,CS4013,2026,1,A1",
                lines.get(1)
        );
    }

    @Test
    void loadsExistingResultsFromCsv() throws IOException {
        Files.write(resultsFile, List.of(
                "studentId,moduleCode,year,semester,grade",
                "S1001,CS4013,2026,1,A1",
                "S1002,CS4023,2026,2,B2"
        ));

        ResultRepository repository = createRepository();

        assertEquals(2, repository.findAll().size());
        assertSame(student1, repository.findAll().get(0).getStudent());
        assertSame(module1, repository.findAll().get(0).getModule());
        assertEquals(Grade.A1, repository.findAll().get(0).getGrade());
        assertEquals(Grade.B2, repository.findAll().get(1).getGrade());
    }

    @Test
    void findByStudentReturnsOnlyThatStudentsResults() {
        ResultRepository repository = createRepository();
        repository.save(result(student1, module1, 2026, 1, Grade.A1));
        repository.save(result(student2, module2, 2026, 2, Grade.B2));
        repository.save(result(student1, module2, 2026, 2, Grade.C1));

        List<ModuleResult> found = repository.findByStudent("S1001");

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(
                r -> r.getStudent().getId().equals("S1001")
        ));
    }

    @Test
    void findByStudentAndYearReturnsOnlyMatchingAcademicYear() {
        ResultRepository repository = createRepository();
        repository.save(result(student1, module1, 2025, 1, Grade.A1));
        repository.save(result(student1, module2, 2026, 2, Grade.B2));
        repository.save(result(student2, module1, 2026, 1, Grade.C1));

        List<ModuleResult> found =
                repository.findByStudent("S1001", 2026);

        assertEquals(1, found.size());
        assertEquals(2026, found.get(0).getYear());
        assertEquals(Grade.B2, found.get(0).getGrade());
    }

    @Test
    void findByStudentYearAndSemesterReturnsOnlyMatchingResults() {
        ResultRepository repository = createRepository();
        repository.save(result(student1, module1, 2026, 1, Grade.A1));
        repository.save(result(student1, module2, 2026, 2, Grade.B2));
        repository.save(result(student1, module1, 2025, 1, Grade.C1));

        List<ModuleResult> found =
                repository.findByStudent("S1001", 2026, 2);

        assertEquals(1, found.size());
        assertEquals(2026, found.get(0).getYear());
        assertEquals(2, found.get(0).getSemester());
        assertEquals(Grade.B2, found.get(0).getGrade());
    }

    @Test
    void skipsInvalidRowsWhenLoadingCsv() throws IOException {
        Files.write(resultsFile, List.of(
                "studentId,moduleCode,year,semester,grade",
                "S1001,CS4013,2026,1,A1",
                "S9999,CS4013,2026,1,A1",
                "S1001,UNKNOWN,2026,1,A1",
                "S1001,CS4013,not-a-year,1,A1",
                "S1001,CS4013,2026,3,A1",
                "S1001,CS4013,2026,1,NOT_A_GRADE",
                "S1001,CS4013,2026"
        ));

        ResultRepository repository = createRepository();

        assertEquals(1, repository.findAll().size());
        assertEquals(Grade.A1, repository.findAll().get(0).getGrade());
    }

    @Test
    void keepsMultipleAttemptsForTheSameModule() {
        ResultRepository repository = createRepository();

        repository.save(result(student1, module1, 2026, 1, Grade.F));
        repository.save(result(student1, module1, 2026, 1, Grade.B2));

        List<ModuleResult> found = repository.findByStudent(
                "S1001", 2026, 1
        );

        assertEquals(2, found.size());
        assertEquals(Grade.F, found.get(0).getGrade());
        assertEquals(Grade.B2, found.get(1).getGrade());
    }

    @Test
    void findAllReturnsCopyOfInternalResultsList() {
        ResultRepository repository = createRepository();
        repository.save(result(student1, module1, 2026, 1, Grade.A1));

        List<ModuleResult> returned = repository.findAll();
        returned.clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void savingNullResultThrowsNullPointerException() {
        ResultRepository repository = createRepository();

        assertThrows(
                NullPointerException.class,
                () -> repository.save(null)
        );
    }
}
