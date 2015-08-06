package de.restfulresponsehandler;

import com.google.common.base.Preconditions;

import javax.ws.rs.core.Response;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMapping<R> {

    private ResponseMapping() {

    }

    /**
     * Handling the response of a jersey-client request by returning the expected domain object, if the status is the success case.
     * Otherwise throwing the domain exceptions according to the configured mapping.
     *
     * @param response The response object from a jersey-client service call.
     * @return The deserialized domain object or throwing a domain exception.
     */
    public R handleResponse(Response response) {

        Preconditions.checkNotNull(response, "Response most not be null");


        return null;
    }

    /**
     * Returns a builder for building a response mapping.
     * @return ResponseMappingBuilder
     */
    public static ResponseMappingBuilder builder() {
        return new ResponseMappingBuilder();
    }

    public  static class ResponseMappingBuilder {


        private ResponseMappingBuilder() {
            super();
        }

        public ResponseMapping build() {

            return new ResponseMapping();
        }

    }
}
