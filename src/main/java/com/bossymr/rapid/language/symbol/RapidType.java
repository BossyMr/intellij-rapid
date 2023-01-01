package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.symbol.virtual.VirtualAtomic;
import com.bossymr.rapid.language.symbol.virtual.VirtualComponent;
import com.bossymr.rapid.language.symbol.virtual.VirtualRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RapidType {

    public static final RapidType NUMBER = new RapidType(new VirtualAtomic("num"));
    public static final RapidType DOUBLE = new RapidType(new VirtualAtomic("dnum"));
    public static final RapidType BOOLEAN = new RapidType(new VirtualAtomic("bool"));
    public static final RapidType STRING = new RapidType(new VirtualAtomic("string"));
    public static final RapidType POSITION = new RapidType(new VirtualRecord("pos", List.of(new VirtualComponent("x", NUMBER), new VirtualComponent("y", NUMBER), new VirtualComponent("z", NUMBER))));
    public static final RapidType ORIENTATION = new RapidType(new VirtualRecord("orient", List.of(new VirtualComponent("q1", NUMBER), new VirtualComponent("q2", NUMBER), new VirtualComponent("q3", NUMBER), new VirtualComponent("q4", NUMBER))));
    public static final RapidType POSE = new RapidType(new VirtualRecord("pose", List.of(new VirtualComponent("trans", POSITION), new VirtualComponent("rot", ORIENTATION))));

    private final RapidStructure structure;
    private final String name;
    private final int dimensions;

    public RapidType(@Nullable RapidStructure structure) {
        this(structure, 0);
    }

    public RapidType(@Nullable RapidStructure structure, @Nullable String name) {
        this(structure, name, 0);
    }

    public RapidType(@Nullable RapidStructure structure, int dimensions) {
        this(structure, structure != null ? structure.getName() : null, dimensions);
    }

    public RapidType(@Nullable RapidStructure structure, @Nullable String name, int dimensions) {
        this.structure = structure;
        this.name = name;
        this.dimensions = dimensions;
    }

    public static boolean isAssignable(@NotNull RapidType left, @NotNull RapidType right) {
        if (left.equals(right)) return true;
        if (left.getDimensions() != right.getDimensions()) return false;
        RapidStructure leftStructure = left.getTargetStructure();
        RapidStructure rightStructure = right.getTargetStructure();
        if (leftStructure == null || rightStructure == null) {
            String leftText = left.getText();
            String rightText = right.getText();
            return leftText.equals(rightText);
        }
        return Objects.equals(leftStructure, rightStructure);
    }

    public @Nullable ValueType getValueType() {
        if (structure == null) return null;
        if (this == RapidType.NUMBER) return ValueType.VALUE_TYPE;
        if (this == RapidType.DOUBLE) return ValueType.VALUE_TYPE;
        if (this == RapidType.BOOLEAN) return ValueType.VALUE_TYPE;
        if (this == RapidType.STRING) return ValueType.VALUE_TYPE;
        if (structure instanceof RapidAtomic atomic) {
            return atomic.getType() != null ? ValueType.SEMI_VALUE_TYPE : ValueType.NON_VALUE_TYPE;
        }
        if (structure instanceof RapidAlias alias) {
            RapidType type = alias.getType();
            return type != null ? type.getValueType() : null;
        }
        if (structure instanceof RapidRecord record) {
            List<ValueType> valueTypes = new ArrayList<>();
            for (RapidComponent component : record.getComponents()) {
                RapidType type = component.getType();
                if (type == null) return null;
                valueTypes.add(type.getValueType());
            }
            if (valueTypes.contains(null)) return null;
            if (valueTypes.contains(ValueType.NON_VALUE_TYPE)) return ValueType.NON_VALUE_TYPE;
            if (valueTypes.stream().allMatch(valueType -> valueType.equals(ValueType.VALUE_TYPE)))
                return ValueType.VALUE_TYPE;
            return ValueType.SEMI_VALUE_TYPE;
        }
        throw new IllegalStateException();
    }

    public boolean isAssignable(@NotNull RapidType type) {
        return isAssignable(this, type);
    }

    public @NotNull RapidType createArrayType(int dimensions) {
        return new RapidType(getStructure(), name, dimensions);
    }

    public int getDimensions() {
        return dimensions;
    }

    public @Nullable RapidStructure getStructure() {
        return structure;
    }

    public @Nullable RapidStructure getTargetStructure() {
        RapidStructure structure = getStructure();
        while (structure instanceof RapidAlias || structure instanceof RapidAtomic) {
            RapidType type;
            if (structure instanceof RapidAlias alias) {
                type = alias.getType();
            } else {
                RapidAtomic atomic = (RapidAtomic) structure;
                type = atomic.getType();
            }
            if (type != null) {
                structure = type.getStructure();
            } else {
                break;
            }
        }
        return structure;
    }

    public @NotNull String getText() {
        return String.valueOf(name);
    }

    public @NotNull String getPresentableText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText());
        if (getDimensions() > 0) {
            stringBuilder.append("{");
            for (int i = 0; i < getDimensions(); i++) {
                if (i > 0) stringBuilder.append(",");
                stringBuilder.append("*");
            }
            stringBuilder.append("}");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidType rapidType = (RapidType) o;
        return getDimensions() == rapidType.getDimensions() && Objects.equals(getStructure(), rapidType.getStructure()) && Objects.equals(name, rapidType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStructure(), name, getDimensions());
    }

    @Override
    public String toString() {
        return "RapidType:" + getPresentableText();
    }
}
