package com.bossymr.rapid.language.psi.stubs.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.bossymr.rapid.language.psi.RapidSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidSymbolNameIndex extends StringStubIndexExtension<RapidSymbol> {
    public static final StubIndexKey<String, RapidSymbol> KEY = StubIndexKey.createIndexKey("rapid.symbol.name");

    private static final RapidSymbolNameIndex INSTANCE = new RapidSymbolNameIndex();

    public static RapidSymbolNameIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, RapidSymbol> getKey() {
        return KEY;
    }

    public Collection<RapidSymbol> get(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, RapidSymbol.class);
    }
}
