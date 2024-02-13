package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.symbol.ParameterType;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameter;
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

public class RapidParameterElementType extends RapidStubElementType<RapidParameterStub, PhysicalParameter> {

    public RapidParameterElementType() {
        super("PARAMETER");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalParameter(node);
    }

    @Override
    public PhysicalParameter createPsi(@NotNull RapidParameterStub stub) {
        return new PhysicalParameter(stub);
    }

    @Override
    public @NotNull RapidParameterStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        ParameterType attribute = ParameterType.getAttribute(tree, node);
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = StubUtil.getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        int dimensions = StubUtil.getLength(tree, node);
        return new RapidParameterStub(parentStub, attribute, name, type, dimensions);
    }

    @Override
    public void serialize(@NotNull RapidParameterStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
        dataStream.writeVarInt(stub.getDimensions());
    }

    @Override
    public @NotNull RapidParameterStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        ParameterType attribute = ParameterType.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        int dimensions = dataStream.readVarInt();
        return new RapidParameterStub(parentStub, attribute, name, type, dimensions);
    }

    @Override
    public void indexStub(@NotNull RapidParameterStub stub, @NotNull IndexSink sink) {}
}
