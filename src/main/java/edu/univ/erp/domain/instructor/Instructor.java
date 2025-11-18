package edu.univ.erp.domain.instructor;

import java.util.Objects;

public final class Instructor {
    private final String userId;
    private final String department;
    private final String title;

    public Instructor(String userId, String department, String title) {
        this.userId = userId;
        this.department = department;
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Instructor that)) {
            return false;
        }
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

