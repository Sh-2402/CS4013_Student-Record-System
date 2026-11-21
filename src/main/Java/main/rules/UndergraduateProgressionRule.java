package main.rules;

import main.model.ModuleResult;

import java.util.List;
import java.util.Objects;

/**
 * Progression rule for undergraduate programmes.
 *
 * <p>UL Handbook 2026/27, AR 1.5.3: the minimum standard is a cumulative
 * QCA of 2.00 with full credits in all modules to date. A student
 * therefore progresses only if:</p>
 * <ul>
 *   <li>they have no deficient grades (F, NG, I, I2, M, N), which earn no
 *       credits (MS 2.5.4), and</li>
 *   <li>their cumulative QCA is 2.00 or higher.</li>
 * </ul>
 *
 * <p>A D1 is a compensating fail: it earns credits, so a QCA of 2.00 or
 * higher compensates for it in any module (Table 2.1).</p>
 */
public class UndergraduateProgressionRule implements ProgressionRule {

    /** Minimum cumulative QCA (AR 1.5.3). */
    private static final double MIN_QCA = 2.00;

    /** Allows for rounding errors when comparing a QCA with 2.00. */
    private static final double TOLERANCE = 1e-9;

    private final QcaCalculator calculator = new QcaCalculator();

    @Override
    public ProgressionStatus evaluate(List<ModuleResult> results) {
        Objects.requireNonNull(results, "Results must not be null");

        // No results recorded: the minimum standard cannot be confirmed.
        if (results.isEmpty()) {
            return ProgressionStatus.DOES_NOT_PROGRESS;
        }

        boolean hasQualityHours = false;

        for (ModuleResult result : results) {
            Objects.requireNonNull(result, "Result must not be null");

            // Full credits are needed in every module (AR 1.5.3, MS 2.5.4).
            if (result.isDeficient()) {
                return ProgressionStatus.DOES_NOT_PROGRESS;
            }

            if (result.getQcaCredits() > 0) {
                hasQualityHours = true;
            }
        }

        // Only P or EX results: there is no QCA, so the QCA test does not apply.
        if (hasQualityHours && calculator.overallQca(results) < MIN_QCA - TOLERANCE) {
            return ProgressionStatus.DOES_NOT_PROGRESS;
        }

        return ProgressionStatus.PROGRESS;
    }
}
