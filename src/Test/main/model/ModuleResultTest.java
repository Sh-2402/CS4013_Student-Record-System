package main.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


class ModuleResultTest {

     @Test
    void qualityPointsAreGradePointsTimesCredits() {
        Module m = new Module("CS4013", "Software Dev", 6, 1, "L001");
        Student s = new Student("Ann", "2501234", "ann@ul.ie", null);
        ModuleResult r = new ModuleResult(s, m, 2026, 1, Grade.A2);
        assertEquals(21.6, r.getQpv(), 0.001);
        assertEquals(6, r.getCreditsEarned());
    }

    @Test
    void failEarnsNoCredits() {
        Module m = new Module("CS4013", "Software Dev", 6, 1, "L001");
        Student s = new Student("Ann", "2501234", "ann@ul.ie", null);
        ModuleResult r = new ModuleResult(s, m, 2026, 1, Grade.F);
        assertEquals(0, r.getCreditsEarned());
    }

    @Test
    void toStringShowsModuleCodeAndGrade() {
    Module m = new Module("CS4013", "Software Dev", 6, 1, "L001");
    Student s = new Student("Ann", "2501234", "ann@ul.ie", null);
    ModuleResult r = new ModuleResult(s, m, 2026, 1, Grade.A2);
    assertEquals("CS4013 A2 (2026 Sem 1)", r.toString());
}

    
}
