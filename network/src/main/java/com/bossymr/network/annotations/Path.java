package com.bossymr.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this value is concatenated into the request path.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Path {

    /**
     * Specifies the name of the field.
     *
     * @return the name of the field.
     */
    @NotNull String value();
}
