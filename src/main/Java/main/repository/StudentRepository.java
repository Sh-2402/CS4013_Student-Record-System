package main.repository;

import  main.model.Student;
import java.util.ArrayList;
import java.util.List;


public class StudentRepository {

    private ArrayList<Student> students = new ArrayList<>();
    private final String filePath;

    public StudentRepository() {
        this("Data/students.csv");
    }

    public StudentRepository(String filePath){
        this.filePath = filePath;
        loadStudents();
    }

    private void loadStudents() {
        List<String[]> rows = CsvUtil.readAll(filePath);

            for (int i = 1; i < rows.size(); i++) {
                String[] parts = rows.get(i);

                if (parts.length == 3) {

                    boolean valid = true;

                    for (int j = 0; j < parts.length; j++) {
                        if (parts[j].trim().isEmpty()) {
                            valid = false;
                        }
                    }

                    if (valid) {
                        Student studentObj = new Student(parts[1], parts[0], parts[2],null);
                        students.add(studentObj);
                    } else {
                        System.out.println("Invalid CSV record: One or more fields are empty - " + CsvUtil.joinFields(parts));
                    }

                } else {
                    System.out.println("Invalid CSV record: Incorrect number of fields - " + CsvUtil.joinFields(parts));
                }

            }

        }


    public ArrayList<Student> loadAll(){
        return new ArrayList<>(students);
    }

    public void save(Student s) {
        if(findById(s.getId()) != null){
            throw new IllegalArgumentException("Student ID already exists");
        }

        List<String[]> rows = CsvUtil.readAll(filePath);
        String[] newRow = {s.getId(), s.getName(), s.getEmail()};
        rows.add(newRow);

        CsvUtil.writeAll(filePath, rows);
        students.add(s);
    }

    public Student findById(String id) {
        for (Student s : students) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

}