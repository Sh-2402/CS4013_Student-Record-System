package main.ui;

import java.util.Scanner;

public class InputHelper {

    private final Scanner scanner = new Scanner(System.in);
    public String readText(String prompt){
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    public int readInt(String prompt){
       while (true) {
        System.out.print(prompt);
        String input = scanner.nextLine();
       
       try{
         return Integer.parseInt(input);
       }catch(NumberFormatException e){
        System.out.println("Invalid input. Enter a whole number");
       }
        
       }
    }

    public int readChoice(int min, int max){
        while (true) {
            int choice = readInt("Enter your choice: ");
            if (choice >= min && choice <= max) {
                return choice;
            }
            System.out.println("Invalid choice. Enter a number between " +min+ " and "+max);
        }
    }
}
