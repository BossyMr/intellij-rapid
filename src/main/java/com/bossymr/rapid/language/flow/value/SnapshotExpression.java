package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.data.snapshots.ArraySnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.RecordSnapshot;
import com.bossymr.rapid.language.flow.data.snapshots.VariableSnapshot;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface SnapshotExpression extends ReferenceExpression {

    @Nullable ReferenceExpression getUnderlyingVariable();

    @Override
    default @Nullable RapidExpression getElement() {
        ReferenceExpression underlyingVariable = getUnderlyingVariable();
        if (underlyingVariable != null) {
            return underlyingVariable.getElement();
        } else {
            return null;
        }
    }

    static @NotNull SnapshotExpression createSnapshot(@NotNull RapidType type) {
        return Objects.requireNonNull(createSnapshot(type, null));
    }

    static @Nullable SnapshotExpression createSnapshot(@NotNull ReferenceExpression expression) {
        return createSnapshot(expression.getType(), expression);
    }

    static @Nullable SnapshotExpression createSnapshot(@NotNull RapidType type, @Nullable ReferenceExpression expression) {
        if (expression instanceof FieldExpression) {
            return null;
        }
        if (type.getDimensions() > 0) {
            Function<ArraySnapshot, Expression> defaultValue = (arraySnapshot) -> {
                VariableSnapshot indexSnapshot = new VariableSnapshot(RapidPrimitiveType.NUMBER);
                IndexExpression indexExpression = new IndexExpression(arraySnapshot, indexSnapshot);
                return createSnapshot(indexExpression.getType(), indexExpression);
            };
            return new ArraySnapshot(defaultValue, type, expression);
        } else if (type.getRootStructure() instanceof RapidRecord record) {
            RecordSnapshot snapshot = new RecordSnapshot(type, expression);
            for (RapidComponent component : record.getComponents()) {
                String componentName = component.getName();
                RapidType componentType = component.getType();
                if (componentName == null || componentType == null) {
                    continue;
                }
                ComponentExpression componentExpression = new ComponentExpression(componentType, snapshot, componentName);
                SnapshotExpression componentSnapshot = createSnapshot(componentType, componentExpression);
                snapshot.assign(componentName, componentSnapshot);
            }
            return snapshot;
        } else {
            return new VariableSnapshot(type, expression);
        }
    }
}
