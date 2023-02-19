package com.bossymr.rapid.robot.network;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@code Grant} is a permission.
 */
public enum Grant {

    /**
     * Grants access to start execution (stopping execution is always allowed), move the program pointer to main and to
     * execute service routines.
     */
    EXECUTE_RAPID("UAS_RAPID_EXECUTE");

    private static final @NotNull Map<String, Grant> TITLE_TO_GRANT = Arrays.stream(values())
            .collect(Collectors.toMap(Grant::getName, grant -> grant));
    private final @NotNull String name;

    Grant(@NotNull String name) {
        this.name = name;
    }

    public static @NotNull Set<Grant> getGrants(@NotNull Collection<UserGrant> userGrants) {
        Set<Grant> grants = new HashSet<>();
        for (UserGrant userGrant : userGrants) {
            String title = userGrant.getTitle();
            if (TITLE_TO_GRANT.containsKey(title)) {
                grants.add(TITLE_TO_GRANT.get(title));
            }
        }
        return grants;
    }

    public @NotNull String getName() {
        return name;
    }
}
