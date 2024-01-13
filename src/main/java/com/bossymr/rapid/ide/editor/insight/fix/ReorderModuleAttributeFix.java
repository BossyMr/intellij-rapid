package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ReorderModuleAttributeFix extends PsiUpdateModCommandAction<RapidAttributeList> {

    public ReorderModuleAttributeFix(@NotNull RapidAttributeList attributeList) {
        super(attributeList);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.reorder.module.attributes");
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull RapidAttributeList element, @NotNull ModPsiUpdater updater) {
        List<ModuleType> attributes = element.getAttributes();
        RapidElementFactory factory = RapidElementFactory.getInstance(context.project());
        element.replace(factory.createAttributeList(attributes));
    }
}
