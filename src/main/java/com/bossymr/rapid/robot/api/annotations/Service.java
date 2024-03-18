package com.bossymr.rapid.robot.api.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Indicates that this interface is a service. This annotation should only be annotated on interfaces.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {

    /**
     * Specifies the path to this service.
     *
     * @return the path to this service.
     */
    @NotNull String value() default "";
}
