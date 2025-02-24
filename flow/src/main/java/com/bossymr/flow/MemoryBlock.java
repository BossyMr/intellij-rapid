package com.bossymr.flow;

import com.bossymr.flow.instruction.AssignmentInstruction;
import com.bossymr.flow.instruction.ExitInstruction;
import com.bossymr.flow.instruction.Instruction;
import com.bossymr.flow.state.MemorySnapshot;

import java.util.*;

/**
 * A {@code MemoryBlock} represents the data flow of a method.
 */
public class MemoryBlock {

    private final Method method;

    private final List<MemorySnapshot> entryPoints = new ArrayList<>();
    private final List<MemorySnapshot> exitPoints = new ArrayList<>();

    private MemoryBlock(Method method) {
        this.method = method;
    }

    public static MemoryBlock compute(Method method) {
        MemoryBlock memoryBlock = new MemoryBlock(method);
        Deque<MemorySnapshot> queue = new ArrayDeque<>();
        if (method.getInstructions().isEmpty()) {
            throw new IllegalArgumentException("method '" + method + "' is empty");
        }
        MemorySnapshot initialState = MemorySnapshot.emptyState();
        queue.push(initialState);
        queue.push(initialState.successorState(method.getInstructions().getFirst()));
        while (!queue.isEmpty()) {
            MemorySnapshot snapshot = queue.pop();
            switch (snapshot.getInstruction()) {
                case AssignmentInstruction assignmentInstruction -> {
                    snapshot.assign(assignmentInstruction.getVariable(), assignmentInstruction.getExpression());
                };
                case ExitInstruction exitInstruction -> memoryBlock.getExitPoints().add(snapshot);
                default -> throw new IllegalStateException("unexpected element: " + instruction);
            }
        }
        return memoryBlock;
    }

    /**
     * Returns a list of program states at the start of this method.
     *
     * @return a list of program states.
     */
    public List<MemorySnapshot> getEntryPoints() {
        return entryPoints;
    }

    /**
     * Returns a list of program states at the end of this method.
     *
     * @return a list of program states.
     */
    public List<MemorySnapshot> getExitPoints() {
        return exitPoints;
    }

    /**
     * Returns a list of possible program states before the specified instruction.
     *
     * @param instruction the instruction.
     * @return a list of possible program states.
     */
    public List<MemorySnapshot> beforeElement(Instruction instruction) {
        Deque<MemorySnapshot> queue = new ArrayDeque<>(this.entryPoints);
        List<MemorySnapshot> states = new ArrayList<>();
        while (!queue.isEmpty()) {
            // Search all snapshots for snapshots belonging to the specified instruction.
            // If an instruction belongs to the specified instruction, add its predecessor to the list.
            // We need to search until we reach the end of the method, since an instruction might be encountered
            // than once.
            MemorySnapshot snapshot = queue.pop();
            if (snapshot.getInstruction().equals(instruction)) {
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
    public List<MemorySnapshot> afterElement(Instruction instruction) {
        Deque<MemorySnapshot> queue = new ArrayDeque<>(this.entryPoints);
        List<MemorySnapshot> states = new ArrayList<>();
        while (!queue.isEmpty()) {
            // Search all snapshots for snapshots where it's predecessor belongs to the specified instruction, but not
            // the instruction itself.
            MemorySnapshot snapshot = queue.pop();
            if (!snapshot.getInstruction().equals(instruction)) {
                if (snapshot.getPredecessor().getInstruction().equals(instruction)) {
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
    public List<MemorySnapshot> returnSnapshot(MemorySnapshot caller) {
        return null;
    }

}
