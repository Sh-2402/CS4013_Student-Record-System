
package main.model;

/**
 * Module grades from the UL Handbook of Academic Regulations
 * and Procedures 2026/27.
 *
 * Table 2.1 (MS 2.5.2): academic module grades.
 * Table 2.2 (MS 2.5.5): administrative grades.
 *
 * Flags per grade:
 * - qcaPoints: quality point value (QPV).
 * - creditsAwarded: whether module credits are awarded.
 * - countsInQca: whether the grade contributes to QCA.
 *   F and NG count with zero QPV.
 *   P, N and administrative grades do not.
 * - deficient: deficient grades F, NG, I, I2, M and N.
 * - administrative: identifies Table 2.2 grades.
 */
public enum Grade {

    // ---- Table 2.1: quality-graded modules ----
    A1(4.00, true,  true,  false, false),
    A2(3.60, true,  true,  false, false),
    B1(3.20, true,  true,  false, false),
    B2(3.00, true,  true,  false, false),
    B3(2.80, true,  true,  false, false),
    C1(2.60, true,  true,  false, false),
    C2(2.40, true,  true,  false, false),
    C3(2.00, true,  true,  false, false),
    D1(1.60, true,  true,  false, false),
    F (0.00, false, true,  true,  false),
    NG(0.00, false, true,  true,  false),

    // ---- Table 2.1: pass/fail modules ----
    P (0.00, true,  false, false, false),
    N (0.00, false, false, true,  false),

    // ---- Table 2.2: administrative grades ----
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

    /*
     * NOTE:
     * The handbook document-control history mentions s and S2.
     * Their meanings and behaviour need confirmation from the lecturer
     * before adding them to this enum.
     */

    /** Minimum mark (%) for C3. */
    public static final double MIN_C3_PERCENT = 40.0;

    /** Lower mark (%) for D1. */
    public static final double MIN_D1_PERCENT = 35.0;

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

    /** Returns the QPV of this grade. */
    public double getQcaPoints() {
        return qcaPoints;
    }

    /** Returns whether credits are awarded. */
    public boolean isCreditsAwarded() {
        return creditsAwarded;
    }

    /** Returns whether this grade contributes to QCA. */
    public boolean countsTowardsQca() {
        return countsInQca;
    }

    /** Returns whether this is a deficient grade. */
    public boolean isDeficient() {
        return deficient;
    }

    /** Returns whether this is an administrative grade. */
    public boolean isAdministrative() {
        return administrative;
    }

    /** Returns true for quality-graded results A1 to NG. */
    public boolean isQualityGraded() {
        return countsInQca;
    }

    /**
     * Returns true for an ordinary academic pass:
     * C3 or better, or P in a pass/fail module.
     *
     * EX is not an ordinary pass; it is an exemption.
     * D1 is a compensating fail, not an ordinary pass.
     */
    public boolean isPass() {
        return this == P ||
               (countsInQca && qcaPoints >= C3.qcaPoints);
    }

    /**
     * Returns true if the result is a pass or an exemption.
     * EX is treated separately from an ordinary academic pass.
     */
    public boolean isPassOrExempt() {
        return isPass() || this == EX;
    }

    /** D1 is a compensating fail that carries credits. */
    public boolean isCompensatingFail() {
        return this == D1;
    }

    /**
     * Fail with no credits:
     * F and NG count towards QCA with zero points.
     * N is excluded from QCA.
     */
    public boolean isFail() {
        return this == F || this == NG || this == N;
    }

    /** Returns true for pass/fail module grades P or N. */
    public boolean isPassFailGrade() {
        return this == P || this == N;
    }
}

