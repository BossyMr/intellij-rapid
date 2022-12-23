package com.bossymr.rapid.robot.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this query will send a request with the method {@code GET}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GET {

    /**
     * Specifies the path of the request.
     *
     * @return the path of the request.
     */
    @NotNull String value();
}