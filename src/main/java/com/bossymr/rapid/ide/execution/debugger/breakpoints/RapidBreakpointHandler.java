package com.bossymr.rapid.ide.execution.debugger.breakpoints;

import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import org.jetbrains.annotations.NotNull;

public abstract class RapidBreakpointHandler<E extends XBreakpointType<T, ?>, T extends XBreakpoint<?>> extends XBreakpointHandler<T> {

    protected RapidBreakpointHandler(@NotNull Class<E> breakpointTypeClass) {
        super(breakpointTypeClass);
    }
}
