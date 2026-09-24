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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.tenant.TenantContext;
import org.openecomp.sdc.common.tenant.TenantGuard;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * Unit tests for {@link ServiceServlet#createService} against hand-built {@link TenantContext}s, with no Docker
 * dependency. See {@link ServiceServletMultitenancyIT} for the equivalent coverage against a real Keycloak issuer.
 */
class ServiceServletTest {

    private static final String DATA = "{\"name\":\"svc\"}";

    private final ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private final ServletUtils servletUtils = mock(ServletUtils.class);
    private final ServiceBusinessLogic serviceBusinessLogic = mock(ServiceBusinessLogic.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private MultitenancyConfig previousConfig;

    private ServiceServlet servlet() {
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        return new ServiceServlet(mock(ComponentInstanceBusinessLogic.class), componentsUtils, servletUtils, mock(ResourceImportManager.class),
            serviceBusinessLogic, mock(ResourceBusinessLogic.class), mock(ElementBusinessLogic.class), mock(UserBusinessLogic.class));
    }

    private static Service service(String tenant) {
        Service service = new Service();
        service.setName("svc");
        service.setTenant(tenant);
        return service;
    }

    private void stubParsedService(Service service) {
        when(componentsUtils.convertJsonToObjectUsingObjectMapper(eq(DATA), any(User.class), eq(Service.class),
            eq(AuditingActionEnum.CREATE_SERVICE), eq(ComponentTypeEnum.SERVICE))).thenReturn(Either.left(service));
    }

    @BeforeEach
    void captureMultitenancyConfig() {
        ConfigurationManager manager = ConfigurationManager.getConfigurationManager();
        previousConfig = manager == null || manager.getConfiguration() == null ? null : manager.getConfiguration().getMultitenancy();
    }

    @AfterEach
    void restoreMultitenancyConfig() {
        setMultitenancyConfig(previousConfig);
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
    void disabledCreatesServiceAndPassesTenantThrough() {
        setMultitenancyConfig(null);
        Service service = service("tenant-a");
        stubParsedService(service);
        when(serviceBusinessLogic.createService(eq(service), any(User.class))).thenReturn(Either.left(service));
        when(componentsUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(new ResponseFormat(HttpStatus.SC_CREATED));

        Response response = servlet().createService(DATA, request, "designer");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
        verify(serviceBusinessLogic).createService(eq(service), any(User.class));
        assertThat(service.getTenant()).isEqualTo("tenant-a");
    }

    @Test
    void enabledRefusesServiceForAnotherTenant() {
        MultitenancyConfig enabled = new MultitenancyConfig();
        enabled.setEnabled(true);
        setMultitenancyConfig(enabled);
        when(request.getAttribute(TenantContext.ATTRIBUTE)).thenReturn(new TenantContext(Arrays.asList("tenant-b")));
        Service service = service("tenant-a");
        stubParsedService(service);

        Response response = servlet().createService(DATA, request, "bob");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
        assertThat(response.getStatusInfo().getReasonPhrase()).isEqualTo(TenantGuard.TENANT_NOT_PERMITTED);
        verify(serviceBusinessLogic, never()).createService(any(Service.class), any(User.class));
    }
}
