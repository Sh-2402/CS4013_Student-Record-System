
import main.service.StudentService;
import main.model.Student;
import main.repository.StudentRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class TestStudentService {

    public static void main(String[] args) throws IOException {

        // Create a temporary CSV file for testing
        Path testFile = Files.createTempFile("students_test_", ".csv");

        try {
            Files.writeString(testFile,
                    "id,name,email\n" +
                    "101,John,john@example.com\n" +
                    "102,Mary,mary@example.com\n");

            StudentRepository repo =
                    new StudentRepository(testFile.toString());

            StudentService service = new StudentService(repo);

            // Test adding a new student
            Student newStudent =
                    new Student("Liam", "103", "liam@example.com", null);

            service.addStudent(newStudent);

            // Test finding an existing student
            Student student = service.findById("101");

            System.out.println(student != null
                    ? student.getName() : "Not found");

            // Test ID checking
            System.out.println(service.idExists("101"));
            System.out.println(service.idExists("999"));

            // Check that the new student was saved
            System.out.println(service.idExists("103"));

        } finally {
            // Delete the temporary test file
            Files.deleteIfExists(testFile);
        }
    }
}

