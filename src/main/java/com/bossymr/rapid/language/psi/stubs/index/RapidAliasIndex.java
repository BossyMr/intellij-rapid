package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalAlias;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidAliasIndex extends StringStubIndexExtension<PhysicalAlias> {
    public static final StubIndexKey<String, PhysicalAlias> KEY = StubIndexKey.createIndexKey("rapid.symbol.alias");

    private static final RapidAliasIndex INSTANCE = new RapidAliasIndex();

    public static RapidAliasIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalAlias> getKey() {
        return KEY;
    }

    public Collection<PhysicalAlias> get(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalAlias.class);
    }
}
