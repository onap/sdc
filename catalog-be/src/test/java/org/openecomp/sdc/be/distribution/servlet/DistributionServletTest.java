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
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.DistributionBusinessLogic;
import org.openecomp.sdc.be.distribution.api.client.KafkaDataResponse;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

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
    public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private static ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

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
        when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);

        when(request.isUserInRole(anyString())).thenReturn(true);

        when(distributionBusinessLogic.getKafkaData()).thenCallRealMethod();
    }

    @Test
    public void removedCambriaEndpointsAre404() {
        for (String path : List.of("/v1/distributionUebCluster", "/v1/registerForDistribution", "/v1/unRegisterForDistribution")) {
            Invocation.Builder request = target(path).request().headers(defaultHeaders());
            Response r = path.contains("egister") ? request.post(Entity.json("{}")) : request.get();
            assertEquals(path, 404, r.getStatus());
        }
    }

    @Test
    public void kafkaDataIsUnchanged() {
        Response r = target("/v1/distributionKafkaData").request().headers(defaultHeaders()).get();
        assertEquals(200, r.getStatus());
        KafkaDataResponse body = new Gson().fromJson(r.readEntity(String.class), KafkaDataResponse.class);
        assertEquals("kafka-bootstrap:9092", body.getKafkaBootStrapServer());
        assertEquals("ASDC-DISTR-NOTIF-TOPIC-PROD", body.getDistrNotificationTopicName());
        assertEquals("ASDC-DISTR-STATUS-TOPIC-PROD", body.getDistrStatusTopicName());
    }

    private static MultivaluedMap<String, Object> defaultHeaders() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add(Constants.X_ECOMP_INSTANCE_ID_HEADER, "myApplicationInstanceID");
        headers.add(Constants.ACCEPT_HEADER, MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    protected Application configure() {
        UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
        GroupBusinessLogic groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
        ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);

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
