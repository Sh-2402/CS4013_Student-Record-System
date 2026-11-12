import java.util.List;

import main.repository.CsvUtil;

public class TestCsvUtil {
    public static void main(String[] args) {

        String line = "101,John,john@example.com";

String[] fields = CsvUtil.splitLine(line);
System.out.println(fields[0]);
System.out.println(fields[1]);
System.out.println(fields[2]);
String joined = CsvUtil.joinFields(fields);
System.out.println(joined);
List<String[]> rows = CsvUtil.readAll("Data/students.csv");
System.out.println(rows.size());
CsvUtil.writeAll("Data/test_students.csv", rows);
List<String[]> savedRows = CsvUtil.readAll("Data/test_students.csv");
System.out.println("Saved rows: " + savedRows.size());
    }
}