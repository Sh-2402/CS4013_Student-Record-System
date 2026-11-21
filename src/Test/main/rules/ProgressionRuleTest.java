package main.rules;

import static org.junit.jupiter.api.Assertions.*;

import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.PostgraduateProgramme;
import main.model.Student;
import main.model.UndergraduateProgramme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for UndergraduateProgressionRule and PostgraduateProgressionRule,
 * based on the UL Handbook 2026/27:
 * AR 1.5.3 (QCA 2.00 and full credits), MS 2.5.4 (deficient grades),
 * Table 2.1 (D1 compensating fail) and MS 2.7.1 (dissertation C3 or better).
 */
class ProgressionRuleTest {

    private final ProgressionRule undergrad = new UndergraduateProgressionRule();
    private final ProgressionRule postgrad = new PostgraduateProgressionRule();

    private final Student student = new Student("Ann", "101", "ann@ul.ie", null);

    private ModuleResult result(String code, String title, Grade grade) {
        Module module = new Module(code, title, 6, 1, "L001");
        return new ModuleResult(student, module, 2026, 1, grade);
    }

    private ModuleResult result(String code, Grade grade) {
        return result(code, "Test module", grade);
    }

    private ModuleResult dissertation(Grade grade) {
        return result("CS6104", "MSc Project", grade);
    }

    // ---------- QCA boundary (AR 1.5.3) ----------

    @Test
    void qcaExactlyTwoProgresses() {
        List<ModuleResult> results = List.of(
                result("A", Grade.C3), result("B", Grade.C3));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
        assertEquals(ProgressionStatus.PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void qcaExactlyTwoWithD1Progresses() {
        // (2.4 x 6 + 1.6 x 6) / 12 = 2.00
        List<ModuleResult> results = List.of(
                result("A", Grade.C2), result("B", Grade.D1));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
    }

    @Test
    void qcaJustBelowTwoDoesNotProgress() {
        // (2.0 x 6 + 1.6 x 6) / 12 = 1.80
        List<ModuleResult> results = List.of(
                result("A", Grade.C3), result("B", Grade.D1));

        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, undergrad.evaluate(results));
        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void qcaWellAboveTwoProgresses() {
        List<ModuleResult> results = List.of(
                result("A", Grade.A1), result("B", Grade.B1));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
    }

    // ---------- Deficient grades (MS 2.5.4) ----------

    @Test
    void failBlocksProgressionEvenWithHighQca() {
        // QCA = (0 + 4 + 4 + 4) / 4 = 3.00, but F earns no credits
        List<ModuleResult> results = List.of(
                result("A", Grade.F), result("B", Grade.A1),
                result("C", Grade.A1), result("D", Grade.A1));

        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, undergrad.evaluate(results));
        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void everyDeficientGradeBlocksProgression() {
        Grade[] deficient = {Grade.F, Grade.NG, Grade.I, Grade.I2, Grade.M, Grade.N};

        for (Grade grade : deficient) {
            List<ModuleResult> results = List.of(
                    result("A", grade), result("B", Grade.A1), result("C", Grade.A1));

            assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, undergrad.evaluate(results),
                    grade + " should block undergraduate progression");
            assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(results),
                    grade + " should block postgraduate progression");
        }
    }

    // ---------- D1 compensation (Table 2.1) ----------

    @Test
    void d1IsCompensatedWhenQcaIsHighEnough() {
        // (1.6 x 6 + 3.2 x 6) / 12 = 2.40
        List<ModuleResult> results = List.of(
                result("A", Grade.D1), result("B", Grade.B1));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
    }

    // ---------- Non-quality grades ----------

    @Test
    void onlyPassAndExemptGradesProgress() {
        // No quality-graded results, so there is no QCA to check
        List<ModuleResult> results = List.of(
                result("A", Grade.P), result("B", Grade.EX));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
        assertEquals(ProgressionStatus.PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void passGradeDoesNotRaiseALowQca() {
        // P is excluded from QCA, so the QCA is the D1 alone: 1.60
        List<ModuleResult> results = List.of(
                result("A", Grade.P), result("B", Grade.D1));

        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, undergrad.evaluate(results));
    }

    // ---------- Empty and invalid input ----------

    @Test
    void noResultsDoesNotProgress() {
        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, undergrad.evaluate(new ArrayList<>()));
        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(new ArrayList<>()));
    }

    @Test
    void nullResultsAreRejected() {
        assertThrows(NullPointerException.class, () -> undergrad.evaluate(null));
        assertThrows(NullPointerException.class, () -> postgrad.evaluate(null));
    }

    @Test
    void nullResultInsideListIsRejected() {
        List<ModuleResult> results = Arrays.asList(result("A", Grade.A1), null);

        assertThrows(NullPointerException.class, () -> undergrad.evaluate(results));
        assertThrows(NullPointerException.class, () -> postgrad.evaluate(results));
    }

    // ---------- Postgraduate dissertation (MS 2.7.1) ----------

    @Test
    void postgradD1InDissertationDoesNotProgress() {
        // QCA = (1.6 + 4.0) / 2 = 2.80, but the dissertation needs C3
        List<ModuleResult> results = List.of(
                dissertation(Grade.D1), result("B", Grade.A1));

        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void undergradD1InProjectIsCompensated() {
        // Same results as above: the undergraduate rule has no dissertation check
        List<ModuleResult> results = List.of(
                dissertation(Grade.D1), result("B", Grade.A1));

        assertEquals(ProgressionStatus.PROGRESS, undergrad.evaluate(results));
    }

    @Test
    void postgradC3InDissertationProgresses() {
        List<ModuleResult> results = List.of(
                dissertation(Grade.C3), result("B", Grade.B1));

        assertEquals(ProgressionStatus.PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void postgradD1InTaughtModuleIsCompensated() {
        List<ModuleResult> results = List.of(
                result("A", Grade.D1), result("B", Grade.A1));

        assertEquals(ProgressionStatus.PROGRESS, postgrad.evaluate(results));
    }

    @Test
    void postgradRecognisesDissertationInTitle() {
        List<ModuleResult> results = List.of(
                result("CS6999", "Research Dissertation", Grade.D1),
                result("B", Grade.A1));

        assertEquals(ProgressionStatus.DOES_NOT_PROGRESS, postgrad.evaluate(results));
    }

    // ---------- Each programme supplies its own rule ----------

    @Test
    void programmesSupplyTheirOwnRule() {
        assertInstanceOf(UndergraduateProgressionRule.class,
                new UndergraduateProgramme("LM051", "Computer Systems", "CSIS").getProgressionRule());

        assertInstanceOf(PostgraduateProgressionRule.class,
                new PostgraduateProgramme("MSCSE", "Software Engineering", "CSIS").getProgressionRule());
    }

    // ---------- ProgressionStatus ----------

    @Test
    void everyStatusHasADescription() {
        for (ProgressionStatus status : ProgressionStatus.values()) {
            assertFalse(status.getDescription().isBlank(), status + " needs a description");
        }
    }
}
