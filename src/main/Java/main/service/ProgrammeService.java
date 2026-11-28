package main.service;

import main.model.Module;
import main.model.Programme;
import main.repository.ModuleRepository;
import main.repository.ProgrammeRepository;

import java.util.Objects;

/**
 * Adds and finds programmes and modules.
 *
 * <p>Checks new programmes and modules before saving them, so the CSV files
 * stay valid. Any problem (a duplicate code, a blank field, a comma that
 * would break the CSV file) is reported with an IllegalArgumentException
 * whose message can be shown to the user.</p>
 */
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final ModuleRepository moduleRepository;

    /**
     * Creates the service.
     *
     * @param programmeRepository stores programmes (programme.csv)
     * @param moduleRepository    stores modules (modules.csv)
     */
    public ProgrammeService(ProgrammeRepository programmeRepository,
                            ModuleRepository moduleRepository) {

        this.programmeRepository = Objects.requireNonNull(
                programmeRepository, "Programme repository must not be null");
        this.moduleRepository = Objects.requireNonNull(
                moduleRepository, "Module repository must not be null");
    }

    /**
     * Adds a new programme.
     *
     * @param p the programme to add
     * @throws IllegalArgumentException if a field is blank or contains a
     *                                  comma, or the code is already used
     */
    public void addProgramme(Programme p) {
        Objects.requireNonNull(p, "Programme must not be null");

        checkField("Programme code", p.getCode());
        checkField("Programme title", p.getTitle());
        checkField("Department", p.getDepartment());

        if (findProgramme(p.getCode()) != null) {
            throw new IllegalArgumentException(
                    "Programme " + p.getCode() + " already exists");
        }

        programmeRepository.save(p);
    }

    /**
     * Adds a new module. Credits and semester are already checked by the
     * Module constructor.
     *
     * @param m the module to add
     * @throws IllegalArgumentException if a field is blank or contains a
     *                                  comma, or the code is already used
     */
    public void addModule(Module m) {
        Objects.requireNonNull(m, "Module must not be null");

        checkField("Module code", m.getCode());
        checkField("Module title", m.getTitle());
        checkField("Lecturer ID", m.getLecturerId());

        if (findModule(m.getCode()) != null) {
            throw new IllegalArgumentException(
                    "Module " + m.getCode() + " already exists");
        }

        moduleRepository.save(m);
    }

    /**
     * Finds a programme by its code.
     *
     * @param code the programme code, for example "LM051"
     * @return the programme, or null if there is none with that code
     */
    public Programme findProgramme(String code) {
        if (code == null) {
            return null;
        }
        return programmeRepository.findByCode(code.trim());
    }

    /**
     * Finds a module by its code.
     *
     * @param code the module code, for example "CS4013"
     * @return the module, or null if there is none with that code
     */
    public Module findModule(String code) {
        if (code == null) {
            return null;
        }
        return moduleRepository.findByCode(code.trim());
    }

    /**
     * Checks that a field is filled in and has no comma, because a comma
     * would split the field in two when the CSV file is read back.
     */
    private void checkField(String name, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (value.contains(",")) {
            throw new IllegalArgumentException(name + " must not contain a comma");
        }
    }
}

