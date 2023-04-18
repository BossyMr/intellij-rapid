package com.bossymr.rapid.robot.network;


import com.bossymr.network.NetworkQuery;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Service;
import org.jetbrains.annotations.NotNull;

@Service("/ctrl")
public interface ControllerService {

        @Fetch("/identity")
  @NotNull NetworkQuery<Identity> getIdentity();

}
