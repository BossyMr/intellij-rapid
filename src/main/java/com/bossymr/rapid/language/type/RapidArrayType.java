package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RapidArrayType implements RapidType {

    private final @NotNull RapidType underlyingType;
    private final @Nullable List<SmartPsiElementPointer<RapidExpression>> length;

    public RapidArrayType(@NotNull RapidType underlyingType, @Nullable List<RapidExpression> length) {
        this.underlyingType = underlyingType;
        this.length = length != null ? length.stream().map(SmartPointerManager::createPointer).toList() : null;
    }

    public @NotNull RapidType getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public @NotNull RapidType createArrayType(int dimensions, @Nullable List<RapidExpression> length) {
        return getUnderlyingType().createArrayType(dimensions, length);
    }

    @Override
    public @NotNull RapidType createArrayType(int dimensions) {
        List<RapidExpression> expressions = length == null ? null : length.stream()
                .map(SmartPsiElementPointer::dereference)
                .toList();
        if(expressions != null && expressions.contains(null)) {
            expressions = null;
        }
        return getUnderlyingType().createArrayType(dimensions, expressions);
    }

    @Override
    public int getDimensions() {
        if(underlyingType instanceof RapidArrayType arrayType) {
            return arrayType.getDimensions() + 1;
        } else {
            return 1;
        }
    }

    public @Nullable RapidExpression getLength() {
        if(length == null) {
            return null;
        }
        int dimension = getDimensions();
        if(dimension < 0 || dimension > length.size()) {
            return null;
        }
        return length.get(length.size() - dimension).dereference();
    }

    @Override
    public @Nullable RapidStructure getStructure() {
        return underlyingType.getStructure();
    }

    @Override
    public @Nullable RapidStructure getRootStructure() {
        return underlyingType.getRootStructure();
    }

    @Override
    public @NotNull ValueType getValueType() {
        return underlyingType.getValueType();
    }

    @Override
    public @NotNull String getText() {
        return underlyingType.getText();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        RapidArrayType that = (RapidArrayType) object;
        return Objects.equals(underlyingType, that.underlyingType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(underlyingType);
    }

    @Override
    public String toString() {
        return getPresentableText();
    }
}
