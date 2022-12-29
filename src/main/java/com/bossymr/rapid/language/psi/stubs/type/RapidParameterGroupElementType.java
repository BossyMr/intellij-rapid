package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidParameterGroupElementType extends RapidStubElementType<RapidParameterGroupStub, PhysicalParameterGroup> {

    public RapidParameterGroupElementType() {
        super("PARAMETER_GROUP");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalParameterGroup(node);
    }

    @Override
    public PhysicalParameterGroup createPsi(@NotNull RapidParameterGroupStub stub) {
        return new PhysicalParameterGroup(stub);
    }

    @Override
    public @NotNull RapidParameterGroupStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        boolean isOptional = StubUtil.hasChild(tree, node, RapidTokenTypes.BACKSLASH);
        return new RapidParameterGroupStub(parentStub, isOptional);
    }

    @Override
    public void serialize(@NotNull RapidParameterGroupStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(stub.isOptional());
    }

    @Override
    public @NotNull RapidParameterGroupStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        boolean isOptional = dataStream.readBoolean();
        return new RapidParameterGroupStub(parentStub, isOptional);
    }

    @Override
    public void indexStub(@NotNull RapidParameterGroupStub stub, @NotNull IndexSink sink) {}
}
