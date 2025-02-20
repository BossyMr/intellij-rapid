package com.bossymr.flow.state;

import com.bossymr.flow.value.Variable;

public class VariableSnapshot extends Variable {

    public VariableSnapshot(Variable variable) {
        super(variable.getType());
    }
}
