package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

@Service("/rw/rapid/execution")
public interface ExecutionService {

    @GET("")
    @NotNull NetworkCall<ExecutionStatus> getStatus();

    @POST("?action=start")
    @NotNull NetworkCall<Void> start(
            @NotNull @Field("regain") RegainState regainState,
            @NotNull @Field("execmode") ExecutionMode executionMode,
            @NotNull @Field("cycle") ExecutionCycle executionCycle,
            @NotNull @Field("condition") ConditionState conditionState,
            @NotNull @Field("stopatbp") BreakpointState breakpointState,
            @NotNull @Field("alltaskbytsp") TSPMode tspMode
    );

    @POST("?action=stop")
    @NotNull NetworkCall<Void> stop(
            @NotNull @Field("stopmode") StopMode stopMode,
            @NotNull @Field("usetsp") UseTSPMode useTSPMode
    );

    @POST("?action=startprodentry")
    @NotNull NetworkCall<Void> startProduction();

    @POST("?action=resetpp")
    @NotNull NetworkCall<Void> resetPointer();

    @POST("?action=setcycle")
    @NotNull NetworkCall<Void> setCycles(
            @NotNull @Field("cycle") ExecutionCycle executionCycle
    );

    @Subscribable("/rw/rapid/execution;ctrlexecstate")
    @NotNull SubscribableNetworkCall<ExecutionStateEvent> onExecutionState();


    @Subscribable("/rw/rapid/execution;rapidexeccycle")
    @NotNull SubscribableNetworkCall<ExecutionCycleEvent> onExecutionCycle();

    @Subscribable("/rw/rapid/execution;hdtrun")
    @NotNull SubscribableNetworkCall<HoldToRunEvent> onHoldToRun();

    /**
     * Sets the hold to run state. To start execution of a program, the state must be set to
     * {@link HoldToRunMode#PRESS PRESS}. To continue execution, the state must be polled once every two seconds, by
     * setting the state to {@link HoldToRunMode#HELD HELD}. To halt execution, the state must be set to
     * {@link HoldToRunMode#RELEASE RELEASE}. The program must be executed with motors on, and in manual (reduced speed)
     * or manual (full speed).
     * <p>
     * This action is only supported on a {@code Virtual Controller (RC)}, and this user must be logged in as a local
     * user.
     *
     * @param mode the new state.
     */
    @POST("?action=holdtorun-state")
    @NotNull NetworkCall<Void> setHoldToRun(
            @NotNull @Field("state") HoldToRunMode mode
    );
}
