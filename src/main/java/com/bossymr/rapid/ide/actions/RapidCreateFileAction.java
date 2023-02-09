package com.bossymr.rapid.ide.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.language.lexer.RapidLexer;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiEditorUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RapidCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {

    public RapidCreateFileAction() {
        super(RapidBundle.message("action.create.new.file.text"), RapidBundle.message("action.create.new.file.description"), RapidIcons.RAPID);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(RapidBundle.message("action.create.new.file.text"))
                .addKind(RapidBundle.message("action.create.new.file.module"), RapidIcons.MODULE, "Rapid Module")
                .setValidator(new InputValidatorEx() {
                    @Override
                    public @Nullable String getErrorText(@NonNls String inputString) {
                        if (!(RapidLexer.isIdentifier(inputString))) {
                            return RapidBundle.message("action.create.new.file.validator");
                        }
                        return null;
                    }

                    @Override
                    public boolean canClose(@Nullable String inputString) {
                        return inputString != null && getErrorText(inputString) == null;
                    }
                });
    }

    @Override
    protected @NotNull String getActionName(@Nullable PsiDirectory directory, @NonNls @NotNull String newName, @NonNls String templateName) {
        return RapidBundle.message("action.create.new.file.text");
    }

    @Override
    protected void postProcess(@NotNull PsiFile createdElement, @Nullable String templateName, @Nullable Map<String, String> customProperties) {
        super.postProcess(createdElement, templateName, customProperties);
        PsiElement element = getNavigationElement(createdElement);
        if (element != null) {
            Editor editor = PsiEditorUtil.findEditor(createdElement);
            if (editor != null) {
                editor.getCaretModel().moveToOffset(element.getTextOffset() + element.getTextLength());
            }
        }
    }

    private @Nullable PsiElement getNavigationElement(@NotNull PsiFile createdElement) {
        if (!(createdElement instanceof RapidFile file)) return null;
        List<PhysicalModule> modules = file.getModules();
        if (modules.size() != 1) return null;
        PhysicalModule module = modules.get(0);
        RapidAttributeList attributeList = module.getAttributeList();
        if (attributeList.getLastChild() != null) {
            return attributeList.getLastChild();
        } else {
            return module.getNameIdentifier();
        }
    }
}
