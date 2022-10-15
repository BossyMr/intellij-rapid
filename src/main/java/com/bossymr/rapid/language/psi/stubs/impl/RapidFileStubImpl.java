package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import com.bossymr.rapid.language.parser.RapidParserDefinition;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.stubs.RapidFileStub;
import org.jetbrains.annotations.NotNull;

public class RapidFileStubImpl extends PsiFileStubImpl<RapidFile> implements RapidFileStub {

    public RapidFileStubImpl(RapidFile file) {
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
