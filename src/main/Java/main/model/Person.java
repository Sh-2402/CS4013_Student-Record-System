package main.model;

public abstract class Person{
    private String name;
    private String id;
    private String email;

    protected Person(String name,String id,String email){
        this.name = name;
        this.id = id;
        this.email = email;
    }

    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public abstract String getRole();

}