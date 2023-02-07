package com.bossymr.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;


/**
 * Indicates that this method should provide a response value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Property {

    /**
     * Specifies the name of the field to provide by this method.
     *
     * @return the name of the field to provide by this method.
     */
    @NotNull String value();
}
