package com.bossymr.rapid.ide.execution.debugger.breakpoints;

import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import org.jetbrains.annotations.NotNull;

public abstract class RapidBreakpointProperties<T extends RapidBreakpointProperties<T>> extends XBreakpointProperties<T> {

    @Override
    public void loadState(@NotNull T state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
