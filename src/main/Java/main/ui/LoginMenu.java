package main.ui;

import java.util.List;
import main.repository.CsvUtil;

public class LoginMenu{

    private final InputHelper input = new InputHelper();
    public void start(){
        String id = input.readText("Enter your user ID: ");
        List<String[]> users = CsvUtil.readAll("Data/users.csv");

        for(int i = 1; i < users.size();i++){
            String[] user = users.get(i);
            if(user.length == 2 && id.equals(user[0])){
                openMenuForRole(user[1]);
                return;
            }
        }
        System.out.println("User ID not found");
    }

    public void openMenuForRole(String role){
        if("STUDENT".equals(role)){
            System.out.println("Opening Student Menu");
        }else if("FACULTY".equals(role)){
            System.out.println("Opening Faculty Menu");
        }else if("DEPARTMENT".equals(role)){
            System.out.println("Opening Department Menu");
        }else{
            System.out.println("Invalid role");
        }
    }
}