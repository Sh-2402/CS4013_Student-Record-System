package main.rules;

import main.model.ModuleResult;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Progression rule for taught postgraduate (master's) programmes.
 *
 * <p>The same minimum standard applies as for undergraduates: no deficient
 * grades and a cumulative QCA of 2.00 or higher (AR 1.5.3). In addition,
 * the dissertation must be C3 or better (MS 2.7.1), so a D1 in the
 * dissertation cannot be compensated.</p>
 *
 * <p>Assumption: a dissertation module is one whose title contains
 * "dissertation" or "project" (for example CS6104 MSc Project).</p>
 */
public class PostgraduateProgressionRule implements ProgressionRule {

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

            // The dissertation must be C3 or better, so a D1 is not enough (MS 2.7.1).
            if (isDissertation(result) && result.isCompensatingFail()) {
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

    /** True if the result is for a dissertation or project module. */
    private boolean isDissertation(ModuleResult result) {
        String title = result.getModule().getTitle().toLowerCase(Locale.ROOT);
        return title.contains("dissertation") || title.contains("project");
    }
}
