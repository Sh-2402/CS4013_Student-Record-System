package main.ui;

import java.util.List;
import main.repository.CsvUtil;

public class LoginMenu{

    private static final String USERS_FILE = "Data/users.csv";

    private final InputHelper input;

    public LoginMenu(){
        this(new InputHelper());
    }

    /**
     * Creates the login menu using a shared input helper.
     *
     * @param input the shared input helper created by Main
     */
    public LoginMenu(InputHelper input){
        this.input = input;
    }

    /**
     * Asks for a user ID and looks it up in users.csv.
     *
     * @return {id, role} for a known user, or null if the ID is not found
     */
    public String[] login(){
        String id = input.readText("Enter your user ID: ").trim();
        List<String[]> users = CsvUtil.readAll(USERS_FILE);

        for(int i = 1; i < users.size();i++){
            String[] user = users.get(i);
            if(user.length == 2 && id.equals(user[0].trim())){
                return new String[] {user[0].trim(), user[1].trim()};
            }
        }
        System.out.println("User ID not found");
        return null;
    }

    public void start(){
        String[] user = login();
        if(user != null){
            openMenuForRole(user[1]);
        }
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
