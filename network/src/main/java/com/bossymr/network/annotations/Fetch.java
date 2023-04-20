package com.bossymr.network.annotations;

import com.bossymr.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * A method annotated with {@code Fetch} will send a request and return the response. This annotation must only be used
 * on methods in an interface. The instance of the interface must be managed by a {@link NetworkManager}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fetch {

    /**
     * Specifies the method of the request.
     *
     * @return the method of the request.
     */
    @NotNull FetchMethod method() default FetchMethod.GET;

    /**
     * Specifies the path of the request.
     *
     * @return the path of the request.
     */
    @NotNull String value();

    /**
     * Specifies the query arguments of the request.
     *
     * @return the query arguments of the request.
     */
    @NotNull String[] arguments() default {};
}
