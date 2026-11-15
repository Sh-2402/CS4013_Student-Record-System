package main.rules;

import main.model.ModuleResult;

import java.util.List;
import java.util.Objects;

/**
 * Calculates semester, cumulative and weighted Quality Credit Averages.
 *
 * UL Handbook of Academic Regulations and Procedures 2026/27:
 *
 * SP 3.3.1.2:
 * QCA = Sum of QCS / (Sum of attempted hours - Sum of non-quality hours).
 *
 * QCS = module QPV x module credits.
 *
 * Grades A1 to NG are quality grades and count towards QCA.
 * P, N and administrative grades are excluded from QCA.
 * F and NG count in the QCA denominator with zero QCS.
 *
 * SP 3.2.6:
 * For repeated modules, the higher of the original and repeat grade stands.
 * Annual repeat grades are capped at C3.
 *
 * MS 2.6.3:
 * A three-year Part 2 programme uses year weightings of 1:2:2.
 *
 * IMPORTANT:
 * The results supplied to this calculator should contain the final grade
 * that stands for each module after repeat rules have been applied.
 */
public class QcaCalculator {

    /**
     * Weightings for a three-year Part 2 programme.
     * Year 1 of Part 2 = 1
     * Year 2 of Part 2 = 2
     * Year 3 of Part 2 = 2
     */
    private static final int[] PART2_THREE_YEAR_WEIGHTS = {1, 2, 2};

    // ---------- Semester QCA ----------

    /**
     * Calculates QCA for a list of results from one semester.
     *
     * @param results results from one semester
     * @return semester QCA, or 0.0 if there are no quality hours
     */
    public double semesterQca(List<ModuleResult> results) {
        return calculateQca(results);
    }

    /**
     * Calculates QCA for a particular academic year and semester.
     *
     * @param results all relevant results for the student
     * @param year academic year start, e.g. 2026
     * @param semester semester number (1 or 2)
     * @return semester QCA
     */
    public double semesterQca(
            List<ModuleResult> results, int year, int semester) {

        Objects.requireNonNull(results, "Results must not be null");

        if (semester != 1 && semester != 2) {
            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2");
        }

        double totalQcs = 0.0;
        int totalQualityHours = 0;

        for (ModuleResult result : results) {
            Objects.requireNonNull(result, "Result must not be null");

            if (result.getYear() == year
                    && result.getSemester() == semester) {

                totalQcs += result.getQpv();
                totalQualityHours += result.getQcaCredits();
            }
        }

        return calculateAverage(totalQcs, totalQualityHours);
    }

    // ---------- Overall QCA ----------

    /**
     * Calculates cumulative QCA across all supplied results.
     *
     * The list should contain the final result that stands for each module.
     *
     * @param results results across the programme
     * @return cumulative QCA
     */
    public double overallQca(List<ModuleResult> results) {
        return calculateQca(results);
    }

    // ---------- Weighted QCA ----------

    /**
     * Calculates weighted QCA for Part 2 of a programme.
     *
     * For three years of Part 2, the weighting is 1:2:2.
     * For one or two years, no additional weighting is applied.
     *
     * Each inner list represents one year of Part 2.
     *
     * @param years results grouped by Part 2 year
     * @return weighted QCA
     */
    public double weightedQca(List<List<ModuleResult>> years) {

        Objects.requireNonNull(years, "Years must not be null");

        if (years.size() > PART2_THREE_YEAR_WEIGHTS.length) {
            throw new IllegalArgumentException(
                    "Part 2 weighting supports a maximum of 3 years");
        }

        double weightedQcs = 0.0;
        double weightedQualityHours = 0.0;

        for (int i = 0; i < years.size(); i++) {

            List<ModuleResult> yearResults =
                    Objects.requireNonNull(
                            years.get(i),
                            "Year results must not be null");

            int weight = years.size() == 3
                    ? PART2_THREE_YEAR_WEIGHTS[i]
                    : 1;

            double[] yearTotals = totals(yearResults);

            weightedQcs += yearTotals[0] * weight;
            weightedQualityHours += yearTotals[1] * weight;
        }

        return calculateAverage(
                weightedQcs, weightedQualityHours);
    }

    // ---------- Core QCA Calculation ----------

    /**
     * Calculates QCA using the QCS and quality hours.
     *
     * @param results module results
     * @return QCA
     */
    private double calculateQca(List<ModuleResult> results) {

        double[] resultTotals = totals(results);

        return calculateAverage(
                resultTotals[0], resultTotals[1]);
    }

    /**
     * Calculates the QCA average.
     *
     * @param totalQcs sum of quality credit scores
     * @param qualityHours sum of quality-graded module credits
     * @return QCA, or zero when there are no quality hours
     */
    private double calculateAverage(
            double totalQcs, double qualityHours) {

        if (qualityHours == 0) {
            return 0.0;
        }

        return totalQcs / qualityHours;
    }

    /**
     * Calculates total QCS and quality hours.
     *
     * ModuleResult.getQpv() returns QPV multiplied by module credits.
     * ModuleResult.getQcaCredits() returns credits included in QCA.
     *
     * F and NG are included in quality hours even though their QCS is zero.
     * P, N and administrative grades are excluded.
     *
     * @param results module results
     * @return array containing {total QCS, total quality hours}
     */
    private double[] totals(List<ModuleResult> results) {

        Objects.requireNonNull(results, "Results must not be null");

        double totalQcs = 0.0;
        int totalQualityHours = 0;

        for (ModuleResult result : results) {

            Objects.requireNonNull(
                    result, "Module result must not be null");

            totalQcs += result.getQpv();
            totalQualityHours += result.getQcaCredits();
        }

        return new double[] {
                totalQcs,
                totalQualityHours
        };
    }
}
