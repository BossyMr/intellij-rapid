package com.bossymr.network.annotations;

import java.lang.annotation.*;

/**
 * Indicates that this method will send a {@code POST} request.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {

    /**
     * Specifies the path to send the request to.
     *
     * @return the path to send the request to.
     */
    String value();

    /**
     * Specifies the arguments to send with this request.
     *
     * @return the arguments to send with this request.
     */
    String[] arguments() default {};

}
