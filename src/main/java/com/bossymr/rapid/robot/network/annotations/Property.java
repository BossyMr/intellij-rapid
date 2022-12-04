package com.bossymr.rapid.robot.network.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that this method should provide a response value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {

    /**
     * Specifies the name of the field to provide by this method.
     *
     * @return the name of the field to provide by this method.
     */
    @NotNull String value();
}
