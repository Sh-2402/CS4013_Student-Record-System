package main.service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import main.model.UndergraduateProgramme;
import main.repository.CsvUtil;
import main.repository.ModuleRepository;
import main.repository.ProgrammeRepository;

public class ProgrammeServiceTest {

    @TempDir
    Path tempDir;

    ProgrammeService service;

    @BeforeEach
    void setUp() {
        String programmeFile = tempDir.resolve("programmes.csv").toString();
        String moduleFile = tempDir.resolve("modules.csv").toString();

        List<String[]> programmeHeader = new ArrayList<>();
        programmeHeader.add(new String[] { "code", "title", "department", "type" });
        CsvUtil.writeAll(programmeFile, programmeHeader);

        List<String[]> moduleHeader = new ArrayList<>();
        moduleHeader.add(new String[] { "code", "title", "credits", "semester", "lecturerId" });
        CsvUtil.writeAll(moduleFile, moduleHeader);

        service = new ProgrammeService(new ProgrammeRepository(programmeFile), new ModuleRepository(moduleFile));
    }

    @Test
    void addProgrammeThenFindIt() {
        service.addProgramme(new UndergraduateProgramme("LM051", "Computer Systems", "CSIS"));
        assertNotNull(service.findProgramme("LM051"));
    }

    @Test
    void duplicateProgrammeThrowsException() {
        service.addProgramme(new UndergraduateProgramme("LM051", "Computer Systems", "CSIS"));
        assertThrows(IllegalArgumentException.class, () -> {
            service.addProgramme(new UndergraduateProgramme("LM051", "Computer Systems", "CSIS"));
        });
    }

    @Test
    void findMissingProgrammeReturnsNull() {
        assertNull(service.findProgramme("XX999"));
    }

}
