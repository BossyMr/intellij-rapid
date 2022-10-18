package com.bossymr.rapid.robot.state;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.ArrayList;
import java.util.List;

@Tag("robot")
public class RobotState {

    @Attribute("name")
    public String name;

    @Attribute("path")
    public String path;

    public List<SymbolState> symbols = new ArrayList<>();

}
