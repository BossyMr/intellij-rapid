package com.bossymr.rapid.language.flow.expression;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SnapshotExpression implements ReferenceExpression {

    private final @NotNull Snapshot snapshot;
    private final @Nullable SmartPsiElementPointer<RapidExpression> element;

    public SnapshotExpression(@NotNull Snapshot snapshot) {
        this(snapshot, (RapidExpression) null);
    }

    public SnapshotExpression(@NotNull Snapshot snapshot, @Nullable Expression underlyingElement) {
        this(snapshot, underlyingElement != null ? underlyingElement.getElement() : null);
    }

    public SnapshotExpression(@NotNull Snapshot snapshot, @Nullable RapidExpression element) {
        this.snapshot = snapshot;
        this.element = element != null ? SmartPointerManager.createPointer(element) : null;
    }

    public @NotNull Snapshot getSnapshot() {
        return snapshot;
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
        if(element == null) {
            return null;
        }
        return element.getElement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotExpression that = (SnapshotExpression) o;
        return Objects.equals(snapshot, that.snapshot);
    }

    @Override
    public int hashCode() {
        return snapshot.hashCode();
    }

    @Override
    public String toString() {
        return snapshot.toString();
    }
}
