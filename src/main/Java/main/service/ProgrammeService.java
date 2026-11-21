package main.service;

import main.model.Module;
import main.model.Programme;
import main.repository.ModuleRepository;
import main.repository.ProgrammeRepository;

public class ProgrammeService {
    private final ProgrammeRepository programmeRepository;
    private final ModuleRepository moduleRepository;

    public ProgrammeService(ProgrammeRepository programmeRepository, ModuleRepository moduleRepository) {
        this.programmeRepository = programmeRepository;
        this.moduleRepository = moduleRepository;
    }

    public Programme findProgramme(String code) {
        return programmeRepository.findByCode(code);
    }

    public Module findModule(String code) {
        return moduleRepository.findByCode(code);
    }

    public void addProgramme(Programme p) {
        if (findProgramme(p.getCode()) != null) {
            throw new IllegalArgumentException("Programme " + p.getCode() + " already exists");
        }
        programmeRepository.save(p);
    }

    public void addModule(Module m) {
        if (findModule(m.getCode()) != null) {
            throw new IllegalArgumentException("Module " + m.getCode() + " already exists");
        }
        moduleRepository.save(m);
    }

}
