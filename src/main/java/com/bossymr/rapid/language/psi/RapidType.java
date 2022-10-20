package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.psi.light.LightAtomic;
import com.bossymr.rapid.language.psi.light.LightComponent;
import com.bossymr.rapid.language.psi.light.LightRecord;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a type (alias, record, atomic, or array).
 */
public class RapidType {

    public static final Function<Project, RapidType> NUMBER = (project) -> new RapidType(new LightAtomic(PsiManager.getInstance(project), "num"));
    public static final Function<Project, RapidType> DOUBLE = (project) -> new RapidType(new LightAtomic(PsiManager.getInstance(project), "dnum"));
    public static final Function<Project, RapidType> BOOLEAN = (project) -> new RapidType(new LightAtomic(PsiManager.getInstance(project), "bool"));
    public static final Function<Project, RapidType> STRING = (project) -> new RapidType(new LightAtomic(PsiManager.getInstance(project), "string"));
    public static final Function<Project, RapidType> POSITION = (project) -> new RapidType(new LightRecord(PsiManager.getInstance(project), "pos", List.of(new LightComponent(PsiManager.getInstance(project), "x", NUMBER.apply(project)), new LightComponent(PsiManager.getInstance(project), "y", NUMBER.apply(project)), new LightComponent(PsiManager.getInstance(project), "z", NUMBER.apply(project)))));
    public static final Function<Project, RapidType> ORIENTATION = (project) -> new RapidType(new LightRecord(PsiManager.getInstance(project), "orient", List.of(new LightComponent(PsiManager.getInstance(project), "q1", NUMBER.apply(project)), new LightComponent(PsiManager.getInstance(project), "q2", NUMBER.apply(project)), new LightComponent(PsiManager.getInstance(project), "q3", NUMBER.apply(project)), new LightComponent(PsiManager.getInstance(project), "q4", NUMBER.apply(project)))));
    public static final Function<Project, RapidType> POSE = (project) -> new RapidType(new LightRecord(PsiManager.getInstance(project), "pose", List.of(new LightComponent(PsiManager.getInstance(project), "trans", POSITION.apply(project)), new LightComponent(PsiManager.getInstance(project), "rot", ORIENTATION.apply(project)))));

    private final RapidStructure structure;
    private final int dimensions;

    public RapidType(@Nullable RapidStructure structure) {
        this(structure, 0);
    }

    public RapidType(@Nullable RapidStructure structure, int dimensions) {
        this.structure = structure;
        this.dimensions = dimensions;
    }

    /**
     * Checks if a value of the specified {@code right} type can be assigned to a field of {@code left} type.
     *
     * @param left  the type to assign to.
     * @param right the value type.
     * @return if the right type can be assigned to a field of the left type.
     */
    public static boolean isAssignable(@NotNull RapidType left, @NotNull RapidType right) {
        if (left.equals(right)) return true;
        if (left.getDimensions() != right.getDimensions()) return false;
        RapidStructure leftStructure = unwrap(left);
        RapidStructure rightStructure = unwrap(right);
        if (leftStructure == null || rightStructure == null) return false;
        return Objects.equals(leftStructure, rightStructure);
    }

    private static @Nullable RapidStructure unwrap(@NotNull RapidType type) {
        RapidStructure structure = type.getStructure();
        while (structure instanceof RapidAlias) {
            RapidType raw = ((RapidAlias) structure).getType();
            structure = raw != null ? raw.getStructure() : null;
        }
        return structure;
    }

    /**
     * Creates a new array type with the specified dimensions and the same type as this type.
     *
     * @param dimensions the dimensions of the new type.
     * @return a new array type.
     */
    @Contract(pure = true)
    public @NotNull RapidType createArrayType(int dimensions) {
        return new RapidType(getStructure(), dimensions);
    }

    /**
     * Returns the structure of this type.
     *
     * @return the structure of this type.
     */
    public @Nullable RapidStructure getStructure() {
        return structure;
    }

    /**
     * Returns the dimensions of this structure, a dimension of 0 represents a non-array type.
     *
     * @return the dimensions of this structure.
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * Returns the name of the structure of this type.
     *
     * @return the name of the structure of this type.
     */
    public @Nullable String getCanonicalText() {
        return getStructure() != null ? getStructure().getName() : null;
    }

    /**
     * Returns a presentable representation of this type.
     *
     * @return a presentable representation of this type.
     */
    public @NotNull String getPresentableText() {
        StringBuilder builder = new StringBuilder();
        builder.append(getCanonicalText());
        if(getDimensions() > 0) {
            builder.append('{');
            for (int i = 0; i < dimensions; i++) {
                if (i > 0) builder.append(",");
                builder.append('*');
            }
            builder.append('}');
        }
        return builder.toString();
    }

    /**
     * Checks if a value of the specified type can be assigned to this type.
     *
     * @param type the value type.
     * @return if the specified type can be assigned to a field of this type.
     */
    public boolean isAssignable(@NotNull RapidType type) {
        return isAssignable(this, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidType rapidType = (RapidType) o;
        return getDimensions() == rapidType.getDimensions() && Objects.equals(getStructure(), rapidType.getStructure());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStructure(), getDimensions());
    }

    @Override
    public String toString() {
        return "RapidType:" + getPresentableText();
    }
}
