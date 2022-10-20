package com.bossymr.rapid.robot.state;

import com.bossymr.rapid.robot.network.controller.rapid.SymbolType;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.Objects;

@Tag("symbol")
public class SymbolState {

    @Attribute("name")
    public String name;

    @Attribute("path")
    public String path;

    @Attribute("type")
    public SymbolType type;

    @Attribute("dataType")
    public String dataType;

    @Attribute("mode")
    public String mode;

    @Attribute("required")
    public Boolean isRequired;

    @Attribute("length")
    public Integer length;

    @Attribute("index")
    public Integer index;

    @Attribute("dimension")
    public Integer dimension;

    @Attribute("size")
    public Integer size;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolState state = (SymbolState) o;
        return Objects.equals(name, state.name) && Objects.equals(path, state.path) && type == state.type && Objects.equals(dataType, state.dataType) && Objects.equals(mode, state.mode) && Objects.equals(isRequired, state.isRequired) && Objects.equals(length, state.length) && Objects.equals(index, state.index) && Objects.equals(dimension, state.dimension) && Objects.equals(size, state.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, type, dataType, mode, isRequired, length, index, dimension, size);
    }

    @Override
    public String toString() {
        return "SymbolState{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                ", dataType='" + dataType + '\'' +
                ", mode='" + mode + '\'' +
                ", required=" + isRequired +
                ", length=" + length +
                ", index=" + index +
                ", dimension=" + dimension +
                ", size=" + size +
                '}';
    }
}
