package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidParameterList;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.impl.RapidParameterListImpl;
import com.bossymr.rapid.language.psi.stubs.RapidParameterListStub;
import com.bossymr.rapid.language.psi.stubs.node.RapidParameterListElement;
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

public class RapidParameterListElementType extends RapidStubElementType<RapidParameterListStub, RapidParameterList> {

    public RapidParameterListElementType() {
        super("PARAMETER_LIST");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidParameterListElement(RapidElementTypes.PARAMETER_LIST);
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidParameterListImpl(node);
    }

    @Override
    public RapidParameterList createPsi(@NotNull RapidParameterListStub stub) {
        return new RapidParameterListImpl(stub);
    }

    @Override
    public @NotNull RapidParameterListStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        return new RapidParameterListStub(parentStub);
    }

    @Override
    public void serialize(@NotNull RapidParameterListStub stub, @NotNull StubOutputStream dataStream) throws IOException {}

    @Override
    public @NotNull RapidParameterListStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new RapidParameterListStub(parentStub);
    }

    @Override
    public void indexStub(@NotNull RapidParameterListStub stub, @NotNull IndexSink sink) {}
}
