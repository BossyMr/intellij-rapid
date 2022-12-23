package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Field;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("rap-program")
public interface Program extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("entrypoint")
    @Nullable String getEntryPoint();

    @POST("{@self}?action=save")
    @NotNull Query<Void> save(
            @NotNull @Field("path") String path
    );

    @POST("{@self}?action=loadprog")
    @NotNull Query<Void> load(
            @NotNull @Field("progpath") String path,
            @NotNull @Field("loadmode") LoadProgramMode mode
    );

}
