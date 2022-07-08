package io.github.bossymr.language.psi.stubs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import io.github.bossymr.language.psi.RapidSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidSymbolIndex extends StringStubIndexExtension<RapidSymbol> {
    public static final StubIndexKey<String, RapidSymbol> KEY = StubIndexKey.createIndexKey("rapid.symbol.name");

    @Override
    public @NotNull StubIndexKey<String, RapidSymbol> getKey() {
        return KEY;
    }

    public static Collection<RapidSymbol> find(String name, Project project) {
        return StubIndex.getElements(KEY, StringUtil.toLowerCase(name), project, ProjectScope.getProjectScope(project), RapidSymbol.class);
    }
}
