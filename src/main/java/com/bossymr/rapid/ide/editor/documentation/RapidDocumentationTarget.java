package com.bossymr.rapid.ide.editor.documentation;

import com.bossymr.rapid.ide.editor.highlight.RapidColor;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidArray;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidTargetVariable;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.documentation.DocumentationManagerUtil;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.model.Pointer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class RapidDocumentationTarget<T extends RapidSymbol> implements DocumentationTarget {

    private final @NotNull Project project;
    private final @NotNull T symbol;

    protected RapidDocumentationTarget(@NotNull Project project, @NotNull T symbol) {
        this.project = project;
        this.symbol = symbol;
    }

    public @NotNull Project getProject() {
        return project;
    }

    public @NotNull T getSymbol() {
        return symbol;
    }

    public @NotNull String getName() {
        return symbol.getPresentableName();
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return getSymbol().getTargetPresentation();
    }

    @Override
    public abstract @Nullable DocumentationResult computeDocumentation();

    @Override
    public @Nullable String computeDocumentationHint() {
        return getSignature().replaceAll("\n|( {4})", "");
    }

    protected @NotNull String getSignature() {
        return DocumentationMarkup.DEFINITION_START + getPresentableText() + DocumentationMarkup.DEFINITION_END;
    }

    protected @NotNull String getPresentableText() {
        if (symbol instanceof RapidModule module) {
            return getPresentableText(module);
        }
        if (symbol instanceof RapidAtomic atomic) {
            return getPresentableText(atomic);
        }
        if (symbol instanceof RapidAlias alias) {
            return getPresentableText(alias);
        }
        if (symbol instanceof RapidRecord record) {
            return getPresentableText(record);
        }
        if (symbol instanceof RapidComponent component) {
            return getPresentableText(component);
        }
        if (symbol instanceof RapidField field) {
            return getPresentableText(field);
        }
        if (symbol instanceof RapidRoutine routine) {
            return getPresentableText(routine);
        }
        if (symbol instanceof RapidParameterGroup parameterGroup) {
            return getPresentableText(parameterGroup);
        }
        if (symbol instanceof RapidParameter parameter) {
            return getPresentableText(parameter);
        }
        if (symbol instanceof RapidTargetVariable variable) {
            return getPresentableText(variable);
        }
        if (symbol instanceof RapidLabelStatement statement) {
            return getPresentableText(statement);
        }
        throw new IllegalStateException("Unexpected symbol: " + symbol);
    }

    private @NotNull String getPresentableText(@NotNull RapidLabelStatement statement) {
        return statement.getPresentableName() + ":";
    }

    private @NotNull String getPresentableText(@NotNull RapidModule module) {
        StringBuilder stringBuilder = new StringBuilder();
        appendText(stringBuilder, RapidColor.KEYWORD, "MODULE");
        stringBuilder.append(" ");
        appendText(stringBuilder, RapidColor.ATOMIC, module.getPresentableName());
        if (!(module.getAttributes().isEmpty())) {
            stringBuilder.append("(");
            stringBuilder.append(module.getAttributes().stream()
                                       .map(ModuleType::getText)
                                       .collect(Collectors.joining(", ")));
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidAtomic atomic) {
        StringBuilder stringBuilder = new StringBuilder();
        appendVisibility(stringBuilder, atomic);
        appendText(stringBuilder, RapidColor.KEYWORD, "ATOMIC");
        stringBuilder.append(" ");
        appendType(stringBuilder, atomic.getAssociatedType());
        appendText(stringBuilder, RapidColor.ATOMIC, atomic.getPresentableName());
        appendText(stringBuilder, RapidColor.SEMICOLON, ";");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidAlias alias) {
        StringBuilder stringBuilder = new StringBuilder();
        appendVisibility(stringBuilder, alias);
        appendText(stringBuilder, RapidColor.KEYWORD, "ALIAS");
        stringBuilder.append(" ");
        appendType(stringBuilder, alias.getType());
        appendText(stringBuilder, RapidColor.ALIAS, alias.getPresentableName());
        appendText(stringBuilder, RapidColor.SEMICOLON, ";");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidRecord record) {
        StringBuilder stringBuilder = new StringBuilder();
        appendVisibility(stringBuilder, record);
        appendText(stringBuilder, RapidColor.KEYWORD, "RECORD");
        stringBuilder.append(" ");
        appendText(stringBuilder, RapidColor.RECORD, record.getPresentableName());
        for (RapidComponent component : record.getComponents()) {
            stringBuilder.append("\n").append(" ".repeat(4));
            stringBuilder.append(getPresentableText(component));
        }
        stringBuilder.append("\n");
        appendText(stringBuilder, RapidColor.KEYWORD, "ENDRECORD");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidComponent component) {
        StringBuilder stringBuilder = new StringBuilder();
        appendType(stringBuilder, component.getType());
        appendText(stringBuilder, RapidColor.COMPONENT, component.getPresentableName());
        appendText(stringBuilder, RapidColor.SEMICOLON, ";");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidField field) {
        StringBuilder stringBuilder = new StringBuilder();
        appendVisibility(stringBuilder, field);
        appendText(stringBuilder, RapidColor.KEYWORD, field.getFieldType().getText());
        stringBuilder.append(" ");
        RapidType type = field.getType();
        appendType(stringBuilder, type);
        appendText(stringBuilder, switch (field.getFieldType()) {
            case VARIABLE -> RapidColor.VARIABLE;
            case CONSTANT -> RapidColor.CONSTANT;
            case PERSISTENT -> RapidColor.PERSISTENT;
        }, field.getPresentableName());
        if (type != null && type.isArray()) {
            appendText(stringBuilder, RapidColor.BRACES, "{");
            if (field instanceof PhysicalField physicalField && physicalField.getArray() != null) {
                RapidArray array = physicalField.getArray();
                List<RapidExpression> dimensions = array.getDimensions();
                for (int i = 0; i < dimensions.size(); i++) {
                    if (i > 0) {
                        appendText(stringBuilder, RapidColor.COMMA, ", ");
                    }
                    HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(stringBuilder, project, RapidLanguage.getInstance(), dimensions.get(i).getText(), 1);
                }
            } else {
                for (int i = 0; i < type.getDimensions(); i++) {
                    if (i > 0) {
                        appendText(stringBuilder, RapidColor.COMMA, ", ");
                    }
                    appendText(stringBuilder, RapidColor.OPERATOR_SIGN, "*");
                }
            }
            appendText(stringBuilder, RapidColor.BRACES, "}");
        }
        RapidExpression initializer = field.getDetachedInitializer();
        if (initializer != null) {
            appendText(stringBuilder, RapidColor.OPERATOR_SIGN, " := ");
            HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(stringBuilder, project, RapidLanguage.getInstance(), initializer.getText(), 1);
        }
        appendText(stringBuilder, RapidColor.SEMICOLON, ";");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidTargetVariable variable) {
        StringBuilder stringBuilder = new StringBuilder();
        appendText(stringBuilder, RapidColor.VARIABLE, variable.getPresentableName());
        appendText(stringBuilder, RapidColor.SEMICOLON, ";");
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidRoutine routine) {
        StringBuilder stringBuilder = new StringBuilder();
        appendVisibility(stringBuilder, routine);
        appendText(stringBuilder, RapidColor.KEYWORD, routine.getRoutineType().getText());
        stringBuilder.append(" ");
        appendType(stringBuilder, routine.getType());
        appendText(stringBuilder, switch (routine.getRoutineType()) {
            case FUNCTION -> RapidColor.FUNCTION;
            case PROCEDURE -> RapidColor.PROCEDURE;
            case TRAP -> RapidColor.TRAP;
        }, routine.getPresentableName());
        List<? extends RapidParameterGroup> parameters = routine.getParameters();
        if (parameters != null) {
            appendText(stringBuilder, RapidColor.PARENTHESES, "(");
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    appendText(stringBuilder, RapidColor.COMMA, ", ");
                }
                stringBuilder.append("\n").append(" ".repeat(4));
                RapidParameterGroup group = parameters.get(i);
                stringBuilder.append(getPresentableText(group));
            }
            if (!(parameters.isEmpty())) {
                stringBuilder.append("\n");
            }
            appendText(stringBuilder, RapidColor.PARENTHESES, ")");
        }
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidParameterGroup group) {
        StringBuilder stringBuilder = new StringBuilder();
        if (group.isOptional()) {
            appendText(stringBuilder, RapidColor.OPERATOR_SIGN, "\\");
        }
        List<? extends RapidParameter> parameters = group.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                appendText(stringBuilder, RapidColor.LINE, " | ");
            }
            RapidParameter parameter = parameters.get(i);
            stringBuilder.append(getPresentableText(parameter));
        }
        return stringBuilder.toString();
    }

    private @NotNull String getPresentableText(@NotNull RapidParameter parameter) {
        RapidParameterGroup group = parameter.getParameterGroup();
        StringBuilder stringBuilder = new StringBuilder();
        appendText(stringBuilder, RapidColor.KEYWORD, switch (parameter.getParameterType()) {
            case INPUT -> "";
            case VARIABLE -> "VAR ";
            case PERSISTENT -> "PERS ";
            case INOUT -> "INOUT ";
            case REFERENCE -> "REF ";
        });
        appendType(stringBuilder, parameter.getType());
        appendText(stringBuilder, group.isOptional() ? RapidColor.OPTIONAL_PARAMETER : RapidColor.PARAMETER, parameter.getPresentableName());
        return stringBuilder.toString();
    }

    private void appendText(@NotNull StringBuilder stringBuilder, @NotNull RapidColor color, @Nullable String text) {
        appendText(stringBuilder, color.textAttributesKey(), text);
    }

    private void appendText(@NotNull StringBuilder stringBuilder, @NotNull TextAttributesKey textAttributesKey, @Nullable String text) {
        if (text == null) {
            return;
        }
        HtmlSyntaxInfoUtil.appendStyledSpan(stringBuilder, textAttributesKey, text, 1);
    }

    private void appendVisibility(@NotNull StringBuilder stringBuilder, @NotNull RapidVisibleSymbol symbol) {
        if (symbol.getVisibility() == Visibility.GLOBAL) {
            return;
        }
        appendText(stringBuilder, RapidColor.KEYWORD, symbol.getVisibility().getText());
        stringBuilder.append(" ");
    }

    private void appendType(@NotNull StringBuilder stringBuilder, @Nullable RapidType type) {
        if (type == null) {
            return;
        }
        RapidStructure structure = type.getStructure();
        if (structure instanceof RapidAtomic) {
            appendLink(stringBuilder, structure);
        } else if (structure instanceof RapidAlias) {
            appendLink(stringBuilder, structure);
        } else if (structure instanceof RapidRecord) {
            appendLink(stringBuilder, structure);
        } else {
            appendText(stringBuilder, HighlightInfoType.WRONG_REF.getAttributesKey(), type.getPresentableText());
        }
        stringBuilder.append(" ");
    }

    private void appendLink(@NotNull StringBuilder stringBuilder, @NotNull RapidSymbol symbol) {
        String name = symbol.getPresentableName();
        DocumentationManagerUtil.createHyperlink(stringBuilder, name.toLowerCase(), name, false, false);
    }

    @Override
    public @NotNull Pointer<? extends DocumentationTarget> createPointer() {
        return Pointer.delegatingPointer(getSymbol().createPointer(), symbol -> RapidDocumentationTargetProvider.createDocumentationTarget(project, symbol));
    }
}
