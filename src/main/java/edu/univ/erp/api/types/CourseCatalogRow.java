package edu.univ.erp.api.types;

public record CourseCatalogRow(
        String sectionId,
        String courseCode,
        String courseTitle,
        int credits,
        String instructorName,
        String schedule,
        int capacity,
        int seatsTaken) {
}

