package main.model;

public class Student extends Person {

private String programme;

    public Student(String name,String ID,String programme){
        super(name, ID);
        this.programme = programme;
    }
//TODO: remove programme from this class if someone maked a class for it
 
    public String getProgramme(){
        return programme;
    }
}
