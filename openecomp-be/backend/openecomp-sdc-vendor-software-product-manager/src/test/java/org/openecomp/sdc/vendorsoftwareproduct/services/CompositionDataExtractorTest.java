package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;

public class CompositionDataExtractorTest {


  private static ToscaServiceModel loadToscaServiceModel(String serviceTemplatesPath,
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

  private static void loadServiceTemplates(String serviceTemplatesPath,
                                           ToscaExtensionYamlUtil toscaExtensionYamlUtil,
                                           Map<String, ServiceTemplate> serviceTemplates)
      throws IOException {
    URL urlFile = CompositionDataExtractorTest.class.getResource(serviceTemplatesPath);
    if (urlFile != null) {
      File pathFile = new File(urlFile.getFile());
      File[] files = pathFile.listFiles();
      if (files != null) {
        addServiceTemplateFiles(serviceTemplates, files, toscaExtensionYamlUtil);
      } else {
        throw new NotDirectoryException(serviceTemplatesPath);
      }
    } else {
      throw new NotDirectoryException(serviceTemplatesPath);
    }
  }

  private static void addServiceTemplateFiles(Map<String, ServiceTemplate> serviceTemplates,
                                              File[] files,
                                              ToscaExtensionYamlUtil toscaExtensionYamlUtil)
      throws IOException {
    for (File file : files) {
      try (InputStream yamlFile = new FileInputStream(file)) {
        ServiceTemplate serviceTemplateFromYaml =
            toscaExtensionYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        serviceTemplates.put(file.getName(), serviceTemplateFromYaml);
        try {
          yamlFile.close();
        } catch (IOException ignore) {
        }
      } catch (FileNotFoundException e) {
        throw e;
      } catch (IOException e) {
        throw e;
      }
    }
  }

  @Test
  public void testExtractNetworks() throws Exception {
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/extractServiceComposition/networks/",
            "/extractServiceComposition/toscaGlobalServiceTemplates/", null);
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData compositionData =
        CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
    Assert.assertEquals(compositionData.getComponents().size(), 0);
    Assert.assertEquals(compositionData.getNetworks().size(), 7);

    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network network : compositionData.getNetworks()) {
      boolean dhcp = network.isDhcp();
      switch (network.getName()) {
        case "contail-net-default-true-dhcp":
          Assert.assertEquals(dhcp, true);
          break;
        case "contail-net-dhcp-false-param":
          Assert.assertEquals(dhcp, false);
          break;
        case "contail-net-dhcp-false":
          Assert.assertEquals(dhcp, false);
          break;
        case "contail-net-dhcp-true-param":
          Assert.assertEquals(dhcp, true);
          break;
        case "contail-net-dhcp-true":
          Assert.assertEquals(dhcp, true);
          break;
        case "contail-net-dhcp-default-true-param":
          Assert.assertEquals(dhcp, true);
          break;
        case "neutron-net-default-dhcp":
          Assert.assertEquals(dhcp, true);
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
            "/extractServiceComposition/toscaGlobalServiceTemplates/", "OnlyComponentsST.yaml");
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData compositionData =
        CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
    Assert.assertEquals(compositionData.getComponents().size(), 3);
    Assert.assertEquals(compositionData.getNetworks().size(), 0);

    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component component : compositionData.getComponents()) {
      switch (component.getData().getName()) {
        case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
          Assert.assertNull(component.getNics());
          Assert.assertEquals(component.getData().getDisplayName(), "pcrf_psm");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
          Assert.assertNull(component.getNics());
          Assert.assertEquals(component.getData().getDisplayName(), "Server");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.pcm":
          Assert.assertNull(component.getNics());
          Assert.assertEquals(component.getData().getDisplayName(), "pcm");
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
            "/extractServiceComposition/toscaGlobalServiceTemplates/", "ComponentsWithPortST.yaml");
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData compositionData =
        CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel);

