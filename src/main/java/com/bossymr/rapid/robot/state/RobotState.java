package com.bossymr.rapid.robot.state;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag("robot")
public class RobotState {

    @Attribute("name")
    public String name;

    @Attribute("path")
    public String path;

    public Map<String, List<SymbolState>> symbols = new HashMap<>();

}
