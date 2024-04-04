package com.bossymr.rapid.ide.editor.parameterInfo;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.*;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.lang.ASTNode;
import com.intellij.lang.parameterInfo.*;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RapidParameterInfoHandler implements ParameterInfoHandler<RapidArgumentList, RapidParameterInfoHandler.RoutineInfo> {

    private static final Key<Set<String>> PRESENT_ARGUMENTS = Key.create("present.arguments");

    @Override
    public @Nullable RapidArgumentList findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        RapidArgumentList argumentList = findArgumentList(context, null);
        if (argumentList == null) {
            // We couldn't find the argument list.
            return null;
        }
        PsiElement parent = argumentList.getParent();
        if (!(parent instanceof RapidCallExpression expression)) {
            // This shouldn't be possible.
            return null;
        }
        if (!(expression.getReferenceExpression() instanceof RapidReferenceExpression referenceExpression)) {
            // This function call is late bound, i.e. we don't know which function will be called.
            return null;
        }
        RapidSymbol symbol = referenceExpression.getSymbol();
        if (!(symbol instanceof RapidRoutine routine)) {
            // This function call is invalid.
            return null;
        }
        context.setItemsToShow(new Object[]{getRoutineInfo(routine)});
        return argumentList;
    }

    private @NotNull RoutineInfo getRoutineInfo(@NotNull RapidRoutine routine) {
        RoutineInfo routineInfo = new RoutineInfo(new ArrayList<>());
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters == null) {
            return routineInfo;
        }
        for (RapidParameterGroup parameterGroup : parameters) {
            ParameterGroupInfo parameterGroupInfo = new ParameterGroupInfo(new ArrayList<>());
            routineInfo.groups().add(parameterGroupInfo);
            for (RapidParameter parameter : parameterGroup.getParameters()) {
                String parameterInfo = parameter.getPresentableName();
                if(parameter.getParameterType() != ParameterType.INPUT) {
                    parameterInfo = parameter.getParameterType().getText().toLowerCase() + " " + parameterInfo;
                }
                if (parameterGroup.isOptional()) {
                    parameterInfo = "\\" + parameterInfo;
                }
                parameterGroupInfo.parameters().add(parameterInfo);
            }
        }
        return routineInfo;
    }

    private @Nullable RapidArgumentList findArgumentList(@NotNull ParameterInfoContext context, @Nullable RapidArgumentList previous) {
        int offset = context.getOffset();
        PsiElement element = context.getFile().findElementAt(offset);
        if (element == null) {
            return null;
        }
        RapidArgumentList argumentList = findArgumentList(element, previous);
        if (argumentList != null) {
            return argumentList;
        }
        ASTNode node = element.getNode();
        if (node.getElementType() == TokenType.WHITE_SPACE) {
            int endOffset = node.getTextRange().getEndOffset();
            if (endOffset == offset) {
                // This is the last character in the whitespace element.
                PsiElement nextLeaf = PsiTreeUtil.nextLeaf(element, true);
                return findArgumentList(nextLeaf, previous);
            }
        }
        return null;
    }

    private @Nullable RapidArgumentList findArgumentList(@Nullable PsiElement element, @Nullable RapidArgumentList previous) {
        if (element == null) {
            return null;
        }
        if (PsiTreeUtil.isAncestor(previous, element, false)) {
            return previous;
        }
        RapidCallExpression expression = PsiTreeUtil.getParentOfType(element, RapidCallExpression.class, false);
        if (expression == null) {
            return null;
        }
        return expression.getArgumentList();
    }

    @Override
    public void showParameterInfo(@NotNull RapidArgumentList element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    @Override
    public @Nullable RapidArgumentList findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return findArgumentList(context, (RapidArgumentList) context.getParameterOwner());
    }

    @Override
    public void updateParameterInfo(@NotNull RapidArgumentList argumentList, @NotNull UpdateParameterInfoContext context) {
        RapidArgument argument = findCurrentArgument(argumentList, context);
        if (argument == null) {
            context.setCurrentParameter(-1);
            return;
        }
        RapidParameter parameter = argument.getSymbol();
        if (parameter == null) {
            context.setCurrentParameter(-1);
            return;
        }
        RapidParameterGroup parameterGroup = parameter.getParameterGroup();
        RapidRoutine routine = parameterGroup.getRoutine();
        if (routine.getParameters() == null) {
            context.setCurrentParameter(-1);
            return;
        }
        int currentParameter = 0;
        for (RapidParameterGroup group : routine.getParameters()) {
            if (group.equals(parameterGroup)) {
                currentParameter += group.getParameters().indexOf(parameter);
                break;
            } else {
                currentParameter += group.getParameters().size();
            }
        }
        context.setCurrentParameter(currentParameter);
    }

    private @Nullable RapidArgument findCurrentArgument(@NotNull RapidArgumentList argumentList, @NotNull ParameterInfoContext context) {
        PsiElement element = context.getFile().findElementAt(context.getOffset());
        if (element == null) {
            return null;
        }
        if (element.getNode().getElementType() == TokenType.WHITE_SPACE) {
            element = PsiTreeUtil.nextLeaf(element);
        }
        if (element == null) {
            return null;
        }
        if (element.getNode().getElementType() == RapidTokenTypes.COMMA || element.getNode().getElementType() == RapidTokenTypes.RPARENTH) {
            element = PsiTreeUtil.prevLeaf(element);
        }
        if (element instanceof RapidArgument argument && argumentList.getArguments().contains(element)) {
            return argument;
        }
        while (true) {
            RapidArgument parent = PsiTreeUtil.getParentOfType(element, RapidArgument.class);
            if (parent == null) {
                return null;
            }
            if (argumentList.getArguments().contains(parent)) {
                return parent;
            }
            element = parent;
        }
    }

    @Override
    public void updateUI(@NotNull RoutineInfo routineInfo, @NotNull ParameterInfoUIContext context) {
        String text = computeText(routineInfo);
        TextRange textRange = routineInfo.getTextRange(context.getCurrentParameterIndex());
        context.setupUIComponentPresentation(text, textRange.getStartOffset(), textRange.getEndOffset(), !context.isUIComponentEnabled(), false, false, context.getDefaultParameterColor());
    }

    private @NotNull String computeText(@NotNull RoutineInfo routineInfo) {
        if (routineInfo.groups().isEmpty()) {
            return CodeInsightBundle.message("parameter.info.no.parameters");
        }
        return routineInfo.getText();
    }

    public record RoutineInfo(@NotNull List<ParameterGroupInfo> groups) {

        public @NotNull String getText() {
            return groups().stream()
                    .map(ParameterGroupInfo::getText)
                    .collect(Collectors.joining(", "));
        }

        public @NotNull TextRange getTextRange(int index) {
            if (index < 0) {
                return TextRange.EMPTY_RANGE;
            }
            int offset = 0;
            for (ParameterGroupInfo parameterGroupInfo : groups) {
                int parameters = parameterGroupInfo.parameters().size();
                if (index < parameters) {
                    return parameterGroupInfo.getTextRange(index, offset);
                }
                index -= parameters;
                offset += parameterGroupInfo.parameters().stream().mapToInt(text -> text.length() + 3).sum() - 1;
            }
            return TextRange.EMPTY_RANGE;
        }

    }

    public record ParameterGroupInfo(@NotNull List<String> parameters) {

        public @NotNull String getText() {
            return String.join(" | ", parameters());
        }

        public @NotNull TextRange getTextRange(int index, int offset) {
            if (index < 0 || index >= parameters.size()) {
                return TextRange.EMPTY_RANGE;
            }
            int startOffset = offset;
            for (int i = 0; i < index; i++) {
                startOffset += parameters.get(i).length();
            }
            int endOffset = startOffset + parameters.get(index).length();
            return TextRange.create(startOffset, endOffset);
        }
    }
}
