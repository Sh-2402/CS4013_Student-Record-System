package model;

public class Registration{

private final String studentId;
private final String programmeCode;
private final int startYear;
private int currentYear;      // can change, so no final
private String status;        // can change, so no final

    public Registration(String studentId, String programmeCode, int startYear, int currentYear, String status) {

            if(currentYear < 0){
            throw new IllegalArgumentException("Current year must be aleast 1");
        }
        this.studentId = studentId;
        this.programmeCode = programmeCode;
        this.startYear = startYear;
        this.currentYear = currentYear;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }
    public String getProgrammeCode() {
        return programmeCode;
    }
    public int getStartYear() {
        return startYear;
    }
    public int getCurrentYear() {
        return currentYear;
    }
    public String getStatus() {
        return status;
    }

    @Override 
    public String toString() {
        return studentId + " - " + programmeCode + " (" + startYear + " start year, Current Year: " + currentYear + ", Status: " + status + ")";                                
                
    }

    public static void main(String[] args) {
        Registration reg = new Registration("S1234", "CS101", 2020,1, "Active");
        System.out.println(reg);
    }
}
