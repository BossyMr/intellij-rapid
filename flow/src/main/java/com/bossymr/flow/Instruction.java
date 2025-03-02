package com.bossymr.flow;

import com.bossymr.flow.constraint.Constraint;
import com.bossymr.flow.expression.BinaryExpression;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.expression.LiteralExpression;
import com.bossymr.flow.expression.UnaryExpression;
import com.bossymr.flow.state.FlowEngine;
import com.bossymr.flow.state.FlowMethod;
import com.bossymr.flow.state.FlowSnapshot;
import com.bossymr.flow.state.Variable;
import com.bossymr.flow.type.EmptyType;
import com.bossymr.flow.type.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@code Instruction} represents an instruction.
 */
public sealed interface Instruction {

    /**
     * Calls this instruction with the current memory state.
     *
     * @param engine the engine used to compute the data flow.
     * @param method the current method.
     * @param predecessor the current memory state.
     * @return a list of possible memory states after this instruction.
     */
    List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor);

    sealed interface LinearInstruction extends Instruction {
        @Override
        default List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            snapshot.push(getExpression(snapshot));
            FlowSnapshot successor = snapshot.successorState(method);
            return List.of(successor);
        }

        Expression getExpression(FlowSnapshot predecessor);
    }

    /**
     * A label represents a point in the program.
     */
    final class Label implements Instruction {
        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            FlowSnapshot successor = snapshot.successorState(method);
            return List.of(successor);
        }

        @Override
        public String toString() {
            return "label";
        }
    }

    /**
     * Pushes the specified byte to the stack.
     * <p>
     * {@code -> [byte]}
     */
    final class ConstantByteInstruction implements LinearInstruction {

        private final byte value;

        public ConstantByteInstruction(byte value) {
            this.value = value;
        }

        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return LiteralExpression.integerLiteral(value);
        }

        public byte getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "pushByte " + value;
        }
    }

    /**
     * Pushes the specified integer to the stack.
     * <p>
     * {@code -> [integer]}
     */
    final class ConstantIntegerInstruction implements LinearInstruction {

        private final int value;

        public ConstantIntegerInstruction(int value) {
            this.value = value;
        }

        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return LiteralExpression.integerLiteral(value);
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "pushInteger " + value;
        }
    }

    /**
     * Pushes the specified long to the stack.
     * <p>
     * {@code -> [long]}
     */
    final class ConstantLongInstruction implements LinearInstruction {

        private final long value;

        public ConstantLongInstruction(long value) {
            this.value = value;
        }

        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return LiteralExpression.integerLiteral(value);
        }

        public long getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "pushLong " + value;
        }
    }

    /**
     * Pushes the specified string to the stack.
     * <p>
     * {@code -> [string]}
     */
    final class ConstantStringInstruction implements LinearInstruction {

        private final String value;

        public ConstantStringInstruction(String value) {
            this.value = value;
        }

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            snapshot.push(LiteralExpression.stringLiteral(value));
            FlowSnapshot successor = snapshot.successorState(method);
            return List.of(successor);

        }

        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return null;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "pushString \"" + value + "\"";
        }
    }

    /**
     * Pushes the specified boolean to the stack.
     * <p>
     * {@code -> [boolean]}
     */
    final class ConstantBooleanInstruction implements LinearInstruction {

        private final boolean value;

        public ConstantBooleanInstruction(boolean value) {
            this.value = value;
        }

        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return LiteralExpression.booleanLiteral(value);
        }

        public boolean getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "pushBoolean " + value;
        }
    }

    /**
     * Duplicates the value at the top of the stack.
     * <p>
     * {@code [any] -> [any] [any]}
     */
    final class DuplicateInstruction implements Instruction {

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            snapshot.push(snapshot.getStack().getLast());
            FlowSnapshot successor = snapshot.successorState(method);
            return List.of(successor);
        }

        @Override
        public String toString() {
            return "duplicate";
        }
    }

    /**
     * Pops two values off the stack, adds the values and pushes the result to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     * <p>
     * {@code [real] [real] -> [real]}
     * <p>
     * {@code [string] [string] -> [string]}
     */
    final class AddInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.ADD, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "add";
        }
    }

    /**
     * Pops two values off the stack, subtracts the values and pushes the result to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     * <p>
     * {@code [real] [real] -> [real]}
     */
    final class SubtractInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            Expression right = predecessor.pop();
            Expression left = predecessor.pop();
            return new BinaryExpression(BinaryExpression.Operator.SUBTRACT, left, right);
        }

        @Override
        public String toString() {
            return "subtract";
        }
    }

    /**
     * Pops two values off the stack, multiplies the values and pushes the result to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     * <p>
     * {@code [real] [real] -> [real]}
     */
    final class MultiplyInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.MULTIPLY, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "multiply";
        }
    }

    /**
     * Pops two values off the stack, divides the values and pushes the result to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     * <p>
     * {@code [real] [real] -> [real]}
     */
    final class DivideInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            Expression right = predecessor.pop();
            Expression left = predecessor.pop();
            return new BinaryExpression(BinaryExpression.Operator.DIVIDE, left, right);
        }

        @Override
        public String toString() {
            return "divide";
        }
    }

    /**
     * Pops two values off the stack, performs modulo on the values and pushes the result to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     */
    final class ModuloInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.MODULO, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "modulo";
        }
    }

    /**
     * Pops a value off the stack, converts it into a real value and pushes it to the stack.
     * <p>
     * {@code [integer] -> [real]}
     */
    final class IntegerToRealInstruction implements LinearInstruction {
        @Override public Expression getExpression(FlowSnapshot predecessor) {
            return new UnaryExpression(UnaryExpression.Operator.INTEGER_TO_REAL, predecessor.pop());
        }

        @Override
        public String toString() {
            return "integerToReal";
        }
    }

    /**
     * Pops a value off the stack, converts it into an integer value and pushes it to the stack.
     * <p>
     * {@code [real] -> [integer]}
     */
    final class RealToIntegerInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new UnaryExpression(UnaryExpression.Operator.REAL_TO_INTEGER, predecessor.pop());
        }

        @Override
        public String toString() {
            return "realToInteger";
        }
    }

    /**
     * Pops two values off the stack, checks the values for equality and pushes it to the stack.
     * <p>
     * {@code [any] [any] -> [boolean]}
     */
    final class EqualToInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "equalTo";
        }
    }

    /**
     * Pops two values off the stack, checks if the first value is smaller than the second and pushes it to the stack.
     * <p>
     * {@code [integer] [integer] -> [integer]}
     * <p>
     * {@code [real] [real] -> [real]}
     */
    final class LessThanInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            Expression right = predecessor.pop();
            Expression left = predecessor.pop();
            return new BinaryExpression(BinaryExpression.Operator.LESS_THAN, left, right);
        }

        @Override
        public String toString() {
            return "lessThan";
        }
    }

    /**
     * Pops two values off the stack, checks if the first value is larger than the second and pushes it to the stack.
     * <p>
     * {@code [integer] [integer] -> [boolean]}
     * <p>
     * {@code [real] [real] -> [boolean]}
     */
    final class GreaterThanInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            Expression right = predecessor.pop();
            Expression left = predecessor.pop();
            return new BinaryExpression(BinaryExpression.Operator.GREATER_THAN, left, right);
        }

        @Override
        public String toString() {
            return "greaterThan";
        }
    }

    /**
     * Pops two values off the stack, performs and the values and pushes it to the stack.
     * <p>
     * {@code [boolean] [boolean] -> [boolean]}
     */
    final class AndInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.AND, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "and";
        }
    }

    /**
     * Pops two values off the stack, performs xor the values and pushes it to the stack.
     * <p>
     * {@code [boolean] [boolean] -> [boolean]}
     */
    final class XorInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.XOR, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "xor";
        }
    }

    /**
     * Pops two values off the stack, performs or on the values and pushes it to the stack.
     * <p>
     * {@code [boolean] [boolean] -> [boolean]}
     */
    final class OrInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new BinaryExpression(BinaryExpression.Operator.OR, predecessor.pop(), predecessor.pop());
        }

        @Override
        public String toString() {
            return "or";
        }
    }

    /**
     * Pops a value off the stack, performs not on the value and pushes it to the stack.
     * <p>
     * {@code [boolean] -> [boolean]}
     */
    final class NotInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new UnaryExpression(UnaryExpression.Operator.NOT, predecessor.pop());
        }

        @Override
        public String toString() {
            return "not";
        }
    }

    /**
     * Pops a value off the stack, negates the value and pushes it to the stack.
     * <p>
     * {@code [integer] -> [integer]}
     * <p>
     * {@code [real] -> [real]}
     */
    final class NegateInstruction implements LinearInstruction {
        @Override
        public Expression getExpression(FlowSnapshot predecessor) {
            return new UnaryExpression(UnaryExpression.Operator.NEGATE, predecessor.pop());
        }

        @Override
        public String toString() {
            return "negate";
        }
    }

    /**
     * Pops a value off the stack, jumps to the specified instruction if the value is {@code true}.
     * <p>
     * {@code [boolean] -> }
     */
    final class ConditionalJumpInstruction implements Instruction {

        private final Label instruction;

        public ConditionalJumpInstruction(Label instruction) {
            this.instruction = instruction;
        }

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            Expression condition = snapshot.pop();
            Constraint constraint = engine.getConstraintEngine().getConstraint(snapshot, condition);
            List<FlowSnapshot> successors = new ArrayList<>();
            if (constraint == Constraint.ANY_VALUE || constraint == Constraint.UNKNOWN || constraint == Constraint.ALWAYS_TRUE) {
                FlowSnapshot successor = snapshot.successorState(instruction);
                successor.require(condition);
                successors.add(successor);
            }
            if (constraint == Constraint.ANY_VALUE || constraint == Constraint.UNKNOWN || constraint == Constraint.ALWAYS_FALSE) {
                FlowSnapshot successor = snapshot.successorState(method);
                successor.require(new UnaryExpression(UnaryExpression.Operator.NOT, condition));
                successors.add(successor);
            }
            return List.copyOf(successors);
        }

        public Label getInstruction() {
            return instruction;
        }

        @Override
        public String toString() {
            return "conditionalJump";
        }
    }

    /**
     * Jumps to the instruction at the index specified by the next integer.
     */
    final class JumpInstruction implements Instruction {

        private final Label instruction;

        public JumpInstruction(Label instruction) {
            this.instruction = instruction;
        }

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            // This might seem wasteful, but we currently create a new snapshot for each instruction. In order to be
            // able to find a snapshot for each instruction, in the future, this should be reworked.
            FlowSnapshot snapshot = predecessor.successorState(this);
            FlowSnapshot successor = snapshot.successorState(instruction);
            return List.of(successor);
        }

        public Label getInstruction() {
            return instruction;
        }

        @Override
        public String toString() {
            return "jump";
        }
    }

    /**
     * Depending on return type of the current method, pops a value off the stack and returns it from this method. If
     * the current method does not return a value, this method does not modify the stack.
     * <p>
     * {@code [return type] -> }
     */
    final class ReturnInstruction implements Instruction {
        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            method.getExitPoints().add(snapshot);
            return List.of();
        }

        @Override
        public String toString() {
            return "return";
        }
    }

    /**
     * Assign the variable at the top of the stack to the specified variable.
     */
    final class AssignInstruction implements Instruction {

        private final Variable variable;

        public AssignInstruction(Variable variable) {
            this.variable = variable;
        }

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            FlowSnapshot snapshot = predecessor.successorState(this);
            Expression value = snapshot.pop();
            predecessor.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, variable, value));
            FlowSnapshot successor = predecessor.successorState(method);
            return List.of(successor);
        }

        public Variable getVariable() {
            return variable;
        }

        @Override
        public String toString() {
            return "assign";
        }
    }

    /**
     * Calls a method specified by the current method at the index specified by the next integer. Depending on the
     * method, all arguments are popped off the stack in the order they are defined. The return value of the method is
     * pushed onto the stack.
     * <p>
     * {@code [argument 1] [argument 2] [...] -> [return type]}
     */
    final class CallInstruction implements Instruction {

        private final Method method;

        public CallInstruction(Method method) {
            this.method = method;
        }

        @Override
        public List<FlowSnapshot> call(FlowEngine engine, FlowMethod method, FlowSnapshot predecessor) {
            if (this.method.getReturnType() instanceof EmptyType) {
                // This method is as far as we are considered 'pure'. We don't keep track of modifications to external
                // fields, so unless the memory returns a variable, it can't modify the program's state.
                FlowSnapshot snapshot = predecessor.successorState(this);
                FlowSnapshot successor = snapshot.successorState(method);
                return List.of(successor);
            }
            List<FlowSnapshot> successors = new ArrayList<>();
            FlowMethod target = engine.getMethod(this.method);
            // Iterate over all possible memory states at the end of the provided method.
            for (FlowSnapshot exitPoint : target.getExitPoints()) {
                // Create a disconnected copy of the memory state.
                // The copy won't be seen by calling: exitPoint.getSuccessors()
                FlowSnapshot copy = exitPoint.disconnectedState();
                // Add all constraints at the call site to the memory state.
                copy.getConstraints().addAll(predecessor.getConstraints());
                List<ValueType> arguments = this.method.getArguments();
                // For all arguments, pop the argument from the stack (in reverse order since the first argument is
                // popped last). Set the passed value to be equal to the argument in the method.
                Map<Expression, Expression> table = new HashMap<>();
                for (int i = 1; i < arguments.size() + 1; i++) {
                    Expression expression = target.getParameter(arguments.size() - i);
                    Expression value = predecessor.getStack().get(predecessor.getStack().size() - i);
                    table.put(expression, value);
                    copy.require(new BinaryExpression(BinaryExpression.Operator.EQUAL_TO, expression, value));
                }
                // All constraints from the call site have been added to the disconnected state, as such, we can check
                // if it is possible for this memory state to be returned given the arguments we pass to the method.
                if (copy.isReachable()) {
                    FlowSnapshot snapshot = predecessor.successorState(this);
                    // We need to replace the argument placeholder with the actual argument value.
                    Expression returnValue = copy.pop().translate(child -> {
                        if (table.containsKey(child)) {
                            return table.get(child);
                        }
                        return child;
                    });
                    snapshot.push(returnValue);
                    FlowSnapshot successor = snapshot.successorState(method);
                    successors.add(successor);
                }
            }
            return successors;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public String toString() {
            return "call " + method.getName();
        }
    }
}
