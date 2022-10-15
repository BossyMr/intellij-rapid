package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.RapidModule;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class RapidFileImpl extends PsiFileBase implements RapidFile {

    public RapidFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RapidLanguage.INSTANCE);
    }

    @Override
    public @NotNull List<RapidModule> getModules() {
        return PsiTreeUtil.getStubChildrenOfTypeAsList(this, RapidModule.class);
    }

    @Override
    protected @Nullable Icon getElementIcon(int flags) {
        return getFileType().getIcon();
    }

    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "RapidFile:" + getName();
    }
}
