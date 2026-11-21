package main.service;

import main.model.Registration;
import main.repository.RegistrationRepository;

public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final StudentService studentService;
    private final ProgrammeService programmeService;

    public RegistrationService(RegistrationRepository registrationRepository, StudentService studentService,
            ProgrammeService programmeService) {
        this.registrationRepository = registrationRepository;
        this.studentService = studentService;
        this.programmeService = programmeService;
    }

    public Registration findRegistration(String studentId) {
        return registrationRepository.findByStudent(studentId);
    }

    public void register(String studentId, String programmeCode) {

        if (!studentService.idExists(studentId)) {
            throw new IllegalArgumentException("Student " + studentId + " does not exist");
        }

        if (programmeService.findProgramme(programmeCode) == null) {
            throw new IllegalArgumentException("Programme " + programmeCode + " does not exist");
        }

        if (findRegistration(studentId) != null) {
            throw new IllegalArgumentException("Student " + studentId + " is already registered");
        }

        int thisYear = java.time.Year.now().getValue();
        Registration reg = new Registration(studentId, programmeCode, thisYear, 1, "ACTIVE");
        registrationRepository.save(reg);
    }

}
