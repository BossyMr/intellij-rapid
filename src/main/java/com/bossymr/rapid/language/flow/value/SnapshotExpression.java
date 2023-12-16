package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.Optionality;
import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SnapshotExpression implements ReferenceExpression {

    private final @NotNull Snapshot snapshot;
    private final @Nullable ReferenceExpression underlyingElement;

    public SnapshotExpression(@NotNull Snapshot snapshot, @Nullable ReferenceExpression underlyingElement) {
        this.snapshot = snapshot;
        this.underlyingElement = underlyingElement;
    }

    public static @NotNull SnapshotExpression createSnapshot(@NotNull RapidType type) {
        return createSnapshot(type, Optionality.PRESENT);
    }

    public static @NotNull SnapshotExpression createSnapshot(@NotNull ReferenceExpression expression) {
        return createSnapshot(expression.getType(), Optionality.PRESENT);
    }

    public static @NotNull SnapshotExpression createSnapshot(@NotNull RapidType type, @NotNull Optionality optionality) {
        if (type.getDimensions() > 0) {
            Function<ArraySnapshot, Expression> defaultValue = (arraySnapshot) -> {
                VariableSnapshot indexSnapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER, optionality);
                IndexExpression indexExpression = new IndexExpression(arraySnapshot, indexSnapshot);
                return createSnapshot(indexExpression.getType(), optionality);
            };
            return new ArraySnapshot(type, optionality, defaultValue);
        } else if (type.getRootStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(type, optionality);
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }
                ComponentExpression componentExpression = new ComponentExpression(componentType, snapshot, componentName);
                SnapshotExpression componentSnapshot = createSnapshot(componentType, optionality);
                snapshot.assign(componentName, componentSnapshot);
            }
            return snapshot;
        } else {
            return new VariableSnapshot(type, optionality);
        }
    }

    public @NotNull Snapshot getSnapshot() {
        return snapshot;
    }

    public @Nullable ReferenceExpression getUnderlyingVariable() {
        return underlyingElement;
    }

    @Override
    public @NotNull RapidType getType() {
        return snapshot.getType();
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitSnapshotExpression(this);
    }

    @Override
    public @Nullable RapidExpression getElement() {
        ReferenceExpression underlyingVariable = getUnderlyingVariable();
        if (underlyingVariable != null) {
            return underlyingVariable.getElement();
        } else {
            return null;
        }
    }
}
