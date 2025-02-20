package com.bossymr.flow;

import com.bossymr.flow.instruction.Instruction;
import com.bossymr.flow.value.Variable;

import java.util.List;

/**
 * A {@code FlowMethod} represents a method and its control flow. A method isn't bound to any module or scope, and as
 * such, can be referenced by any other method.
 */
public class Method {

    /**
     * Returns all parameters declared in this method.
     *
     * @return all parameters declared in this method.
     */
    public List<Variable> getParameters() {
        return null;
    }

    /**
     * Returns all variables declared in this method.
     *
     * @return all variables declared in this method.
     */
    public List<Variable> getVariables() {
        return null;
    }

    /**
     * Returns all instructions in this method.
     *
     * @return all instructions in this method.
     */
    public List<Instruction> getInstructions() {
        return null;
    }
}
