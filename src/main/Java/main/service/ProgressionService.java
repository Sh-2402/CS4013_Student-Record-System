package main.service;

import main.model.ModuleResult;
import main.model.Programme;
import main.model.Registration;
import main.model.Student;
import main.repository.StudentRepository;
import main.rules.ProgressionRule;
import main.rules.ProgressionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Checks whether students meet the minimum academic standard and can
 * progress (UL Handbook 2026/27, AR 1.5.3). Used by the department to
 * review students at the end of the year.
 *
 * <p>The service does not decide the rule itself. It asks the student's
 * programme for its rule ({@link Programme#getProgressionRule()}) and calls
 * {@link ProgressionRule#evaluate} on whichever rule it gets back, so
 * undergraduate and postgraduate students are handled polymorphically.</p>
 */
public class ProgressionService {

    /** Registration status of a student who has left the University. */
    private static final String WITHDRAWN = "WITHDRAWN";

    private final StudentRepository studentRepository;
    private final TranscriptService transcriptService;
    private final RegistrationService registrationService;
    private final ProgrammeService programmeService;

    /**
     * Creates the service.
     *
     * @param studentRepository   source of all students
     * @param transcriptService   gives the grades that stand for each student
     *                            after repeat rules are applied
     * @param registrationService finds the programme a student is registered on
     * @param programmeService    finds a programme by its code
     */
    public ProgressionService(StudentRepository studentRepository,
                              TranscriptService transcriptService,
                              RegistrationService registrationService,
                              ProgrammeService programmeService) {

        this.studentRepository = Objects.requireNonNull(
                studentRepository, "Student repository must not be null");
        this.transcriptService = Objects.requireNonNull(
                transcriptService, "Transcript service must not be null");
        this.registrationService = Objects.requireNonNull(
                registrationService, "Registration service must not be null");
        this.programmeService = Objects.requireNonNull(
                programmeService, "Programme service must not be null");
    }

    /**
     * Returns the progression rule for a programme.
     *
     * @param p the programme
     * @return the rule supplied by that programme
     * @throws NullPointerException if p is null
     */
    public ProgressionRule selectRule(Programme p) {
        Objects.requireNonNull(p, "Programme must not be null");
        return p.getProgressionRule();
    }

    /**
     * Checks whether one student meets the minimum standard.
     *
     * @param id the student's ID
     * @return the student's progression status
     * @throws IllegalArgumentException if the student does not exist or is
     *                                  not registered on a programme
     */
    public ProgressionStatus evaluate(String id) {
        if (studentRepository.findById(id) == null) {
            throw new IllegalArgumentException("Student " + id + " does not exist");
        }

        Programme programme = findProgrammeOf(id);

        if (programme == null) {
            throw new IllegalArgumentException(
                    "Student " + id + " is not registered on a programme");
        }

        ProgressionRule rule = selectRule(programme);
        List<ModuleResult> results = transcriptService.getStandingResults(id);

        return rule.evaluate(results);
    }

    /**
     * Returns every registered student who does not meet the minimum
     * standard. Students who are not registered on a programme, or who have
     * withdrawn, are skipped, because there is no programme rule to check
     * them against.
     *
     * @return students whose status is DOES_NOT_PROGRESS
     */
    public List<Student> listNotProgressing() {
        List<Student> notProgressing = new ArrayList<>();

        for (Student student : studentRepository.loadAll()) {
            Registration registration =
                    registrationService.findRegistration(student.getId());

            if (registration == null
                    || WITHDRAWN.equals(registration.getStatus())
                    || findProgrammeOf(student.getId()) == null) {
                continue;
            }

            if (evaluate(student.getId()) == ProgressionStatus.DOES_NOT_PROGRESS) {
                notProgressing.add(student);
            }
        }

        return notProgressing;
    }

    /**
     * Returns the programme a student is registered on.
     *
     * @param id the student's ID
     * @return the programme, or null if the student is not registered
     */
    private Programme findProgrammeOf(String id) {
        Registration registration = registrationService.findRegistration(id);

        if (registration == null) {
            return null;
        }
        return programmeService.findProgramme(registration.getProgrammeCode());
    }
}
