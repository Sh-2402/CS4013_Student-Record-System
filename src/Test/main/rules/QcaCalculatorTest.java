package main.rules;

import static org.junit.jupiter.api.Assertions.*;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class QcaCalculatorTest {

    private static final double DELTA = 0.001;

    private final QcaCalculator calc = new QcaCalculator();

    private final Student s =
            new Student("Ann", "2501234", "ann@ul.ie", null);

    private ModuleResult result(String code, int credits, Grade grade) {
        return result(code, credits, 2026, 1, grade);
    }

    private ModuleResult result(
            String code, int credits, int year, int semester, Grade grade) {

        Module module =
                new Module(code, "Test module", credits, 1, "L001");

        return new ModuleResult(s, module, year, semester, grade);
    }

    // ---------- Basic QCA Formula ----------

    @Test
    void twoEqualModules() {
        // (3.6 x 6 + 2.6 x 6) / 12 = 3.1

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A2),
                result("B", 6, Grade.C1)
        );

        assertEquals(3.1, calc.overallQca(list), DELTA);
    }

    @Test
    void differentCreditsAreWeighted() {
        // (4.0 x 10 + 2.0 x 5) / 15 = 3.333...

        List<ModuleResult> list = List.of(
                result("A", 10, Grade.A1),
                result("B", 5, Grade.C3)
        );

        assertEquals(3.3333, calc.overallQca(list), DELTA);
    }

    @Test
    void semesterQcaUsesSameFormula() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A2),
                result("B", 6, Grade.C1)
        );

        assertEquals(3.1, calc.semesterQca(list), DELTA);
    }

    @Test
    void emptyListGivesZero() {
        assertEquals(0.0, calc.overallQca(List.of()), DELTA);
    }

    @Test
    void onlyNonQualityGradesGiveZero() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.P),
                result("B", 6, Grade.N),
                result("C", 6, Grade.EX),
                result("D", 6, Grade.I)
        );

        assertEquals(0.0, calc.overallQca(list), DELTA);
    }

    // ---------- Null Validation ----------

    @Test
    void nullListsAreRejected() {

        assertThrows(NullPointerException.class,
                () -> calc.overallQca(null));

        assertThrows(NullPointerException.class,
                () -> calc.semesterQca(null));

        assertThrows(NullPointerException.class,
                () -> calc.semesterQca(null, 2026, 1));

        assertThrows(NullPointerException.class,
                () -> calc.yearQca(null, 2026));

        assertThrows(NullPointerException.class,
                () -> calc.programmeQca(null, Set.of("A")));

        assertThrows(NullPointerException.class,
                () -> calc.weightedQca(null));
    }

    @Test
    void nullResultInsideListIsRejected() {

        List<ModuleResult> list = Arrays.asList(
                result("A", 6, Grade.A1),
                null
        );

        assertThrows(NullPointerException.class,
                () -> calc.overallQca(list));

        assertThrows(NullPointerException.class,
                () -> calc.yearQca(list, 2026));

        assertThrows(NullPointerException.class,
                () -> calc.programmeQca(list, Set.of("A")));
    }

    @Test
    void nullYearListIsRejected() {

        List<List<ModuleResult>> years = Arrays.asList(
                List.of(result("A", 6, Grade.A1)),
                null
        );

        assertThrows(NullPointerException.class,
                () -> calc.weightedQca(years));
    }

    // ---------- Fails and Compensating Fails ----------

    @Test
    void failPullsQcaDown() {
        // (4.0 x 6 + 0.0 x 6) / 12 = 2.0

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.F)
        );

        assertEquals(2.0, calc.overallQca(list), DELTA);
    }

    @Test
    void ngPullsQcaDownLikeF() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.NG)
        );

        assertEquals(2.0, calc.overallQca(list), DELTA);
    }

    @Test
    void d1CountsWithItsQualityPoints() {
        // (4.0 x 6 + 1.6 x 6) / 12 = 2.8

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.D1)
        );

        assertEquals(2.8, calc.overallQca(list), DELTA);
    }

    @Test
    void d1IsIncludedInQcaDespiteBeingCompensatingFail() {

        ModuleResult d1 = result("A", 6, Grade.D1);

        assertTrue(d1.isCompensatingFail());
        assertFalse(d1.isPass());

        assertEquals(1.6, calc.overallQca(List.of(d1)), DELTA);
    }

    // ---------- Non-Quality Hours ----------

    @Test
    void passGradeIsExcludedFromQca() {
        // Only A1 contributes: QCA = 4.0

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.P)
        );

        assertEquals(4.0, calc.overallQca(list), DELTA);
    }

    @Test
    void nGradeIsExcludedFromQca() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.N)
        );

        assertEquals(4.0, calc.overallQca(list), DELTA);
    }

    @Test
    void administrativeGradesAreExcludedFromQca() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.EX),
                result("C", 6, Grade.I),
                result("D", 6, Grade.W)
        );

        assertEquals(4.0, calc.overallQca(list), DELTA);
    }

    @Test
    void administrativeGradesDoNotAffectQcaDenominator() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.R),
                result("C", 6, Grade.G),
                result("D", 6, Grade.RE),
                result("E", 6, Grade.X),
                result("F", 6, Grade.Y),
                result("G", 6, Grade.W)
        );

        assertEquals(4.0, calc.overallQca(list), DELTA);
    }

    // ---------- Semester Filtering ----------

    @Test
    void semesterQcaFiltersByYearAndSemester() {

        List<ModuleResult> list = List.of(
                result("A", 6, 2026, 1, Grade.A1),
                result("B", 6, 2026, 2, Grade.F),
                result("C", 6, 2027, 1, Grade.C3)
        );

        assertEquals(4.0, calc.semesterQca(list, 2026, 1), DELTA);
        assertEquals(0.0, calc.semesterQca(list, 2026, 2), DELTA);
        assertEquals(2.0, calc.semesterQca(list, 2027, 1), DELTA);
        assertEquals(0.0, calc.semesterQca(list, 2030, 1), DELTA);
    }

    @Test
    void invalidSemesterIsRejected() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1)
        );

        assertThrows(IllegalArgumentException.class,
                () -> calc.semesterQca(list, 2026, 0));

        assertThrows(IllegalArgumentException.class,
                () -> calc.semesterQca(list, 2026, 3));
    }

    // ---------- Year QCA ----------

    @Test
    void yearQcaCoversBothSemesters() {
        // 2026: (4.0 x 6 + 2.0 x 6) / 12 = 3.0

        List<ModuleResult> list = List.of(
                result("A", 6, 2026, 1, Grade.A1),
                result("B", 6, 2026, 2, Grade.C3),
                result("C", 6, 2027, 1, Grade.F)
        );

        assertEquals(3.0, calc.yearQca(list, 2026), DELTA);
        assertEquals(0.0, calc.yearQca(list, 2027), DELTA);
    }

    @Test
    void yearQcaForYearWithNoResultsIsZero() {

        List<ModuleResult> list = List.of(
                result("A", 6, 2026, 1, Grade.A1)
        );

        assertEquals(0.0, calc.yearQca(list, 2030), DELTA);
    }

    @Test
    void yearQcaIgnoresNonQualityGrades() {

        List<ModuleResult> list = List.of(
                result("A", 6, 2026, 1, Grade.B1),
                result("B", 6, 2026, 2, Grade.P),
                result("C", 6, 2026, 2, Grade.EX)
        );

        assertEquals(3.2, calc.yearQca(list, 2026), DELTA);
    }

    // ---------- Programme QCA ----------

    @Test
    void programmeQcaUsesOnlyProgrammeModules() {
        // A and B: (4.0 x 6 + 2.0 x 6) / 12 = 3.0

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1),
                result("B", 6, Grade.C3),
                result("C", 6, Grade.F)
        );

        assertEquals(3.0,
                calc.programmeQca(list, Set.of("A", "B")), DELTA);

        assertEquals(0.0,
                calc.programmeQca(list, Set.of("C")), DELTA);
    }

    @Test
    void programmeQcaWithNoMatchingModulesIsZero() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1)
        );

        assertEquals(0.0, calc.programmeQca(list, Set.of()), DELTA);
        assertEquals(0.0, calc.programmeQca(list, Set.of("Z")), DELTA);
    }

    @Test
    void programmeQcaRejectsNullModuleCodes() {

        List<ModuleResult> list = List.of(
                result("A", 6, Grade.A1)
        );

        assertThrows(NullPointerException.class,
                () -> calc.programmeQca(list, null));
    }

    // ---------- Three-Year Part 2 Weighting ----------

    @Test
    void threeYearPart2UsesOneTwoTwoWeighting() {

        // QCS: 24 x 1 + 12 x 2 + 12 x 2 = 72
        // Hours: 6 x 1 + 6 x 2 + 6 x 2 = 30
        // Weighted QCA = 72 / 30 = 2.4

        List<List<ModuleResult>> years = List.of(
                List.of(result("A", 6, 2024, 1, Grade.A1)),
                List.of(result("B", 6, 2025, 1, Grade.C3)),
                List.of(result("C", 6, 2026, 1, Grade.C3))
        );

        assertEquals(2.4, calc.weightedQca(years), DELTA);
    }

    @Test
    void twoYearPart2IsUnweighted() {

        // (24 + 12) / 12 = 3.0

        List<List<ModuleResult>> years = List.of(
                List.of(result("A", 6, 2025, 1, Grade.A1)),
                List.of(result("B", 6, 2026, 1, Grade.C3))
        );

        assertEquals(3.0, calc.weightedQca(years), DELTA);
    }

    @Test
    void oneYearPart2IsUnweighted() {

        List<List<ModuleResult>> years = List.of(
                List.of(result("A", 6, 2026, 1, Grade.A1))
        );

        assertEquals(4.0, calc.weightedQca(years), DELTA);
    }

    @Test
    void weightedQcaWithNoYearsGivesZero() {
        assertEquals(0.0, calc.weightedQca(List.of()), DELTA);
    }

    @Test
    void weightedQcaWithNoQualityHoursGivesZero() {

        List<List<ModuleResult>> years = List.of(
                List.of(
                        result("A", 6, 2026, 1, Grade.P),
                        result("B", 6, 2026, 1, Grade.N)
                ),
                List.of(
                        result("C", 6, 2027, 1, Grade.EX)
                ),
                List.of(
                        result("D", 6, 2028, 1, Grade.I)
                )
        );

        assertEquals(0.0, calc.weightedQca(years), DELTA);
    }

    @Test
    void weightedQcaRejectsMoreThanThreeYears() {

        List<List<ModuleResult>> years = List.of(
                List.of(result("A", 6, Grade.A1)),
                List.of(result("B", 6, Grade.A1)),
                List.of(result("C", 6, Grade.A1)),
                List.of(result("D", 6, Grade.A1))
        );

        assertThrows(IllegalArgumentException.class,
                () -> calc.weightedQca(years));
    }
}
