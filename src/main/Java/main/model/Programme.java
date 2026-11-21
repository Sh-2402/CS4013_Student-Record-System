package main.model;

import main.rules.ProgressionRule;

public abstract class Programme {

    private final String code;
    private final String title;
    private final String department;

    public Programme(String code, String title, String department) {
        this.code = code;
        this.title = title;
        this.department = department;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public abstract int getDurationYears();

    public abstract String getType();

    /**
     * Returns the progression rule for this type of programme. Each
     * subclass supplies its own rule (strategy pattern).
     *
     * @return the progression rule for this programme
     */
    public abstract ProgressionRule getProgressionRule();

    @Override
    public String toString() {
        return code + " - " + title + " (" + department + ")";
    }

}