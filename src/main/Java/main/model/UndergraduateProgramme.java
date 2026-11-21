package main.model;

import main.rules.ProgressionRule;
import main.rules.UndergraduateProgressionRule;

public class UndergraduateProgramme extends Programme {

    public UndergraduateProgramme(String code, String title, String department) {
        super(code, title, department);
    }

    @Override
    public int getDurationYears() {
        return 4;
    }

    @Override
    public String getType() {
        return "UNDERGRAD";
    }

    @Override
    public ProgressionRule getProgressionRule() {
        return new UndergraduateProgressionRule();
    }

}