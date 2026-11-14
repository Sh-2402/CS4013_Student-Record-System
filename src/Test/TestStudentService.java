import main.service.StudentService;
import main.model.Student;
import main.repository.StudentRepository;

public class TestStudentService {
    public static void main(String[] args) {

StudentRepository repo = new StudentRepository("Data/test_students.csv");
StudentService service = new StudentService(repo);
Student newStudent = new Student("Liam", "103", "liam@example.com", null);
service.addStudent(newStudent);
Student student = service.findById("101");

if(student != null){
    System.out.println(student.getName());
}else{
    System.out.println("Not found");
}

System.out.println(service.idExists("101"));
System.out.println(service.idExists("999"));
    }
}