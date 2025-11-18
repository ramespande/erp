package edu.univ.erp.api.types;

public record TimetableEntry(
        String day,
        String timeRange,
        String courseCode,
        String sectionId,
        String room) {
}

