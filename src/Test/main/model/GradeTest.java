package main.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GradeTest {

    private static final double DELTA = 0.001;

    // ---------- Table 2.1: Quality Point Values ----------

    @Test
    void qualityPointsMatchHandbook() {
        assertEquals(4.00, Grade.A1.getQcaPoints(), DELTA);
        assertEquals(3.60, Grade.A2.getQcaPoints(), DELTA);
        assertEquals(3.20, Grade.B1.getQcaPoints(), DELTA);
        assertEquals(3.00, Grade.B2.getQcaPoints(), DELTA);
        assertEquals(2.80, Grade.B3.getQcaPoints(), DELTA);
        assertEquals(2.60, Grade.C1.getQcaPoints(), DELTA);
        assertEquals(2.40, Grade.C2.getQcaPoints(), DELTA);
        assertEquals(2.00, Grade.C3.getQcaPoints(), DELTA);
        assertEquals(1.60, Grade.D1.getQcaPoints(), DELTA);
        assertEquals(0.00, Grade.F.getQcaPoints(), DELTA);
        assertEquals(0.00, Grade.NG.getQcaPoints(), DELTA);
    }

    // ---------- Pass / Fail / Exemption Logic ----------

    @Test
    void gradesFromA1ToC3ArePasses() {
        Set<Grade> passingGrades = EnumSet.of(
                Grade.A1, Grade.A2, Grade.B1, Grade.B2,
                Grade.B3, Grade.C1, Grade.C2, Grade.C3
        );

        for (Grade grade : passingGrades) {
            assertTrue(grade.isPass(), grade + " should be a pass");
        }
    }

    @Test
    void d1IsNotAnOrdinaryPass() {
        assertFalse(Grade.D1.isPass());
        assertTrue(Grade.D1.isCompensatingFail());
    }

    @Test
    void failGradesAreNotPasses() {
        assertFalse(Grade.F.isPass());
        assertFalse(Grade.NG.isPass());
        assertFalse(Grade.N.isPass());
    }

    @Test
    void passFailGradesAreHandledCorrectly() {
        assertTrue(Grade.P.isPass());
        assertFalse(Grade.N.isPass());
    }

    @Test
    void exemptionIsSeparateFromOrdinaryPass() {
        assertFalse(Grade.EX.isPass());
        assertTrue(Grade.EX.isCreditsAwarded());
    }

    @Test
    void incompleteGradesAreNotPasses() {
        assertFalse(Grade.I.isPass());
        assertFalse(Grade.I2.isPass());
        assertFalse(Grade.M.isPass());
    }

    // ---------- Compensating Fail ----------

    @Test
    void onlyD1IsACompensatingFail() {
        for (Grade grade : Grade.values()) {
            assertEquals(
                    grade == Grade.D1,
                    grade.isCompensatingFail(),
                    grade + " compensating-fail status is incorrect"
            );
        }
    }

    // ---------- Fail Classification ----------

    @Test
    void onlyFNgAndNAreClassifiedAsFails() {
        for (Grade grade : Grade.values()) {
            boolean expectedFail =
                    grade == Grade.F ||
                    grade == Grade.NG ||
                    grade == Grade.N;

            assertEquals(
                    expectedFail,
                    grade.isFail(),
                    grade + " fail status is incorrect"
            );
        }
    }

    // ---------- Credits ----------

    @Test
    void qualityGradesAndD1AwardCredits() {
        Set<Grade> creditedGrades = EnumSet.of(
                Grade.A1, Grade.A2, Grade.B1, Grade.B2,
                Grade.B3, Grade.C1, Grade.C2, Grade.C3,
                Grade.D1
        );

        for (Grade grade : creditedGrades) {
            assertTrue(
                    grade.isCreditsAwarded(),
                    grade + " should award credits"
            );
        }
    }

    @Test
    void failGradesDoNotAwardCredits() {
        assertFalse(Grade.F.isCreditsAwarded());
        assertFalse(Grade.NG.isCreditsAwarded());
        assertFalse(Grade.N.isCreditsAwarded());
    }

    @Test
    void passAndExemptionAwardCredits() {
        assertTrue(Grade.P.isCreditsAwarded());
        assertTrue(Grade.EX.isCreditsAwarded());
    }

    @Test
    void otherAdministrativeGradesDoNotAwardCredits() {
        for (Grade grade : Grade.values()) {
            if (grade.isAdministrative() && grade != Grade.EX) {
                assertFalse(
                        grade.isCreditsAwarded(),
                        grade + " should not award credits"
                );
            }
        }
    }

    // ---------- QCA Inclusion ----------

    @Test
    void passFailGradesDoNotCountTowardsQca() {
        assertFalse(Grade.P.countsTowardsQca());
        assertFalse(Grade.N.countsTowardsQca());
    }

    @Test
    void qualityGradedResultsCountTowardsQca() {
        assertTrue(Grade.A1.countsTowardsQca());
        assertTrue(Grade.C3.countsTowardsQca());
        assertTrue(Grade.D1.countsTowardsQca());
        assertTrue(Grade.F.countsTowardsQca());
        assertTrue(Grade.NG.countsTowardsQca());
    }

    @Test
    void administrativeGradesDoNotCountTowardsQca() {
        for (Grade grade : Grade.values()) {
            if (grade.isAdministrative()) {
                assertFalse(
                        grade.countsTowardsQca(),
                        grade + " should not count towards QCA"
                );
            }
        }
    }

    // ---------- Deficient Grades ----------

    @Test
    void deficientGradesAreCorrectlyIdentified() {
        Set<Grade> deficientGrades = EnumSet.of(
                Grade.F, Grade.NG, Grade.I,
                Grade.I2, Grade.M, Grade.N
        );

        for (Grade grade : Grade.values()) {
            assertEquals(
                    deficientGrades.contains(grade),
                    grade.isDeficient(),
                    grade + " deficient status is incorrect"
            );
        }
    }

    @Test
    void d1IsNotDeficient() {
        assertFalse(Grade.D1.isDeficient());
    }

    // ---------- Administrative Grades ----------

    @Test
    void administrativeGradesAreCorrectlyIdentified() {
        Set<Grade> administrativeGrades = EnumSet.of(
                Grade.EX, Grade.R, Grade.G, Grade.I,
                Grade.I2, Grade.M, Grade.RE, Grade.X,
                Grade.Y, Grade.W
        );

        for (Grade grade : Grade.values()) {
            assertEquals(
                    administrativeGrades.contains(grade),
                    grade.isAdministrative(),
                    grade + " administrative status is incorrect"
            );
        }
    }

    // ---------- QCA Point Range ----------

    @Test
    void allQcaPointsAreBetweenZeroAndFour() {
        for (Grade grade : Grade.values()) {
            assertTrue(
                    grade.getQcaPoints() >= 0.0 &&
                    grade.getQcaPoints() <= 4.0,
                    grade + " has an invalid QCA point value"
            );
        }
    }

    // ---------- Annual repeat cap (SP 3.2.6.1) ----------

    @Test
    void capAtC3LowersGradesBetterThanC3() {
        assertEquals(Grade.C3, Grade.A1.capAtC3());
        assertEquals(Grade.C3, Grade.A2.capAtC3());
        assertEquals(Grade.C3, Grade.B1.capAtC3());
        assertEquals(Grade.C3, Grade.B2.capAtC3());
        assertEquals(Grade.C3, Grade.B3.capAtC3());
        assertEquals(Grade.C3, Grade.C1.capAtC3());
        assertEquals(Grade.C3, Grade.C2.capAtC3());
    }

    @Test
    void capAtC3LeavesOtherGradesUnchanged() {
        assertEquals(Grade.C3, Grade.C3.capAtC3());
        assertEquals(Grade.D1, Grade.D1.capAtC3());
        assertEquals(Grade.F, Grade.F.capAtC3());
        assertEquals(Grade.NG, Grade.NG.capAtC3());
        assertEquals(Grade.P, Grade.P.capAtC3());
        assertEquals(Grade.N, Grade.N.capAtC3());
        assertEquals(Grade.EX, Grade.EX.capAtC3());
        assertEquals(Grade.I, Grade.I.capAtC3());
    }

    // ---------- Higher of original and repeat (SP 3.2.6) ----------

    @Test
    void higherOfReturnsRepeatWhenBetter() {
        assertEquals(Grade.C3, Grade.higherOf(Grade.F, Grade.C3));
        assertEquals(Grade.C3, Grade.higherOf(Grade.D1, Grade.C3));
    }

    @Test
    void higherOfKeepsOriginalWhenBetter() {
        assertEquals(Grade.B1, Grade.higherOf(Grade.B1, Grade.C3));
        assertEquals(Grade.D1, Grade.higherOf(Grade.D1, Grade.F));
    }

    @Test
    void higherOfReturnsFirstWhenEqual() {
        assertEquals(Grade.C3, Grade.higherOf(Grade.C3, Grade.C3));
        assertEquals(Grade.F, Grade.higherOf(Grade.F, Grade.NG));
    }

    @Test
    void incompleteDuringRepeatKeepsOriginalGrade() {
        // SP 3.2.3.14
        assertEquals(Grade.F, Grade.higherOf(Grade.F, Grade.I));
        assertEquals(Grade.D1, Grade.higherOf(Grade.D1, Grade.I));
    }

    @Test
    void realGradeReplacesIncomplete() {
        assertEquals(Grade.C3, Grade.higherOf(Grade.I, Grade.C3));
    }

     @Test
    void passOnRepeatReplacesEarlierN() {
        // Pass/fail module: N then P must end as P.
        assertEquals(Grade.P, Grade.higherOf(Grade.N, Grade.P));
    }

    @Test
    void nOnRepeatDoesNotReplaceEarlierP() {
        assertEquals(Grade.P, Grade.higherOf(Grade.P, Grade.N));
    }

    @Test
    void passBeatsIncompleteButNotAnotherPass() {
        assertEquals(Grade.P, Grade.higherOf(Grade.I, Grade.P));
        assertEquals(Grade.P, Grade.higherOf(Grade.P, Grade.I));
        assertEquals(Grade.P, Grade.higherOf(Grade.P, Grade.P));
    }

    @Test
    void higherOfRejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> Grade.higherOf(null, Grade.A1));
        assertThrows(NullPointerException.class,
                () -> Grade.higherOf(Grade.A1, null));
    }
}