package main.model;

public class Registration {

    private final String studentId;
    private final String programmeCode;
    private final int startYear;
    private int currentYear; // can change, so no final
    private String status; // can change, so no final

    public Registration(String studentId, String programmeCode, int startYear, int currentYear, String status) {

        if (currentYear < 1) {
            throw new IllegalArgumentException("Current year must be at least 1");
        }

        validateStatus(status);

        this.studentId = studentId;
        this.programmeCode = programmeCode;
        this.startYear = startYear;
        this.currentYear = currentYear;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getProgrammeCode() {
        return programmeCode;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return studentId + " - " + programmeCode + " (" + startYear + " start year, Current Year: " + currentYear
                + ", Status: " + status + ")";

    }

    public void advanceYear() {
        currentYear++;
    }

    public void changeStatus(String newStatus) {
        validateStatus(newStatus);
        status = newStatus;
    }

    private void validateStatus(String status) {
        if (!status.equals("ACTIVE") && !status.equals("REPEATING") && !status.equals("WITHDRAWN")) {
            throw new IllegalArgumentException("Status must be either 'ACTIVE', 'REPEATING', or 'WITHDRAWN'");
        }
    }
} 