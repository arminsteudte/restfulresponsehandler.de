package de.restfulresponsehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.restfulresponsehandler.exceptions.EntitySerializationException;
import org.apache.http.client.config.RequestConfig;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMappingTest {

    public static final int PORT = 8089;
    public static final String EXAMPLES_RESOURCE_PATH = "/examples";
    private static ObjectMapper mapper = new ObjectMapper();
    private static Client restClient = RestClientBuilder.createDefaultRestClient();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT); // No-args constructor defaults to port 8080

    @Test(expected = NullPointerException.class)
    public void handleResponse_WithNullResponse_ShouldThrowIllegalArgumentException() {

        // given
        Response response = null;
        ResponseMapping<ExampleDomainObject> responseMapping = ResponseMapping.builder().build();

        // when
        responseMapping.handleResponse(response);

        fail("Precondition not checked.");
    }


    @Test
    public void handleResponse_WithSuccessStatus200AndExpectedBody_ShouldReturnDeserializedObjectFromBody() throws JsonProcessingException {

        // given
        final String name = "TestName";
        final ExampleDomainObject payload = new ExampleDomainObject(name);

        final String json = toJson(payload, mapper);

        createSuccessExampleResourceWithBody(json);

        final ResponseMapping<ExampleDomainObject> responseMapping = ResponseMapping.<ExampleDomainObject>builder()
                .addSuccessType(ExampleDomainObject.class)
                .build();

        final Response jerseyResponse = callExampleResource();
        // when
        final ExampleDomainObject domainObject = responseMapping.handleResponse(jerseyResponse);

        // then
        assertNotNull(domainObject);
        assertEquals(payload.getName(), domainObject.getName());

    }



    @Test(expected = EntitySerializationException.class)
    public void handleResponse_WithNotSerializeableBody_ShouldThrowExceptionAndLogBodyAsString() {

        // given
        final String notSerializeableBody = "{ notExistingProperty: ''}";
        final ResponseMapping<ExampleDomainObject> responseMapping = ResponseMapping.<ExampleDomainObject>builder()
                .addSuccessType(ExampleDomainObject.class)
                .build();

        createSuccessExampleResourceWithBody(notSerializeableBody);

        final Response response = callExampleResource();

        // when
        final ExampleDomainObject domainObject = responseMapping.handleResponse(response);

        fail("ProcessingException not thrown!");


    }

    private Response callExampleResource() {
        return restClient
                .target("http://localhost:" + PORT)
                .path(EXAMPLES_RESOURCE_PATH)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get();
    }

    private void createSuccessExampleResourceWithBody(String json) {
        stubFor(get(urlMatching(EXAMPLES_RESOURCE_PATH)).
                willReturn(aResponse()
                        .withStatus(Status.OK.getStatusCode())
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)));
    }

    private String toJson(Object target, ObjectMapper mapper) throws JsonProcessingException {

        final String json = mapper.writeValueAsString(target);

        return json;

    }

    @XmlRootElement
    private static class ExampleDomainObject {

        private String name = "";

        private ExampleDomainObject() {
            super();
        }

        public ExampleDomainObject(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }

    }

    private static class RestClientBuilder {

        private static Client createDefaultRestClient() {

            final ClientConfig clientConfig = new ClientConfig();
            clientConfig.connectorProvider(new ApacheConnectorProvider());

            RequestConfig reqConfig = RequestConfig.custom()
                    .setConnectTimeout(2000)
                    .setSocketTimeout(2000)
                    .setConnectionRequestTimeout(200)
                    .build();

            clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig);
            final Client client = ClientBuilder.newClient(clientConfig);

            return client;
        }
    }

}