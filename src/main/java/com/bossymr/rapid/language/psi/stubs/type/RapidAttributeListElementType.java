package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.impl.RapidAttributeListImpl;
import com.bossymr.rapid.language.psi.stubs.RapidAttributeListStub;
import com.bossymr.rapid.language.psi.stubs.node.RapidAttributeListElement;
import com.bossymr.rapid.language.symbol.RapidModule.Attribute;
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
import java.util.Set;

public class RapidAttributeListElementType extends RapidStubElementType<RapidAttributeListStub, RapidAttributeList> {

    public RapidAttributeListElementType() {
        super("ATTRIBUTE_LIST");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidAttributeListElement(RapidElementTypes.ATTRIBUTE_LIST);
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidAttributeListImpl(node);
    }

    @Override
    public RapidAttributeList createPsi(@NotNull RapidAttributeListStub stub) {
        return new RapidAttributeListImpl(stub);
    }

    @Override
    public @NotNull RapidAttributeListStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        Set<Attribute> attributeSet = Attribute.getAttributes(tree, node);
        return new RapidAttributeListStub(parentStub, attributeSet);
    }

    @Override
    public void serialize(@NotNull RapidAttributeListStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeVarInt(stub.getMask());
    }

    @Override
    public @NotNull RapidAttributeListStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        int mask = dataStream.readVarInt();
        return new RapidAttributeListStub(parentStub, mask);
    }

    @Override
    public void indexStub(@NotNull RapidAttributeListStub stub, @NotNull IndexSink sink) {}
}
