package com.bossymr.rapid.language.psi.stubs.type;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.bossymr.rapid.language.psi.RapidParameterGroup;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidParameterGroupImpl;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidParameterGroupStubImpl;
import com.bossymr.rapid.language.psi.stubs.node.RapidParameterGroupElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidParameterGroupElementType extends RapidStubElementType<RapidParameterGroupStub, RapidParameterGroup> {

    public RapidParameterGroupElementType() {
        super("PARAMETER_GROUP");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidParameterGroupElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidParameterGroupImpl(node);
    }

    @Override
    public RapidParameterGroup createPsi(@NotNull RapidParameterGroupStub stub) {
        return new RapidParameterGroupImpl(stub);
    }

    @Override
    public @NotNull RapidParameterGroupStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        boolean isOptional = hasChild(tree, node, RapidTokenTypes.BACKSLASH);
        return new RapidParameterGroupStubImpl(parentStub, isOptional);
    }

    @Override
    public void serialize(@NotNull RapidParameterGroupStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(stub.isOptional());
    }

    @Override
    public @NotNull RapidParameterGroupStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        boolean isOptional = dataStream.readBoolean();
        return new RapidParameterGroupStubImpl(parentStub, isOptional);
    }

    @Override
    public void indexStub(@NotNull RapidParameterGroupStub stub, @NotNull IndexSink sink) {
    }
}
