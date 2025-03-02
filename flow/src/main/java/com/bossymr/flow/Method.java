package com.bossymr.flow;

import com.bossymr.flow.type.ValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@code Method} represents a method. A method isn't bound to any module or scope, and as such, can be referenced by
 * any other method.
 */
public class Method {

    private final String name;
    private final ValueType returnType;
    private final List<Instruction> instructions;
    private final List<ValueType> arguments;

    public Method(String name, ValueType returnType, List<ValueType> arguments, Consumer<CodeBuilder> code) {
        this.name = name;
        this.returnType = returnType;
        this.arguments = List.copyOf(arguments);
        CodeBuilder codeBuilder = new CodeBuilder(this, new ArrayList<>(arguments));
        code.accept(codeBuilder);
        List<Instruction> block = codeBuilder.getInstructions();
        this.instructions = List.copyOf(block);
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
     * Returns all parameters in this method.
     *
     * @return all parameters in this method.
     */
    public List<ValueType> getArguments() {
        return arguments;
    }

    /**
     * Returns all instructions in this method.
     *
     * @return all instructions in this method.
     */
    public List<Instruction> getInstructions() {
        return instructions;
    }
}
