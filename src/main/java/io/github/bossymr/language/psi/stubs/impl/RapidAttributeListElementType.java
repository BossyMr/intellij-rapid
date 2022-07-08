package io.github.bossymr.language.psi.stubs.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import io.github.bossymr.language.psi.ModuleAttribute;
import io.github.bossymr.language.psi.RapidAttributeList;
import io.github.bossymr.language.psi.RapidStubElementType;
import io.github.bossymr.language.psi.impl.RapidAttributeListImpl;
import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidAttributeListElementType extends RapidStubElementType<RapidAttributeListStub, RapidAttributeList> {

    public RapidAttributeListElementType() {
        super("ATTRIBUTE_LIST");
    }

    @Override
    public @NotNull PsiElement createElement(@NotNull ASTNode node) {
        return new RapidAttributeListImpl(node);
    }

    @Override
    public RapidAttributeList createPsi(@NotNull RapidAttributeListStub stub) {
        return new RapidAttributeListImpl(stub);
    }

    @Override
    public @NotNull RapidAttributeListStub createStub(@NotNull RapidAttributeList psi, StubElement<? extends PsiElement> parentStub) {
        int packed = 0;
        for (ModuleAttribute attribute : psi.getAttributes()) {
            packed |= RapidAttributeListStub.Mask.getMask(attribute);
        }
        return new RapidAttributeListStubImpl(parentStub, packed);
    }

    @Override
    public void serialize(@NotNull RapidAttributeListStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        dataStream.writeVarInt(stub.getMask());
    }

    @Override
    public @NotNull RapidAttributeListStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new RapidAttributeListStubImpl(parentStub, dataStream.readVarInt());
    }
}
