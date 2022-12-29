package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidRecordStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidRecordIndex;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolIndex;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
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

public class RapidRecordElementType extends RapidStubElementType<RapidRecordStub, PhysicalRecord> {

    public RapidRecordElementType() {
        super("RECORD");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalRecord(node);
    }

    @Override
    public PhysicalRecord createPsi(@NotNull RapidRecordStub stub) {
        return new PhysicalRecord(stub);
    }

    @Override
    public @NotNull RapidRecordStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        Visibility visibility = Visibility.getVisibility(tree, node);
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        return new RapidRecordStub(parentStub, visibility, name);
    }

    @Override
    public void serialize(@NotNull RapidRecordStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getVisibility().name());
        dataStream.writeName(stub.getName());
    }

    @Override
    public @NotNull RapidRecordStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Visibility visibility = Visibility.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        return new RapidRecordStub(parentStub, visibility, name);
    }

    @Override
    public void indexStub(@NotNull RapidRecordStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolIndex.KEY, StringUtil.toLowerCase(name));
            sink.occurrence(RapidRecordIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
