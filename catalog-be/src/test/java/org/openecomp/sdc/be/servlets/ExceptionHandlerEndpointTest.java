package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceptionHandlerEndpointTest extends JerseySpringBaseTest {

    private static ComponentsUtils componentUtils;

    @org.springframework.context.annotation.Configuration
    @Import(BaseTestConfig.class)
    static class ExceptionHandlerConfig {

        @Bean
        ExceptionHandlerEndpoint exceptionHandlerEndpoint() {
            return new ExceptionHandlerEndpoint(componentUtils);
        }
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
    }

    @Override
    protected ResourceConfig configure() {
        componentUtils = mock(ComponentsUtils.class);

        return super.configure(ExceptionHandlerConfig.class)
                .register(ExceptionHandlerEndpoint.class);
    }

    @Test
    public void getHandleException() {
        when(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        Response response = target().path("/v1/catalog/handleException").request().get(Response.class);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }
}
