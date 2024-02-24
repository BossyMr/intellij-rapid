package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.parser.RapidParserDefinition;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.stubs.RapidFileStub;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractRapidFile extends PsiFileImpl implements RapidFile {

    protected AbstractRapidFile(@NotNull FileViewProvider viewProvider) {
        super(RapidParserDefinition.FILE, RapidParserDefinition.FILE, viewProvider);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        visitor.visitFile(this);
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
    public @NotNull List<PhysicalModule> getModules() {
        StubElement<?> stub = getGreenStub();
        if (stub != null) {
            return List.of(stub.getChildrenByType(RapidElementTypes.MODULE, PhysicalModule[]::new));
        }
        return List.of(findChildrenByClass(PhysicalModule.class));
    }


    @Override
    public @NotNull FileType getFileType() {
        return RapidFileType.getInstance();
    }
}
