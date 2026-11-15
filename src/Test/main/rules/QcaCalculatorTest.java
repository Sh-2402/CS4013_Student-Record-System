package main.rules;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import main.model.Grade;
import main.model.Module;
import main.model.ModuleResult;
import main.model.Student;

class QcaCalculatorTest {

    private final QcaCalculator calc = new QcaCalculator();
    private final Student s = new Student("Ann", "2501234", "ann@ul.ie", null);

    private ModuleResult result(String code, int credits, Grade g) {
        Module m = new Module(code, "Test module", credits, 1, "L001");
        return new ModuleResult(s, m, 2026, 1, g);   // use YOUR constructor order
    }

    @Test
    void twoEqualModules() {
        // (3.6x6 + 2.6x6) / 12 = 3.1
        List<ModuleResult> list = List.of(result("A", 6, Grade.A2), result("B", 6, Grade.C1));
        assertEquals(3.1, calc.overallQca(list), 0.001);
    }

    @Test
    void differentCreditsAreWeighted() {
        // (4.0x10 + 2.0x5) / 15 = 3.333...
        List<ModuleResult> list = List.of(result("A", 10, Grade.A1), result("B", 5, Grade.C3));
        assertEquals(3.3333, calc.overallQca(list), 0.001);
    }

    @Test
    void emptyListGivesZero() {
        assertEquals(0.0, calc.overallQca(List.of()), 0.001);
    }

    @Test
    void failPullsQcaDown() {
        // (4.0x6 + 0.0x6) / 12 = 2.0
        List<ModuleResult> list = List.of(result("A", 6, Grade.A1), result("B", 6, Grade.F));
        assertEquals(2.0, calc.overallQca(list), 0.001);
    }

    @Test
    void passFailModuleIsIgnored() {
        // P has no QPV, so only the A1 counts: 4.0
        List<ModuleResult> list = List.of(result("A", 6, Grade.A1), result("B", 6, Grade.P));
        assertEquals(4.0, calc.overallQca(list), 0.001);
    }
}