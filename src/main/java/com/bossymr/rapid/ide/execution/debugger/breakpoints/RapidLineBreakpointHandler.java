package com.bossymr.rapid.ide.execution.debugger.breakpoints;

import com.bossymr.rapid.ide.execution.debugger.RapidDebugProcess;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

public class RapidLineBreakpointHandler extends RapidBreakpointHandler<RapidLineBreakpointType, XLineBreakpoint<RapidLineBreakpointProperties>> {

    private final @NotNull RapidDebugProcess debugProcess;

    public RapidLineBreakpointHandler(@NotNull RapidDebugProcess debugProcess) {
        super(RapidLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<RapidLineBreakpointProperties> breakpoint) {
        debugProcess.registerBreakpoint(breakpoint);
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<RapidLineBreakpointProperties> breakpoint, boolean temporary) {
        debugProcess.unregisterBreakpoint(breakpoint);
    }
}
