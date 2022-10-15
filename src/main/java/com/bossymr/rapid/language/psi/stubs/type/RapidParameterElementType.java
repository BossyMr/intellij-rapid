package com.bossymr.rapid.language.psi.stubs.type;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LightTreeUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IElementType;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidParameter.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidParameterImpl;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidParameterStubImpl;
import com.bossymr.rapid.language.psi.stubs.node.RapidParameterElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class RapidParameterElementType extends RapidStubElementType<RapidParameterStub, RapidParameter> {

    public RapidParameterElementType() {
        super("PARAMETER");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidParameterElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidParameterImpl(node);
    }

    @Override
    public RapidParameter createPsi(@NotNull RapidParameterStub stub) {
        return new RapidParameterImpl(stub);
    }

    @Override
    public @NotNull RapidParameterStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        LighterASTNode typeNode = LightTreeUtil.firstChildOfType(tree, node, Attribute.TOKEN_SET);
        Attribute attribute = typeNode != null ? Attribute.getAttribute(typeNode.getTokenType()) : Attribute.INPUT;
        String name = getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        return new RapidParameterStubImpl(parentStub, attribute, name, type);
    }

    @Override
    public void serialize(@NotNull RapidParameterStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
    }

    @Override
    public @NotNull RapidParameterStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Attribute attribute = Attribute.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        return new RapidParameterStubImpl(parentStub, attribute, name, type);
    }

    @Override
    public void indexStub(@NotNull RapidParameterStub stub, @NotNull IndexSink sink) {
    }
}
