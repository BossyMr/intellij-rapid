package com.bossymr.flow.state;

import com.bossymr.flow.value.Value;
import com.bossymr.flow.type.ValueType;

public final class VariableSnapshot implements Value {

    private final ValueType type;

    /**
     * Create a new {@code VariableSnapshot}.
     *
     * @param type the type of the variable.
     */
    public VariableSnapshot(ValueType type) {
        this.type = type;
    }

    /**
     * Returns the type of this snapshot.
     *
     * @return the type of this snapshot.
     */
    public ValueType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return "{" + hashCode() + "}";
    }
}
