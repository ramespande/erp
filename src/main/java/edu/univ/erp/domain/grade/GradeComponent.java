package edu.univ.erp.domain.grade;

import java.io.Serializable;
import java.util.Objects;

public final class GradeComponent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final double score;
    private final double weight;

    public GradeComponent(String name, double score, double weight) {
        this.name = name;
        this.score = score;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GradeComponent that)) {
            return false;
        }
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

