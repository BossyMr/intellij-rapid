package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidComponentStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidComponentIndex;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolIndex;
import com.bossymr.rapid.language.psi.stubs.node.RapidComponentElement;
import com.bossymr.rapid.language.symbol.physical.PhysicalComponent;
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

public class RapidComponentElementType extends RapidStubElementType<RapidComponentStub, PhysicalComponent> {

    public RapidComponentElementType() {
        super("COMPONENT");
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
        return new RapidComponentElement();
    }

    @Override
    public @NotNull PsiElement createPsi(@NotNull ASTNode node) {
        return new PhysicalComponent(node);
    }

    @Override
    public PhysicalComponent createPsi(@NotNull RapidComponentStub stub) {
        return new PhysicalComponent(stub);
    }

    @Override
    public @NotNull RapidComponentStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = StubUtil.getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        return new RapidComponentStub(parentStub, name, type);
    }

    @Override
    public void serialize(@NotNull RapidComponentStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
    }

    @Override
    public @NotNull RapidComponentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        return new RapidComponentStub(parentStub, name, type);
    }

    @Override
    public void indexStub(@NotNull RapidComponentStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolIndex.KEY, StringUtil.toLowerCase(name));
            sink.occurrence(RapidComponentIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
