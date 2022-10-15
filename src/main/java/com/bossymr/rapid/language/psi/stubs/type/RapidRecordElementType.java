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
import com.bossymr.rapid.language.psi.RapidRecord;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidRecordImpl;
import com.bossymr.rapid.language.psi.stubs.RapidRecordStub;
import com.bossymr.rapid.language.psi.stubs.impl.RapidRecordStubImpl;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolNameIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidRecordElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidRecordElementType extends RapidStubElementType<RapidRecordStub, RapidRecord> {

    public RapidRecordElementType() {
        super("RECORD");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidRecordElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new RapidRecordImpl(node);
    }

    @Override
    public RapidRecord createPsi(@NotNull RapidRecordStub stub) {
        return new RapidRecordImpl(stub);
    }

    @Override
    public @NotNull RapidRecordStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        LighterASTNode identifier = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.IDENTIFIER);
        String name = identifier != null ? LightTreeUtil.toFilteredString(tree, identifier, null) : null;
        boolean isLocal = LightTreeUtil.firstChildOfType(tree, node, RapidTokenTypes.LOCAL_KEYWORD) != null;
        return new RapidRecordStubImpl(parentStub, name, isLocal);
    }

    @Override
    public void serialize(@NotNull RapidRecordStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeBoolean(stub.isLocal());
    }

    @Override
    public @NotNull RapidRecordStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        boolean isLocal = dataStream.readBoolean();
        return new RapidRecordStubImpl(parentStub, name, isLocal);
    }

    @Override
    public void indexStub(@NotNull RapidRecordStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolNameIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
