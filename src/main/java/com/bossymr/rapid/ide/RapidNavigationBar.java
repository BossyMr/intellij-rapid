package com.bossymr.rapid.ide;

import com.bossymr.rapid.language.RapidLanguage;
import com.bossymr.rapid.language.psi.FormatUtil;
import com.bossymr.rapid.language.psi.FormatUtil.Option;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class RapidNavigationBar extends StructureAwareNavBarModelExtension {
    @Override
    public @Nullable String getPresentableText(Object object) {
        return getPresentableText(object, false);
    }

    @Override
    public @Nullable @Nls String getPresentableText(Object object, boolean forPopup) {
        if (object instanceof RapidSymbol symbol) {
            if (forPopup && symbol instanceof RapidRoutine routine) {
                return FormatUtil.format(routine,
                        EnumSet.of(Option.SHOW_NAME, Option.SHOW_TYPE, Option.SHOW_TYPE_AFTER),
                        EnumSet.of(Option.SHOW_TYPE));
            }
            return symbol.getName();
        }
        if (object instanceof PsiDirectory directory) {
            return directory.getName();
        }
        return null;
    }

    @Override
    public @Nullable PsiElement adjustElement(@NotNull PsiElement element) {
        if (element instanceof PsiDirectory directory) {
            VirtualFile virtualFile = directory.getVirtualFile();
            RemoteRobotService remoteService = RemoteRobotService.getInstance();
            Robot robot = remoteService.getRobot();
            if (robot != null) {
                for (RapidTask task : robot.getTasks()) {
                    if (task.getDirectory().equals(virtualFile)) {
                        return null;
                    }
                }
            }
        }
        PsiFile containingFile = element.getContainingFile();
        if (containingFile != null) {
            if (element instanceof RapidFile file) {
                List<PhysicalModule> modules = file.getModules();
                if (modules.size() == 1) {
                    // If this element is a file, which contains a single module; show the module instead.
                    return modules.get(0);
                }
            }
        }
        return super.adjustElement(element);
    }

    @Override
    protected boolean acceptParentFromModel(@Nullable PsiElement element) {
        if (element instanceof RapidFile file) {
            return file.getModules().size() > 1;
        }
        return true;
    }

    @NotNull
    @Override
    protected Language getLanguage() {
        return RapidLanguage.INSTANCE;
    }
}