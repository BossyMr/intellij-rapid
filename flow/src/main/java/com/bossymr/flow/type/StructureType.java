package com.bossymr.flow.type;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@code StructureType} represents a structure with fields.
 */
public class StructureType implements ValueType {

    private final String name;
    private final List<Field> fields;

    /**
     * Create a new {@code StructureType}.
     *
     * @param name the name of the structure.
     * @param fields the fields of this structure.
     */
    public StructureType(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    /**
     * Returns the name of this structure.
     *
     * @return the name of this structure.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fields of this structure.
     *
     * @return the fields of this structure.
     */
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public boolean isStructure() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StructureType that = (StructureType) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fields);
    }

    @Override public String toString() {
        return "structure{" + name + ", " + fields.stream()
                .map(Field::toString)
                .collect(Collectors.joining(", ")) + "}";
    }

    /**
     * A {@code Field} represents the field of a structure.
     */
    public static class Field {

        private final String name;
        private final ValueType type;

        /**
         * Create a new {@code Field}.
         *
         * @param name the name of the field.
         * @param type the type of the field.
         */
        public Field(String name, ValueType type) {
            this.name = name;
            this.type = type;
        }

        /**
         * Returns the name of the field.
         *
         * @return the name of the field.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the type of the field.
         *
         * @return the type of the field.
         */
        public ValueType getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Field field = (Field) o;
            return Objects.equals(name, field.name) && Objects.equals(type, field.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }

        @Override
        public String toString() {
            return name + ": " + type;
        }
    }
}
