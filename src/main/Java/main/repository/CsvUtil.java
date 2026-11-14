package main.repository;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CsvUtil {

    public static List<String[]> readAll(String file){
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = splitLine(line);
                rows.add(parts);
            }
        }catch(IOException e){
                System.err.println("Error reading CSV file:" + e.getMessage());
                throw new IllegalStateException("Unable to read CSV file", e);
            }
            return rows;

    }

    public static String[] splitLine(String line){
        String[] parts = line.split(",",-1);
        return parts;
    }

    public static String joinFields(String[] fields){
        return String.join(",", fields);
    }

    public static void writeAll(String file, List<String[]> rows){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
            for(String[] row:rows){
                String parts = joinFields(row);
                writer.write(parts);
                writer.newLine();
            }
        }catch(IOException e){
            System.err.println("Error writing CSV:" + e.getMessage());
            throw new  IllegalStateException("Unable to write CSV file", e);
        }
    }
}