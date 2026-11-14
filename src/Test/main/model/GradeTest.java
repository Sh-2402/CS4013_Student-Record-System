package main.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GradeTest {

    @Test
    void pointsMatchHandbook() {
        assertEquals(4.00, Grade.A1.getQcaPoints(), 0.001);
        assertEquals(3.60, Grade.A2.getQcaPoints(), 0.001);
        assertEquals(3.20, Grade.B1.getQcaPoints(), 0.001);
        assertEquals(1.60, Grade.D1.getQcaPoints(), 0.001);
    }

    @Test
    void c3IsPassButD1IsNot() {
        assertTrue(Grade.C3.isPass());
        assertFalse(Grade.D1.isPass());
    }

    @Test
    void failsAwardNoCredits() {
        assertFalse(Grade.F.isCreditsAwarded());
        assertFalse(Grade.NG.isCreditsAwarded());
        assertTrue(Grade.D1.isCreditsAwarded());
    }

    @Test
    void passFailGradesAreLeftOutOfQca() {
        assertFalse(Grade.P.countsTowardsQca());
        assertTrue(Grade.F.countsTowardsQca());
    }
}