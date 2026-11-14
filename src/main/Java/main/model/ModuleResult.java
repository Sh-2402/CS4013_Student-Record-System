package main.model;

public class ModuleResult{
    private final Student student;
    private final Module module;
    private final int year;
    private final int semester;
    private final Grade grade;


public ModuleResult(Student student, Module module, int year, int semester, Grade grade) {
    if (student == null || module == null || grade == null) {
        throw new IllegalArgumentException("Student, module, and grade must not be null");
    }
    if (year < 1) {
        throw new IllegalArgumentException("Year must be a positive integer");
    }
    if (semester != 1 && semester != 2) {
        throw new IllegalArgumentException("Semester must be 1 or 2");
    }
    this.student = student;
    this.module = module;
    this.year = year;
    this.semester = semester;
    this.grade = grade;
}

public Student getStudent() { return student; }
public Module getModule() { return module; }
public int getYear() { return year; }
public int getSemester() { return semester; }
public Grade getGrade() { return grade; }


/**Credits x grade points, the top half of the QCA formula */
public double getQpv() {
    return module.getCredits() * grade.getQcaPoints();
}

/**Credits actually earned: full credits if the grade awards them, zero otherwise */
public int getCreditsEarned() {
    return grade.isCreditsAwarded() ? module.getCredits() : 0;
}

@Override 
public String toString() {
       return module.getCode() + " " + grade + " (" + year + " Sem " + semester + ")";
}

}