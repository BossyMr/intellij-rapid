package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidModuleIndex;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidModuleElementType extends RapidStubElementType<RapidModuleStub, PhysicalModule> {

    public RapidModuleElementType() {
        super("MODULE");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalModule(node);
    }

    @Override
    public PhysicalModule createPsi(@NotNull RapidModuleStub stub) {
        return new PhysicalModule(stub);
    }

    @Override
    public @NotNull RapidModuleStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        return new RapidModuleStub(parentStub, name);
    }

    @Override
    public void serialize(@NotNull RapidModuleStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
    }

    @Override
    public @NotNull RapidModuleStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        return new RapidModuleStub(parentStub, name);
    }

    @Override
    public void indexStub(@NotNull RapidModuleStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidModuleIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
