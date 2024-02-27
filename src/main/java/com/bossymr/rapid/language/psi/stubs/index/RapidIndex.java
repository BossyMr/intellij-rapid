package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface RapidIndex<T extends PhysicalSymbol> {

    Collection<String> getAllKeys(Project project);

    @NotNull Collection<T> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope);

    default @NotNull Collection<T> getAllElements(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        List<T> modules = new ArrayList<>();
        for (String routineName : getAllKeys(project)) {
            modules.addAll(getElements(routineName, project, scope));
        }
        return modules;
    }
}
