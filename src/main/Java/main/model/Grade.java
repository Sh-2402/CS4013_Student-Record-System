package main.model;

import java.util.Objects;

/**
 * This enum stores all the module grades a student can get at UL,
 * based on the Handbook of Academic Regulations and Procedures 2026/27.
 *
 * I used an enum because there is a fixed set of grades that never
 * changes while the program runs. Each grade is a single object that
 * knows its own rules, so other classes don't need long if-else chains.
 *
 * Table 2.1 (MS 2.5.2): academic module grades.
 * Table 2.2 (MS 2.5.5): administrative grades.
 *
 * Every grade has five values: 
 * - qcaPoints: quality point value (QPV).
 * - creditsAwarded: whether module credits are awarded.
 * - countsInQca: whether the grade contributes to QCA.
 *   F and NG count with zero QPV.
 *   P, N and administrative grades do not.
 * - deficient: deficient grades F, NG, I, I2, M and N.
 * - administrative: identifies Table 2.2 grades.
 */
public enum Grade {

    // Quality grades (Table 2.1)
    A1(4.00, true,  true,  false, false),
    A2(3.60, true,  true,  false, false),
    B1(3.20, true,  true,  false, false),
    B2(3.00, true,  true,  false, false),
    B3(2.80, true,  true,  false, false),
    C1(2.60, true,  true,  false, false),
    C2(2.40, true,  true,  false, false),
    C3(2.00, true,  true,  false, false),
    D1(1.60, true,  true,  false, false),
    F (0.00, false, true,  true,  false), // counts as 0 in QCA 
    NG(0.00, false, true,  true,  false), // counts as 0 in QCA

    // Pass/fail grades (Table 2.1)
    P (0.00, true,  false, false, false),
    N (0.00, false, false, true,  false),

    // Administrative grades (Table 2.2)
    EX(0.00, true,  false, false, true),
    R (0.00, false, false, false, true),
    G (0.00, false, false, false, true),
    I (0.00, false, false, true,  true),
    I2(0.00, false, false, true,  true),
    M (0.00, false, false, true,  true),
    RE(0.00, false, false, false, true),
    X (0.00, false, false, false, true),
    Y (0.00, false, false, false, true),
    W (0.00, false, false, false, true);

    
    // The fields are private so other classes can't access them directly
    // (encapsulation), and final so they can't change once the grade is made.
    private final double qcaPoints;
    private final boolean creditsAwarded;
    private final boolean countsInQca;
    private final boolean deficient;
    private final boolean administrative;

    Grade(double qcaPoints, boolean creditsAwarded,
          boolean countsInQca, boolean deficient,
          boolean administrative) {

        this.qcaPoints = qcaPoints;
        this.creditsAwarded = creditsAwarded;
        this.countsInQca = countsInQca;
        this.deficient = deficient;
        this.administrative = administrative;
    }

    /**
     * Gets the quality points for this grade.
     *
     * @return the points, from 0.00 to 4.00
     */
    public double getQcaPoints() {
        return qcaPoints;
    }
    
    /**
     * Checks if the module credits are given.
     *
     * @return true if credits are awarded
     */
    public boolean isCreditsAwarded() {
        return creditsAwarded;
    }
    
    /**
     * Checks if this grade is used in the QCA.
     *
     * @return true if it counts towards the QCA
     */
    public boolean countsTowardsQca() {
        return countsInQca;
    }
    
    /**
     * Checks if this is a deficient grade.
     *
     * @return true for F, NG, I, I2, M and N
     */
    public boolean isDeficient() {
        return deficient;
    }

    /**
     * Checks if this grade is from the administrative table.
     *
     * @return true if it is an administrative grade
     */
    public boolean isAdministrative() {
        return administrative;
    }

    /** Pass = C3 or above, or P. D1 doesn't count as a pass. 
     * 
     * @return true if this grade is a pass. 
     */
    public boolean isPass() {
        return this == P ||
               (countsInQca && qcaPoints >= C3.qcaPoints);
    }

    /** D1 is a compensating fail that carries credits.
     * 
     * @return true if this grade is D1.
     */
    public boolean isCompensatingFail() {
        return this == D1;
    }

    /** Fail with no credits. 
     * 
     * @return true if this grade is F, NG or N.
     */
    public boolean isFail() {
        return this == F || this == NG || this == N;
    }

    /** Annual repeat: anything above C3 is capped at C3 ( not for link-in repeats).
     * 
     * @return C3 if this grade is higher, otherwise this grade.
     */
    public Grade capAtC3() {
        return (countsInQca && qcaPoints > C3.qcaPoints) ? C3 : this;
    }

    /** Repeat: Keeps the better of the two grades. Original wins a tie. 
     * 
     * @param first the original grade
     * @param second the repeat grade
     * @return the grade that stands
     */
    public static Grade higherOf(Grade first, Grade second) {
        Objects.requireNonNull(first, "First grade must not be null");
        Objects.requireNonNull(second, "Second grade must not be null");

        return second.standing() > first.standing() ? second : first;
    }

    /** Ranking for higherOf: points for graded results, P = -0.5, others = -1.0. */
    private double standing() {
        if (countsInQca) {
            return qcaPoints;
        }
        return this == P ? -0.5 : -1.0;
    }
}

