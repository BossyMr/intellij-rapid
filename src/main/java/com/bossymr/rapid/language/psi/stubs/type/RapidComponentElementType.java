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
import com.bossymr.rapid.language.psi.RapidComponent;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidComponentImpl;
import com.bossymr.rapid.language.psi.stubs.RapidComponentStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidComponentStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidComponentElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidComponentElementType extends RapidStubElementType<RapidComponentStub, RapidComponent> {

    public RapidComponentElementType() {
        super("COMPONENT");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidComponentElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidComponentImpl(node);
    }

    @Override
    public RapidComponent createPsi(@NotNull RapidComponentStub stub) {
        return new RapidComponentImpl(stub);
    }

    @Override
    public @NotNull RapidComponentStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        LighterASTNode identifier = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.IDENTIFIER);
        String name = identifier != null ? LightTreeUtil.toFilteredString(tree, identifier, null) : null;
        LighterASTNode typeElement = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = typeElement != null ? LightTreeUtil.toFilteredString(tree, typeElement, null) : null;
        return new RapidComponentStubImpl(parentStub, name, type);
    }

    @Override
    public void serialize(@NotNull RapidComponentStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
    }

    @Override
    public @NotNull RapidComponentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        return new RapidComponentStubImpl(parentStub, name, type);
    }

    @Override
    public void indexStub(@NotNull RapidComponentStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
