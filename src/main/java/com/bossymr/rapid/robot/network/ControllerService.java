package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.GET;
import org.jetbrains.annotations.NotNull;

@Service("/ctrl")
public interface ControllerService {

    @GET("/identity")
    @NotNull Query<Identity> getIdentity();

}
