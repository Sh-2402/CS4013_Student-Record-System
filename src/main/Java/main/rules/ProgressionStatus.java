package main.rules;

/**
 * The progression decision recommended for a student at the end of an
 * academic year.
 *
 * <p>Based on the UL Handbook of Academic Regulations and Procedures 2026/27:
 * students who meet the minimum standard proceed (AR 1.5.4). Students who
 * do not are given one of the outcomes in SP 3.3.6.2: annual repeats,
 * repeat a semester, repeat the year, or termination of enrolment.</p>
 *
 * <p>The system recommends a status; the final decision belongs to the
 * Faculty Examination Board.</p>
 */
public enum ProgressionStatus {

    /** Meets the minimum standard: cumulative QCA of 2.00 or higher and full credits (AR 1.5.3). */
    PROGRESS("Progress to the next year", true),

    /** Repeat up to four failed or D1 modules at the annual repeats (SP 3.3.6.2 a). */
    ANNUAL_REPEATS("Take annual repeats in failed modules", false),

    /** Repeat a full semester (SP 3.3.6.2 b). */
    REPEAT_SEMESTER("Repeat a full semester", false),

    /** Repeat the full academic year (SP 3.3.6.2 c). */
    REPEAT_YEAR("Repeat the full year", false),

    /** Enrolment terminated (SP 3.3.6.2 d). */
    TERMINATE("Enrolment terminated", false);

    private final String description;
    private final boolean progressing;

    ProgressionStatus(String description, boolean progressing) {
        this.description = description;
        this.progressing = progressing;
    }

    /**
     * Returns a readable description for menus and reports.
     *
     * @return the description of this status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the student moves on to the next year.
     *
     * @return true only for PROGRESS
     */
    public boolean isProgressing() {
        return progressing;
    }

    /**
     * Returns whether the student must repeat some or all of their work.
     *
     * @return true for ANNUAL_REPEATS, REPEAT_SEMESTER and REPEAT_YEAR
     */
    public boolean isRepeat() {
        return this == ANNUAL_REPEATS || this == REPEAT_SEMESTER || this == REPEAT_YEAR;
    }

    @Override
    public String toString() {
        return name() + " - " + description;
    }
}
