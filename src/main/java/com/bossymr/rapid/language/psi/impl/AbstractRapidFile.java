package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.stubs.RapidFileStub;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRapidFile extends PsiFileBase {

    protected AbstractRapidFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, RapidLanguage.getInstance());
    }

    @Override
    public @NotNull AbstractRapidFile getOriginalFile() {
        return (AbstractRapidFile) super.getOriginalFile();
    }

    @Override
    public @Nullable RapidFileStub getStub() {
        StubElement<?> stub = super.getStub();
        if (stub instanceof RapidFileStub fileStub) {
            return fileStub;
        }
        return null;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.getInstance();
    }
}
