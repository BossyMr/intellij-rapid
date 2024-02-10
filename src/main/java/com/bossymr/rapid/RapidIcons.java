package com.bossymr.rapid;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * {@code RapidIcons} contains all icons used by this plugin.
 */
public final class RapidIcons {

    public static final Icon DIRECTORY = AllIcons.Nodes.Package;
    public static final Icon RAPID =  IconLoader.getIcon("/icons/toolWindow.svg", RapidIcons.class);
    public static final Icon TASK = AllIcons.Nodes.Module;
    public static final Icon MODULE = AllIcons.Nodes.Method;
    public static final Icon SYSTEM_MODULE = AllIcons.Nodes.AbstractClass;
    public static final Icon ALIAS = AllIcons.Nodes.Alias;
    public static final Icon ATOMIC = AllIcons.Nodes.Type;
    public static final Icon COMPONENT = AllIcons.Nodes.Class;
    public static final Icon VARIABLE = AllIcons.Nodes.Variable;
    public static final Icon CONSTANT = AllIcons.Nodes.Constant;
    public static final Icon PERSISTENT = AllIcons.Nodes.Parameter;
    public static final Icon PARAMETER = AllIcons.Nodes.Parameter;
    public static final Icon RECORD = AllIcons.Nodes.Record;
    public static final Icon FUNCTION = AllIcons.Nodes.Function;
    public static final Icon PROCEDURE = AllIcons.Nodes.Property;
    public static final Icon TRAP = AllIcons.Nodes.Test;

    public static final Icon ROBOT = RAPID;
    public static final Icon ROBOT_TOOL_WINDOW = ROBOT;

    private RapidIcons() {
        throw new AssertionError();
    }
}
