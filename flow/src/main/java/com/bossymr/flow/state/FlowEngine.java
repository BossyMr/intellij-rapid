package com.bossymr.flow.state;

import com.bossymr.flow.Method;
import com.bossymr.flow.constraint.ConstraintEngine;

public class FlowEngine {

    private final ConstraintEngine constraintEngine;

    public FlowEngine() {
        this.constraintEngine = new ConstraintEngine();
    }

    public FlowMethod getMethod(Method method) {
        return FlowMethod.compute(this, method);
    }

    public ConstraintEngine getConstraintEngine() {
        return constraintEngine;
    }
}
