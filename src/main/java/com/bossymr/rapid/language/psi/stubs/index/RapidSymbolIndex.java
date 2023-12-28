package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidSymbolIndex extends StringStubIndexExtension<PhysicalSymbol> implements RapidIndex<PhysicalSymbol> {
    public static final StubIndexKey<String, PhysicalSymbol> KEY = StubIndexKey.createIndexKey("rapid.symbol");

    private static final RapidSymbolIndex INSTANCE = new RapidSymbolIndex();

    public static RapidSymbolIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalSymbol> getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<PhysicalSymbol> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalSymbol.class);
    }
}
