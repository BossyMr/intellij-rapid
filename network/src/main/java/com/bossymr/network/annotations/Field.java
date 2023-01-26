package com.bossymr.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this value is included in the request body.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Field {

    /**
     * Specifies the name of the field.
     *
     * @return the name of the field.
     */
    @NotNull String value();
}
