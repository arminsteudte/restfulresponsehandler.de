package de.restfulresponsehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Created by Armin on 06.08.2015.
 */
public class ResponseMappingIT {

    public static final int PORT = 8089;
    public static final String EXAMPLES_RESOURCE_PATH = "/examples";
    public static final String EMPTY_JSON = "{}";
    private static ObjectMapper mapper = new ObjectMapper();
    private static Client restClient = RestClientBuilder.createDefaultRestClient();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

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


    @Test(expected = ProcessingException.class)
    public void handleResponse_WithNotSerializeableBody_ShouldThrowException() {

        // given
        final String notSerializeableBody = "{ notExistingProperty: ''}";
        final ErrorReporter reporterMock = mock(ErrorReporter.class);

        final ResponseMapping<ExampleDomainObject> responseMapping = ResponseMapping.<ExampleDomainObject>builder()
                .addSuccessType(ExampleDomainObject.class)
                .build();

        createSuccessExampleResourceWithBody(notSerializeableBody);

        final Response response = callExampleResource();

        // when
        final ExampleDomainObject domainObject = responseMapping.handleResponse(response);

        fail("No exception on serialization!");
    }

    @Test(expected = ProcessingException.class)
    public void handleResponse_WithNotSerializeableBody_ShouldNotifyErrorReporter() {

        // given
        final String notSerializeableBody = "{ \"notExistingProperty\": \" \"}";
        final ErrorReporter reporterMock = mock(ErrorReporter.class);

        final ResponseMapping<ExampleDomainObject> responseMapping = ResponseMapping.<ExampleDomainObject>builder()
                .addSuccessType(ExampleDomainObject.class)
                .addErrorReporter(reporterMock)
                .build();

        createSuccessExampleResourceWithBody(notSerializeableBody);

        final Response response = callExampleResource();

        // when
        final ExampleDomainObject domainObject = responseMapping.handleResponse(response);

        // then
        verify(reporterMock).reportSerializationError(Status.OK, ExampleDomainObject.class, anyString());
        verifyNoMoreInteractions(reporterMock);
    }

    @Test(expected = ExampleException.class)
    public void handleResponse_WithStatus404_ShouldThrowCorrespondingException() {

        // given

        final ResponseMapping responseMapping = ResponseMapping.builder().addErrorSituation(Status.NOT_FOUND, new ResponseMapping.ErrorHandlingDescription
                (ExampleDomainObject.class, () -> {
                    throw new ExampleException();
                })).build();

        createExampleResourceWithStatusAndBody(Status.NOT_FOUND, EMPTY_JSON);

        final Response response = callExampleResource();

        // when
        responseMapping.handleResponse(response);

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
        createExampleResourceWithStatusAndBody(Status.OK, json);
    }

    private void createExampleResourceWithStatusAndBody(Status status, String json) {
        stubFor(get(urlMatching(EXAMPLES_RESOURCE_PATH)).
                willReturn(aResponse()
                        .withStatus(status.getStatusCode())
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

    public static class ExampleException extends RuntimeException {

    }

}