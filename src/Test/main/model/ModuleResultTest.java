package main.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModuleResultTest {

    private static final double DELTA = 0.001;

    private Module module(int credits) {
        return new Module("CS4013", "Software Dev", credits, 1, "L001");
    }

    private Student student() {
        return new Student("Ann", "2501234", "ann@ul.ie", null);
    }

    private ModuleResult result(Grade grade) {
        return new ModuleResult(student(), module(6), 2026, 1, grade);
    }

    // ---------- QCS / QPV calculations ----------

    @Test
    void qualityPointsAreGradePointsTimesCredits() {
        ModuleResult r = result(Grade.A2);

        assertEquals(21.6, r.getQpv(), DELTA);
        assertEquals(6, r.getCreditsEarned());
        assertEquals(6, r.getQcaCredits());
    }

    @Test
    void getQpvMatchesQualityCreditScore() {
        for (Grade grade : Grade.values()) {
            ModuleResult r = result(grade);
            assertEquals(r.getQualityCreditScore(), r.getQpv(), DELTA,
                    grade + " QPV and QCS should match");
        }
    }

    @Test
    void a1GivesFourTimesModuleCredits() {
        assertEquals(24.0, result(Grade.A1).getQualityCreditScore(), DELTA);
    }

    @Test
    void d1GivesQualityPointsAndCredits() {
        ModuleResult r = result(Grade.D1);

        assertEquals(9.6, r.getQpv(), DELTA);
        assertEquals(6, r.getCreditsEarned());
        assertEquals(6, r.getQcaCredits());

        assertTrue(r.isCompensatingFail());
        assertFalse(r.isPass());
        assertFalse(r.isDeficient());
    }

    @Test
    void qcsScalesWithModuleCredits() {
        ModuleResult r = new ModuleResult(
                student(), module(3), 2026, 1, Grade.B1);

        assertEquals(9.6, r.getQpv(), DELTA);
        assertEquals(3, r.getCreditsEarned());
        assertEquals(3, r.getQcaCredits());
    }

    // ---------- Quality-graded fail results ----------

    @Test
    void failEarnsNoCreditsButCountsInQca() {
        ModuleResult r = result(Grade.F);

        assertEquals(0, r.getCreditsEarned());
        assertEquals(0.0, r.getQpv(), DELTA);
        assertEquals(6, r.getQcaCredits());

        assertTrue(r.isDeficient());
        assertTrue(r.isFail());
        assertFalse(r.isPass());
    }

    @Test
    void ngEarnsNoCreditsButCountsInQca() {
        ModuleResult r = result(Grade.NG);

        assertEquals(0, r.getCreditsEarned());
        assertEquals(0.0, r.getQpv(), DELTA);
        assertEquals(6, r.getQcaCredits());

        assertTrue(r.isDeficient());
        assertTrue(r.isFail());
    }

    // ---------- Pass / Fail modules ----------

    @Test
    void passGradeEarnsCreditsButIsExcludedFromQca() {
        ModuleResult r = result(Grade.P);

        assertEquals(6, r.getCreditsEarned());
        assertEquals(0.0, r.getQpv(), DELTA);
        assertEquals(0, r.getQcaCredits());

        assertTrue(r.isPass());
        assertFalse(r.isDeficient());
        assertFalse(r.isFail());
    }

    @Test
    void nGradeEarnsNoCreditsAndIsExcludedFromQca() {
        ModuleResult r = result(Grade.N);

        assertEquals(0, r.getCreditsEarned());
        assertEquals(0, r.getQcaCredits());
        assertEquals(0.0, r.getQpv(), DELTA);

        assertFalse(r.isPass());
        assertTrue(r.isDeficient());
        assertTrue(r.isFail());
    }

    // ---------- Exemption ----------

    @Test
    void exemptionEarnsCreditsButIsExcludedFromQca() {
        ModuleResult r = result(Grade.EX);

        assertEquals(6, r.getCreditsEarned());
        assertEquals(0, r.getQcaCredits());
        assertEquals(0.0, r.getQpv(), DELTA);

        // EX is separate from an ordinary pass.
        assertTrue(r.isExempt());
        assertFalse(r.isPass());
        assertTrue(r.isPassOrExempt());
    }

    @Test
    void onlyExIsExempt() {
        for (Grade grade : Grade.values()) {
            assertEquals(grade == Grade.EX, result(grade).isExempt(),
                    grade + " exempt status is incorrect");
        }
    }

    // ---------- Pass classification ----------

    @Test
    void passOrExemptMatchesGrade() {
        for (Grade grade : Grade.values()) {
            assertEquals(grade.isPassOrExempt(),
                    result(grade).isPassOrExempt(),
                    grade + " pass-or-exempt status is incorrect");
        }
    }

    // ---------- Administrative grades ----------

    @Test
    void incompleteGradeEarnsNothingAndIsDeficient() {
        ModuleResult r = result(Grade.I);

        assertEquals(0, r.getCreditsEarned());
        assertEquals(0, r.getQcaCredits());
        assertEquals(0.0, r.getQpv(), DELTA);

        assertTrue(r.isDeficient());
        assertFalse(r.isPass());
        assertTrue(r.isAdministrative());
    }

    @Test
    void allAdministrativeGradesAreExcludedFromQca() {
        Grade[] administrativeGrades = {
                Grade.EX, Grade.R, Grade.G, Grade.I,
                Grade.I2, Grade.M, Grade.RE, Grade.X,
                Grade.Y, Grade.W
        };

        for (Grade grade : administrativeGrades) {
            ModuleResult r = result(grade);

            assertTrue(r.isAdministrative(), grade + " should be administrative");

            assertEquals(0, r.getQcaCredits(),
                    grade + " should be excluded from QCA");

            assertEquals(0.0, r.getQpv(), DELTA,
                    grade + " should have zero QCS");
        }
    }

    @Test
    void administrativeGradesOtherThanExAwardNoCredits() {
        Grade[] administrativeGrades = {
                Grade.R, Grade.G, Grade.I, Grade.I2,
                Grade.M, Grade.RE, Grade.X, Grade.Y, Grade.W
        };

        for (Grade grade : administrativeGrades) {
            assertEquals(0, result(grade).getCreditsEarned(),
                    grade + " should not award credits");
        }
    }

    @Test
    void qualityGradesAreNotAdministrative() {
        assertFalse(result(Grade.A1).isAdministrative());
        assertFalse(result(Grade.F).isAdministrative());
        assertFalse(result(Grade.P).isAdministrative());
    }

    // ---------- Validation ----------

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new ModuleResult(
                        null, module(6), 2026, 1, Grade.A1));

        assertThrows(NullPointerException.class,
                () -> new ModuleResult(
                        student(), null, 2026, 1, Grade.A1));

        assertThrows(NullPointerException.class,
                () -> new ModuleResult(
                        student(), module(6), 2026, 1, null));
    }

    @Test
    void invalidSemesterIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleResult(
                        student(), module(6), 2026, 0, Grade.A1));

        assertThrows(IllegalArgumentException.class,
                () -> new ModuleResult(
                        student(), module(6), 2026, 3, Grade.A1));
    }

    @Test
    void invalidAcademicYearIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModuleResult(
                        student(), module(6), 0, 1, Grade.A1));
    }

    // ---------- Getters ----------

    @Test
    void gettersReturnConstructorValues() {
        Student s = student();
        Module m = module(6);

        ModuleResult r = new ModuleResult(
                s, m, 2026, 2, Grade.B3);

        assertSame(s, r.getStudent());
        assertSame(m, r.getModule());
        assertEquals(2026, r.getYear());
        assertEquals(2, r.getSemester());
        assertEquals(Grade.B3, r.getGrade());
    }

    // ---------- toString ----------

    @Test
    void toStringShowsModuleCodeGradeYearAndSemester() {
        assertEquals(
                "CS4013 A2 (2026 Sem 1)",
                result(Grade.A2).toString()
        );
    }
}

