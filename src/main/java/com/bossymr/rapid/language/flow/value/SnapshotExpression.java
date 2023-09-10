package com.bossymr.rapid.language.flow.value;

import org.jetbrains.annotations.Nullable;

public interface SnapshotExpression extends ReferenceExpression {

    @Nullable ReferenceExpression getUnderlyingVariable();

}
