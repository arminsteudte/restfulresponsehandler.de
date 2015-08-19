package de.restfulresponsehandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.Response.*;

/**
 * Created by Armin on 19.08.2015.
 */
public class Slf4jErrorReporter implements ErrorReporter {

    private static Logger LOG = LoggerFactory.getLogger(Slf4jErrorReporter.class);

    public void reportSerializationError(Status status, Class<?> expectedEntityClass, String serviceResponse) {
        LOG.error("Could not serialize service response for status {} and expected class {}. \n Response was: {}",
                status.getStatusCode(), expectedEntityClass.getSimpleName(), serviceResponse);
    }

}
