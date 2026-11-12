import main.ui.InputHelper;

public class TestInputHelper {

    public static void main(String[] args) {
        
InputHelper input = new InputHelper();

String name = input.readText("Enter your name: ");
System.out.println("Hello, "+name);

int age = input.readInt("Enter your age: ");
System.out.println("Your age is: " + age);
    
int choice = input.readChoice(1, 4);
System.out.println("You selected: " + choice);
}


}