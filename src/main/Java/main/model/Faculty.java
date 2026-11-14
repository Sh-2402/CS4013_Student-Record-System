package main.model;

import java.util.ArrayList;

public class Faculty extends Person{

   private ArrayList<Module>modules;
   private String department;

    public Faculty(String name,String id,String email,String department){
        super(name, id,email);
      modules = new ArrayList<>();
      this.department = department;
    }

    public void addModule(Module module){
        if(modules.contains(module)){
            System.out.println("Already an existing module");
        }else{
            modules.add(module);
        }
    }

    public ArrayList<Module> getModulesTaught(){
        ArrayList<Module> moduleCopy = new ArrayList<>(modules);
        return moduleCopy;
    }
    @Override
    public String getRole(){
        return "FACULTY";
    }

}
