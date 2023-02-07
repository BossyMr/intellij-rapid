package com.bossymr.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this value is included in the request path.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * Specifies the name of the field.
     *
     * @return the name of the field.
     */
    @NotNull String value();
}
