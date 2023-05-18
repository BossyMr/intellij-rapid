package com.bossymr.rapid.ide.editor.refactoring;

import com.bossymr.rapid.language.lexer.RapidLexer;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RapidNamesValidator implements NamesValidator {

    @Override
    public boolean isKeyword(@NotNull String name, Project project) {
        return RapidLexer.isKeyword(name);
    }

    @Override
    public boolean isIdentifier(@NotNull String name, Project project) {
        return RapidLexer.isIdentifier(name);
    }
}
