package com.bossymr.rapid.robot.network.robotware.rapid.execution;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Field;
import com.bossymr.rapid.robot.api.annotations.Service;
import com.bossymr.rapid.robot.api.annotations.Subscribable;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import com.bossymr.rapid.robot.network.Grant;
import com.bossymr.rapid.robot.network.HoldToRunMode;
import com.bossymr.rapid.robot.network.HoldToRunState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code Service} used to execute a task.
 */
@Service("/rw/rapid/execution")
public interface ExecutionService {

    /**
     * Returns the current execution state.
     *
     * @return the current execution state.
     * @see #onExecutionState()
     */
    @Fetch("")
    @NotNull NetworkQuery<ExecutionStatus> getState();

    /**
     * Start program execution. To start program execution the robot must in automatic mode. Additionally, mastership
     * must be acquired for the {@code RAPID} domain, and the {@link Grant#EXECUTE_RAPID EXECUTE_RAPID} grant is
     * required.
     *
     * @param regainMode the regain mode.
     * @param executionMode the execution mode.
     * @param executionCycle the number of cycles.
     * @param conditionState whether to check the program.
     * @param breakpointMode whether to stop at a breakpoint.
     * @param taskMode the task execution mode.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=start")
    @NotNull NetworkQuery<Void> start(@NotNull @Field("regain") RegainMode regainMode,
                                      @NotNull @Field("execmode") ExecutionMode executionMode,
                                      @NotNull @Field("cycle") ExecutionCycle executionCycle,
                                      @NotNull @Field("condition") ConditionState conditionState,
                                      @NotNull @Field("stopatbp") BreakpointMode breakpointMode,
                                      @NotNull @Field("alltaskbytsp") TaskExecutionMode taskMode);

    /**
     * Stops program execution.
     *
     * @param stopMode the stop mode.
     * @param taskMode the task execution mode.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=stop")
    default NetworkQuery<Void> stop(@NotNull @Field("stopmode") StopMode stopMode,
                                    @NotNull @Field("usetsp") TaskExecutionMode taskMode) {
        return stop(stopMode, switch (taskMode) {
            case ALL -> "alltsk";
            case NORMAL -> "normal";
        });
    }

    @ApiStatus.Internal
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=stop")
    @NotNull NetworkQuery<Void> stop(@NotNull @Field("stopmode") StopMode stopMode,
                                     @NotNull @Field("usetsp") String taskMode);

    /**
     * Starts execution from the production entry.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=startprodentry")
    @NotNull NetworkQuery<Void> startProduction();

    /**
     * Resets the program pointer to the main routine.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=resetpp")
    @NotNull NetworkQuery<Void> resetProgramPointer();

    /**
     * Sets the number of cycles to use.
     *
     * @param executionCycle the number of cycles.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=setcycle")
    @NotNull NetworkQuery<Void> setCycles(@NotNull @Field("cycle") ExecutionCycle executionCycle);

    /**
     * Subscribe to updates to the program execution state.
     */
    @Subscribable("/rw/rapid/execution;ctrlexecstate")
    @NotNull SubscribableNetworkQuery<ExecutionStatusEvent> onExecutionState();

    /**
     * Subscribe to updates to the execution cycles.
     */
    @Subscribable("/rw/rapid/execution;rapidexeccycle")
    @NotNull SubscribableNetworkQuery<ExecutionCycleEvent> onExecutionCycle();

    /**
     * Subscribe to updates to the {@link HoldToRunState}.
     */
    @Subscribable("/rw/rapid/execution;hdtrun")
    @NotNull SubscribableNetworkQuery<HoldToRunEvent> onHoldToRun();

    /**
     * Sets the hold to run state.
     * <p>
     * To start execution of a program set the state to {@link HoldToRunMode#PRESS PRESS}.
     * <p>
     * To continue execution the state must be set to {@link HoldToRunMode#HELD HELD} once every two seconds.
     * <p>
     * To halt execution set the state to {@link HoldToRunMode#RELEASE RELEASE}.
     * <p>
     * The program must be executed with motors on and in manual with reduced or full speed. This action is only
     * supported on a {@code Virtual Controller (RC)}, and this user must be logged in as a local user.
     *
     * @param mode the new state.
     */
    @Fetch(method = FetchMethod.POST, value = "", arguments = "action=holdtorun-state")
    @NotNull NetworkQuery<Void> setHoldToRun(@NotNull @Field("state") HoldToRunMode mode);
}
