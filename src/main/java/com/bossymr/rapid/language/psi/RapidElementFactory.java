package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.impl.fragment.RapidExpressionCodeFragmentImpl;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Creates a new attribute list with the specified attributes.
     *
     * @param attributes the attributes.
     * @return the attribute list.
     */
    public @NotNull RapidAttributeList createAttributeList(@NotNull List<RapidModule.Attribute> attributes) {
        String text = attributes.stream()
                .map(RapidModule.Attribute::getText)
                .collect(Collectors.joining(","));
        RapidFile file = createDummyFile("MODULE DUMMY(" + text + ") ENDMODULE");
        return file.getModules().get(0).getAttributeList();
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
