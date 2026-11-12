package main.model;

import java.util.ArrayList;

public class Student extends Person {

private Registration registration;
private ArrayList<ModuleResult> results = new ArrayList<>();

    public Student(String name,String id,String email,Registration registration){
        super(name, id, email);
        this.registration = registration;
    }

    public Registration getRegistration(){
        return registration;
    }

    @Override
    public String getRole(){
        return "STUDENT";
    }

    // TODO: ModuleResult class has not yet been added to the project.
// This dependency is required by the UML and is being implemented by another team member.
    public ArrayList<ModuleResult> getResults(){
        return new ArrayList<>(results);
    }
}
