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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.CheckEtsiNsPropertiesFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateServiceFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.EditServicePropertiesFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class EtsiNetworkServiceUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiNetworkServiceUiTests.class);

    private WebDriver webDriver;

    @Test
    public void createEtsiNetworkService() throws UnzipException {
        webDriver = DriverFactory.getDriver();

        final CreateServiceFlow createServiceFlow = createService();
        final CheckEtsiNsPropertiesFlow checkEtsiNsPropertiesFlow = checkServiceProperties();
        final ServiceComponentPage serviceComponentPage = checkEtsiNsPropertiesFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ServiceComponentPage"));

        final Map<String, Object> propertyMap = createPropertyToEditMap();
        editProperties(serviceComponentPage, propertyMap);

        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(serviceComponentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));

        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));

        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        propertyMap.entrySet().removeIf(e -> e.getValue() == null);
        checkEtsiNsPackage(createServiceFlow.getServiceCreateData().getName(), downloadedCsarName, propertyMap);
    }

    private CreateServiceFlow createService() {
        final ServiceCreateData serviceCreateData = createServiceFormData();
        final CreateServiceFlow createServiceFlow = new CreateServiceFlow(webDriver, serviceCreateData);
        final TopNavComponent topNavComponent = new TopNavComponent(webDriver);
        createServiceFlow.run(new HomePage(webDriver, topNavComponent));
        return createServiceFlow;
    }

    private CheckEtsiNsPropertiesFlow checkServiceProperties() {
        final CheckEtsiNsPropertiesFlow checkEtsiNsPropertiesFlow = new CheckEtsiNsPropertiesFlow(webDriver);
        checkEtsiNsPropertiesFlow.run();
        return checkEtsiNsPropertiesFlow;
    }

    private void editProperties(final ServiceComponentPage serviceComponentPage, final Map<String, Object> propertyMap) {
        final EditServicePropertiesFlow editServicePropertiesFlow = new EditServicePropertiesFlow(webDriver, propertyMap);
        editServicePropertiesFlow.run(serviceComponentPage);
    }

    private DownloadCsarArtifactFlow downloadCsarArtifact(final ServiceComponentPage serviceComponentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.run(serviceComponentPage);
        return downloadCsarArtifactFlow;
    }

    private Map<String, Object> createPropertyToEditMap() {
        final Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("designer", "designer1");
        propertyMap.put("descriptor_id", "descriptor_id1");
        propertyMap.put("flavour_id", "flavour_id1");
        propertyMap.put("invariant_id", "invariant_id1");
        propertyMap.put("name", "name1");
        propertyMap.put("version", "version1");
        propertyMap.put("service_availability_level", 1);
        //does not work yet with TOSCA complex types
        propertyMap.put("ns_profile", null);
        return propertyMap;
    }

    private ServiceCreateData createServiceFormData() {
        final ServiceCreateData serviceCreateData = new ServiceCreateData();
        serviceCreateData.setRandomName("EtsiNfvNetworkService");
        serviceCreateData.setCategory(ServiceCategoriesEnum.ETSI_NFV_NETWORK_SERVICE.getValue());
        serviceCreateData.setEtsiVersion("2.5.1");
        serviceCreateData.setDescription("aDescription");
        return serviceCreateData;
    }

    private void checkEtsiNsPackage(final String serviceName, final String downloadedCsarName,
                                    final Map<String, Object> expectedPropertyMap) throws UnzipException {
        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> filesFromZip = FileHandling.getFilesFromZip(downloadFolderPath, downloadedCsarName);
        final Optional<String> etsiPackageEntryOpt =
            filesFromZip.keySet().stream().filter(s -> s.startsWith("Artifacts/ETSI_PACKAGE")).findFirst();
        if (etsiPackageEntryOpt.isEmpty()) {
            Assertions.fail("Could not find the NSD package in Artifacts/ETSI_PACKAGE");
        }
        final String nodeType = String.format("org.openecomp.service.%s",
            serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1).toLowerCase());
        final String etsiPackageEntry = etsiPackageEntryOpt.get();
        final String nsdPackageBaseName = FilenameUtils.getBaseName(etsiPackageEntry);
        final String nsdCsarFile = nsdPackageBaseName + ".csar";
        final byte[] etsiPackageBytes = filesFromZip.get(etsiPackageEntry);
        if (etsiPackageEntry.endsWith(".zip")) {
            final Map<String, byte[]> nsPackageFileMap = FileHandling.getFilesFromZip(etsiPackageBytes);
            assertThat("Expecting 3 files inside the NSD CSAR, the CSAR itself and its signature and certificate",
                nsPackageFileMap.size(), is(3));
            assertThat("Expecting the NSD CSAR file " + nsdCsarFile, nsPackageFileMap, hasKey(nsdCsarFile));
            final String nsdCsarSignature = nsdPackageBaseName + ".cms";
            assertThat("Expecting the NSD CSAR signature " + nsdCsarSignature, nsPackageFileMap, hasKey(nsdCsarSignature));
            final String nsdCertificate = nsdPackageBaseName + ".cert";
            assertThat("Expecting the NSD CSAR certificate " + nsdCertificate, nsPackageFileMap, hasKey(nsdCertificate));
            checkNsCsar(nsdPackageBaseName, nodeType, expectedPropertyMap, nsPackageFileMap.get(nsdCsarFile));
            return;
        }
        if (etsiPackageEntry.endsWith(".csar")) {
            final Map<String, byte[]> nsPackageFileMap = FileHandling.getFilesFromZip(etsiPackageBytes);
            checkNsCsar(nsdPackageBaseName, nodeType, expectedPropertyMap, nsPackageFileMap.get(nsdCsarFile));
            return;
        }
        fail(String.format("Unexpected ETSI NS PACKAGE entry '%s'. Expecting a '.csar' or '.zip'", etsiPackageEntry));
    }

    private void checkNsCsar(final String expectedServiceName, final String expectedServiceNodeType, final Map<String, Object> expectedPropertiesMap,
                             final byte[] nsCsar) {
        try {
            final Map<String, byte[]> csarFileMap = FileHandling.getFilesFromZip(nsCsar);
            final String mainDefinitionFile = String.format("Definitions/%s.yaml", expectedServiceName);
            final byte[] mainDefinitionFileBytes = csarFileMap.get(mainDefinitionFile);
            if (mainDefinitionFileBytes == null) {
                Assertions.fail(String.format("Could not find the Main Definition file in '%s'", mainDefinitionFile));
            }

            final Map<String, Object> mainDefinitionYamlMap = loadYamlObject(mainDefinitionFileBytes);
            final Map<String, Object> topologyTemplateTosca = getMapEntry(mainDefinitionYamlMap, "topology_template");
            assertThat(String.format("'%s' should contain a topology_template entry", mainDefinitionFile), topologyTemplateTosca, notNullValue());
            final Map<String, Object> substitutionMappingsTosca = getMapEntry(topologyTemplateTosca, "substitution_mappings");
            assertThat(String.format("'%s' should contain a substitution_mappings entry", mainDefinitionFile), substitutionMappingsTosca, notNullValue());
            final String nodeType = (String) substitutionMappingsTosca.get("node_type");
            assertThat("substitution_mappings->node_type should be as expected", nodeType, is(expectedServiceNodeType));

            final Map<String, Object> nodeTemplatesTosca = getMapEntry(topologyTemplateTosca, "node_templates");
            assertThat(String.format("'%s' should contain a node_templates entry", mainDefinitionFile), nodeTemplatesTosca, notNullValue());
            final Map<String, Object> serviceNodeTemplate = getMapEntry(nodeTemplatesTosca, expectedServiceNodeType);
            assertThat(String.format("'%s' should contain a '%s' entry in node_templates", mainDefinitionFile, expectedServiceNodeType),
                serviceNodeTemplate, notNullValue());
            final Map<String, Object> properties = getMapEntry(serviceNodeTemplate, "properties");
            assertThat(String.format("'%s' node template in '%s' should contain a properties entry", expectedServiceNodeType, mainDefinitionFile),
                properties, notNullValue());
            assertThat(String.format("'%s' node template should contain '%s' properties", expectedServiceNodeType, expectedPropertiesMap.size()),
                properties.size(), is(expectedPropertiesMap.size()));
            for (final Entry<String, Object> expectedPropertyEntry : expectedPropertiesMap.entrySet()) {
                final String expectedPropertyName = expectedPropertyEntry.getKey();
                assertThat(String.format("'%s' node template should contain the property '%s'", expectedServiceNodeType, expectedPropertyName),
                    properties, hasKey(expectedPropertyName));
                final Object expectedPropertyValue = expectedPropertyEntry.getValue();
                if (expectedPropertyValue != null) {
                    final Object actualPropertyValue = properties.get(expectedPropertyName);
                    final String msg = String.format("The property '%s', in '%s' node template should have the expected value '%s'",
                        expectedPropertyName, expectedServiceNodeType, actualPropertyValue);
                    assertThat(msg, actualPropertyValue, is(expectedPropertyValue));
                }
            }

        } catch (final UnzipException e) {
            final String errorMsg = "Could not unzip Network Service CSAR.";
            LOGGER.info(errorMsg, e);
            fail(String.format("%s Error: %s", errorMsg, e.getMessage()));
        }
    }

    private Map<String, Object> getMapEntry(final Map<String, Object> yamlObj, final String entryName) {
        try {
            return (Map<String, Object>) yamlObj.get(entryName);
        } catch (final Exception e) {
            final String errorMsg = String.format("Could not get the '%s' entry.", entryName);
            LOGGER.error(errorMsg, e);
            fail(errorMsg + "Error message: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> loadYamlObject(final byte[] mainDefinitionFileBytes) {
        return new Yaml().load(new String(mainDefinitionFileBytes));
    }

}

