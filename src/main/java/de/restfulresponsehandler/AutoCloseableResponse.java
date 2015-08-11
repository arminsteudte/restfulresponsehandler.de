package de.restfulresponsehandler;

import javax.ws.rs.core.Response;

/**
 * Created by Armin on 11.08.2015.
 */
public class AutoCloseableResponse implements AutoCloseable {

    private final Response response;

    public AutoCloseableResponse(Response response) {
        this.response = response;
    }


    @Override
    public void close() {
        response.close();
    }
}
