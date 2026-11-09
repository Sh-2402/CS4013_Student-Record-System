package main.model;

public abstract class Person{
    private String name;
    private String ID;

    protected Person(String name,String ID){
        this.name = name;
        this.ID = ID;
    }

    public String getName(){
        return name;
    }
    
    public String getID(){
        return ID;
    }

}