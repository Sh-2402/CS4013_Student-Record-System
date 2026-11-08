package model;

public class PostgraduateProgramme extends Programme {

    public PostgraduateProgramme(String code, String title, String department){
        super(code , title, department);
    }

    @Override 
    public int getDurationYears() {
        return 1;
    }
}