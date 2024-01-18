package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.impl.fragment.RapidExpressionCodeFragmentImpl;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.physical.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class RapidElementFactory {

    private static final String DUMMY_FILE_NAME = "DUMMY.mod";

    private final Project project;

    public RapidElementFactory(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull RapidElementFactory getInstance(@NotNull Project project) {
        return project.getService(RapidElementFactory.class);
    }

    private @NotNull RapidFile createDummyFile(@NotNull String text) {
        return (RapidFile) PsiFileFactory.getInstance(project).createFileFromText(DUMMY_FILE_NAME, RapidFileType.getInstance(), text);
    }

    /**
     * Creates a new identifier element with the specified name.
     *
     * @param name the name of the identifier.
     * @return the identifier.
     * @throws IllegalArgumentException if the specified name is invalid.
     */
    public @NotNull PsiElement createIdentifier(@NotNull String name) {
        RapidFile file = createDummyFile("MODULE " + name + " ENDMODULE");
        PsiElement element = file.getModules().get(0).getIdentifyingElement();
        if (element == null) throw new IllegalArgumentException("Invalid name '" + name + "'");
        return element;
    }

    /**
     * Creates a new expression from the specified text.
     *
     * @param text the text.
     * @return the expression.
     */
    public @NotNull RapidExpression createExpressionFromText(@NotNull String text) {
        RapidFile file = createDummyFile("MODULE DUMMY VAR DUMMY DUMMY := " + text + "; ENDMODULE");
        RapidField field = file.getModules().get(0).getFields().get(0);
        if (field.getInitializer() == null) throw new IllegalArgumentException("Invalid expression '" + text + "'");
        return field.getInitializer();
    }

    public @NotNull RapidStatement createStatementFromText(@NotNull String text) {
        RapidFile file = createDummyFile("MODULE DUMMY PROC DUMMY() " + text + " ENDPROC ENDMODULE");
        RapidRoutine field = file.getModules().get(0).getRoutines().get(0);
        return field.getStatements().get(0);
    }

    /**
     * Creates a new attribute list with the specified attributes.
     *
     * @param moduleTypes the attributes.
     * @return the attribute list.
     */
    public @NotNull RapidAttributeList createAttributeList(@NotNull List<ModuleType> moduleTypes) {
        String text = moduleTypes.stream()
                                 .map(ModuleType::getText)
                                 .collect(Collectors.joining(","));
        RapidFile file = createDummyFile("MODULE DUMMY(" + text + ") ENDMODULE");
        return Objects.requireNonNull(file.getModules().get(0).getAttributeList());
    }

    public @NotNull RapidTypeElement createTypeElement(@NotNull String name) {
        RapidFile file = createDummyFile("MODULE DUMMY FUNC " + name + " DUMMY() ENDFUNC ENDMODULE");
        PhysicalModule module = file.getModules().get(0);
        PhysicalRoutine symbol = ((PhysicalRoutine) module.getSymbols().get(0));
        return Objects.requireNonNull(symbol.getTypeElement());
    }

    public @NotNull RapidArray createArray(@NotNull List<RapidExpression> expressions) {
        String expressionText = expressions.stream()
                                           .map(PsiElement::getText)
                                           .collect(Collectors.joining(", "));
        RapidFile file = createDummyFile("MODULE DUMMY VAR num DUMMY{" + expressionText + "}; ENDMODULE");
        PhysicalModule module = file.getModules().get(0);
        PhysicalField symbol = ((PhysicalField) module.getSymbols().get(0));
        return Objects.requireNonNull(symbol.getArray());
    }

    public @NotNull List<PsiElement> createArray(int dimensions) {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (int i = 0; i < dimensions; i++) {
            String s = "*";
            joiner.add(s);
        }
        String expressionText = joiner.toString();
        RapidFile file = createDummyFile("MODULE DUMMY PROC DUMMY DUMMY(num DUMMY" + expressionText + ") ENDPROC ENDMODULE");
        PhysicalModule module = file.getModules().get(0);
        PhysicalRoutine symbol = ((PhysicalRoutine) module.getSymbols().get(0));
        List<PhysicalParameterGroup> parameterGroups = symbol.getParameters();
        Objects.requireNonNull(parameterGroups);
        List<PhysicalParameter> parameters = parameterGroups.get(0).getParameters();
        PhysicalParameter parameter = parameters.get(0);
        List<PsiElement> elements = new ArrayList<>();
        ASTNode element = parameter.getNode().findChildByType(RapidTokenTypes.LBRACE);
        while (element != null) {
            elements.add(element.getPsi());
            if (element.getElementType() == RapidTokenTypes.RBRACE) {
                element = null;
            } else {
                element = element.getTreeNext();
            }
        }
        return elements;
    }

    /**
     * Creates a new Rapid expression code fragment from the specified text.
     *
     * @param text the text of the expression.
     * @param context the context used to resolve references.
     * @param isPhysical whether the code fragment is physical, see {@link PsiElement#isPhysical()}.
     * @return the code fragment.
     */
    public @NotNull RapidExpressionCodeFragment createExpressionCodeFragment(@NotNull String text,
                                                                             @Nullable PsiElement context,
                                                                             boolean isPhysical) {
        return new RapidExpressionCodeFragmentImpl(project, isPhysical, "fragment.mod", text, context);
    }
}
