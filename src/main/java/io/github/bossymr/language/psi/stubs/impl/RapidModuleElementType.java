package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import io.github.bossymr.language.psi.RapidModule;
import io.github.bossymr.language.psi.RapidStubElementType;
import io.github.bossymr.language.psi.impl.RapidModuleImpl;
import io.github.bossymr.language.psi.stubs.RapidModuleStub;
import io.github.bossymr.language.psi.stubs.RapidSymbolNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidModuleElementType extends RapidStubElementType<RapidModuleStub, RapidModule> {

    public RapidModuleElementType() {
        super("MODULE");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new RapidModuleImpl(node);
    }

    @Override
    public RapidModule createPsi(@NotNull RapidModuleStub stub) {
        return new RapidModuleImpl(stub);
    }

    @Override
    public @NotNull RapidModuleStub createStub(@NotNull RapidModule psi, StubElement<? extends PsiElement> parentStub) {
        return new RapidModuleStubImpl(parentStub, psi.getName());
    }

    @Override
    public void serialize(@NotNull RapidModuleStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public @NotNull RapidModuleStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        return new RapidModuleStubImpl(parentStub, name);
    }

    @Override
    public void indexStub(@NotNull RapidModuleStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if(name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }

    }
}
