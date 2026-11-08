package main.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RegistrationTest {
    
    @Test 
    void yearZeroThrowsException(){
        assertThrows(IllegalArgumentException.class, () -> {
            Registration r = new Registration ("24142628","LM051", 2025, 0,"ACTIVE");
        });
    }

    @Test 
    void yearOneIsAllowed(){
        Registration r = new Registration ("24142628","LM051", 2025, 1,"ACTIVE");
        assertEquals(1, r.getCurrentYear());
    }

    @Test
    void advanceYearIncreasesYearByOne(){
        Registration r = new Registration ("24142628","LM051", 2025, 1,"ACTIVE");
        r.advanceYear();
        assertEquals(2, r.getCurrentYear());
    }

    @Test
    void invalidStatusThrowsException(){
        assertThrows(IllegalArgumentException.class, () -> {
            Registration r = new Registration ("24142628","LM051", 2025, 1,"Banana");
        });

    }

    @Test 
    void changeStatusUpdatesStatus(){
        Registration r = new Registration ("24142628","LM051", 2025, 1,"ACTIVE");
        r.changeStatus("REPEATING");
        assertEquals("REPEATING", r.getStatus());
    }

    @Test 
    void changeStatusToInvalidThrowsException(){
        Registration r = new Registration ("24142628","LM051", 2025, 1,"ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> {
            r.changeStatus("Banana");
        });
    }
     


}
