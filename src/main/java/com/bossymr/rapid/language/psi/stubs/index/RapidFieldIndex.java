package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidFieldIndex extends StringStubIndexExtension<PhysicalField> implements RapidIndex<PhysicalField> {
    public static final StubIndexKey<String, PhysicalField> KEY = StubIndexKey.createIndexKey("rapid.symbol.field");

    private static final RapidFieldIndex INSTANCE = new RapidFieldIndex();

    public static RapidFieldIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalField> getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<PhysicalField> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalField.class);
    }
}
