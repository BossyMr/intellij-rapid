package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidTrapIndex extends StringStubIndexExtension<PhysicalRoutine> implements RapidIndex<PhysicalRoutine> {
    public static final StubIndexKey<String, PhysicalRoutine> KEY = StubIndexKey.createIndexKey("rapid.symbol.trap");

    private static final RapidTrapIndex INSTANCE = new RapidTrapIndex();

    public static RapidTrapIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalRoutine> getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<PhysicalRoutine> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalRoutine.class);
    }
}
