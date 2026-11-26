package main.rules;

import main.model.ModuleResult;
import java.util.List;
import java.util.Objects;
/**
 * Calculates semester, year and overall QCA for one student.
 * QCA = total of (points x credits) / total credits that count (SP 3.3.1.2).
 * F and NG count as 0 points, but their credits still count.
 * P, N and administrative grades are left out.
 * The results passed in should already have repeats sorted out
 * (this is done in TranscriptService).
 */
public class QcaCalculator {

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

                totalQcs += result.getQualityCreditScore();
                totalQualityHours += result.getQcaCredits();
            }
        }

        return calculateAverage(totalQcs, totalQualityHours);
    }

    /**
     * Works out the QCA for one whole year, using semester 1 and 2 together.
     *
     * @param results the student's module results 
     * @param year the starting year of the academic year, e.g. 2026 means 2026/27
     * @return the QCA for the year, or 0.0 if there are no results for that year.
     */
    public double yearQca(List<ModuleResult> results, int year) {
            double totalQcs = 0.0;
            int totalQualityHours = 0;

        for (ModuleResult result : results) {
            if (result.getYear() == year) {
                totalQcs += result.getQualityCreditScore();
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
     * ModuleResult.getQualityCreditScore() returns QPV multiplied by module credits.
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

            totalQcs += result.getQualityCreditScore();
            totalQualityHours += result.getQcaCredits();
        }

        return new double[] {
                totalQcs,
                totalQualityHours
        };
    }
}