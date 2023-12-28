package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidModuleIndex extends StringStubIndexExtension<PhysicalModule> implements RapidIndex<PhysicalModule> {
    public static final StubIndexKey<String, PhysicalModule> KEY = StubIndexKey.createIndexKey("rapid.symbol.module");

    private static final RapidModuleIndex INSTANCE = new RapidModuleIndex();

    public static RapidModuleIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalModule> getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<PhysicalModule> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalModule.class);
    }
}
