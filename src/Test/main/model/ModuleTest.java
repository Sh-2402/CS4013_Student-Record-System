package main.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ModuleTest {
    
    @Test 
    void getCreditsReturnCredits(){
        Module m = new Module ("CS101", "Introduction to Programming", 6, 1, "L001");
        assertEquals(6, m.getCredits());
    }

    @Test 
    void getCodeReturnCode(){
        Module m = new Module ("CS101", "Introduction to Programming", 6, 1, "L001");
        assertEquals("CS101", m.getCode());
    }

    @Test 
    void zeroCreditsThrowsException(){
        assertThrows(IllegalArgumentException.class, () -> {
            Module m = new Module ("CS101", "Introduction to Programming", 0, 1, "L001");
        });
    }

    @Test
    void semesterThreeThrowsException(){
        assertThrows(IllegalArgumentException.class, () -> {
            Module m = new Module ("CS101", "Introduction to Programming", 6, 3, "L001");
        });
    }

    @Test 
    void semesterTwoIsAllowed(){
        Module m = new Module ("CS101", "Introduction to Programming", 6, 2, "L001");
        assertEquals(2, m.getSemester());
    }

}
