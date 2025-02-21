package com.bossymr.flow;

import com.bossymr.flow.instruction.Instruction;
import com.bossymr.flow.value.Variable;
import com.bossymr.flow.type.ValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@code FlowMethod} represents a method and its control flow. A method isn't bound to any module or scope, and as
 * such, can be referenced by any other method.
 */
public class Method {

    private final String name;
    private final ValueType returnType;
    private final List<Instruction> instructions = new ArrayList<>();
    private final List<Variable> variables = new ArrayList<>();
    private final List<Variable> parameters = new ArrayList<>();

    public Method(String name, ValueType returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    /**
     * Returns the name of this method.
     *
     * @return the name of this method.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the return type of this method.
     *
     * @return the return type of this method.
     */
    public ValueType getReturnType() {
        return returnType;
    }

    /**
     * Returns all parameters declared in this method.
     *
     * @return all parameters declared in this method.
     */
    public List<Variable> getParameters() {
        return parameters;
    }

    /**
     * Returns all variables declared in this method.
     *
     * @return all variables declared in this method.
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Returns all instructions in this method.
     *
     * @return all instructions in this method.
     */
    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return Objects.equals(name, method.name) && Objects.equals(returnType, method.returnType) && Objects.equals(instructions, method.instructions) && Objects.equals(variables, method.variables) && Objects.equals(parameters, method.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType, instructions, variables, parameters);
    }

    @Override
    public String toString() {
        return "Method{" +
               "name='" + name + '\'' +
               ", returnType=" + returnType +
               ", parameters=" + parameters +
               ", variables=" + variables +
               ", instructions=" + instructions +
               '}';
    }
}
