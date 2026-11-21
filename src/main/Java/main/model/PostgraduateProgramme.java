package main.model;

import main.rules.PostgraduateProgressionRule;
import main.rules.ProgressionRule;

public class PostgraduateProgramme extends Programme {

    public PostgraduateProgramme(String code, String title, String department){
        super(code , title, department);
    }

    @Override 
    public int getDurationYears() {
        return 1;
    }

    @Override 
    public String getType() {
        return "POSTGRAD";
    }

    @Override
    public ProgressionRule getProgressionRule() {
        return new PostgraduateProgressionRule();
    }
}