package edu.univ.erp.domain.course;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class Section implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String sectionId;
    private final String courseId;
    private final String instructorId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String room;
    private final int capacity;
    private final int semester;
    private final int year;
    private final LocalDate registrationDeadline;
    private final String weightingRule;
    private final String componentNames;

    public Section(String sectionId,
                   String courseId,
                   String instructorId,
                   DayOfWeek dayOfWeek,
                   LocalTime startTime,
                   LocalTime endTime,
                   String room,
                   int capacity,
                   int semester,
                   int year,
                   LocalDate registrationDeadline,
                   String weightingRule,
                   String componentNames) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.registrationDeadline = registrationDeadline;
        this.weightingRule = weightingRule;
        this.componentNames = componentNames;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }

    public LocalDate getRegistrationDeadline() {
        return registrationDeadline;
    }

    public String getWeightingRule() {
        return weightingRule;
    }

    public String getComponentNames() {
        return componentNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Section section)) {
            return false;
        }
        return Objects.equals(sectionId, section.sectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionId);
    }
}

