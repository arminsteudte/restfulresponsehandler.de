package de.restfulresponsehandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import org.glassfish.jersey.internal.util.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMapping<R> {

    private static Logger LOG = LoggerFactory.getLogger(ResponseMapping.class);

    private final ErrorReporter reporter;
    private final Status successStatus;
    private final Class<R> successType;
    private final ImmutableMultimap<Status, ErrorHandlingDescription> statusErrorMapping;

    private ResponseMapping(ErrorReporter reporter, Status successStatus, Class<R> successType,
                            ImmutableMultimap<Status,
            ErrorHandlingDescription>
            statusErrorMapping) {
        this.reporter = reporter;
        this.successStatus = successStatus;
        this.successType = successType;
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

        response.bufferEntity();

        try{

            if (response.getStatus() == successStatus.getStatusCode()) {
                    responseObject = response.readEntity(successType);
            }

        }catch (ProcessingException ex) {
            LOG.debug("Error deserializing service response.");
            logServiceResponseAsString(response);
            throw ex;
        }finally {
            if(response != null) {
                response.close();
                LOG.debug("Response closed.");
            }
        }

        return responseObject;
    }

    private void logServiceResponseAsString(Response r) {
        // TODO try-catch and error message if entity cannot be read as String
        final String serviceResponse = r.readEntity(String.class);
        reporter.reportSerializationError(Status.fromStatusCode(r.getStatus()), successType, serviceResponse);
    }

    public static class ResponseMappingBuilder<S> {

        private final Status successStatus = Status.OK;
        private Class<S> successType;
        private Map<Status, ErrorHandlingDescription> statusCodeErrorHandlingMapping = new HashMap<>();
        private ErrorReporter reporter;

        private ResponseMappingBuilder() {
            super();
        }

        public ResponseMappingBuilder addSuccessType(Class<S> responseObjectType) {
            this.successType = responseObjectType;
            return this;
        }

        public ResponseMappingBuilder addErrorReporter(ErrorReporter reporter) {
            this.reporter = reporter;
            return this;
        }

        /**
         * Will add an error situation and how to handle it to the response mapping. If a there has been a mapping
         * before this will be overwritten.
         *
         * @param responseStatus Http status to react to.
         * @param errorHandling Object holding error handling informations.
         * @return
         */
        public ResponseMappingBuilder addErrorSituation(Status responseStatus, ErrorHandlingDescription errorHandling) {

            statusCodeErrorHandlingMapping.put(responseStatus, errorHandling);

            return this;
        }

        public ResponseMapping build() {

            if(this.reporter == null) {
                this.reporter = new Slf4jErrorReporter();
            }

            return new ResponseMapping<S>(this.reporter, this.successStatus, this.successType, ImmutableMultimap
                    .of());
        }

    }

    /**
     * Object holding the necessary types and functions to produce a domain exception for a specific service response.
     */
    public static class ErrorHandlingDescription {

        private final Class<?> responseObjectType;

        private final Producer<? extends RuntimeException> exceptionProducer;

        public ErrorHandlingDescription(Class<?> responseObjectType,
                                         Producer<? extends RuntimeException> exceptionProducer) {
            this.responseObjectType = responseObjectType;
            this.exceptionProducer = exceptionProducer;
        }
    }
}