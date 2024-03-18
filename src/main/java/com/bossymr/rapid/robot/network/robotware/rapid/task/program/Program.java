package com.bossymr.rapid.robot.network.robotware.rapid.task.program;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Field;
import com.bossymr.rapid.robot.api.annotations.Property;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import com.bossymr.rapid.robot.network.LoadProgramMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Entity("rap-program")
public interface Program {

    @Property("name")
    @NotNull String getName();

    @Property("entrypoint")
    @Nullable String getEntryPoint();

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=save")
    @NotNull NetworkQuery<Void> save(@NotNull @Field("path") String path);

    @Fetch(method = FetchMethod.POST, value = "{@self}", arguments = "action=loadprog")
    @NotNull NetworkQuery<Void> load(@NotNull @Field("progpath") String path,
                                     @NotNull @Field("loadmode") LoadProgramMode mode);

    @Fetch(method = FetchMethod.POST, value = "{@self}/breakpoint", arguments = "action=set")
    @NotNull NetworkQuery<Void> setBreakpoint(@NotNull @Field("module") String module,
                                              @Field("row") int row,
                                              @Field("column") int column);

    @Fetch("{@self}/breakpoint")
    @NotNull NetworkQuery<List<Breakpoint>> getBreakpoints();

    @Fetch("{@self}/builderror")
    @NotNull NetworkQuery<List<BuildLogError>> getBuildErrors();

}
