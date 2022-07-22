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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import com.aventstack.extentreports.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PackageTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.CategorySelect;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentProperty;
import org.onap.sdc.frontend.ci.tests.datatypes.ModelName;
import org.onap.sdc.frontend.ci.tests.datatypes.ServiceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.VspCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.VspOnboardingProcedure;
import org.onap.sdc.frontend.ci.tests.datatypes.composition.RelationshipInformation;
import org.onap.sdc.frontend.ci.tests.exception.UnzipException;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CheckComponentPropertiesFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateResourceFromVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateServiceFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVlmFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.ImportVspFlow;
import org.onap.sdc.frontend.ci.tests.flow.composition.CreateRelationshipFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.ServiceComponentPage;
import org.onap.sdc.frontend.ci.tests.pages.TopNavComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionDetailSideBarComponent.CompositionDetailTabName;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionRequirementsCapabilitiesTab;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class EtsiNetworkServiceUiTests extends SetupCDTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiNetworkServiceUiTests.class);

    private WebDriver webDriver;
    private ComponentInstance virtualLinkableVnf1;
    private ComponentInstance virtualLinkableVnf2;
    private ComponentInstance nsVirtualLink;

    @Test
    public void etsiNetworkServiceTest() throws UnzipException {
        webDriver = DriverFactory.getDriver();

        createVlm();
        final String resourceName = createVsp();
        ResourceCreatePage resourceCreatePage = importVsp(resourceName);
        resourceCreatePage = createAndCertifyVf(resourceName, resourceCreatePage);
        resourceCreatePage.isLoaded();
        final HomePage homePage = resourceCreatePage.goToHomePage();
        homePage.isLoaded();

        final ServiceCreateData serviceCreateData = createServiceFormData();
        final CreateServiceFlow createServiceFlow = createService(serviceCreateData);

        final CheckComponentPropertiesFlow checkComponentPropertiesFlow = checkServiceProperties();
        ComponentPage componentPage = checkComponentPropertiesFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ServiceComponentPage"));

        //adding node
        componentPage = addNodesAndCreateRelationships(resourceName, serviceCreateData, componentPage);

        final Map<String, Object> propertyMap = createPropertyToEditMap();

        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = downloadCsarArtifact(componentPage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));

        assertThat("No artifact download was found", toscaArtifactsPage.getDownloadedArtifactList(), not(empty()));

        final String downloadedCsarName = toscaArtifactsPage.getDownloadedArtifactList().get(0);
        propertyMap.entrySet().removeIf(e -> e.getValue() == null);
        checkEtsiNsPackage(createServiceFlow.getServiceCreateData().getName(), downloadedCsarName, propertyMap);
    }

    private ServiceComponentPage addNodesAndCreateRelationships(final String resourceName, final ServiceCreateData serviceCreateData,
                                                                final ComponentPage componentPage) {
        //add first VF node
        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(serviceCreateData.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.SERVICE);
        final ComponentData resourceToAdd = new ComponentData();
        resourceToAdd.setName(resourceName);
        resourceToAdd.setVersion("1.0");
        resourceToAdd.setComponentType(ComponentType.RESOURCE);
        CompositionPage compositionPage = componentPage.goToComposition();
        AddNodeToCompositionFlow addNodeToCompositionFlow = addNodeToComposition(parentComponent, resourceToAdd, compositionPage);
        virtualLinkableVnf1 = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));
        //add second VF node
        addNodeToCompositionFlow = addNodeToComposition(parentComponent, resourceToAdd, compositionPage);
        virtualLinkableVnf2 = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));
        //add NsVirtualLink node
        final ComponentData nsVirtualLinkToAdd = new ComponentData();
        nsVirtualLinkToAdd.setName("NsVirtualLink");
        nsVirtualLinkToAdd.setVersion("1.0");
        nsVirtualLinkToAdd.setComponentType(ComponentType.RESOURCE);
        addNodeToCompositionFlow = addNodeToComposition(parentComponent, nsVirtualLinkToAdd, compositionPage);
        nsVirtualLink = addNodeToCompositionFlow.getCreatedComponentInstance()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Could not get the created component instance"));

        //create a relationship from NsVirtualLink to first VF
        final String virtualLinkableCapability = "tosca.capabilities.nfv.VirtualLinkable";
        compositionPage = createRelationship(compositionPage, nsVirtualLink.getName(), virtualLinkableCapability,
            virtualLinkableVnf1.getName(), virtualLinkableCapability);
        CreateRelationshipFlow createRelationshipFlow;
        //create a relationship from NsVirtualLink to second VF
        final RelationshipInformation relationshipInfoVirtualLinkToVnf2 =
            new RelationshipInformation(nsVirtualLink.getName(), virtualLinkableCapability, virtualLinkableVnf2.getName(), virtualLinkableCapability);
        createRelationshipFlow = new CreateRelationshipFlow(webDriver, relationshipInfoVirtualLinkToVnf2);
        compositionPage = (CompositionPage) createRelationshipFlow.run(compositionPage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage instance"));
        final ServiceComponentPage serviceComponentPage = compositionPage.goToServiceGeneral();
        serviceComponentPage.isLoaded();
        return serviceComponentPage;
    }

    private ResourceCreatePage createAndCertifyVf(final String resourceName, final ResourceCreatePage resourceCreatePage) {
        final CreateResourceFromVspFlow createResourceFlow = new CreateResourceFromVspFlow(webDriver, resourceName);
        final ResourceCreatePage resourceCreatePage1 = createResourceFlow.run(resourceCreatePage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
        resourceCreatePage1.isLoaded();
        final CompositionPage compositionPage = resourceCreatePage1.goToComposition();
        compositionPage.isLoaded();
        //selecting node
        final String mgmtPortNodeName = "mgmt_port";
        compositionPage.selectNode(mgmtPortNodeName);
        final CompositionDetailSideBarComponent detailSideBar = compositionPage.getDetailSideBar();
        detailSideBar.isLoaded();
        //going to requirements and capabilities tab and externalizing requirement
        final CompositionRequirementsCapabilitiesTab compositionRequirementsCapabilitiesTab =
            (CompositionRequirementsCapabilitiesTab) detailSideBar.selectTab(CompositionDetailTabName.REQUIREMENTS_CAPABILITIES);
        compositionRequirementsCapabilitiesTab.isLoaded();
        ExtentTestActions.takeScreenshot(Status.INFO, "requirement-capabilities-tab-loaded", "Requirement and Capabilities tab is loaded");
        compositionRequirementsCapabilitiesTab.clickOnRequirements();
        final String externalVirtualLinkRequirement = "external_virtual_link";
        getExtendTest().log(Status.INFO,
            String.format("Externalizing requirement '%s' in node '%s'", externalVirtualLinkRequirement, mgmtPortNodeName));
        compositionRequirementsCapabilitiesTab.toggleRequirementAsExternal(externalVirtualLinkRequirement);
        ExtentTestActions.takeScreenshot(Status.INFO, "requirement-externalized",
            String.format("Requirement '%s' of node '%s' was externalized", externalVirtualLinkRequirement, mgmtPortNodeName));

        final ComponentPage componentPage = compositionPage.goToGeneral();
        componentPage.isLoaded();
        componentPage.certifyComponent();
        return resourceCreatePage1;
    }

    private CompositionPage createRelationship(final CompositionPage compositionPage, final String fromComponentInstanceName,
                                               final String fromCapability, final String toComponentInstanceName, final String toRequirement) {
        final RelationshipInformation relationshipInformation =
            new RelationshipInformation(fromComponentInstanceName, fromCapability, toComponentInstanceName, toRequirement);
        CreateRelationshipFlow createRelationshipFlow = new CreateRelationshipFlow(webDriver, relationshipInformation);
        return (CompositionPage) createRelationshipFlow.run(compositionPage)
            .orElseThrow(() -> new UiTestFlowRuntimeException("Expecting a CompositionPage instance"));
    }

    private ResourceCreatePage importVsp(final String resourceName) {
        final ImportVspFlow importVspFlow = new ImportVspFlow(webDriver, resourceName);
        return importVspFlow.run()
            .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected return ResourceCreatePage"));
    }

    private String createVsp() {
        final String resourceName = ElementFactory.addRandomSuffixToName(ElementFactory.getResourcePrefix());
        final String virtualLinkableVnf = "etsi-vnf-virtual-linkable.csar";
        final String rootFolder = org.onap.sdc.backend.ci.tests.utils.general.FileHandling.getPackageRepositoryPath(PackageTypeEnum.VNF);
        var vspCreateData = new VspCreateData();
        vspCreateData.setName(resourceName);
        vspCreateData.setCategory(CategorySelect.COMMON_NETWORK_RESOURCES);
        vspCreateData.setDescription("description");
        vspCreateData.setOnboardingProcedure(VspOnboardingProcedure.NETWORK_PACKAGE);
        final CreateVspFlow createVspFlow = new CreateVspFlow(webDriver, vspCreateData, virtualLinkableVnf, rootFolder);
        createVspFlow.run(new TopNavComponent(webDriver));
        return resourceName;
    }

    private void createVlm() {
        getExtendTest().log(Status.INFO, "Creating a VLM");
        final CreateVlmFlow createVlmFlow = new CreateVlmFlow(webDriver);
        createVlmFlow.run();
    }

    public AddNodeToCompositionFlow addNodeToComposition(final ComponentData parentComponent, final ComponentData resourceToAdd,
                                                         CompositionPage compositionPage) {

        final AddNodeToCompositionFlow addNodeToCompositionFlow = new AddNodeToCompositionFlow(webDriver, parentComponent, resourceToAdd);
        addNodeToCompositionFlow.run(compositionPage);
        return addNodeToCompositionFlow;
    }

    private CreateServiceFlow createService(final ServiceCreateData serviceCreateData) {
        final CreateServiceFlow createServiceFlow = new CreateServiceFlow(webDriver, serviceCreateData);
        createServiceFlow.run(new HomePage(webDriver));
        return createServiceFlow;
    }

    private CheckComponentPropertiesFlow checkServiceProperties() {
        final Set<ComponentProperty<?>> componentPropertySet = Set.of(
            new ComponentProperty<>("descriptor_id"),
            new ComponentProperty<>("designer"),
            new ComponentProperty<>("flavour_id"),
            new ComponentProperty<>("invariant_id"),
            new ComponentProperty<>("name"),
            new ComponentProperty<>("ns_profile"),
            new ComponentProperty<>("version"),
            new ComponentProperty<>("service_availability_level")
        );

        final var checkVfPropertiesFlow = new CheckComponentPropertiesFlow(componentPropertySet, webDriver);
        checkVfPropertiesFlow.run();
        return checkVfPropertiesFlow;
    }

    private DownloadCsarArtifactFlow downloadCsarArtifact(final ComponentPage componentPage) {
        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.run(componentPage);
        return downloadCsarArtifactFlow;
    }

    private Map<String, Object> createPropertyToEditMap() {
        final Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("descriptor_id", "{\"get_input\":\"descriptor_id\"}");
        propertyMap.put("designer", "{\"get_input\":\"designer\"}");
        propertyMap.put("flavour_id", "{\"get_input\":\"flavour_id\"}");
        propertyMap.put("invariant_id", "{\"get_input\":\"invariant_id\"}");
        propertyMap.put("name", "{\"get_input\":\"name\"}");
        propertyMap.put("service_availability_level", "{\"get_input\":\"service_availability_level\"}");
        propertyMap.put("version", "{\"get_input\":\"version\"}");
        //does not work yet with TOSCA complex types
        propertyMap.put("ns_profile", "{\"get_input\":\"ns_profile\"}");
        return propertyMap;
    }

    private ServiceCreateData createServiceFormData() {
        final ServiceCreateData serviceCreateData = new ServiceCreateData();
        serviceCreateData.setRandomName("EtsiNfvNetworkService");
        serviceCreateData.setModel(ModelName.DEFAULT_MODEL_NAME.getName());
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
                             final byte[] nsCsar) throws UnzipException {
        final Map<String, byte[]> csarFileMap = FileHandling.getFilesFromZip(nsCsar);

        final String mainDefinitionFile = String.format("Definitions/%s.yaml", expectedServiceName);
        final byte[] mainDefinitionFileBytes = csarFileMap.get(mainDefinitionFile);
        if (mainDefinitionFileBytes == null) {
            fail(String.format("Could not find the Main Definition file in '%s'", mainDefinitionFile));
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

        checkVirtualLinkableNode(mainDefinitionFile, virtualLinkableVnf1, nodeTemplatesTosca);
        checkVirtualLinkableNode(mainDefinitionFile, virtualLinkableVnf2, nodeTemplatesTosca);
        //checking tosca.nodes.nfv.NsVirtualLink node
        final Map<String, Object> nsVirtualLinkNode = getMapEntry(nodeTemplatesTosca, nsVirtualLink.getName());
        assertThat(String.format("'%s' should contain a '%s' entry in node_templates", mainDefinitionFile, nsVirtualLink.getName()),
            nsVirtualLinkNode, notNullValue());
        assertThat(String.format("Type from '%s' should be as expected", nsVirtualLink.getName()),
            nsVirtualLinkNode.get("type"), is("tosca.nodes.nfv.NsVirtualLink"));

        //checking the main service node
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
                Object actualPropertyValue = properties.get(expectedPropertyName);
                final String msg = String.format("The property '%s', in '%s' node template should have the expected value '%s'",
                    expectedPropertyName, expectedServiceNodeType, expectedPropertyValue);
                assertThat(msg, (new JSONObject()).toJSONString((Map) actualPropertyValue), is(expectedPropertyValue));
            }
        }
    }

    private void checkVirtualLinkableNode(final String mainDefinitionFileName, final ComponentInstance virtualLinkableVnf,
                                          final Map<String, Object> nodeTemplatesTosca) {
        final Map<String, Object> virtualLinkableVnfNode = getMapEntry(nodeTemplatesTosca, virtualLinkableVnf.getName());
        assertThat(String.format("'%s' should contain a '%s' entry in node_templates", mainDefinitionFileName, virtualLinkableVnf.getName()),
            virtualLinkableVnfNode, notNullValue());
        assertThat(String.format("Type from '%s' should be as expected", virtualLinkableVnf.getName()),
            virtualLinkableVnfNode.get("type"), is("org.openecomp.resource.EtsiVnfVirtualLinkable"));
        final Object requirementsObj = virtualLinkableVnfNode.get("requirements");
        assertThat(String.format("'%s' should contain a requirements entry in node_templates", virtualLinkableVnf.getName()),
            requirementsObj, notNullValue());
        if (!(requirementsObj instanceof List)) {
            fail(String.format("Requirements in '%s' is not a list", virtualLinkableVnf.getName()));
        }
        final var requirements = (List<Map<String, Object>>) requirementsObj;
        assertThat(String.format("'%s' should contain only one requirement", virtualLinkableVnf.getName()), requirements, hasSize(1));
        final Map<String, Object> externalVirtualLinkRequirement = getMapEntry(requirements.get(0), "external_virtual_link");
        assertThat(String.format("'%s' should contain the requirement 'external_virtual_link'", virtualLinkableVnf.getName()),
            externalVirtualLinkRequirement, notNullValue());
        assertThat(
            String.format("Requirement 'external_virtual_link' in '%s' should contain the capability 'virtual_linkable'",
                virtualLinkableVnf.getName()),
            externalVirtualLinkRequirement.get("capability"), is("virtual_linkable"));
        assertThat(
            String.format("Requirement 'external_virtual_link' in '%s' should relate to the node '%s'",
                virtualLinkableVnf.getName(), nsVirtualLink.getName()),
            externalVirtualLinkRequirement.get("node"), is(nsVirtualLink.getName()));
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

