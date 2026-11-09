package main.repository;

import  main.model.Student;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class StudentRepository {

    private ArrayList<Student> students = new ArrayList<>();

    public StudentRepository() {
        loadStudents();
    }

    private void loadStudents() {
        try (BufferedReader reader = new BufferedReader(new FileReader("Data/students.csv"))) {

            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",", -1);

                if (parts.length == 3) {

                    boolean valid = true;

                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].trim().isEmpty()) {
                            valid = false;
                        }
                    }

                    if (valid) {
                        Student studentObj = new Student(parts[1], parts[0], parts[2]);
                        students.add(studentObj);
                    } else {
                        System.out.println("Invalid CSV record: One or more fields are empty - " + line);
                    }

                } else {
                    System.out.println("Invalid CSV record: Incorrect number of fields - " + line);
                }

            }

        } catch (IOException e) {
            System.err.println("Error reading students.csv: " + e.getMessage());
            throw new IllegalStateException("Unable to load records", e);
        }
    }

    public void saveStudent(Student s) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Data/students.csv", true))) {

            String row = s.getID() + "," + s.getName() + "," + s.getProgramme();

            writer.write(row);
            writer.newLine();

        } catch (IOException e) {
            System.err.println("Error saving student: " + e.getMessage());
            throw new IllegalStateException("Unable to save records", e);
        }
        students.add(s);
    }

    public Student searchID(String ID) {
        for (Student s : students) {
            if (s.getID().equals(ID)) {
                return s;
            }
        }
        return null;
    }

}