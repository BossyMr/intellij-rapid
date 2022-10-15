package com.bossymr.rapid.language.psi.stubs.type;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.bossymr.rapid.language.psi.RapidModule;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidModuleImpl;
import com.bossymr.rapid.language.psi.stubs.RapidModuleStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidModuleStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidModuleElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidModuleElementType extends RapidStubElementType<RapidModuleStub, RapidModule> {

    public RapidModuleElementType() {
        super("MODULE");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidModuleElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidModuleImpl(node);
    }

    @Override
    public RapidModule createPsi(@NotNull RapidModuleStub stub) {
        return new RapidModuleImpl(stub);
    }

    @Override
    public @NotNull RapidModuleStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        LighterASTNode identifier = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.IDENTIFIER);
        String name = identifier != null ? LightTreeUtil.toFilteredString(tree, identifier, null) : null;
        return new RapidModuleStubImpl(parentStub, name);
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
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
