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
import com.bossymr.rapid.language.psi.RapidAlias;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidAliasImpl;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidAliasStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidAliasElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidAliasElementType extends RapidStubElementType<RapidAliasStub, RapidAlias> {

    public RapidAliasElementType() {
        super("ALIAS");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidAliasElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidAliasImpl(node);
    }

    @Override
    public RapidAlias createPsi(@NotNull RapidAliasStub stub) {
        return new RapidAliasImpl(stub);
    }

    @Override
    public @NotNull RapidAliasStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        LighterASTNode identifier = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.IDENTIFIER);
        String name = identifier != null ? LightTreeUtil.toFilteredString(tree, identifier, null) : null;
        boolean isLocal = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.LOCAL_KEYWORD) != null;
        LighterASTNode typeElement = LightTreeUtil.firstChildOfType(tree, node, RapidElementTypes.TYPE_ELEMENT);
        String type = typeElement != null ? LightTreeUtil.toFilteredString(tree, typeElement, null) : null;
        return new RapidAliasStubImpl(parentStub, name, type, isLocal);
    }

    @Override
    public void serialize(@NotNull RapidAliasStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
        dataStream.writeBoolean(stub.isLocal());
    }

    @Override
    public @NotNull RapidAliasStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        boolean isLocal = dataStream.readBoolean();
        return new RapidAliasStubImpl(parentStub, name, type, isLocal);
    }

    @Override
    public void indexStub(@NotNull RapidAliasStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
