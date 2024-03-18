package com.bossymr.rapid.robot.network;


import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Service;
import org.jetbrains.annotations.NotNull;

@Service("/ctrl")
public interface ControllerService {

        @Fetch("/identity")
  @NotNull NetworkQuery<Identity> getIdentity();

}
