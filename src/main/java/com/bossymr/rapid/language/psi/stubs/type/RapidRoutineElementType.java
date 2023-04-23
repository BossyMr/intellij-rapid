package com.bossymr.rapid.language.psi.stubs.type;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidStubElementType;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import com.bossymr.rapid.language.psi.stubs.StubUtil;
import com.bossymr.rapid.language.psi.stubs.index.RapidRoutineIndex;
import com.bossymr.rapid.language.psi.stubs.index.RapidSymbolIndex;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
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

public class RapidRoutineElementType extends RapidStubElementType<RapidRoutineStub, PhysicalRoutine> {

    public RapidRoutineElementType() {
        super("ROUTINE");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new PhysicalRoutine(node);
    }

    @Override
    public PhysicalRoutine createPsi(@NotNull RapidRoutineStub stub) {
        return new PhysicalRoutine(stub);
    }

    @Override
    public @NotNull RapidRoutineStub createStub(@NotNull LighterAST tree, @NotNull LighterASTNode node, @NotNull StubElement<?> parentStub) {
        Visibility visibility = Visibility.getVisibility(tree, node);
        RoutineType attribute = RoutineType.getAttribute(tree, node);
        String name = StubUtil.getText(tree, node, RapidTokenTypes.IDENTIFIER);
        String type = StubUtil.getText(tree, node, RapidElementTypes.TYPE_ELEMENT);
        return new RapidRoutineStub(parentStub, visibility, attribute, name, type);
    }

    @Override
    public void serialize(@NotNull RapidRoutineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeName(stub.getVisibility().name());
        dataStream.writeName(stub.getAttribute().name());
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getType());
    }

    @Override
    public @NotNull RapidRoutineStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        Visibility visibility = Visibility.valueOf(dataStream.readNameString());
        RoutineType attribute = RoutineType.valueOf(dataStream.readNameString());
        String name = dataStream.readNameString();
        String type = dataStream.readNameString();
        return new RapidRoutineStub(parentStub, visibility, attribute, name, type);
    }

    @Override
    public void indexStub(@NotNull RapidRoutineStub stub, @NotNull IndexSink sink) {
        final String name = stub.getName();
        if (name != null) {
            sink.occurrence(RapidSymbolIndex.KEY, StringUtil.toLowerCase(name));
            sink.occurrence(RapidRoutineIndex.KEY, StringUtil.toLowerCase(name));
        }
    }
}
