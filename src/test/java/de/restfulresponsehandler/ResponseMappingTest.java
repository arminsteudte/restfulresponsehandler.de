package de.restfulresponsehandler;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.fail;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMappingTest {

    @Test(expected = NullPointerException.class)
    public void handleResponse_WithNullResponse_ShouldThrowIllegalArgumentException() {

        // given
        Response response = null;
        ResponseMapping<DomainObject> responseMapping = ResponseMapping.builder().build();

        // when
        responseMapping.handleResponse(response);

        fail("Precondition not checked.");
    }


    private static class DomainObject {

    }
}