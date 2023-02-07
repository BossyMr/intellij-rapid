package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity("rap-program")
public interface Program extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("entrypoint")
    @Nullable String getEntryPoint();

    @POST("{@self}?action=save")
    @NotNull NetworkCall<Void> save(
            @NotNull @Field("path") String path
    );

    @POST("{@self}?action=loadprog")
    @NotNull NetworkCall<Void> load(
            @NotNull @Field("progpath") String path,
            @NotNull @Field("loadmode") LoadProgramMode mode
    );

}
