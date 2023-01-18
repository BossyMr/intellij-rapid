package com.bossymr.rapid.ide.documentation;

import com.bossymr.rapid.ide.highlight.RapidColor;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.ResolveUtil;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public @Nullable @Nls String generateDoc(@Nullable PsiElement element, @Nullable PsiElement original) {
        if (element == null) return null;
        RapidSymbol symbol = getSymbol(element);
        if (symbol != null) {
            return generateDoc(element.getProject(), symbol);
        }
        return null;
    }

    @Override
    public @Nullable @Nls String getQuickNavigateInfo(@Nullable PsiElement element, @Nullable PsiElement originalElement) {
        if (element == null) return null;
        RapidSymbol symbol = getSymbol(element);
        if (symbol != null) {
            StringBuilder builder = new StringBuilder();
            writeSymbol(element.getProject(), builder, symbol);
            String text = builder.toString();
            text = text.replace("\n", "");
            text = text.replace("\t", "");
            return text;
        }
        return null;
    }


    private @Nullable RapidSymbol getSymbol(@NotNull PsiElement element) {
        if (element instanceof RapidReferenceExpression expression) {
            RapidSymbol symbol = expression.getSymbol();
            if (symbol != null) return symbol;
        }
        if (element instanceof FakeObject<?> object) {
            return object.getValue(VirtualSymbol.class);
        }
        if (element instanceof RapidSymbol symbol) {
            return symbol;
        }
        return null;
    }

    private @Nls @NotNull String generateDoc(@NotNull Project project, @NotNull RapidSymbol symbol) {
        StringBuilder builder = new StringBuilder();
        writeSymbol(project, builder, symbol);
        if (symbol instanceof PhysicalSymbol physicalSymbol) {
            writePhysical(builder, physicalSymbol);
        }
        return builder.toString();
    }

    @Override
    public @Nullable PsiElement getDocumentationElementForLink(@Nullable PsiManager manager, @Nullable String link, @Nullable PsiElement context) {
        if (manager == null || link == null || context == null) return null;
        List<RapidSymbol> symbols = ResolveUtil.getSymbols(context, link);
        if (symbols.size() > 0) {
            RapidSymbol symbol = symbols.get(0);
            if (symbol instanceof PhysicalSymbol physicalSymbol) {
                return physicalSymbol;
            }
            if (symbol instanceof VirtualSymbol virtualSymbol) {
                return new FakeObject<>(manager, context, virtualSymbol);
            }
        }
        return null;
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement, int targetOffset) {
        if (contextElement == null) return null;
        IElementType elementType = contextElement.getNode().getElementType();
        return RapidTokenTypes.KEYWORDS.contains(elementType) ? contextElement : null;
    }

    private void writePhysical(@NotNull StringBuilder builder, @NotNull PhysicalSymbol symbol) {
        builder.append(DocumentationMarkup.CONTENT_START);
        for (PsiElement element = symbol.getFirstChild(); element != null; element = element.getNextSibling()) {
            IElementType elementType = element.getNode().getElementType();
            if (TokenSet.WHITE_SPACE.contains(elementType)) continue;
            if (elementType != RapidTokenTypes.COMMENT) break;
            String text = element.getText();
            text = text.strip().substring(1).strip() + " ";
            builder.append(text);
        }
        builder.append(DocumentationMarkup.CONTENT_END);
    }

    private void writeSymbol(@NotNull Project project, @NotNull StringBuilder builder, @NotNull RapidSymbol symbol) {
        builder.append(DocumentationMarkup.DEFINITION_START);
        if (symbol instanceof RapidAtomic atomic) writeAtomic(builder, atomic);
        if (symbol instanceof RapidAlias alias) writeAlias(builder, alias);
        if (symbol instanceof RapidRecord record) writeRecord(builder, record);
        if (symbol instanceof RapidComponent component) writeComponent(builder, component);
        if (symbol instanceof RapidField field) writeField(project, builder, field);
        if (symbol instanceof RapidRoutine routine) writeRoutine(builder, routine);
        builder.append(DocumentationMarkup.DEFINITION_END);
    }

    private void write(@NotNull StringBuilder builder, @NotNull RapidColor color, @Nullable String text) {
        write(builder, color.textAttributesKey(), text);
    }

    private void write(@NotNull StringBuilder builder, @NotNull TextAttributesKey textAttributesKey, @Nullable String text) {
        if (text != null) HtmlSyntaxInfoUtil.appendStyledSpan(builder, textAttributesKey, text, 1);
    }

    private void writeVisibility(@NotNull StringBuilder builder, @NotNull RapidAccessibleSymbol symbol) {
        if (symbol.getVisibility() != Visibility.GLOBAL) {
            write(builder, RapidColor.KEYWORD, symbol.getVisibility().getText());
            builder.append(" ");
        }
    }

    private void writeType(@NotNull StringBuilder builder, @NotNull RapidType type) {
        RapidStructure structure = type.getStructure();
        if (structure != null) {
            if (structure instanceof RapidAtomic) {
                writeLink(builder, RapidColor.ATOMIC, structure);
            }
            if (structure instanceof RapidAlias) {
                writeLink(builder, RapidColor.ALIAS, structure);
            }
            if (structure instanceof RapidRecord) {
                writeLink(builder, RapidColor.RECORD, structure);
            }
        } else {
            write(builder, HighlightInfoType.WRONG_REF.getAttributesKey(), type.getPresentableText());
        }
    }

    private void writeLink(@NotNull StringBuilder builder, @NotNull RapidColor color, @NotNull RapidSymbol symbol) {
        String name = symbol.getName();
        if (name != null) {
            write(builder, color, "<a href=\"" + DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL + name.toLowerCase() + "\">" + name + "<a/>");
        }
    }

    private void writeAtomic(@NotNull StringBuilder builder, @NotNull RapidAtomic atomic) {
        writeVisibility(builder, atomic);
        write(builder, RapidColor.KEYWORD, "ATOMIC");
        builder.append(" ");
        if (atomic.getType() != null) {
            writeType(builder, atomic.getType());
            builder.append(" ");
        }
        write(builder, RapidColor.ATOMIC, atomic.getName());
        write(builder, RapidColor.SEMICOLON, ";");
    }

    private void writeAlias(@NotNull StringBuilder builder, @NotNull RapidAlias alias) {
        writeVisibility(builder, alias);
        write(builder, RapidColor.KEYWORD, "ALIAS");
        builder.append(" ");
        if (alias.getType() != null) {
            writeType(builder, alias.getType());
            builder.append(" ");
        }
        write(builder, RapidColor.ALIAS, alias.getName());
        write(builder, RapidColor.SEMICOLON, ";");
    }

    private void writeRecord(@NotNull StringBuilder builder, @NotNull RapidRecord record) {
        writeVisibility(builder, record);
        write(builder, RapidColor.KEYWORD, "RECORD");
        builder.append(" ");
        write(builder, RapidColor.RECORD, record.getName());
        for (RapidComponent component : record.getComponents()) {
            builder.append("\n\t");
            writeComponent(builder, component);
        }
        builder.append("\n");
        write(builder, RapidColor.KEYWORD, "ENDRECORD");
    }

    private void writeComponent(@NotNull StringBuilder builder, @NotNull RapidComponent component) {
        if (component.getType() != null) {
            writeType(builder, component.getType());
            builder.append(" ");
        }
        write(builder, RapidColor.COMPONENT, component.getName());
        write(builder, RapidColor.SEMICOLON, ";");
    }

    private void writeField(@NotNull Project project, @NotNull StringBuilder builder, @NotNull RapidField field) {
        writeVisibility(builder, field);
        write(builder, RapidColor.KEYWORD, field.getAttribute().getText());
        builder.append(" ");
        if (field.getType() != null) {
            writeType(builder, field.getType());
            builder.append(" ");
        }
        write(builder, switch (field.getAttribute()) {
            case VARIABLE -> RapidColor.VARIABLE;
            case CONSTANT -> RapidColor.CONSTANT;
            case PERSISTENT -> RapidColor.PERSISTENT;
        }, field.getName());
        if (field.getInitializer() != null) {
            builder.append(" := ");
            HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(builder, project, RapidLanguage.getInstance(), field.getInitializer().getText(), 1);
        }
        write(builder, RapidColor.SEMICOLON, ";");
    }

    private void writeRoutine(@NotNull StringBuilder builder, @NotNull RapidRoutine routine) {
        writeVisibility(builder, routine);
        write(builder, RapidColor.KEYWORD, routine.getAttribute().getText());
        builder.append(" ");
        if (routine.getType() != null) {
            writeType(builder, routine.getType());
            builder.append(" ");
        }
        write(builder, switch (routine.getAttribute()) {
            case FUNCTION -> RapidColor.FUNCTION;
            case PROCEDURE -> RapidColor.PROCEDURE;
            case TRAP -> RapidColor.TRAP;
        }, routine.getName());
        if (routine.getParameters() != null) {
            write(builder, RapidColor.PARENTHESES, "(");
            List<? extends RapidParameterGroup> parameters = routine.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) write(builder, RapidColor.COMMA, ", ");
                builder.append("\n\t");
                RapidParameterGroup group = parameters.get(i);
                writeParameterGroup(builder, group);
            }
            builder.append("\n");
            write(builder, RapidColor.PARENTHESES, ")");
        }
    }

    private void writeParameterGroup(@NotNull StringBuilder builder, @NotNull RapidParameterGroup group) {
        if (group.isOptional()) {
            write(builder, RapidColor.OPERATOR_SIGN, "\\");
        }
        List<? extends RapidParameter> parameters = group.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) write(builder, RapidColor.LINE, " | ");
            RapidParameter parameter = parameters.get(i);
            write(builder, RapidColor.KEYWORD, switch (parameter.getAttribute()) {
                case INPUT -> "";
                case VARIABLE -> "VAR ";
                case PERSISTENT -> "PERS ";
                case INOUT -> "INOUT ";
                case REFERENCE -> "REF ";
            });
            if (parameter.getType() != null) {
                writeType(builder, parameter.getType());
                builder.append(" ");
            }
            write(builder, group.isOptional() ? RapidColor.OPTIONAL_PARAMETER : RapidColor.PARAMETER, parameter.getName());
        }
    }

    private static class FakeObject<T> extends FakePsiElement implements PsiNamedElement {

        private final PsiManager manager;
        private final PsiElement context;
        private final T value;

        private FakeObject(@NotNull PsiManager manager, @NotNull PsiElement context, @NotNull T value) {
            this.manager = manager;
            this.context = context;
            this.value = value;
        }

        @Override
        public @Nullable @NonNls String getText() {
            return value.toString();
        }

        public @NotNull T getValue() {
            return value;
        }

        public <E> @Nullable E getValue(@NotNull Class<E> clazz) {
            return clazz.isInstance(getValue()) ? clazz.cast(getValue()) : null;
        }

        @Override
        public @NotNull PsiManager getManager() {
            return manager;
        }

        @Override
        public @NotNull PsiElement getParent() {
            return context;
        }
    }
}
