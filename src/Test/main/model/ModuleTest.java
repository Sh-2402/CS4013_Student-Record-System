package main.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ModuleTest {
    
    @Test 
    void getCreditsReturnCredits(){
        Module m = new Module ("CS101", "Introduction to Programming", 6, 1, "L001");
        assertEquals(6, m.getCredits());
    }
}
