/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Equivalence.Wrapper;
import fj.data.Either;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantGuard;
import org.openecomp.sdc.exception.ResponseFormat;

public class ResourcesServletTest {

    private ResourcesServlet createTestSubject() {
        UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
        ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
        ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);
        ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
        ServletUtils servletUtils = mock(ServletUtils.class);
        ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);

        return new ResourcesServlet(componentInstanceBL, resourceBusinessLogic, componentsUtils, servletUtils,
            resourceImportManager);
    }


    private static MultitenancyConfig currentMultitenancyConfig() {
        ConfigurationManager manager = ConfigurationManager.getConfigurationManager();
        return manager == null || manager.getConfiguration() == null ? null : manager.getConfiguration().getMultitenancy();
    }

    private static void setMultitenancyConfig(MultitenancyConfig config) {
        ConfigurationManager manager = ConfigurationManager.getConfigurationManager();
        if (manager == null) {
            manager = new ConfigurationManager();
        }
        if (manager.getConfiguration() == null) {
            manager.setConfiguration(new Configuration());
        }
        manager.getConfiguration().setMultitenancy(config);
    }

    @Test
    public void testCreateResourceDisabledInvokesBusinessLogic() throws Exception {
        MultitenancyConfig previous = currentMultitenancyConfig();
        try {
            setMultitenancyConfig(null);
            ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
            ServletUtils servletUtils = mock(ServletUtils.class);
            ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
            when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(new ResponseFormat(HttpStatus.SC_CREATED));

            String data = "{\"name\":\"res-a\"}";
            Resource resource = new Resource();
            resource.setName("res-a");
            resource.setTenant("tenant-a");
            when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(data), any(User.class), eq(Resource.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.RESOURCE))).thenReturn(Either.left(resource));
            when(resourceBusinessLogic.createResource(eq(resource), eq(AuditingActionEnum.CREATE_RESOURCE), any(User.class), isNull(), isNull()))
                .thenReturn(resource);

            ResourcesServlet testSubject = new ResourcesServlet(mock(ComponentInstanceBusinessLogic.class), resourceBusinessLogic,
                componentsUtils, servletUtils, mock(ResourceImportManager.class));

            Response response = testSubject.createResource(data, request, "designer");

            assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
            verify(resourceBusinessLogic).createResource(eq(resource), eq(AuditingActionEnum.CREATE_RESOURCE), any(User.class), isNull(), isNull());
        } finally {
            setMultitenancyConfig(previous);
        }
    }

    @Test
    public void testCreateResourceEnabledRefusesAnotherTenant() throws Exception {
        MultitenancyConfig previous = currentMultitenancyConfig();
        try {
            MultitenancyConfig enabled = new MultitenancyConfig();
            enabled.setEnabled(true);
            setMultitenancyConfig(enabled);

            ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
            ServletUtils servletUtils = mock(ServletUtils.class);
            ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
            when(request.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(Collections.singletonList("tenant-b")));

            String data = "{\"name\":\"res-a\"}";
            Resource resource = new Resource();
            resource.setName("res-a");
            resource.setTenant("tenant-a");
            when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(data), any(User.class), eq(Resource.class),
                eq(AuditingActionEnum.CREATE_RESOURCE), eq(ComponentTypeEnum.RESOURCE))).thenReturn(Either.left(resource));

            ResourcesServlet testSubject = new ResourcesServlet(mock(ComponentInstanceBusinessLogic.class), resourceBusinessLogic,
                componentsUtils, servletUtils, mock(ResourceImportManager.class));

            Response response = testSubject.createResource(data, request, "bob");

            assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
            assertThat(response.getStatusInfo().getReasonPhrase()).isEqualTo(TenantGuard.TENANT_NOT_PERMITTED);
            verify(resourceBusinessLogic, never()).createResource(any(), any(), any(), any(), any());
        } finally {
            setMultitenancyConfig(previous);
        }
    }


    @Test
    public void testIsUIImport() throws Exception {
        ResourcesServlet testSubject;
        String data = "";
        boolean result;

        // default test
        testSubject = createTestSubject();
    }


    @Test
    public void testPerformUIImport() throws Exception {
        ResourcesServlet testSubject;
        Wrapper<Response> responseWrapper = null;
        String data = "";
        HttpServletRequest request = null;
        String userId = "";
        String resourceUniqueId = "";

        // default test
    }


    @Test
    public void testParseToResource() throws Exception {
        ResourcesServlet testSubject;
        String resourceJson = "";
        User user = null;
        Either<Resource, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testParseToLightResource() throws Exception {
        ResourcesServlet testSubject;
        String resourceJson = "";
        User user = null;
        Either<Resource, ResponseFormat> result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testDeleteResource() throws Exception {
        ResourcesServlet testSubject;
        String resourceId = "";
        HttpServletRequest request = null;
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testDeleteResourceByNameAndVersion() throws Exception {
        ResourcesServlet testSubject;
        String resourceName = "";
        String version = "";
        HttpServletRequest request = null;
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetResourceById() throws Exception {
        ResourcesServlet testSubject;
        String resourceId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetResourceByNameAndVersion() throws Exception {
        ResourcesServlet testSubject;
        String resourceName = "";
        String resourceVersion = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testValidateResourceName() throws Exception {
        ResourcesServlet testSubject;
        String resourceName = "";
        String resourceType = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetCertifiedAbstractResources() throws Exception {
        ResourcesServlet testSubject;
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetCertifiedNotAbstractResources() throws Exception {
        ResourcesServlet testSubject;
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testUpdateResourceMetadata() throws Exception {
        ResourcesServlet testSubject;
        String resourceId = "";
        String data = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testUpdateResource() throws Exception {
        ResourcesServlet testSubject;
        String data = "";
        HttpServletRequest request = null;
        String userId = "";
        String resourceId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testConvertMapToList() throws Exception {
        Map<String, PropertyDefinition> properties = null;
        List<PropertyDefinition> result;

        // test 1
        properties = null;

    }


    @Test
    public void testGetResourceFromCsar() throws Exception {
        ResourcesServlet testSubject;
        HttpServletRequest request = null;
        String userId = "";
        String csarUUID = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }
}
