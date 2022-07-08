package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import io.github.bossymr.language.parser.RapidParserDefinition;
import io.github.bossymr.language.psi.RapidFile;
import io.github.bossymr.language.psi.stubs.RapidFileStub;
import org.jetbrains.annotations.NotNull;

public class RapidFileStubImpl extends PsiFileStubImpl<RapidFile> implements RapidFileStub {

    public RapidFileStubImpl(RapidFile file) {
        super(file);
    }

    @Override
    public @NotNull IStubFileElementType<RapidFileStub> getType() {
        return RapidParserDefinition.FILE;
    }
}
