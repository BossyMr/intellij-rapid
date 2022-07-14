package io.github.bossymr.language.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a type (alias, record, atomic, or array).
 */
public class RapidType {

    private final Supplier<RapidStructure> factory;
    private final int dimensions;

    public RapidType(@NotNull Supplier<RapidStructure> factory, int dimensions) {
        this.factory = factory;
        this.dimensions = dimensions;
    }

    public static RapidType create(@NotNull RapidReferenceExpression expression) {
        return new RapidType(() -> {
            PsiElement element = expression.resolve();
            return element instanceof RapidStructure ? (RapidStructure) element : null;
        }, 0) {
            @Override
            public @NotNull String getCanonicalText() {
                return expression.getCanonicalText();
            }
        };
    }

    public static RapidType create(@NotNull RapidStructure structure) {
        return new RapidType(() -> structure, 0);
    }

    /**
     * Creates a new array type with the specified dimensions and the same type as this type.
     *
     * @param dimensions the dimensions of the new type.
     * @return a new array type.
     */
    @Contract(pure = true)
    public @NotNull RapidType createArrayType(int dimensions) {
        return new RapidType(factory, dimensions);
    }

    /**
     * Returns the structure of this type.
     *
     * @return the structure of this type.
     */
    public RapidStructure getStructure() {
        return factory.get();
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
        builder.append('{');
        for (int i = 0; i < dimensions; i++) {
            if (i > 0) builder.append(",");
            builder.append('*');
        }
        builder.append('}');
        return builder.toString();
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
