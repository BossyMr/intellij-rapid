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
import com.intellij.psi.tree.IElementType;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidField;
import com.bossymr.rapid.language.psi.RapidField.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidFieldImpl;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidFieldStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidFieldElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class RapidFieldElementType extends RapidStubElementType<RapidFieldStub, RapidField> {

    public RapidFieldElementType() {
        super("FIELD");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidFieldElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidFieldImpl(node);
    }

    @Override
    public RapidField createPsi(@NotNull RapidFieldStub stub) {
        return new RapidFieldImpl(stub);
    }

    @Override
    public @NotNull RapidFieldStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        IElementType elementType = LightTreeUtil.requiredChildOfType(tree, node, Attribute.TOKEN_SET).getTokenType();
        Attribute attribute = Objects.requireNonNull(Attribute.getAttribute(elementType));
        String name = getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        String initializer = getText(tree, node, RapidElementTypes.EXPRESSIONS);
        boolean isLocal = hasChild(tree, node, RapidTokenTypes.LOCAL_KEYWORD);
        boolean isTask = hasChild(tree, node, RapidTokenTypes.TASK_KEYWORD);
        return new RapidFieldStubImpl(parentStub, attribute, name, type, initializer, isLocal, isTask);
    }

    @Override
    public void serialize(@NotNull RapidFieldStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
        dataStream.writeName(stub.getInitializer());
        dataStream.writeBoolean(stub.isLocal());
        dataStream.writeBoolean(stub.isTask());
    }

    @Override
    public @NotNull RapidFieldStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Attribute attribute = Attribute.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        String initializer = dataStream.readNameString();
        boolean isLocal = dataStream.readBoolean();
        boolean isTask = dataStream.readBoolean();
        return new RapidFieldStubImpl(parentStub, attribute, name, type, initializer, isLocal, isTask);
    }

    @Override
    public void indexStub(@NotNull RapidFieldStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
