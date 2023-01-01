package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidField;
import com.bossymr.rapid.language.symbol.RapidModule;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

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
        return (RapidFile) PsiFileFactory.getInstance(project).createFileFromText(DUMMY_FILE_NAME, RapidFileType.INSTANCE, text);
    }

    /**
     * Creates a new identifier element with the specified name.
     *
     * @param name the name of the identifier.
     * @return the identifier element.
     * @throws IllegalArgumentException if the specified name is invalid.
     */
    public @NotNull PsiElement createIdentifier(@NotNull String name) {
        RapidFile file = createDummyFile("MODULE " + name + " ENDMODULE");
        PsiElement element = file.getModules().get(0).getIdentifyingElement();
        if (element == null) throw new IllegalArgumentException("Invalid name '" + name + "'");
        return element;
    }

    public @NotNull RapidExpression createExpression(@NotNull String text) {
        RapidFile file = createDummyFile("MODULE DUMMY VAR DUMMY DUMMY := " + text + "; ENDMODULE");
        RapidField field = file.getModules().get(0).getFields().get(0);
        if (field.getInitializer() == null) throw new IllegalArgumentException("Invalid expression '" + text + "'");
        return field.getInitializer();
    }

    public @NotNull RapidAttributeList createAttributeList(List<RapidModule.Attribute> attributes) {
        String text = attributes.stream()
                .map(RapidModule.Attribute::getText)
                .collect(Collectors.joining(","));
        RapidFile file = createDummyFile("MODULE DUMMY(" + text + ") ENDMODULE");
        RapidAttributeList attributeList = file.getModules().get(0).getAttributeList();
        if (attributeList == null) throw new IllegalArgumentException();
        return attributeList;
    }
}
