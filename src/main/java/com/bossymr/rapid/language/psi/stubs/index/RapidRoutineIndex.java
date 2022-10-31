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

public final class RapidRoutineIndex extends StringStubIndexExtension<PhysicalRoutine> {
    public static final StubIndexKey<String, PhysicalRoutine> KEY = StubIndexKey.createIndexKey("rapid.symbol.routine");

    private static final RapidRoutineIndex INSTANCE = new RapidRoutineIndex();

    public static RapidRoutineIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalRoutine> getKey() {
        return KEY;
    }

    public Collection<PhysicalRoutine> get(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalRoutine.class);
    }
}
