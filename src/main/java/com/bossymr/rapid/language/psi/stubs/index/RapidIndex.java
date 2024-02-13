package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface RapidIndex<T extends PhysicalSymbol> {

    Collection<String> getAllKeys(Project project);

    @NotNull Collection<T> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope);

}
