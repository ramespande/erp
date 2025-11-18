package edu.univ.erp.domain.student;

import java.util.Objects;

public final class Student {
    private final String userId;
    private final String rollNumber;
    private final String program;
    private final int year;

    public Student(String userId, String rollNumber, String program, int year) {
        this.userId = userId;
        this.rollNumber = rollNumber;
        this.program = program;
        this.year = year;
    }

    public String getUserId() {
        return userId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getProgram() {
        return program;
    }

    public int getYear() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student student)) {
            return false;
        }
        return Objects.equals(userId, student.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

