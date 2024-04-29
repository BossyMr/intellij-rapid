package com.bossymr.network.client;

/**
 * The {@code RequestType} represents the different request methods.
 */
public enum RequestMethod {

    /**
     * Sends a request to retrieve a resource.
     */
    GET,

    /**
     * Sends a request to create a new resource.
     */
    POST,

    /**
     * Sends a request to mutate a resource.
     */
    PUT,

    /**
     * Sends a request to delete a resource.
     */
    DELETE

}
