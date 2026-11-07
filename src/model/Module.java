package model;

public class Module {

    private String code;
    private String title;
    private int credits;
    private int semester;
    private String lecturerId;

    public Module(String code, String title, int credits, int semester, String lecturerId) {

        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive");
        }

        if (semester != 1 && semester != 2) {
            throw new IllegalArgumentException("Semester must be either 1 or 2");
        }

        this.code = code;
        this.title = title;
        this.credits = credits;
        this.semester = semester;
        this.lecturerId = lecturerId;
    }
    
    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    @Override
    public String toString() {
        return code + " - " + title + " (" + credits + " credits, Sem " + semester + ")";                                
                
    }
    
    public static void main(String[] args) {
        Module oop = new Module("CS4031", "OOP", 6, 1, "ME001");
        System.out.println(oop);
    }
}



