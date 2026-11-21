package main.rules;

/**
 * The result of a progression check for one student at the end of an
 * academic year.
 *
 * <p>UL Handbook of Academic Regulations and Procedures 2026/27: students
 * who meet the minimum standard proceed to the next year (AR 1.5.4).
 * Students who do not are referred to the Faculty Examination Board, which
 * decides whether they take annual repeats, repeat a semester or year, or
 * have their enrolment terminated (SP 3.3.6.2).</p>
 */
public enum ProgressionStatus {

    /** Meets the minimum standard (AR 1.5.3) and proceeds to the next year. */
    PROGRESS("Meets the minimum standard and progresses to the next year"),

    /** Does not meet the minimum standard and is referred to the exam board. */
    DOES_NOT_PROGRESS("Does not meet the minimum standard; referred to the exam board");

    private final String description;

    ProgressionStatus(String description) {
        this.description = description;
    }

    /**
     * Returns a readable description for menus and reports.
     *
     * @return the description of this status
     */
    public String getDescription() {
        return description;
    }
}
