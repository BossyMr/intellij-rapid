package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.parser.RapidParserDefinition;
import com.bossymr.rapid.language.psi.RapidFile;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NotNull;

public class RapidFileStub extends PsiFileStubImpl<RapidFile> implements PsiFileStub<RapidFile> {

    public RapidFileStub(RapidFile file) {
        super(file);
    }

    @Override
    public @NotNull IStubFileElementType<RapidFileStub> getType() {
        return RapidParserDefinition.FILE;
    }

    @Override
    public String toString() {
        return "RapidFileStub";
    }
}
