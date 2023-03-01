package com.bossymr.rapid.robot.network.robotware.rapid.task.program;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.*;
import com.bossymr.rapid.robot.network.LoadProgramMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Entity("rap-program")
public interface Program extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("entrypoint")
    @Nullable String getEntryPoint();

    @POST(value = "{@self}", arguments = "action=save")
    @NotNull NetworkCall<Void> save(
            @NotNull @Field("path") String path
    );

    @POST(value = "{@self}", arguments = "action=loadprog")
    @NotNull NetworkCall<Void> load(
            @NotNull @Field("progpath") String path,
            @NotNull @Field("loadmode") LoadProgramMode mode
    );

    @POST(value = "{@self}/breakpoint", arguments = "action=set")
    @NotNull NetworkCall<Void> setBreakpoint(
            @NotNull @Field("module") String module,
            @Field("row") int row,
            @Field("column") int column
    );

    @GET("{@self}/breakpoint")
    @NotNull NetworkCall<List<Breakpoint>> getBreakpoints();

}
