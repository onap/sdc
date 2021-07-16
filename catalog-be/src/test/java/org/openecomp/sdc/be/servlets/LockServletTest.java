/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class LockServletTest extends JerseyTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private UserValidations userValidations;

    private static final String postUrl = "/v1/catalog/lock";
    private static final String USER_ID = "cs0008";

    @BeforeEach
    void init() throws Exception {
        super.setUp();
        initConfig();
    }

    @AfterEach
    void destory() throws Exception {
        super.tearDown();
    }

    private void initConfig() {
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        final org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Override
    protected ResourceConfig configure() {
        MockitoAnnotations.openMocks(this);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(LockServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(graphLockOperation).to(IGraphLockOperation.class);
                    bind(userValidations).to(UserValidations.class);
                }
            })
            .property("contextConfig", context);
    }

    @Test
    void disableLockTest() {
        assertEquals(Status.OK.getStatusCode(), postDisableLock(true));
        assertEquals(Status.OK.getStatusCode(), postDisableLock(false));
        assertEquals(Status.OK.getStatusCode(), postDisableLock("true"));
        assertEquals(Status.OK.getStatusCode(), postDisableLock("false"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), postDisableLock("true1"));
        assertEquals(Status.BAD_REQUEST.getStatusCode(), postDisableLock(null));
    }

    private int postDisableLock(Object disable) {
        return target(postUrl).request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .post(Entity.json(disable)).getStatus();
    }
}