
package main.model;

import java.util.Objects;

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
     * Table 4.1 (SP 3.2.2.5) lists an "S" grade among those not awarded for
     * normal participation, and the document-control history mentions S2.
     * The handbook does not define them, so their meaning and behaviour need
     * confirmation from the lecturer before they are added to this enum.
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
    // ---------- Repeat rules (SP 3.2.6) ----------

    /**
     * Applies the annual-repeat cap (SP 3.2.6.1): a quality grade better
     * than C3 becomes C3. C3, D1, F, NG, P, N and administrative grades
     * are returned unchanged.
     *
     * Link-in repeats (SP 3.2.6.2) are not capped, so do not call this
     * for them.
     *
     * @return the grade after the C3 cap
     */
    public Grade capAtC3() {
        return (countsInQca && qcaPoints > C3.qcaPoints) ? C3 : this;
    }

    /**
     * Returns the higher of two attempts at the same module (SP 3.2.6).
     * Quality grades are ranked by QPV. A quality grade ranks above a
     * grade with no QPV (P, N, I, X ...), so an I grade awarded during a
     * repeat leaves the original grade standing (SP 3.2.3.14).
     * If both are equal in rank, the first is returned.
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

    /** Ranking value: QPV for quality grades, -1 for grades with no QPV. */
    private double standing() {
        return countsInQca ? qcaPoints : -1.0;
    }
}

