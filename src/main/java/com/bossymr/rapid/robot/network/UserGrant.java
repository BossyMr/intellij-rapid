package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;

/**
 * A {@code UserGrant} represents a grant. The name of the grant is retrieved using {@link #getTitle()}.
 *
 * @see UserService#getGrants()
 */
@Entity("user-grant")
public interface UserGrant extends EntityModel {}
