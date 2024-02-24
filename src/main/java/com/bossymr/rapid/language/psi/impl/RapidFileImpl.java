package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class RapidFileImpl extends AbstractRapidFile {

    public RapidFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider);
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
    protected @Nullable Icon getElementIcon(int flags) {
        List<PhysicalModule> modules = getModules();
        if (modules.size() == 1) {
            return modules.get(0).getIcon(flags);
        }
        return getFileType().getIcon();
    }

    @Override
    public String toString() {
        return "RapidFile:" + getName();
    }
}
