package com.bossymr.rapid.robot.api.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this endpoint is subscribable.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribable {

    /**
     * Specifies the resource.
     *
     * @return the resource.
     */
    @NotNull String value();
}