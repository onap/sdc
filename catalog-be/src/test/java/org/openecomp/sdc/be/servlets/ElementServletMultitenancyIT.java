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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.test.util.KeycloakTestRealm;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class ElementServletMultitenancyIT extends JerseyTest {

    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static final HttpSession session = mock(HttpSession.class);
    private static final ServletContext servletContext = mock(ServletContext.class);
    private static final WebAppContextWrapper webAppContextWrapper = mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    private static final UserBusinessLogic userAdmin = mock(UserBusinessLogic.class);
    private static final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private static final ElementBusinessLogic elementBusinessLogic = mock(ElementBusinessLogic.class);
    private static final User designer = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(),
        System.currentTimeMillis());
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
        when(userAdmin.getUser(designer.getUserId(), false)).thenReturn(designer);

        ConfigurationManager configurationManager = new ConfigurationManager(
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
        Configuration configuration = new Configuration();
        configuration.setMultitenancy(KeycloakTestRealm.config(keycloak.issuer(), null));
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");

        Map<String, List<? extends Component>> followed = new HashMap<>();
        followed.put("resources", Arrays.asList(resource("res-a", "tenant-a"), resource("res-b", "tenant-b"), resource("res-none", null)));
        followed.put("services", new ArrayList<>(Arrays.asList(service("svc-a", "tenant-a"))));
        when(elementBusinessLogic.getFollowed(designer)).thenReturn(Either.left(followed));
    }

    private static Resource resource(String name, String tenant) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setTenant(tenant);
        return resource;
    }

    private static Service service(String name, String tenant) {
        Service service = new Service();
        service.setName(name);
        service.setTenant(tenant);
        return service;
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
        return new ResourceConfig(ElementServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userAdmin).to(UserBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(mock(ComponentsCleanBusinessLogic.class)).to(ComponentsCleanBusinessLogic.class);
                    bind(elementBusinessLogic).to(ElementBusinessLogic.class);
                    bind(mock(ArtifactsBusinessLogic.class)).to(ArtifactsBusinessLogic.class);
                    bind(mock(ModelBusinessLogic.class)).to(ModelBusinessLogic.class);
                }
            })
            .property("contextConfig", new AnnotationConfigApplicationContext(SpringConfig.class));
    }

    private Map<String, List<String>> followedAs(String username) throws Exception {
        TenantContext context = keycloak.contextFor(username);
        when(request.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(context);
        Response response = target().path("/v1/followed").request().accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, designer.getUserId()).get();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        JsonNode body = new ObjectMapper().readTree(response.readEntity(String.class));
        Map<String, List<String>> names = new HashMap<>();
        body.fields().forEachRemaining(entry -> {
            List<String> list = new ArrayList<>();
            entry.getValue().forEach(component -> list.add(component.get("name").asText()));
            names.put(entry.getKey(), list);
        });
        return names;
    }

    @Test
    void showsOnlyTheCallersTenantAndHidesUntenanted() throws Exception {
        Map<String, List<String>> names = followedAs("alice");
        assertThat(names.get("resources")).containsExactly("res-a");
        assertThat(names.get("services")).containsExactly("svc-a");
    }

    @Test
    void callerWithTwoTenantsSeesBoth() throws Exception {
        Map<String, List<String>> names = followedAs("carol");
        assertThat(names.get("resources")).containsExactly("res-a", "res-b");
        assertThat(names.get("services")).containsExactly("svc-a");
    }

    @Test
    void callerWithoutTenantSeesNothing() throws Exception {
        Map<String, List<String>> names = followedAs("dave");
        assertThat(names.get("resources")).isEmpty();
        assertThat(names.get("services")).isEmpty();
    }
}
