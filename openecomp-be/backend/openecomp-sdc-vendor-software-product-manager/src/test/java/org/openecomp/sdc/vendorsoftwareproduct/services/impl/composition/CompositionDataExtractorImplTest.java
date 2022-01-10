/*
 * Copyright © 2018 European Support Limited
 * Modifications © 2020 AT&T
 *
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
 */

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

public class CompositionDataExtractorImplTest {

    @InjectMocks
    private static CompositionDataExtractorImpl compositionDataExtractor;

    private ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
                                                    String globalServiceTemplatesPath,
                                                    String entryDefinitionServiceTemplate)
        throws IOException {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
        if (entryDefinitionServiceTemplate == null) {
            entryDefinitionServiceTemplate = "MainServiceTemplate.yaml";
        }

        loadServiceTemplates(serviceTemplatesPath, toscaExtensionYamlUtil, serviceTemplates);
        if (globalServiceTemplatesPath != null) {
            loadServiceTemplates(globalServiceTemplatesPath, toscaExtensionYamlUtil, serviceTemplates);
        }

        return new ToscaServiceModel(null, serviceTemplates, entryDefinitionServiceTemplate);
    }

    private void loadServiceTemplates(String serviceTemplatesPath,
                                      ToscaExtensionYamlUtil toscaExtensionYamlUtil,
                                      Map<String, ServiceTemplate> serviceTemplates)
        throws IOException {
        URL urlFile = CompositionDataExtractorImplTest.class.getResource(serviceTemplatesPath);
        if (urlFile != null) {
            File pathFile = new File(urlFile.getFile());
            Collection<File> files = FileUtils.listFiles(pathFile, null, true);
            if (files != null) {
                addServiceTemplateFiles(serviceTemplates, files, toscaExtensionYamlUtil);
            } else {
                throw new NotDirectoryException(serviceTemplatesPath);
            }
        } else {
            throw new NotDirectoryException(serviceTemplatesPath);
        }
    }

    private void addServiceTemplateFiles(Map<String, ServiceTemplate> serviceTemplates,
                                         Collection<File> files,
                                         ToscaExtensionYamlUtil toscaExtensionYamlUtil)
        throws IOException {
        for (File file : files) {
            try (InputStream yamlFile = new FileInputStream(file)) {
                ServiceTemplate serviceTemplateFromYaml =
                    toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
                serviceTemplates.put(ToscaUtil.getServiceTemplateFileName(serviceTemplateFromYaml),
                    serviceTemplateFromYaml);
            }
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExtractNetworks() throws Exception {
        ToscaServiceModel toscaServiceModel =
            loadToscaServiceModel("/extractServiceComposition/networks/",
                "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
        CompositionData compositionData =
            compositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
        assertEquals(0, compositionData.getComponents().size());
        assertEquals(7, compositionData.getNetworks().size());

        for (Network network : compositionData.getNetworks()) {
            boolean dhcp = network.isDhcp();
            switch (network.getName()) {
                case "contail-net-default-true-dhcp":
                    assertTrue(dhcp);
                    break;
                case "contail-net-dhcp-false-param":
                    assertFalse(dhcp);
                    break;
                case "contail-net-dhcp-false":
                    assertFalse(dhcp);
                    break;
                case "contail-net-dhcp-true-param":
                    assertTrue(dhcp);
                    break;
                case "contail-net-dhcp-true":
                    assertTrue(dhcp);
                    break;
                case "contail-net-dhcp-default-true-param":
                    assertTrue(dhcp);
                    break;
                case "neutron-net-default-dhcp":
                    assertTrue(dhcp);
                    break;
                default:
                    throw new Exception("Unexpected Network Name " + network.getName());
            }
        }
    }

    @Test
    public void testExtractOnlyComponents() throws Exception {
        ToscaServiceModel toscaServiceModel =
            loadToscaServiceModel("/extractServiceComposition/onlyComponents/",
                "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
        CompositionData compositionData =
            compositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
        assertEquals(3, compositionData.getComponents().size());
        assertEquals(0, compositionData.getNetworks().size());

        for (Component component : compositionData.getComponents()) {
            switch (component.getData().getName()) {
                case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
                    assertNull(component.getNics());
                    assertEquals("pcrf_psm", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
                    assertNull(component.getNics());
                    assertEquals("Server", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.pcm":
                    assertNull(component.getNics());
                    assertEquals("pcm", component.getData().getDisplayName());
                    break;
                default:
                    throw new Exception("Unexpected ComponentData Name " + component.getData().getName());
            }
        }
    }

    @Test
    public void testExtractComponentsWithPorts() throws Exception {

        ToscaServiceModel toscaServiceModel =
            loadToscaServiceModel("/extractServiceComposition/componentsWithPort/",
                "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
        CompositionData compositionData =
            compositionDataExtractor.extractServiceCompositionData(toscaServiceModel);

        assertEquals(3, compositionData.getComponents().size());
        assertEquals(0, compositionData.getNetworks().size());

        for (Component component : compositionData.getComponents()) {
            switch (component.getData().getName()) {
                case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
                    assertEquals(1, component.getNics().size());
                    assertEquals("psm01_port_0", component.getNics().get(0).getName());
                    assertNull(component.getNics().get(0).getNetworkName());
                    assertEquals("pcrf_psm", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
                    assertEquals(2, component.getNics().size());
                    assertEquals("template_VMInt_OAM_lb_2", component.getNics().get(0).getName());
                    assertNull(component.getNics().get(0).getNetworkName());
                    assertEquals("FSB1_Internal2", component.getNics().get(1).getName());
                    assertNull(component.getNics().get(1).getNetworkName());
                    assertEquals("Server", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.pcm":
                    assertEquals(2, component.getNics().size());
                    assertEquals("pcm", component.getData().getDisplayName());
                    break;
                default:
                    throw new Exception("Unexpected ComponentData Name " + component.getData().getName());
            }
        }
    }

    @Test
    public void testExtractFullComposition() throws Exception {

        ToscaServiceModel toscaServiceModel =
            loadToscaServiceModel("/extractServiceComposition/fullComposition/",
                "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
        CompositionData compositionData =
            compositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
        assertEquals(3, compositionData.getComponents().size());
        assertEquals(4, compositionData.getNetworks().size());

        for (Component component : compositionData.getComponents()) {
            switch (component.getData().getName()) {
                case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
                    assertEquals(1, component.getNics().size());
                    assertEquals("psm01_port_0", component.getNics().get(0).getName());
                    assertNull(component.getNics().get(0).getNetworkName());
                    assertEquals("pcrf_psm", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
                    assertEquals(4, component.getNics().size());
                    assertEquals("Server", component.getData().getDisplayName());
                    for (Nic port : component.getNics()) {
                        switch (port.getName()) {
                            case "FSB1_Internal2_port":
                                assertEquals("Internal2-net", port.getNetworkName());
                                break;
                            case "FSB1_OAM_Port":
                                assertNull(port.getNetworkName());
                                break;
                            case "FSB1_Internal1_port":
                                assertEquals("Internal1-net", port.getNetworkName());
                                break;
                            case "template_VMInt_OAM_lb_2":
                                assertEquals("jsa_net1", port.getNetworkName());
                                break;
                            default:
                                throw new Exception("Unexpected Nic " + port.getName());
                        }
                    }
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.pcm":
                    assertEquals(2, component.getNics().size());
                    assertEquals("pcm", component.getData().getDisplayName());
                    break;
                default:
                    throw new Exception("Unexpected ComponentData Name " + component.getData().getName());
            }
        }
    }

    @Test
    public void testExtractSubstitutionComposition() throws Exception {

        ToscaServiceModel toscaServiceModel =
            loadToscaServiceModel("/extractServiceComposition/substitution/",
                "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
        CompositionData compositionData =
            compositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
        assertEquals(2, compositionData.getComponents().size());
        assertEquals(4, compositionData.getNetworks().size());

        for (Component component : compositionData.getComponents()) {
            switch (component.getData().getName()) {
                case "org.openecomp.resource.vfc.nodes.heat.cmaui_image":
                    assertEquals(1, component.getNics().size());
                    assertEquals("cmaui_port_1", component.getNics().get(0).getName());
                    assertEquals("test_net1", component.getNics().get(0).getNetworkName());
                    assertEquals("cmaui_image", component.getData().getDisplayName());
                    break;
                case "org.openecomp.resource.vfc.nodes.heat.abc_image":
                    assertEquals(1, component.getNics().size());
                    assertEquals("abc_port_1", component.getNics().get(0).getName());
                    assertEquals("test_net2", component.getNics().get(0).getNetworkName());
                    assertEquals("abc_image", component.getData().getDisplayName());
                    break;
                default:
                    throw new Exception("Unexpected ComponentData Name " + component.getData().getName());
            }
        }
        for (Network network : compositionData.getNetworks()) {
            boolean dhcp = network.isDhcp();
            switch (network.getName()) {
                case "test_net2":
                    assertTrue(dhcp);
                    break;
                case "test_net1":
                    assertTrue(dhcp);
                    break;
                case "Internal1-net": // same network display twice since define in 2 nested files with the same key
                    assertTrue(dhcp);
                    break;
                default:
                    throw new Exception("Unexpected Network Name " + network.getName());
            }

        }
    }
}
