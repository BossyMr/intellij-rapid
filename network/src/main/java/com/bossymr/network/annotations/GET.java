package com.bossymr.network.annotations;

import java.lang.annotation.*;

/**
 * Indicates that this method will send a {@code GET} request.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {

    /**
     * Specifies the path to send the request to.
     *
     * @return the path to send the request to.
     */
    String value() default "";

    /**
     * Specifies the arguments to send with this request.
     *
     * @return the arguments to send with this request.
     */
    String[] arguments() default {};

}
