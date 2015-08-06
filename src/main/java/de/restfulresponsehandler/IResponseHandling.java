package de.restfulresponsehandler;

import javax.ws.rs.core.Response;

/**
 * Created by Armin on 06.08.2015.
 */
public interface IResponseHandling<R> {


    /**
     * Method for handling a given Response from jersey-client.
     * @param response The response object from a jersey-client rest call.
     * @return  Domain object which the service returns on a successful call.
     */
    public R handleResponse(Response response);

}
