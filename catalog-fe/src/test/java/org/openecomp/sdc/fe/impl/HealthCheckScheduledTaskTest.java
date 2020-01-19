/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.fe.impl;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.fe.config.Configuration;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckScheduledTaskTest {

    private static final String PROTOCOL = "http";
    private static final String HOST = "192.115.113.25";
    private static final Integer PORT = 8090;
    private static final String URI = "/healthCheck";
    private static final String HC_URL = String.format("%s://%s:%s%s", PROTOCOL, HOST, PORT, URI);

    @Mock
    private Configuration.CatalogFacadeMsConfig catalogFacadeMsConfig;
    @Mock
    private Configuration.DcaeConfig dcaeConfig;
    @Mock
    private Configuration.OnboardingConfig onboardingConfig;
    @Mock
    private Configuration configuration;
    @Mock
    private HealthCheckService healthCheckService;

    @InjectMocks
    private HealthCheckScheduledTask healthCheckScheduledTask;

    @Before
    public void setUp() {
        healthCheckScheduledTask = new HealthCheckScheduledTask(healthCheckService);
        initMocks();
    }

    @Test
    public void getOnboardingUrlWhenConfigurationIsNotProvided() {
        when(configuration.getOnboarding()).thenReturn(null);
        assertNull(healthCheckScheduledTask.getExternalComponentHcUrl(Constants.HC_COMPONENT_ON_BOARDING));
    }

    @Test
    public void getUrlForUnknownComponent() {
        assertNull(healthCheckScheduledTask.getExternalComponentHcUrl("test"));
    }

    @Test
    public void getOnboardingUrlWhenConfigurationIsProvided() {
        when(configuration.getOnboarding()).thenReturn(onboardingConfig);
        assertNull(HealthCheckScheduledTask.getOnboardingHcUrl());
        healthCheckScheduledTask.getExternalComponentHcUrl(Constants.HC_COMPONENT_ON_BOARDING);
        assertEquals(HC_URL, HealthCheckScheduledTask.getOnboardingHcUrl());
    }

    @Test
    public void getCatalogFacadeMsUrlWhenConfigurationIsProvidedAndVerifyThatItIsCalculatedOnlyOnce() {
        when(configuration.getCatalogFacadeMs()).thenReturn(catalogFacadeMsConfig);
        assertNull(HealthCheckScheduledTask.getCatalogFacadeMsHcUrl());

        HealthCheckScheduledTask healthCheckScheduledTaskSpy = Mockito.spy(healthCheckScheduledTask);

        healthCheckScheduledTaskSpy.getExternalComponentHcUrl(Constants.HC_COMPONENT_CATALOG_FACADE_MS);
        assertEquals(HC_URL, HealthCheckScheduledTask.getCatalogFacadeMsHcUrl());
        //try to run again and verify that assignment is not recalled
        healthCheckScheduledTaskSpy.getExternalComponentHcUrl(Constants.HC_COMPONENT_CATALOG_FACADE_MS);
        verify(healthCheckScheduledTaskSpy, times(1)).
                buildHealthCheckUrl(any(String.class), any(String.class), any(Integer.class), any(String.class));
    }

    @Test
    public void getDcaeUrlWhenConfigurationIsProvided() {
        when(configuration.getDcae()).thenReturn(dcaeConfig);
        assertNull(HealthCheckScheduledTask.getDcaeHcUrl());
        healthCheckScheduledTask.getExternalComponentHcUrl(Constants.HC_COMPONENT_DCAE);
        assertEquals(HC_URL, HealthCheckScheduledTask.getDcaeHcUrl());
    }

    @Test
    public void getExcludedComponentListWhenCatalogFacadeMsConfigExists() {
        when(configuration.getCatalogFacadeMs()).thenReturn(catalogFacadeMsConfig);
        when(catalogFacadeMsConfig.getPath()).thenReturn("/uicache");
        when(configuration.getHealthStatusExclude()).thenReturn(Lists.newArrayList("DMAAP", "DCAE"));
        assertFalse(healthCheckScheduledTask.getExcludedComponentList().contains(Constants.HC_COMPONENT_CATALOG_FACADE_MS));
    }

    @Test
    public void getExcludedComponentListWhenCatalogFacadeMsConfigDoesNotExist() {
        when(configuration.getCatalogFacadeMs()).thenReturn(null);
        when(configuration.getHealthStatusExclude()).thenReturn(Lists.newArrayList());
        assertTrue(healthCheckScheduledTask.getExcludedComponentList().contains(Constants.HC_COMPONENT_CATALOG_FACADE_MS));
    }

    @Test
    public void getExcludedComponentListWhenCatalogFacadeMsConfigPathIsNotSet() {
        when(configuration.getCatalogFacadeMs()).thenReturn(catalogFacadeMsConfig);
        when(catalogFacadeMsConfig.getPath()).thenReturn(null);
        when(configuration.getHealthStatusExclude()).thenReturn(Lists.newArrayList());
        assertTrue(healthCheckScheduledTask.getExcludedComponentList().contains(Constants.HC_COMPONENT_CATALOG_FACADE_MS));
    }

    @Test
    public void getMergedHCListWhenFeHcIsEmptyAndMainListIsSet() {
        HealthCheckInfo mainHC = new HealthCheckInfo();
        mainHC.setComponentsInfo(Collections.emptyList());
        assertEquals(0, healthCheckScheduledTask.updateSubComponentsInfoOfBeHc(mainHC, Collections.emptyList()).getComponentsInfo().size());
    }

    @Test
    public void getMergedHCListWhenFeHcIsEmptyAndMainListIsNotSet() {
        assertNull(healthCheckScheduledTask.updateSubComponentsInfoOfBeHc(new HealthCheckInfo(), Collections.emptyList()).getComponentsInfo());
    }

    @Test
    public void getMergedHCListWhenFeHcListAndMainListAreNotEmpty() {
        HealthCheckInfo mainHC = new HealthCheckInfo();
        mainHC.setComponentsInfo(Lists.newArrayList(new HealthCheckInfo()));
        assertEquals(2, healthCheckScheduledTask.updateSubComponentsInfoOfBeHc(mainHC,
                Collections.singletonList(new HealthCheckInfo())).getComponentsInfo().size());
    }

    @Test
    public void getMergedHCListWhenFeHcListIsNotEmptyAndMainListIsEmpty() {
        assertEquals(1, healthCheckScheduledTask.updateSubComponentsInfoOfBeHc(new HealthCheckInfo(),
                Collections.singletonList(new HealthCheckInfo())).getComponentsInfo().size());
    }


    private void initMocks() {
        when(healthCheckService.getConfig()).thenReturn(configuration);

        when(onboardingConfig.getProtocolFe()).thenReturn(PROTOCOL);
        when(onboardingConfig.getHostFe()).thenReturn(HOST);
        when(onboardingConfig.getPortFe()).thenReturn(PORT);
        when(onboardingConfig.getHealthCheckUriFe()).thenReturn(URI);

        when(dcaeConfig.getProtocol()).thenReturn(PROTOCOL);
        when(dcaeConfig.getHost()).thenReturn(HOST);
        when(dcaeConfig.getPort()).thenReturn(PORT);
        when(dcaeConfig.getHealthCheckUri()).thenReturn(URI);

        when(catalogFacadeMsConfig.getProtocol()).thenReturn(PROTOCOL);
        when(catalogFacadeMsConfig.getHost()).thenReturn(HOST);
        when(catalogFacadeMsConfig.getPort()).thenReturn(PORT);
        when(catalogFacadeMsConfig.getHealthCheckUri()).thenReturn(URI);
    }
}
