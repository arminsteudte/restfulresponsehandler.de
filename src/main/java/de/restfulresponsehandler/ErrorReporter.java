package de.restfulresponsehandler;

import javax.ws.rs.core.Response;

/**
 * Created by Armin on 19.08.2015.
 */
public interface ErrorReporter {

    public void reportSerializationError(Response.Status status, Class<?> expectedEntityClass, String serviceResponse);

}