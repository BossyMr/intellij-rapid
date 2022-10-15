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
import com.bossymr.rapid.language.psi.RapidRoutine;
import com.bossymr.rapid.language.psi.RapidRoutine.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidRoutineImpl;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidRoutineStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidRoutineElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class RapidRoutineElementType extends RapidStubElementType<RapidRoutineStub, RapidRoutine> {

    public RapidRoutineElementType() {
        super("ROUTINE");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidRoutineElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidRoutineImpl(node);
    }

    @Override
    public RapidRoutine createPsi(@NotNull RapidRoutineStub stub) {
        return new RapidRoutineImpl(stub);
    }

    @Override
    public @NotNull RapidRoutineStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        IElementType elementType = LightTreeUtil.requiredChildOfType(tree, node, Attribute.TOKEN_SET).getTokenType();
        Attribute attribute = Attribute.getAttribute(elementType);
        Objects.requireNonNull(attribute);
        String name = getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        boolean isLocal = hasChild(tree, node, RapidTokenTypes.LOCAL_KEYWORD);
        return new RapidRoutineStubImpl(parentStub, attribute, name, type, isLocal);
    }

    @Override
    public void serialize(@NotNull RapidRoutineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
        dataStream.writeBoolean(stub.isLocal());
    }

    @Override
    public @NotNull RapidRoutineStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Attribute attribute = Attribute.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        boolean isLocal = dataStream.readBoolean();
        return new RapidRoutineStubImpl(parentStub, attribute, name, type, isLocal);
    }

    @Override
    public void indexStub(@NotNull RapidRoutineStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
