package main.model;

import java.util.Objects;

/**
 * One student's grade in one module for one semester.
 * Each ModuleResult matches one row of results.csv.
 * The grading rules themselves are handled by Grade.
 */
public class ModuleResult {

    private final Student student;
    private final Module module;
    private final int year;
    private final int semester;
    private final Grade grade;

    /**
     * Creates a result. Missing values or a semester other than 1 or 2
     * throw an exception, so an invalid result can't be created. 
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

     /**
     * Gets the student this result belongs to.
     *
     * @return the student
     */
    public Student getStudent() {
        return student;
    }
    
    /**
     * Gets the module this result is for.
     *
     * @return the module
     */
    public Module getModule() {
        return module;
    }

     /**
     * Gets the calendar year the academic year starts in (2026 means 2026/27).
     * This is not the year of study, like "year 2 of the course".
     *
     * @return the starting year of the academic year
     */
    public int getYear() {
        return year;
    }
     
     /**
     * Gets the semester the module was taken in.
     *
     * @return 1 or 2
     */
    public int getSemester() {
        return semester;
    }
     
     /**
     * Gets the grade achieved for this module.
     *
     * @return the grade
     */
    public Grade getGrade() {
        return grade;
    }

     /**
     * QCS = grade points x module credits.
     * Example: A1 (4.00) in a 6-credit module gives 24.
     * Grades left out of the QCA return 0.
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

     /**
     * Checks whether this result is a pass.
     *
     * @return true if the result is an ordinary pass
     */
    public boolean isPass() {
        return grade.isPass();
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

    // Prints a result like : CS101 A1 (2026 Sem 1)

    @Override
    public String toString() {
        return module.getCode() + " " + grade
                + " (" + year + " Sem " + semester + ")";
    }
}
