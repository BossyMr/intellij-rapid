package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidParameter;
import com.bossymr.rapid.language.symbol.RapidParameterGroup;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an argument in a function or procedure call.
 */
public interface RapidArgument extends RapidElement {

    /**
     * Returns the name of the parameter this argument declares.
     *
     * @return the name of the parameter this argument declares.
     */
    @Nullable RapidReferenceExpression getParameter();

    /**
     * Returns the expression of this argument.
     *
     * @return the expression of this argument.
     */
    @Nullable RapidExpression getArgument();

    private static @Nullable RapidParameter getSymbol(@NotNull RapidArgument argument) {
        RapidReferenceExpression parameter = argument.getParameter();
        if (parameter != null) {
            return getSymbol(parameter);
        }
        RapidRoutine routine = getRoutine(argument);
        if (routine == null) {
            return null;
        }
        PsiElement parent = argument.getParent();
        if (!(parent instanceof RapidArgumentList argumentList)) {
            return null;
        }
        List<RapidParameter> previous = new ArrayList<>();
        for (RapidArgument otherArgument : argumentList.getArguments()) {
            RapidParameter symbol = getSymbol(routine, previous, otherArgument);
            previous.add(symbol);
            if (otherArgument.equals(argument)) {
                return symbol;
            }
        }
        return null;
    }

    private static @Nullable RapidParameter getSymbol(@NotNull RapidReferenceExpression parameter) {
        RapidSymbol symbol = parameter.getSymbol();
        if (symbol == null) {
            return null;
        }
        if (symbol instanceof RapidParameter parameterSymbol) {
            return parameterSymbol;
        }
        return null;
    }

    private static @Nullable RapidParameter getSymbol(@NotNull RapidRoutine routine, @NotNull List<RapidParameter> previous, @NotNull RapidArgument argument) {
        RapidReferenceExpression parameter = argument.getParameter();
        if (parameter != null) {
            return getSymbol(parameter);
        }
        List<? extends RapidParameterGroup> parameterGroups = routine.getParameters();
        if (parameterGroups == null) {
            return null;
        }
        for (RapidParameterGroup parameterGroup : parameterGroups) {
            if (parameterGroup.isOptional()) {
                continue;
            }
            List<? extends RapidParameter> parameters = parameterGroup.getParameters();
            if (parameters.isEmpty() || parameters.stream().anyMatch(previous::contains)) {
                continue;
            }
            return parameters.get(0);
        }
        return null;
    }

    private static @Nullable RapidRoutine getRoutine(@NotNull RapidArgument argument) {
        RapidCallExpression expression = PsiTreeUtil.getParentOfType(argument, RapidCallExpression.class);
        if (expression == null) {
            return null;
        }
        RapidExpression routineName = expression.getReferenceExpression();
        if (!(routineName instanceof RapidReferenceExpression referenceExpression)) {
            return null;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine routine)) {
            return null;
        }
        return routine;
    }

    default @Nullable RapidParameter getSymbol() {
        return CachedValuesManager.getProjectPsiDependentCache(this, symbol -> getSymbol(symbol));
    }
}
