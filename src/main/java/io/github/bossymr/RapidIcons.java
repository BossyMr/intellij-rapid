package io.github.bossymr;

import com.intellij.icons.AllIcons;
import com.intellij.ui.LayeredIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * A collection of icons used by plugin components.
 * TODO: Create permanent icons, as all icons are currently temporary.
 */
public class RapidIcons {

    // Language

    public static final @NotNull Icon RAPID = AllIcons.FileTypes.Json;

    // File Type

    public static final @NotNull Icon RAPID_FILE = AllIcons.FileTypes.Json;

    // Mark

    public static final @NotNull Icon TASK_MARK = AllIcons.Nodes.StaticMark; // TODO: 2022-06-22 TASK_MARK should be unique.
    public static final @NotNull Icon CONSTANT_MARK = AllIcons.Nodes.FinalMark;
    public static final @NotNull Icon PERSISTENT_MARK = AllIcons.Nodes.StaticMark;

    public static final @NotNull Icon GLOBAL = AllIcons.Nodes.Public;
    public static final @NotNull Icon LOCAL = AllIcons.Nodes.Private;

    // Elements

    public static final @NotNull Icon ALIAS = AllIcons.Nodes.ObjectTypeAttribute;
    public static final @NotNull Icon RECORD = AllIcons.Nodes.Record;

    public static final @NotNull Icon FIELD = AllIcons.Nodes.Field;

    public static final @NotNull Icon VARIABLE = FIELD;
    public static final @NotNull Icon CONSTANT = new LayeredIcon(FIELD, CONSTANT_MARK);
    public static final @NotNull Icon PERSISTENT = new LayeredIcon(FIELD, PERSISTENT_MARK);

    public static final @NotNull Icon PROCEDURE = AllIcons.Nodes.Property;
    public static final @NotNull Icon Function = AllIcons.Nodes.Function;
    public static final @NotNull Icon TRAP = AllIcons.Nodes.Test;
}
