package main.rules;

import java.util.List;
import main.model.ModuleResult;

public class QcaCalculator {

    /** QCA for one semester. The list passed in should already be that semester's results. */
    public double semesterQca(List<ModuleResult> results) {
        return calculateQca(results);
    }

    /** QCA across all the results in the list. */
    public double overallQca(List<ModuleResult> results) {
        return calculateQca(results);
    }

    /** Weighted average: total QPV divided by total credits, skipping grades with no QPV. */
    private double calculateQca(List<ModuleResult> results) {
        double totalQpv = 0.0;
        int totalCredits = 0;

        for (ModuleResult result : results) {
            if (result.getGrade().countsTowardsQca()) {
                totalQpv += result.getQpv();
                totalCredits += result.getModule().getCredits();
            }
        }

        if (totalCredits == 0) {
            return 0.0; // empty list or only P/N grades: avoid dividing by zero
        }
        return totalQpv / totalCredits;
    }
}