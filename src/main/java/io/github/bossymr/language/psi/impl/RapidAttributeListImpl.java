package io.github.bossymr.language.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.BitUtil;
import io.github.bossymr.language.psi.*;
import io.github.bossymr.language.psi.stubs.RapidAttributeListStub;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static io.github.bossymr.language.psi.ModuleAttribute.*;
import static io.github.bossymr.language.psi.RapidTokenTypes.*;

public class RapidAttributeListImpl extends RapidStubPsiElement<RapidAttributeListStub> implements RapidAttributeList {

    private TokenSet ATTRIBUTES = TokenSet.create(SYSMODULE_KEYWORD, NOVIEW_KEYWORD, NOSTEPIN_KEYWORD, VIEWONLY_KEYWORD, READONLY_KEYWORD);
    private Map<IElementType, ModuleAttribute> TYPE_TO_ATTRIBUTE = Map.of(SYSMODULE_KEYWORD, SYSTEM_MODULE, NOVIEW_KEYWORD, NO_VIEW, NOSTEPIN_KEYWORD, NO_STEP_IN, VIEWONLY_KEYWORD, VIEW_ONLY, READONLY_KEYWORD, READ_ONLY);
    private Map<ModuleAttribute, IElementType> ATTRIBUTE_TO_TYPE = Map.of(SYSTEM_MODULE, SYSMODULE_KEYWORD, NO_VIEW, NOVIEW_KEYWORD, NO_STEP_IN, NOSTEPIN_KEYWORD, VIEW_ONLY, VIEWONLY_KEYWORD, READ_ONLY, READONLY_KEYWORD);

    private volatile Set<ModuleAttribute> attributes;

    public RapidAttributeListImpl(@NotNull RapidAttributeListStub stub) {
        super(stub, RapidStubElementTypes.ATTRIBUTE_LIST);
    }

    public RapidAttributeListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        dropCaches();
        super.subtreeChanged();
    }

    public void dropCaches() {
        attributes = null;
    }

    @Override
    protected Object clone() {
        RapidAttributeListImpl attributeList = (RapidAttributeListImpl) super.clone();
        attributeList.dropCaches();
        return attributeList;
    }

    @Override
    public @NotNull Set<ModuleAttribute> getAttributes() {
        if(attributes != null) return attributes;
        Set<ModuleAttribute> attributeSet = EnumSet.noneOf(ModuleAttribute.class);
        for (ASTNode child : getNode().getChildren(ATTRIBUTES)) {
            ModuleAttribute attribute = TYPE_TO_ATTRIBUTE.get(child.getElementType());
            if (attribute != null) {
                attributeSet.add(attribute);
            }
        }
        return attributes = attributeSet;
    }

    @Override
    public boolean hasAttribute(ModuleAttribute attribute) {
        RapidAttributeListStub stub = getGreenStub();
        if (stub != null) {
            return BitUtil.isSet(stub.getMask(), RapidAttributeListStub.Mask.getMask(attribute));
        } else {
            if(attributes != null) return attributes.contains(attribute);
            return findChildByType(ATTRIBUTE_TO_TYPE.get(attribute)) != null;
        }
    }

    @Override
    public void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException {

    }
}
