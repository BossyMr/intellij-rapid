package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidFieldStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidFieldIndex;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolIndex;
import com.bossymr.rapid.language.symbol.FieldType;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidFieldElementType extends RapidStubElementType<RapidFieldStub, PhysicalField> {

    public RapidFieldElementType() {
        super("FIELD");
    }

    @Override
    public @NotNull PhysicalField createElement(@NotNull ASTNode node) {
        return new PhysicalField(node);
    }

    @Override
    public PhysicalField createPsi(@NotNull RapidFieldStub stub) {
        return new PhysicalField(stub);
    }

    @Override
    public @NotNull RapidFieldStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        FieldType fieldType = FieldType.getAttribute(tree, node);
        Visibility visibility = Visibility.getVisibility(tree, node);
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = StubUtil.getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        String initializer = StubUtil.getText(tree, node, RapidElementTypes.EXPRESSIONS);
        int dimensions = StubUtil.getLength(tree, node);
        return new RapidFieldStub(parentStub, visibility, fieldType, name, type, dimensions, initializer);
    }

    @Override
    public void serialize(@NotNull RapidFieldStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getVisibility().name());
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
        dataStream.writeVarInt(stub.getDimensions());
        dataStream.writeName(stub.getInitializer());
    }

    @Override
    public @NotNull RapidFieldStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Visibility visibility = Visibility.valueOf(dataStream.readNameString());
        FieldType fieldType = FieldType.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        int dimensions = dataStream.readVarInt();
        String initializer = dataStream.readNameString();
        return new RapidFieldStub(parentStub, visibility, fieldType, name, type, dimensions, initializer);
    }

    @Override
    public void indexStub(@NotNull com.bossymr.rapid.language.psi.stubs.RapidFieldStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null && stub.getVisibility() != Visibility.GLOBAL) {
            sink.occurrence(RapidSymbolIndex.KEY, StringUtil.toLowerCase(name));
            sink.occurrence(RapidFieldIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
