/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.distribution.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;

import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.distribution.AuditHandler;
import org.openecomp.sdc.be.distribution.DistributionBusinessLogic;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.distribution.api.client.TopicRegistrationResponse;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class DistributionServletTest extends JerseyTest {

    public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    public static final HttpSession session = Mockito.mock(HttpSession.class);
    public static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    public static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    public static final ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
    public static final DistributionBusinessLogic distributionBusinessLogic = Mockito.mock(DistributionBusinessLogic.class);
    public static final DistributionEngine distributionEngine = Mockito.mock(DistributionEngine.class);
    private static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);


    public static final String ENV_NAME = "myEnv";
    public static final String NOTIFICATION_TOPIC = ENV_NAME + "_Notification";
    public static final String STATUS_TOPIC = ENV_NAME + "_Status";

    @BeforeClass
    public static void setup() {
        ThreadLocalsHolder.setApiType(FilterDecisionEnum.EXTERNAL);
        ExternalConfiguration.setAppName("catalog-be");
        when(request.getSession()).thenReturn(session);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn("myApplicationInstanceID");

        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(DistributionBusinessLogic.class)).thenReturn(distributionBusinessLogic);
        when(distributionBusinessLogic.getDistributionEngine()).thenReturn(distributionEngine);
        when(distributionEngine.isEnvironmentAvailable(ENV_NAME)).thenReturn(StorageOperationStatus.OK);
        when(distributionEngine.isEnvironmentAvailable()).thenReturn(StorageOperationStatus.OK);

        when(request.isUserInRole(anyString())).thenReturn(true);

        mockBusinessLogicResponse();

    }

    private static void mockBusinessLogicResponse() {
        // Mock Register
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Wrapper<Response> responseWrapper = (Wrapper<Response>) args[0];
                TopicRegistrationResponse okTopicResponse = new TopicRegistrationResponse();
                okTopicResponse.setDistrNotificationTopicName(NOTIFICATION_TOPIC);
                okTopicResponse.setDistrStatusTopicName(STATUS_TOPIC);
                responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(okTopicResponse).build());

                return true;
            }
        }).when(distributionBusinessLogic).handleRegistration(Mockito.any(Wrapper.class), Mockito.any(RegistrationRequest.class), Mockito.any(AuditHandler.class));

        // Mock Unregister
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Wrapper<Response> responseWrapper = (Wrapper<Response>) args[0];
                TopicRegistrationResponse okTopicResponse = new TopicRegistrationResponse();
                okTopicResponse.setDistrNotificationTopicName(NOTIFICATION_TOPIC);
                okTopicResponse.setDistrStatusTopicName(STATUS_TOPIC);
                responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(okTopicResponse).build());

                return true;
            }
        }).when(distributionBusinessLogic).handleUnRegistration(Mockito.any(Wrapper.class), Mockito.any(RegistrationRequest.class), Mockito.any(AuditHandler.class));
    }

    @Test
    public void registerSuccessTest() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", ENV_NAME, false);
        Response response = target().path("/v1/registerForDistribution").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(registrationRequest)), Response.class);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }

    @Test
    public void registerSuccessOnTenantTest() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", ENV_NAME, Arrays.asList("11","22"),false);
        Response response = target().path("/v1/registerForDistribution").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(registrationRequest)), Response.class);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }

    @Test
    public void unRegisterSuccessTest() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", ENV_NAME, false);
        Response response = target().path("/v1/unRegisterForDistribution").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(registrationRequest)), Response.class);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }

    @Test
    public void unRegisterSuccessOnTenantTest() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", ENV_NAME, false);
        Response response = target().path("/v1/unRegisterForDistribution").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(registrationRequest)), Response.class);
        assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }

    @Override
    protected Application configure() {
        UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
        GroupBusinessLogic groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
        ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
        ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(DistributionServlet.class)
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                        bind(userBusinessLogic).to(UserBusinessLogic.class);
                        bind(groupBusinessLogic).to(GroupBusinessLogic.class);
                        bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                        bind(componentsUtils).to(ComponentsUtils.class);
                        bind(distributionBusinessLogic).to(DistributionBusinessLogic.class);
                    }
                })
                .property("contextConfig", context);
    }
}
