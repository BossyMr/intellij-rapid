package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class RapidFileImpl extends PsiFileBase implements RapidFile {

    public RapidFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RapidLanguage.INSTANCE);
    }

    @Override
    public void deleteChildRange(@Nullable PsiElement first, @Nullable PsiElement last) throws IncorrectOperationException {
        if (first != null && first == last) {
            if (first instanceof PhysicalModule) {
                List<PhysicalModule> modules = getModules();
                if (modules.size() == 1) {
                    delete();
                    return;
                }
            }
        }
        super.deleteChildRange(first, last);
    }

    @Override
    public @NotNull List<PhysicalModule> getModules() {
        return List.of(findChildrenByClass(PhysicalModule.class));
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
