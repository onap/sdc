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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceInstantiationType;
import org.onap.sdc.frontend.ci.tests.datatypes.LogicalOperator;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceDependencyProperty;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.CreateServiceFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateSubtitutionFilterFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadToscaTemplateFlow;
import org.onap.sdc.frontend.ci.tests.pages.ResourcePropertiesAssignmentPage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateServiceSubstitutionFilterTest extends SetupCDTest {
    private final String serviceName = "CreateServiceSubstitutionFilterTest";
    private final String stringValue = "Test";
    private final String booleanValue = "TRUE";
    private final LogicalOperator operator = LogicalOperator.EQUALS;
    private final List<ServiceDependencyProperty> substitutionFilterProperties = new ArrayList<ServiceDependencyProperty>();
    private final ServiceCreateData serviceCreateData = loadServiceCreateData();

    private WebDriver webDriver;

    @BeforeClass
    public void classSetup() {
        webDriver = DriverFactory.getDriver();
    }

    @Test
    public void createSubsitutionFilter() throws Exception {
       new CreateServiceFlow(webDriver, serviceCreateData).run(new HomePage(webDriver));

       ServiceComponentPage serviceComponentPage = new ServiceComponentPage(webDriver);
       serviceComponentPage.isLoaded();

       loadSubstitutionFilterProperties(serviceComponentPage);
       final CompositionPage compositionPage = serviceComponentPage.goToComposition();

       substitutionFilterProperties.forEach(substitutionFilterProperty -> {
           final CreateSubtitutionFilterFlow createSubtitutionFilterFlow = new CreateSubtitutionFilterFlow(webDriver, substitutionFilterProperty);
           createSubtitutionFilterFlow.run();
       });

       serviceComponentPage = compositionPage.goToServiceGeneral();
       serviceComponentPage.isLoaded();

       verifyToscaTemplateYaml(serviceComponentPage, false);
    }

    private ServiceCreateData loadServiceCreateData() {
        ServiceCreateData serviceCreateData = new ServiceCreateData();
        serviceCreateData.setRandomName(serviceName);
        serviceCreateData.setDescription(serviceName);
        serviceCreateData.setCategory("Network Service");
        serviceCreateData.setInstantiationType(ServiceInstantiationType.A_LA_CARTE);
        return serviceCreateData;
    }

    private void loadSubstitutionFilterProperties(final ServiceComponentPage serviceComponentPage) {
        final ResourcePropertiesAssignmentPage propertiesPage = serviceComponentPage.goToPropertiesAssignment();
        propertiesPage.isLoaded();
        final Map<String, String> propertyNamesAndTypes = propertiesPage.getPropertyNamesAndTypes();
        propertyNamesAndTypes.forEach((name, type)
                -> substitutionFilterProperties.add(new ServiceDependencyProperty(name, getPropertyValueByType(type), operator)));
    }

    private void verifyToscaTemplateYaml(final ServiceComponentPage serviceComponentPage, final boolean delete) throws Exception {
        final DownloadToscaTemplateFlow downloadCsarTemplateFlow = new DownloadToscaTemplateFlow(webDriver);
        final ToscaArtifactsPage toscaArtifactsPage = (ToscaArtifactsPage) downloadCsarTemplateFlow.run(serviceComponentPage).get();
        final Map<?, ?> yaml = FileHandling.parseYamlFile(getConfig().getDownloadAutomationFolder()
                .concat(java.io.File.separator).concat(toscaArtifactsPage.getDownloadedArtifactList().get(0)));
        verifyToscaTemplateHasSubstitutionFilter(yaml);
    }

    private void verifyToscaTemplateHasSubstitutionFilter(final Map<?, ?> yaml) {
        assertNotNull(yaml, "No contents in TOSCA Template");
        final List<?> substitutionFilters = (List<?>) getSubstitutionFilterFromYaml(yaml).get("properties");
        substitutionFilterProperties.forEach(substitutionFilterProperty -> {
            final Map<?, ?> substitutionFilter = (Map<?, ?>) substitutionFilters.stream()
                    .filter(subFilter -> ((Map<?, ?>) subFilter).containsKey(substitutionFilterProperty.getName())).findAny().get();
            assertTrue(substitutionFilter.containsKey(substitutionFilterProperty.getName()), "Added substitution filter not found in TOSCA Template");

            final Map<?, ?> substitutionFilterValue = (Map<?, ?>) ((List<?>) substitutionFilter.get(substitutionFilterProperty.getName())).get(0);
            assertTrue(substitutionFilterValue.containsValue(convertValue(substitutionFilterProperty.getValue()))
                    , "Invalid value for added substitution filters found in TOSCA Template");
            assertTrue(substitutionFilterValue.containsKey(substitutionFilterProperty.getLogicalOperator().getName())
                    , "Invalid logical operator for added substitution filters found in TOSCA Template");
        });
    }

    private Map<?,?> getSubstitutionFilterFromYaml(final Map<?,?> yaml) {
        final Map<?, ?> topology = (Map<?, ?>) yaml.get("topology_template");
        final Map<?, ?> substitutionMappings = (Map<?, ?>) topology.get("substitution_mappings");
        return (Map<?, ?>) substitutionMappings.get("substitution_filter");
    }

    private Object convertValue(String value) {
        return booleanValue.equals(value) ? Boolean.parseBoolean(value) : value;
    }

    private String getPropertyValueByType(String type) {
        return "string".equals(type) ? stringValue : booleanValue;
    }
}
