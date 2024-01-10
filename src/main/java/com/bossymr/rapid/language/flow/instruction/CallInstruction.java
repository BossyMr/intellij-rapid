package com.bossymr.rapid.language.flow.instruction;

import com.bossymr.rapid.language.builder.ArgumentDescriptor;
import com.bossymr.rapid.language.flow.Block;
import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.expression.Expression;
import com.bossymr.rapid.language.flow.expression.ReferenceExpression;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CallInstruction extends Instruction {

    private final @NotNull Expression routineName;
    private final @Nullable ReferenceExpression returnValue;
    private final @NotNull Map<ArgumentDescriptor, ReferenceExpression> arguments;

    public CallInstruction(@NotNull Block block, @Nullable PsiElement element, @NotNull Expression routineName, @Nullable ReferenceExpression returnValue, @NotNull Map<ArgumentDescriptor, ReferenceExpression> arguments) {
        super(block, element);
        if (!(RapidPrimitiveType.STRING.isAssignable(routineName.getType()))) {
            throw new IllegalArgumentException("Invalid reference type: " + routineName.getType());
        }
        this.routineName = routineName;
        this.returnValue = returnValue;
        this.arguments = arguments;
    }

    public @NotNull Instruction getSuccessor() {
        return getSuccessors().get(0);
    }

    public @NotNull Expression getRoutineName() {
        return routineName;
    }

    public @Nullable ReferenceExpression getReturnValue() {
        return returnValue;
    }

    public @NotNull Map<ArgumentDescriptor, ReferenceExpression> getArguments() {
        return arguments;
    }

    @Override
    public <R> R accept(@NotNull ControlFlowVisitor<R> visitor) {
        return visitor.visitCallInstruction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CallInstruction that = (CallInstruction) o;
        return Objects.equals(routineName, that.routineName) && Objects.equals(returnValue, that.returnValue) && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), routineName, returnValue, arguments);
    }

    @Override
    public String toString() {
        return (returnValue != null ? returnValue + " := ": "") + routineName + arguments.entrySet().stream()
                .map(argument -> {
                    if(argument.getKey() instanceof ArgumentDescriptor.Required required) {
                        return required.index() + " := " + argument.getValue();
                    }
                    if(argument.getKey() instanceof ArgumentDescriptor.Optional optional) {
                        return optional.name() + (argument.getValue() != null ? " := " + argument.getValue() : "");
                    }
                    if(argument.getKey() instanceof ArgumentDescriptor.Conditional conditional) {
                        return conditional.name() + "?" + argument.getValue();
                    }
                    throw new AssertionError();
                }).collect(Collectors.joining(", ", "(", ")"));
    }
}
