package com.rinko.infra.dto;

import com.rinko.infra.exception.ValidationException;

import java.util.Locale;
import java.util.Objects;

/**
 * 排序字段 DTO。
 */
public class SortOrder {

    private final String field;
    private final Direction direction;

    public SortOrder(String field, String direction) {
        this.field = Objects.requireNonNull(field, "field must not be null");
        this.direction = Direction.fromString(direction);
    }

    public String getField() {
        return field;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        ASC, DESC;

        public static Direction fromString(String value) {
            if (value == null) {
                return ASC;
            }
            try {
                return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid sort direction: " + value + ". Use ASC or DESC.");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SortOrder sortOrder)) {
            return false;
        }
        return field.equals(sortOrder.field) && direction == sortOrder.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, direction);
    }
}
