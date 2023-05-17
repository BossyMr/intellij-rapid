package com.bossymr.rapid;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * {@code RapidIcons} contains all icons used by this plugin.
 *
 * @implNote Preferably, this class should be an enum, however, it must be a class in order for icons to be used in
 * {@code plugin.xml}.
 */
public final class RapidIcons {

    public static final Icon ROBOT_TOOL_WINDOW = AllIcons.Toolwindows.ToolWindowServices;
    public static final Icon ROBOT = AllIcons.Nodes.Project;
    public static final Icon DIRECTORY = AllIcons.Nodes.Package;
    public static final Icon RAPID = AllIcons.FileTypes.Java;
    public static final Icon TASK = AllIcons.Nodes.Package;
    public static final Icon MODULE = AllIcons.Nodes.Class;
    public static final Icon SYSTEM_MODULE = AllIcons.Nodes.AbstractClass;
    public static final Icon ALIAS = AllIcons.Nodes.Alias;
    public static final Icon ATOMIC = AllIcons.Nodes.Alias;
    public static final Icon COMPONENT = AllIcons.Nodes.Variable;
    public static final Icon VARIABLE = AllIcons.Nodes.Variable;
    public static final Icon CONSTANT = AllIcons.Nodes.Constant;
    public static final Icon PERSISTENT = AllIcons.Nodes.Field;
    public static final Icon PARAMETER = AllIcons.Nodes.Parameter;
    public static final Icon RECORD = AllIcons.Nodes.Record;
    public static final Icon FUNCTION = AllIcons.Nodes.Method;
    public static final Icon PROCEDURE = AllIcons.Nodes.Method;
    public static final Icon TRAP = AllIcons.Nodes.Method;
    public static final Icon LABEL = AllIcons.Nodes.Alias;

    private RapidIcons() {
        throw new AssertionError();
    }
}
