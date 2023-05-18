package com.bossymr.rapid.ide.execution.debugger.breakpoints;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidLineBreakpointType extends AbstractRapidLineBreakpointType<RapidLineBreakpointProperties> {

    public RapidLineBreakpointType() {
        super("RapidLineBreakpointType", RapidBundle.message("debug.breakpoint.type.name"));
    }

    @Override
    public @Nullable RapidLineBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new RapidLineBreakpointProperties();
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile virtualFile, int line, @NotNull Project project) {
        return canPutAtElement(virtualFile, line, project, element -> element instanceof RapidStatement);
    }
}
