package main.model;

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

    @Override 
    public String toString() {
        return code + " - " + title + " (" + department + ")";
    }


}