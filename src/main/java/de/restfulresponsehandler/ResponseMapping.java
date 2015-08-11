package de.restfulresponsehandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import org.glassfish.jersey.internal.util.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.Response.Status;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMapping<R> {

    private static Logger LOG = LoggerFactory.getLogger(ResponseMapping.class);

    private final Status successStatus;
    private final Class<R> succesType;
    private final ImmutableMultimap<Status, ErrorHandlingDescription> statusErrorMapping;


    private ResponseMapping(Status successStatus, Class<R> succesType, ImmutableMultimap<Status,
            ErrorHandlingDescription>
            statusErrorMapping) {
        this.successStatus = successStatus;
        this.succesType = succesType;
        this.statusErrorMapping = statusErrorMapping;
    }

    /**
     * Returns a builder for building a response mapping.
     *
     * @return ResponseMappingBuilder
     */
    public static ResponseMappingBuilder builder() {
        return new ResponseMappingBuilder();
    }

    /**
     * Handling the response of a jersey-client request by returning the expected domain object, if the status is
     * the success case.
     * Otherwise throwing the domain exceptions according to the configured mapping.
     *
     * @param response The response object from a jersey-client service call.
     * @return The deserialized domain object or throwing a domain exception.
     */
    public R handleResponse(Response response) {

        Preconditions.checkNotNull(response, "Response most not be null");

        R responseObject = null;

        try(AutoCloseableResponse autoClosing = new AutoCloseableResponse(response)){

            if (response.getStatus() == successStatus.getStatusCode()) {
                    responseObject = response.readEntity(succesType);
            }

        }catch (ProcessingException ex) {
            LOG.debug("Error deserializing service response.");
            logServiceResponseAsString(response);
            throw ex;
        }

        return responseObject;
    }

    private void logServiceResponseAsString(Response r) {
        // TODO try-catch and error message if entity cannot be read as String
        final String serviceResponse = r.readEntity(String.class);
        LOG.warn("Could deserialize service response into type {}: {}", succesType.getSimpleName(), serviceResponse);
    }

    public static class ResponseMappingBuilder {

        private final Status successStatus = Status.OK;
        private Class<?> successType;
        private Map<Status, ErrorHandlingDescription> statusCodeErrorHandlingMapping;

        private ResponseMappingBuilder() {
            super();
        }

        public ResponseMappingBuilder addSuccessType(Class<?> responseObjetType) {
            this.successType = responseObjetType;
            return this;
        }

        public ResponseMapping build() {
            return new ResponseMapping(successStatus, successType, ImmutableMultimap.of());
        }

    }

    /**
     * Object holding the necessary types and functions to produce a domain exception for a specific service response.
     */
    private static class ErrorHandlingDescription {

        private final Class<?> responseObjectType;

        private final Producer<? extends RuntimeException> exceptionProducer;

        private ErrorHandlingDescription(Class<?> responseObjectType,
                                         Producer<? extends RuntimeException> exceptionProducer) {
            this.responseObjectType = responseObjectType;
            this.exceptionProducer = exceptionProducer;
        }
    }
}