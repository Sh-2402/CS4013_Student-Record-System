package main.model;

import java.util.ArrayList;

public class Faculty extends Person{

   private ArrayList<String>modules;
   
    public Faculty(String name,String ID){
        super(name, ID);
      modules = new ArrayList<>();
    }

    public void addModule(String module){
        if(modules.contains(module)){
            System.out.println("Already an existing module");
        }else{
            modules.add(module);
        }
    }

    public ArrayList<String> getModules(){
        ArrayList<String> moduleCopy = new ArrayList<>(modules);
        return moduleCopy;
    }
    
}
