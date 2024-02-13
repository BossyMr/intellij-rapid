package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.psi.BlockType;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A {@code RapidRoutine} represents a method.
 */
@SuppressWarnings("UnstableApiUsage")
public interface RapidRoutine extends RapidVisibleSymbol {

    /**
     * Returns the routine type.
     *
     * @return the routine type.
     */
    @NotNull RoutineType getRoutineType();

    /**
     * Returns the return type of this routine.
     *
     * @return the return type of this routine, or {@code null} if a return type is not defined.
     */
    @Nullable RapidType getType();

    /**
     * Returns the parameters declared in this routine.
     *
     * @return the parameters declared in this routine, or {@code null} if a parameter list is not defined.
     */
    @Nullable List<? extends RapidParameterGroup> getParameters();

    /**
     * Returns the local fields declared in this routine.
     *
     * @return the local fields declared in this routine.
     */
    @NotNull List<? extends RapidField> getFields();

    /**
     * Returns the statements in this routine.
     *
     * @return the statements in this routine.
     */
    @NotNull List<RapidStatement> getStatements();

    /**
     * Returns the statements in the specified statement list in this routine.
     *
     * @param blockType the type of statement list.
     * @return the statements in the specified statement list, or {@code null} if this routine does not declare a
     * statement list of the specified type.
     */
    @Nullable List<RapidStatement> getStatements(@NotNull BlockType blockType);

    @Nullable List<RapidExpression> getErrorClause();

    @Override
    @NotNull Pointer<? extends RapidRoutine> createPointer();

    @Override
    default @NotNull TargetPresentation getTargetPresentation() {
        return TargetPresentation.builder(getPresentableName())
                .icon(switch (getRoutineType()) {
                    case FUNCTION -> RapidIcons.FUNCTION;
                    case PROCEDURE -> RapidIcons.PROCEDURE;
                    case TRAP -> RapidIcons.TRAP;
                }).presentation();
    }
}
