package main.model;

import java.util.Objects;

/**
 * Represents a student's result for one module in one academic year
 * and semester.
 *
 * Handbook references:
 * - MS 2.5: Grading
 * - SP 3.3.1: Quality Credit Average (QCA)
 *
 * Quality Credit Score (QCS) = Grade QPV x Module Credits.
 *
 * Credits awarded:
 * - A1 to D1: Full credits
 * - P: Full credits
 * - EX: Full credits
 * - F, NG, N, I, I2 and M: No credits
 *
 * QCA calculation:
 * - Quality-graded results from A1 to NG count towards QCA.
 * - F and NG count towards QCA with 0.00 QPV.
 * - P and N are excluded from QCA.
 * - Administrative grades are excluded from QCA.
 * - EX awards credits but does not count towards QCA.
 */
public class ModuleResult {

    private final Student student;
    private final Module module;
    private final int year;
    private final int semester;
    private final Grade grade;

    /**
     * Creates a result for a student in a particular module.
     *
     * @param student the student receiving the result
     * @param module the module being assessed
     * @param year the starting year of the academic year
     * @param semester the semester (1 or 2)
     * @param grade the grade achieved
     */
    public ModuleResult(Student student, Module module,
                        int year, int semester, Grade grade) {

        this.student = Objects.requireNonNull(
                student, "Student must not be null");

        this.module = Objects.requireNonNull(
                module, "Module must not be null");

        this.grade = Objects.requireNonNull(
                grade, "Grade must not be null");

        if (year < 1) {
            throw new IllegalArgumentException(
                    "Academic year must be a positive integer");
        }

        if (semester != 1 && semester != 2) {
            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2");
        }

        this.year = year;
        this.semester = semester;
    }

    // ---------- Getters ----------

    public Student getStudent() {
        return student;
    }

    public Module getModule() {
        return module;
    }

    /**
     * Returns the starting year of the academic year.
     * Example: 2026 represents academic year 2026/27.
     * This is not the year of study.
     */
    public int getYear() {
        return year;
    }

    public int getSemester() {
        return semester;
    }

    public Grade getGrade() {
        return grade;
    }

    // ---------- QCA Calculations ----------

    /**
     * Calculates the Quality Credit Score (QCS).
     *
     * QCS = Grade QPV x Module Credits.
     *
     * Returns zero for grades excluded from QCA,
     * including P, N and administrative grades.
     *
     * F and NG are included with a QPV of zero.
     *
     * @return the quality credit score
     */
    public double getQualityCreditScore() {

        if (!grade.countsTowardsQca()) {
            return 0.0;
        }

        return grade.getQcaPoints() * module.getCredits();
    }

    /**
     * Compatibility method for existing project classes.
     *
     * This returns the QCS, not the individual grade QPV.
     * Prefer {@link #getQualityCreditScore()} in new code.
     *
     * @return the quality credit score
     */
    public double getQpv() {
        return getQualityCreditScore();
    }

    /**
     * Returns the credits earned for this module.
     *
     * @return full module credits if awarded, otherwise zero
     */
    public int getCreditsEarned() {

        return grade.isCreditsAwarded()
                ? module.getCredits()
                : 0;
    }

    /**
     * Returns the credit hours included in the QCA denominator.
     *
     * Quality-graded attempts count, including F and NG.
     * P, N and administrative grades are excluded.
     *
     * @return the QCA credit hours
     */
    public int getQcaCredits() {

        return grade.countsTowardsQca()
                ? module.getCredits()
                : 0;
    }

    // ---------- Result Classification ----------

    /**
     * Checks whether this result is an ordinary pass.
     *
     * Includes C3 or better and P.
     * D1 and EX are not ordinary passes.
     *
     * @return true if the result is an ordinary pass
     */
    public boolean isPass() {
        return grade.isPass();
    }

    /**
     * Checks whether this result is a pass or an exemption.
     *
     * @return true for an ordinary pass or EX
     */
    public boolean isPassOrExempt() {
        return grade.isPassOrExempt();
    }

    /**
     * Checks whether this result is an exemption.
     *
     * @return true if the grade is EX
     */
    public boolean isExempt() {
        return grade == Grade.EX;
    }

    /**
     * Checks whether this result is a compensating fail.
     *
     * @return true if the grade is D1
     */
    public boolean isCompensatingFail() {
        return grade.isCompensatingFail();
    }

    /**
     * Checks whether this result is deficient.
     *
     * @return true if the grade is deficient
     */
    public boolean isDeficient() {
        return grade.isDeficient();
    }

    /**
     * Checks whether this result is classified as a fail.
     *
     * @return true if the grade is F, NG or N
     */
    public boolean isFail() {
        return grade.isFail();
    }

    /**
     * Checks whether this result is an administrative grade.
     *
     * @return true if the grade is administrative
     */
    public boolean isAdministrative() {
        return grade.isAdministrative();
    }

    // ---------- Object Representation ----------

    @Override
    public String toString() {
        return module.getCode() + " " + grade
                + " (" + year + " Sem " + semester + ")";
    }
}
