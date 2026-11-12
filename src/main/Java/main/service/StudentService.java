package main.service;

import main.model.Student;
import main.repository.StudentRepository;

public class StudentService {

    private  final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }

    public void addStudent(Student s){
        studentRepository.save(s);
    }

    public Student findById(String id){
        return studentRepository.findById(id);
    }

    public boolean idExists(String id){
       if(studentRepository.findById(id) != null){
        return true;

       }
       return false;

    }
}
