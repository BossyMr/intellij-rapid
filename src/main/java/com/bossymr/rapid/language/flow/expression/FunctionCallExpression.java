package com.bossymr.rapid.language.flow.expression;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionCallExpression implements Expression {

    private final @Nullable SmartPsiElementPointer<RapidExpression> expression;

    private final @NotNull RapidType returnType;
    private final @NotNull String name;
    private final @NotNull List<Entry> arguments;

    public FunctionCallExpression(@NotNull RapidType returnType, @NotNull String name, @NotNull List<Entry> arguments) {
        this(null, returnType, name, arguments);
    }

    public FunctionCallExpression(@Nullable RapidExpression expression, @NotNull RapidType returnType, @NotNull String name, @NotNull List<Entry> arguments) {
        this.expression = expression != null ? SmartPointerManager.createPointer(expression) : null;
        this.returnType = returnType;
        this.name = name;
        this.arguments = arguments;
    }

    public static @NotNull Expression constant(@NotNull Snapshot variable, @NotNull Expression value) {
        RapidType variableType = variable.getType();
        if (!(variableType.isArray())) {
            throw new IllegalArgumentException("Cannot create constant expression for variable: " + variable + " of type: " + variableType);
        }
        RapidType arrayType = variableType.createArrayType(variableType.getDimensions() - 1);
        if (!(arrayType.isAssignable(value.getType()))) {
            throw new IllegalArgumentException("Cannot create constant expression for variable: " + variable + " of type: " + variableType + " with value of type: " + value.getType());
        }
        return new FunctionCallExpression(variableType, ":ConstantA", List.of(Entry.pointerOf(variable), Entry.valueOf(value)));
    }

    public static @NotNull Expression select(@NotNull Snapshot variable, @NotNull Expression index) {
        RapidType type = variable.getType();
        if (!(type.isArray())) {
            throw new IllegalArgumentException("Cannot create index expression for variable: " + variable + " of type: " + type);
        }
        if (!(index.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException("Cannot reference index of type: " + index.getType());
        }
        RapidType arrayType = type.createArrayType(type.getDimensions() - 1);
        return new FunctionCallExpression(arrayType, ":SelectA", List.of(Entry.valueOf(new SnapshotExpression(variable)), Entry.valueOf(index)));
    }

    public static @NotNull Expression store(@NotNull Snapshot variable, @NotNull Expression index, @NotNull Expression value) {
        RapidType variableType = variable.getType();
        if (!(variableType.isArray())) {
            throw new IllegalArgumentException("Cannot create store expression for variable: " + variable + " of type: " + variableType);
        }
        RapidType indexType = index.getType();
        if (!(indexType.isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException("Cannot reference index of type: " + indexType);
        }
        return new FunctionCallExpression(variableType, ":StoreA", List.of(Entry.valueOf(new SnapshotExpression(variable)), Entry.valueOf(index), Entry.valueOf(value)));
    }

    public static @NotNull Expression select(@NotNull Snapshot variable, @NotNull String componentName) {
        RapidType variableType = variable.getType();
        if (!(variableType.isRecord()) || !(variableType.getRootStructure() instanceof RapidRecord record)) {
            throw new IllegalArgumentException("Cannot create component expression for variable: " + variable + " of type: " + variableType);
        }
        RapidType componentType = Objects.requireNonNull(getComponentType(record, componentName));
        return new FunctionCallExpression(componentType, ":SelectS", List.of(Entry.valueOf(new SnapshotExpression(variable)), Entry.valueOf(new LiteralExpression(componentName))));
    }

    public static @NotNull Expression store(@NotNull Snapshot variable, @NotNull String componentName, @NotNull Expression value) {
        RapidType variableType = variable.getType();
        if (!(variableType.isRecord()) || !(variableType.getRootStructure() instanceof RapidRecord)) {
            throw new IllegalArgumentException("Cannot create store expression for variable: " + variable + " of type: " + variableType);
        }
        return new FunctionCallExpression(variableType, ":StoreS", List.of(Entry.valueOf(new SnapshotExpression(variable)), Entry.valueOf(new LiteralExpression(componentName)), Entry.valueOf(value)));
    }

    public static @NotNull Expression present(@NotNull Snapshot variable) {
        return new FunctionCallExpression(RapidPrimitiveType.BOOLEAN, ":Present", List.of(Entry.pointerOf(variable)));
    }

    public static @NotNull Expression length(@NotNull Snapshot variable, @NotNull Expression depth) {
        if (!(variable.getType().isArray()) || !(depth.getType().isAssignable(RapidPrimitiveType.NUMBER))) {
            throw new IllegalArgumentException("Cannot create dimension expression for variable: " + variable + " at depth: " + depth);
        }
        return new FunctionCallExpression(RapidPrimitiveType.NUMBER, ":Dim", List.of(Entry.pointerOf(variable), Entry.valueOf(depth)));
    }

    private static @Nullable RapidType getComponentType(@NotNull RapidRecord record, @NotNull String componentName) {
        for (RapidComponent component : record.getComponents()) {
            if (componentName.equalsIgnoreCase(component.getName())) {
                return component.getType();
            }
        }
        return null;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<Entry> getArguments() {
        return arguments;
    }

    @Override
    public @NotNull RapidType getType() {
        return returnType;
    }

    @Override
    public @Nullable RapidExpression getElement() {
        return expression != null ? expression.getElement() : null;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitFunctionCallExpression(this);
    }

    @Override
    public String toString() {
        return name + arguments.stream()
                               .map(entry -> {
                                   if (entry instanceof Entry.ValueEntry valueEntry) {
                                       return valueEntry.expression().toString();
                                   }
                                   if (entry instanceof Entry.ReferenceEntry referenceEntry) {
                                       return referenceEntry.snapshot().toString();
                                   }
                                   throw new AssertionError();
                               })
                               .collect(Collectors.joining(", ", "(", ")"));
    }

    public sealed interface Entry {

        static @NotNull ValueEntry valueOf(@NotNull Expression expression) {
            return new ValueEntry(expression);
        }

        static @NotNull ReferenceEntry pointerOf(@NotNull Snapshot snapshot) {
            return new ReferenceEntry(snapshot);
        }

        record ValueEntry(@NotNull Expression expression) implements Entry {}

        record ReferenceEntry(@NotNull Snapshot snapshot) implements Entry {}

    }
}
