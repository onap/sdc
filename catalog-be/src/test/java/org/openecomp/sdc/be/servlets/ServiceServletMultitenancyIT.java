/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.test.util.KeycloakTestRealm;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantGuard;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class ServiceServletMultitenancyIT extends JerseyTest {

    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static final HttpSession session = mock(HttpSession.class);
    private static final ServletContext servletContext = mock(ServletContext.class);
    private static final WebAppContextWrapper webAppContextWrapper = mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    private static final UserBusinessLogic userAdmin = mock(UserBusinessLogic.class);
    private static final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private static final ElementBusinessLogic elementBusinessLogic = mock(ElementBusinessLogic.class);
    private static final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = mock(ComponentInstanceBusinessLogic.class);
    private static final ServletUtils servletUtils = mock(ServletUtils.class);
    private static final ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private static final ServiceBusinessLogic serviceBusinessLogic = mock(ServiceBusinessLogic.class);
    private static final ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);
    private static final User designer = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(),
        System.currentTimeMillis());
    private static final String BODY = "{\"name\":\"svc\"}";
    private static KeycloakTestRealm keycloak;

    @BeforeAll
    static void setUpAll() {
        keycloak = KeycloakTestRealm.get();
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(UserBusinessLogic.class)).thenReturn(userAdmin);
        when(webApplicationContext.getBean(ElementBusinessLogic.class)).thenReturn(elementBusinessLogic);
        when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(new ResponseFormat(HttpStatus.SC_OK));
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(new ResponseFormat(HttpStatus.SC_CREATED));
        when(userAdmin.getUser(designer.getUserId(), false)).thenReturn(designer);
        // AbstractValidationsServlet#getComponentsUtils() delegates to ServletUtils, not to the constructor-injected field.
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);

        ConfigurationManager configurationManager = new ConfigurationManager(
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        Configuration configuration = new Configuration();
        configuration.setMultitenancy(KeycloakTestRealm.config(keycloak.issuer(), null));
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @BeforeEach
    void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig(ServiceServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(serviceBusinessLogic).to(ServiceBusinessLogic.class);
                    bind(resourceBusinessLogic).to(ResourceBusinessLogic.class);
                    bind(elementBusinessLogic).to(ElementBusinessLogic.class);
                    bind(userAdmin).to(UserBusinessLogic.class);
                }
            })
            .register(MultiPartFeature.class)
            .property("contextConfig", new AnnotationConfigApplicationContext(SpringConfig.class));
    }

    private Response createAs(String username, String tenant) throws Exception {
        reset(serviceBusinessLogic);
        Service service = new Service();
        service.setName("svc");
        service.setTenant(tenant);
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(BODY), any(User.class), eq(Service.class),
            eq(AuditingActionEnum.CREATE_SERVICE), eq(ComponentTypeEnum.SERVICE))).thenReturn(Either.left(service));
        when(serviceBusinessLogic.createService(any(Service.class), any(User.class))).thenReturn(Either.left(service));
        when(request.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(keycloak.contextFor(username));
        return target().path("/v1/catalog/services").request().accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, designer.getUserId()).post(Entity.json(BODY));
    }

    @Test
    void createsServiceForTheCallersTenant() throws Exception {
        assertThat(createAs("alice", "tenant-a").getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        verify(serviceBusinessLogic).createService(any(Service.class), any(User.class));
    }

    @Test
    void refusesServiceForAnotherTenant() throws Exception {
        Response response = createAs("bob", "tenant-a");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
        assertThat(response.getStatusInfo().getReasonPhrase()).isEqualTo(TenantGuard.TENANT_NOT_PERMITTED);
        verify(serviceBusinessLogic, never()).createService(any(Service.class), any(User.class));
    }

    @Test
    void refusesServiceWithoutTenant() throws Exception {
        assertThat(createAs("carol", null).getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
        verify(serviceBusinessLogic, never()).createService(any(Service.class), any(User.class));
    }
}
