package com.bossymr.flow.state;

import com.bossymr.flow.Method;
import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.Instruction;
import com.bossymr.flow.type.ValueType;

import java.util.*;

/**
 * A {@code MemoryBlock} represents the data flow of a method.
 */
public class FlowMethod {

    private final Method method;
    private final List<Expression> arguments;

    private final FlowSnapshot entryPoint;
    private final List<FlowSnapshot> exitPoints = new ArrayList<>();

    private FlowMethod(Method method, List<Expression> arguments, FlowSnapshot entryPoint) {
        this.method = method;
        this.arguments = arguments;
        this.entryPoint = entryPoint;
    }

    public static FlowMethod compute(FlowEngine engine, Method method) {
        Deque<FlowSnapshot> queue = new ArrayDeque<>();
        // We need an empty initial state without any instruction. This is because when calling #beforeInstruction(...)
        // on the first instruction in the method, it's predecessor is returned - which is the empty state.
        // Furthermore, the first snapshot contains the method's parameters.
        FlowSnapshot initialState = FlowSnapshot.emptyState(engine);
        List<Expression> arguments = new ArrayList<>();
        int argumentIndex = 0;
        for (ValueType argumentType : method.getArguments()) {
            Variable snapshot = new Variable("Argument #" + argumentIndex, argumentType);
            arguments.add(snapshot);
            initialState.push(snapshot);
            argumentIndex += 1;
        }
        FlowMethod flowMethod = new FlowMethod(method, arguments, initialState);
        queue.addFirst(initialState.successorState(method.getInstructions().getFirst()));
        while (!queue.isEmpty()) {
            FlowSnapshot snapshot = queue.pop();
            Instruction instruction = snapshot.getInstruction();
            List<FlowSnapshot> successors = instruction.call(engine, flowMethod, snapshot);
            for (FlowSnapshot successor : successors.reversed()) {
                queue.addFirst(successor);
            }
        }
        return flowMethod;
    }

    /**
     * Returns the method backing this memory block.
     *
     * @return the method backing this memory block.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns a list of expressions representing each parameter in the order they are declared.
     *
     * @return a list of expressions.
     */
    public List<Expression> getArguments() {
        return arguments;
    }


    /**
     * Returns the expression representing the parameter at the provided index.
     *
     * @param index the index of the parameter.
     * @return the expression.
     */
    public Expression getParameter(int index) {
        return arguments.get(index);
    }

    /**
     * Returns the entry point of this method.
     *
     * @return the entry point of this method.
     */
    public FlowSnapshot getEntryPoint() {
        return entryPoint;
    }

    /**
     * Returns a list of exit points for this method. If this method returns a value, the return value is at the top of
     * the stack.
     *
     * @return a list of program states.
     */
    public List<FlowSnapshot> getExitPoints() {
        return exitPoints;
    }

    /**
     * Returns a list of possible program states before the specified instruction.
     *
     * @param instruction the instruction.
     * @return a list of possible program states.
     */
    public List<FlowSnapshot> beforeElement(Instruction instruction) {
        Deque<FlowSnapshot> queue = new ArrayDeque<>();
        queue.add(entryPoint);
        List<FlowSnapshot> states = new ArrayList<>();
        while (!queue.isEmpty()) {
            // Search all snapshots for snapshots belonging to the specified instruction.
            // If an instruction belongs to the specified instruction, add its predecessor to the list.
            // We need to search until we reach the end of the method, since an instruction might be encountered
            // than once.
            FlowSnapshot snapshot = queue.pop();
            if (instruction.equals(snapshot.getInstruction())) {
                states.add(snapshot.getPredecessor());
            }
            queue.addAll(snapshot.getSuccessors());
        }
        return states;
    }

    /**
     * Returns a list of possible program states after the specified instruction.
     *
     * @param instruction the instruction.
     * @return a list of possible program states.
     */
    public List<FlowSnapshot> afterElement(Instruction instruction) {
        Deque<FlowSnapshot> queue = new ArrayDeque<>();
        queue.add(entryPoint);
        List<FlowSnapshot> states = new ArrayList<>();
        while (!queue.isEmpty()) {
            // Search all snapshots for snapshots where it's predecessor belongs to the specified instruction, but not
            // the instruction itself.
            FlowSnapshot snapshot = queue.pop();
            if (!instruction.equals(snapshot.getInstruction())) {
                FlowSnapshot predecessor = snapshot.getPredecessor();
                if (predecessor != null && instruction.equals(predecessor.getInstruction())) {
                    states.add(snapshot);
                }
            }
            queue.addAll(snapshot.getSuccessors());
        }
        return states;
    }

    /**
     * Returns a list of possible program states that can be returned by this method if called by the specified state.
     * The state should define all arguments to this method without any snapshots.
     *
     * @param caller the state of the program at the call site.
     * @return a list of possible program states.
     */
    public List<FlowSnapshot> returnSnapshot(FlowSnapshot caller) {
        return null;
    }
}
