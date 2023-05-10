package com.bossymr.network.annotations;

import com.bossymr.network.client.FetchMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * A method annotated with {@code Fetch} will send attempt to send a request and convert the response into the return
 * type of the annotated method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fetch {

    /**
     * Specifies the request method of the request, by default the request will send a {@link FetchMethod#GET GET}
     * request.
     *
     * @return the request method of the request.
     */
    @NotNull FetchMethod method() default FetchMethod.GET;

    /**
     * Specifies the path of the request. If the path is relative, it will be resolved against the path to the robot.
     * <p>
     * It is possible to interpolate the path with the value of parameters annotated with {@link Path}. For example, the
     * string {@code {argument}}, will be replaced the value of a parameter where {@link Path#value()} is equal to
     * {@code argument}.
     * <p>
     * If the string starts with @, it will be replaced by the value of a reference with the specified name.
     * Alternatively, if the string starts with #, it will be replaced by a property with the specified name.
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
