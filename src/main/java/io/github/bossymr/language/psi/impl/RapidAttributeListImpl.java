package io.github.bossymr.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.BitUtil;
import io.github.bossymr.language.psi.*;
import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static io.github.bossymr.language.psi.ModuleAttribute.*;
import static io.github.bossymr.language.psi.RapidTokenTypes.*;

public class RapidAttributeListImpl extends RapidStubElementImpl<RapidAttributeListStub> implements RapidAttributeList {

    private final Map<IElementType, ModuleAttribute> TYPE_TO_ATTRIBUTE = Map.of(SYSMODULE_KEYWORD, SYSTEM_MODULE,
            NOVIEW_KEYWORD, NO_VIEW, NOSTEPIN_KEYWORD, NO_STEP_IN, VIEWONLY_KEYWORD, VIEW_ONLY, READONLY_KEYWORD, READ_ONLY);
    private final Map<ModuleAttribute, IElementType> ATTRIBUTE_TO_TYPE = Map.of(SYSTEM_MODULE, SYSMODULE_KEYWORD,
            NO_VIEW, NOVIEW_KEYWORD, NO_STEP_IN, NOSTEPIN_KEYWORD, VIEW_ONLY, VIEWONLY_KEYWORD, READ_ONLY, READONLY_KEYWORD);
    private final Map<ModuleAttribute, String> ATTRIBUTE_TO_NAME = Map.of(SYSTEM_MODULE, "SYSMODULE", NO_VIEW,
            "NOVIEW", NO_STEP_IN, "NOSTEPIN", VIEW_ONLY, "VIEWONLY", READ_ONLY, "READONLY");

    public RapidAttributeListImpl(@NotNull RapidAttributeListStub stub) {
        super(stub, RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    public RapidAttributeListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Set<ModuleAttribute> getAttributes() {
        Set<ModuleAttribute> attributes = EnumSet.noneOf(ModuleAttribute.class);
        RapidAttributeListStub stub = getGreenStub();
        if(stub != null) {
            int masks = stub.getMask();
            for (ModuleAttribute attribute : ModuleAttribute.values()) {
                if(BitUtil.isSet(masks, RapidAttributeListStub.Mask.getMask(attribute))) {
                    attributes.add(attribute);
                }
            }
        } else {
            for (ASTNode child : getNode().getChildren(TokenSet.ANY)) {
                ModuleAttribute attribute = TYPE_TO_ATTRIBUTE.get(child.getElementType());
                if (attribute != null) {
                    attributes.add(attribute);
                }
            }
        }
        return attributes;
    }

    @Override
    public boolean hasAttribute(ModuleAttribute attribute) {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return BitUtil.isSet(stub.getMask(), RapidAttributeListStub.Mask.getMask(attribute));
        } else {
            return findChildByType(ATTRIBUTE_TO_TYPE.get(attribute)) != null;
        }
    }

    @Override
    public void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException {
        if (value) {
            LeafElement element = Factory.createSingleLeafElement(ATTRIBUTE_TO_TYPE.get(attribute), ATTRIBUTE_TO_NAME.get(attribute), null, getManager());
            addInternal(element, element, null, null);
        } else {
            PsiElement node = findChildByType(ATTRIBUTE_TO_TYPE.get(attribute));
            if (node != null) {
                node.delete();
            }
        }
    }

    @Override
    public String toString() {
        return "RapidModifierList:" + getText();
    }
}
