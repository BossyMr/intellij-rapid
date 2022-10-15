package com.bossymr.rapid.language.psi.stubs.type;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidAttributeListImpl;
import com.bossymr.rapid.language.psi.stubs.RapidAttributeListStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidAttributeListStubImpl;
import com.bossymr.rapid.language.psi.stubs.node.RapidParameterListElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class RapidAttributeListElementType extends RapidStubElementType<RapidAttributeListStub, RapidAttributeList> {

    public RapidAttributeListElementType() {
        super("ATTRIBUTE_LIST");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidParameterListElement(RapidElementTypes.ATTRIBUTE_LIST, RapidTokenTypes.ATTRIBUTES);
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
        Set<ModuleAttribute> attributeSet = EnumSet.noneOf(ModuleAttribute.class);
        for (LighterASTNode child : tree.getChildren(node)) {
            ModuleAttribute attribute = ModuleAttribute.getAttribute(child.getTokenType());
            if (attribute != null) {
                attributeSet.add(attribute);
            }
        }
        return new RapidAttributeListStubImpl(parentStub, attributeSet);
    }

    @Override
    public void serialize(@NotNull RapidAttributeListStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeVarInt(stub.getMask());
    }

    @Override
    public @NotNull RapidAttributeListStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new RapidAttributeListStubImpl(parentStub, dataStream.readVarInt());
    }

    @Override
    public void indexStub(@NotNull RapidAttributeListStub stub, @NotNull IndexSink sink) {
    }
}
