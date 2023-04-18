package com.bossymr.rapid.ide.debugger.breakpoints;

import com.bossymr.rapid.ide.debugger.RapidDebugProcess;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RapidLineBreakpointHandler extends RapidBreakpointHandler<RapidLineBreakpointType, XLineBreakpoint<RapidLineBreakpointProperties>> {

    private final @NotNull RapidDebugProcess debugProcess;

    public RapidLineBreakpointHandler(@NotNull RapidDebugProcess debugProcess) {
        super(RapidLineBreakpointType.class);
        this.debugProcess = debugProcess;
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<RapidLineBreakpointProperties> breakpoint) {
        try {
            debugProcess.registerBreakpoint(breakpoint);
        } catch (IOException | InterruptedException ignored) {}
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<RapidLineBreakpointProperties> breakpoint, boolean temporary) {
        try {
            debugProcess.unregisterBreakpoint(breakpoint);
        } catch (IOException | InterruptedException ignored) {}
    }
}
