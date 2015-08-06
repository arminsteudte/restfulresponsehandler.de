package de.restfulresponsehandler;

import com.google.common.base.Preconditions;

import javax.ws.rs.core.Response;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMapping<R> implements IResponseHandling<R> {

    public R handleResponse(Response response) {

        Preconditions.checkNotNull(response, "Response most not be null");


        return null;
    }

}
