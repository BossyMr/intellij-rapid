package com.bossymr.rapid.robot.api.annotations;

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
    String[] value();
}
