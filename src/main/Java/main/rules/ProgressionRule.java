package main.rules;

import main.model.ModuleResult;

import java.util.List;

/**
 * A progression rule decides whether a student meets the minimum academic
 * standard at the end of a year (UL Handbook 2026/27, AR 1.5.3).
 *
 * <p>This interface is the strategy in a strategy pattern. Each programme
 * type supplies its own rule ({@link UndergraduateProgressionRule} or
 * {@link PostgraduateProgressionRule}), and ProgressionService calls
 * {@code evaluate} without needing to know which rule it has.</p>
 */
public interface ProgressionRule {

    /**
     * Checks one student's results against this rule.
     *
     * @param results the standing result for each module the student has
     *                taken on the programme (after repeat rules have been
     *                applied); must not be null
     * @return {@link ProgressionStatus#PROGRESS} if the student meets the
     *         minimum standard, otherwise
     *         {@link ProgressionStatus#DOES_NOT_PROGRESS}
     * @throws NullPointerException if results is null
     */
    ProgressionStatus evaluate(List<ModuleResult> results);
}
