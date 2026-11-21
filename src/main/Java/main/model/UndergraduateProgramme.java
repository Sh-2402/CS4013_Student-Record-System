package main.model;

public class UndergraduateProgramme extends Programme {

   
    public UndergraduateProgramme(String code, String title, String department) {
        super(code, title, department);
        }


    @Override
    public int getDurationYears() {
        return 4;
    }
}