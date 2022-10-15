package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.RapidFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

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
        if (element != null) return element;
        throw new IllegalArgumentException("Invalid name '" + name + "'");
    }

    public @NotNull RapidExpression createExpression(@NotNull String text) {
        RapidFile file = createDummyFile("MODULE DUMMY VAR DUMMY DUMMY := " + text + "; ENDMODULE");
        RapidField field = file.getModules().get(0).getFields().get(0);
        if(field.getInitializer() != null) return field.getInitializer();
        throw new IllegalArgumentException("Invalid expression '" + text + "'");
    }

}
