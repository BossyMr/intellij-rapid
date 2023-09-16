package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.psi.RapidExpression;
import org.jetbrains.annotations.Nullable;

public interface SnapshotExpression extends ReferenceExpression {

    @Nullable ReferenceExpression getUnderlyingVariable();

    @Override
    default @Nullable RapidExpression getElement() {
        if (getUnderlyingVariable() != null) {
            return getUnderlyingVariable().getElement();
        }
        return null;
    }
}
