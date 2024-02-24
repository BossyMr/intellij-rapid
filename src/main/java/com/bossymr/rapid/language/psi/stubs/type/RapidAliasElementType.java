package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidAliasIndex;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolIndex;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalAlias;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidAliasElementType extends RapidStubElementType<RapidAliasStub, PhysicalAlias> {

    public RapidAliasElementType() {
        super("ALIAS");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalAlias(node);
    }

    @Override
    public PhysicalAlias createPsi(@NotNull RapidAliasStub stub) {
        return new PhysicalAlias(stub);
    }

    @Override
    public @NotNull RapidAliasStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        Visibility visibility = Visibility.getVisibility(tree, node);
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = StubUtil.getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        return new RapidAliasStub(parentStub, visibility, name, type);
    }

    @Override
    public void serialize(@NotNull RapidAliasStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getVisibility().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
    }

    @Override
    public @NotNull RapidAliasStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Visibility visibility = Visibility.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        return new RapidAliasStub(parentStub, visibility, name, type);
    }

    @Override
    public void indexStub(@NotNull RapidAliasStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null && stub.getVisibility() == Visibility.GLOBAL) {
            sink.occurrence(RapidSymbolIndex.KEY, StringUtil.toLowerCase(name));
            sink.occurrence(RapidAliasIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
