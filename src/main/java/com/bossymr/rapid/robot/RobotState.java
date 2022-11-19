package com.bossymr.rapid.robot;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Tag("robot")
public final class RobotState {

    @Attribute("name")
    public String name;

    @Attribute("path")
    public String path;

    public @NotNull Set<SymbolState> symbols = new HashSet<>();

    /*
     * All symbols are not provided by the robot, as some symbols have an empty "name" property.
     * As a result, if a robot is currently connected, all symbols are checked individually.
     * If a symbol actually exists, it is added as a symbol state, if it does not, it is cached
     * to avoid checking it again.
     */
    public @NotNull Set<String> cache = new HashSet<>();

    public static final class SymbolState implements Comparable<SymbolState> {

        @Attribute("title")
        public String title;

        @Attribute("name")
        public String name;

        @Attribute("type")
        public String type;

        @Attribute("local")
        public boolean isLocal;

        @Attribute("task")
        public boolean isTask;

        @Attribute("dataType")
        public String dataType;

        @Attribute("required")
        public boolean isRequired;

        @Attribute("length")
        public int length;

        @Attribute("index")
        public int index;

        @Attribute("mode")
        public String mode;

        @Override
        public int compareTo(@NotNull SymbolState o) {
            return title.compareTo(o.title);
        }
    }

}
