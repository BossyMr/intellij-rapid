package com.bossymr.rapid.language.psi.stubs.index;

import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class RapidRecordIndex extends StringStubIndexExtension<PhysicalRecord> implements RapidIndex<PhysicalRecord> {
    public static final StubIndexKey<String, PhysicalRecord> KEY = StubIndexKey.createIndexKey("rapid.symbol.record");

    private static final RapidRecordIndex INSTANCE = new RapidRecordIndex();

    public static RapidRecordIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull StubIndexKey<String, PhysicalRecord> getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<PhysicalRecord> getElements(@NotNull String name, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), StringUtil.toLowerCase(name), project, scope, PhysicalRecord.class);
    }
}
