package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Title;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code UserGrant} represents a grant. The name of the grant is retrieved using {@link #getTitle()}.
 *
 * @see UserService#getGrants()
 */
@Entity("user-grant")
public interface UserGrant {

    @Title
    @NotNull String getTitle();

}
