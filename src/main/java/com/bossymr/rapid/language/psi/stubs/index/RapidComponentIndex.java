package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidComponentIndex extends StringStubIndexExtension<PhysicalComponent> {
    public static final StubIndexKey<String, PhysicalComponent> KEY = StubIndexKey.createIndexKey("rapid.symbol.component");

    private static final RapidComponentIndex INSTANCE = new RapidComponentIndex();

    public static RapidComponentIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalComponent> getKey() {
        return KEY;
    }

    public Collection<PhysicalComponent> get(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalComponent.class);
    }
}