    Assert.assertEquals(compositionData.getComponents().size(), 3);
    Assert.assertEquals(compositionData.getNetworks().size(), 0);

    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component component : compositionData.getComponents()) {
      switch (component.getData().getName()) {
        case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
          Assert.assertEquals(component.getNics().size(), 1);
          Assert.assertEquals(component.getNics().get(0).getName(), "psm01_port_0");
          Assert.assertNull(component.getNics().get(0).getNetworkName());
          Assert.assertEquals(component.getData().getDisplayName(), "pcrf_psm");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
          Assert.assertEquals(component.getNics().size(), 1);
          Assert.assertEquals(component.getNics().get(0).getName(), "FSB1_Internal2");
          Assert.assertNull(component.getNics().get(0).getNetworkName());
          Assert.assertEquals(component.getData().getDisplayName(), "Server");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.pcm":
          Assert.assertEquals(component.getNics().size(), 2);
          Assert.assertEquals(component.getData().getDisplayName(), "pcm");
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
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData compositionData =
        CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
    Assert.assertEquals(compositionData.getComponents().size(), 3);
    Assert.assertEquals(compositionData.getNetworks().size(), 3);

    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component component : compositionData.getComponents()) {
      switch (component.getData().getName()) {
        case "org.openecomp.resource.vfc.nodes.heat.pcrf_psm":
          Assert.assertEquals(component.getNics().size(), 1);
          Assert.assertEquals(component.getNics().get(0).getName(), "psm01_port_0");
          Assert.assertNull(component.getNics().get(0).getNetworkName());
          Assert.assertEquals(component.getData().getDisplayName(), "pcrf_psm");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.nova.Server":
          Assert.assertEquals(component.getNics().size(), 3);
          Assert.assertEquals(component.getData().getDisplayName(), "Server");
          for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic port : component.getNics()) {
            switch (port.getName()) {
              case "FSB1_Internal2_port":
                Assert.assertEquals(port.getNetworkName(), "Internal2-net");
                break;
              case "FSB1_OAM_Port":
                Assert.assertNull(port.getNetworkName());
                break;
              case "FSB1_Internal1_port":
                Assert.assertEquals(port.getNetworkName(), "Internal1-net");
                break;
              default:
                throw new Exception("Unexpected Nic " + port.getName());
            }
          }
          break;
        case "org.openecomp.resource.vfc.nodes.heat.pcm":
          Assert.assertEquals(component.getNics().size(), 2);
          Assert.assertEquals(component.getData().getDisplayName(), "pcm");
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
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData compositionData =
        CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel);
    Assert.assertEquals(compositionData.getComponents().size(), 2);
    Assert.assertEquals(compositionData.getNetworks().size(), 4);

    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component component : compositionData.getComponents()) {
      switch (component.getData().getName()) {
        case "org.openecomp.resource.vfc.nodes.heat.cmaui_image":
          Assert.assertEquals(component.getNics().size(), 1);
          Assert.assertEquals(component.getNics().get(0).getName(), "cmaui_port_1");
          Assert.assertEquals(component.getNics().get(0).getNetworkName(), "test_net1");
          Assert.assertEquals(component.getData().getDisplayName(), "cmaui_image");
          break;
        case "org.openecomp.resource.vfc.nodes.heat.abc_image":
          Assert.assertEquals(component.getNics().size(), 1);
          Assert.assertEquals(component.getNics().get(0).getName(), "abc_port_1");
          Assert.assertEquals(component.getNics().get(0).getNetworkName(), "test_net2");
          Assert.assertEquals(component.getData().getDisplayName(), "abc_image");
          break;
        default:
          throw new Exception("Unexpected ComponentData Name " + component.getData().getName());
      }
    }
    for (org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network network : compositionData.getNetworks()) {
      boolean dhcp = network.isDhcp();
      switch (network.getName()) {
        case "test_net2":
          Assert.assertEquals(dhcp, true);
          break;
        case "test_net1":
          Assert.assertEquals(dhcp, true);
          break;
        case "Internal1-net": // same network display twice since define in 2 nested files with the same key
          Assert.assertEquals(dhcp, true);
          break;
        default:
          throw new Exception("Unexpected Network Name " + network.getName());
      }

    }
  }


}