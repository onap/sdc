package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.ManualVspDataCollectionService;

public class ManualVspToscaManagerImplTest {

  private static final String USER = "manualVspToscaTestUser";
  private static final String INVALID_VSP_ID = "Invalid_Vsp_Id";
  private static final String VSP_ID = "Vsp_Id_1";
  private static final String VSP_VERSION = "1.0";

  private static final String RELEASE_VENDOR = "Vendor-1";
  private static final String COMPONENT_ID = "Component_id";
  private static final String COMPONENT_NAME = "Component_name";
  private static final String SP_PART_NUMBER_1 = "Part_number_123";
  private static final String FEATURE_GROUP_ID_1 = "Feature_Group_id_1";
  private static final String MANUFACTURER_REF_1 = "Manufacturer_Ref_1";
  private static final String VENDOR_MODEL_1 = "Deployment_Flavor_Model_1";
  private static final int NUM_CPUS_1 = 1;
  private static final String DISK_SIZE_1 = "2 GB";
  private static final String MEM_SIZE_1 = "8 GB";

  private static final String SP_PART_NUMBER_2 = "Part_number_345";
  private static final String FEATURE_GROUP_ID_2 = "Feature_Group_id_2";
  private static final String MANUFACTURER_REF_2 = "Manufacturer_Ref_2";
  private static final String VENDOR_MODEL_2 = "Deployment_Flavor_Model_2";
  private static final int NUM_CPUS_2 = 4;
  private static final String DISK_SIZE_2 = "3 GB";
  private static final String MEM_SIZE_2 = "2 GB";

  private static final String IMAGE_VERSION_1 = "3.16.1";
  private static final String IMAGE_HASH_1 = "65edfgye3256hjutve";
  private static final String IMAGE_FILE_NAME_1 = "image-file-name1";
  private static final String IMAGE_VERSION_2 = "3.1.9";
  private static final String IMAGE_HASH_2 = "84rtedfe3256hjutaw";
  private static final String IMAGE_FILE_NAME_2 = "image-file-name1";

  private ManualVspToscaManager manualVspToscaManager = new ManualVspToscaManagerImpl();

  @Spy
  @InjectMocks
  private ManualVspToscaManagerImpl manualVspToscaManagerMock;

  @Mock
  private ManualVspDataCollectionService manualVspDataCollectionServiceMock;
  /*

  @Test
  public void testGatherVspInformationInvalidVsp() {
    MockitoAnnotations.initMocks(this);
    VspModelInfo expectedVspData = new VspModelInfo();
    doThrow(new RuntimeException())
        .when(manualVspDataCollectionServiceMock)
        .getReleaseVendor(INVALID_VSP_ID, Version.valueOf(VSP_VERSION));
    doThrow(new RuntimeException())
        .when(manualVspDataCollectionServiceMock)
        .getAllowedFlavors(INVALID_VSP_ID, Version.valueOf(VSP_VERSION));
    doThrow(new RuntimeException())
        .when(manualVspDataCollectionServiceMock)
        .getVspComponentImages(INVALID_VSP_ID, Version.valueOf(VSP_VERSION));
    doThrow(new RuntimeException())
        .when(manualVspDataCollectionServiceMock)
        .getVspComponents(INVALID_VSP_ID, Version.valueOf(VSP_VERSION));
    doThrow(new RuntimeException())
        .when(manualVspDataCollectionServiceMock)
        .getVspComponentNics(INVALID_VSP_ID, Version.valueOf(VSP_VERSION));
    VspModelInfo vspModelInfo = manualVspToscaManagerMock.gatherVspInformation(INVALID_VSP_ID,
        Version.valueOf(VSP_VERSION));
    Assert.assertEquals(expectedVspData, vspModelInfo);
  }


  @Test
  public void testGatherVspInformationValidVsp() {
    MockitoAnnotations.initMocks(this);
    Map<String, DeploymentFlavorModel> deploymentFlavorData = getDeploymentFlavorData();
    Map<String, List<Nic>> componentNics = getComponentNics();
    Map<String, String> componentData = getComponentData();
    Map<String, List<MultiFlavorVfcImage>> vfcImageData = getVfcImageData();
    doReturn(Optional.of(RELEASE_VENDOR)).when(manualVspDataCollectionServiceMock)
        .getReleaseVendor(VSP_ID, Version.valueOf(VSP_VERSION));
    doReturn(deploymentFlavorData).when(manualVspDataCollectionServiceMock)
        .getAllowedFlavors(VSP_ID, Version.valueOf(VSP_VERSION));
    doReturn(vfcImageData).when(manualVspDataCollectionServiceMock)
        .getVspComponentImages(VSP_ID, Version.valueOf(VSP_VERSION));
    doReturn(componentData).when(manualVspDataCollectionServiceMock)
        .getVspComponents(VSP_ID, Version.valueOf(VSP_VERSION));
    doReturn(componentNics).when(manualVspDataCollectionServiceMock)
        .getVspComponentNics(VSP_ID, Version.valueOf(VSP_VERSION));
    VspModelInfo vspModelInfo = manualVspToscaManagerMock.gatherVspInformation(VSP_ID,
        Version.valueOf(VSP_VERSION));

    VspModelInfo expectedVspData = new VspModelInfo();
    expectedVspData.setReleaseVendor(RELEASE_VENDOR);
    expectedVspData.setComponents(getComponentData());
    expectedVspData.setMultiFlavorVfcImages(getVfcImageData());
    expectedVspData.setAllowedFlavors(getDeploymentFlavorData());
    expectedVspData.setNics(getComponentNics());

    Assert.assertEquals(expectedVspData, vspModelInfo);
  }

  @Test
  public void testGenerateToscaInvalidVspId() {
    VspModelInfo emptyVspCollectedData = new VspModelInfo();
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(emptyVspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    //Service model should contain only the packed global types
    Assert.assertEquals(19, toscaServiceModel.getServiceTemplates().size());
  }

  @Test
  public void testGenerateToscaNoComponent() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setReleaseVendor(RELEASE_VENDOR);
    vspCollectedData.setComponents(null);
    vspCollectedData.setMultiFlavorVfcImages(null);
    vspCollectedData.setAllowedFlavors(getDeploymentFlavorData());
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    //Service model should contain only the packed global types
    Assert.assertEquals(19, toscaServiceModel.getServiceTemplates().size());
  }

  @Test
  public void testGenerateToscaReleaseVendorNotPresent() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(getVfcImageData());
    vspCollectedData.setAllowedFlavors(getDeploymentFlavorData());
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String entryDefinitionServiceTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate mainServiceTemplate = serviceTemplates.get(entryDefinitionServiceTemplate);
    Assert.assertNotNull(mainServiceTemplate);
    Assert.assertNull(mainServiceTemplate.getMetadata().get("releaseVendor"));
  }

  @Test
  public void testGenerateToscaNoImages() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(null);
    vspCollectedData.setAllowedFlavors(getDeploymentFlavorData());
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String entryDefinitionServiceTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate mainServiceTemplate = serviceTemplates.get(entryDefinitionServiceTemplate);
    Assert.assertNotNull(mainServiceTemplate);
    String componentName = vspCollectedData.getComponents().get(COMPONENT_ID);
    Assert.assertNull(mainServiceTemplate.getTopology_template().getNode_templates()
        .get(componentName + GeneratorConstants.VNF_NODE_TEMPLATE_ID_SUFFIX)
        .getProperties().get(IMAGES_PROPERTY));
  }

  @Test
  public void testGenerateToscaNoPorts() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(getVfcImageData());
    vspCollectedData.setAllowedFlavors(getDeploymentFlavorData());
    vspCollectedData.setNics(null);
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String componentName = vspCollectedData.getComponents().get(COMPONENT_ID);
    String substitutionServiceTemplateFileName =
        componentName + GeneratorConstants.TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX;
    ServiceTemplate substitutionServiceTemplate =
        serviceTemplates.get(substitutionServiceTemplateFileName);
    Assert.assertNotNull(substitutionServiceTemplate);
    //Only component node template should be present since there are no ports
    Assert.assertEquals(1, substitutionServiceTemplate.getTopology_template().getNode_templates()
        .size());
  }

  @Test
  public void testGenerateToscaNoManufacturerRefNumAndFeatureGroup() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setReleaseVendor(RELEASE_VENDOR);
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(getVfcImageData());
    Map<String, DeploymentFlavorModel> deploymentFlavorData = getDeploymentFlavorData();
    deploymentFlavorData.get(SP_PART_NUMBER_1).getVendor_info()
        .setManufacturer_reference_number(null);
    deploymentFlavorData.get(SP_PART_NUMBER_1).getLicense_flavor().setFeature_group_uuid(null);
    vspCollectedData.setAllowedFlavors(deploymentFlavorData);
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String entryDefinitionServiceTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate mainServiceTemplate = serviceTemplates.get(entryDefinitionServiceTemplate);
    Assert.assertNotNull(mainServiceTemplate);
    String componentName = vspCollectedData.getComponents().get(COMPONENT_ID);
    Map<String, DeploymentFlavorModel> actualDeploymentFlavor = (Map<String, DeploymentFlavorModel>)
        mainServiceTemplate.getTopology_template().getNode_templates()
        .get(componentName + GeneratorConstants.VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX)
        .getProperties().get(ALLOWED_FLAVORS_PROPERTY);
    Assert.assertNull(actualDeploymentFlavor.get(SP_PART_NUMBER_1).getVendor_info()
        .getManufacturer_reference_number());
    Assert.assertNull(actualDeploymentFlavor.get(SP_PART_NUMBER_1).getLicense_flavor()
        .getFeature_group_uuid());
  }

  @Test
  public void testGenerateToscaNoDeploymentFlavor() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setReleaseVendor(RELEASE_VENDOR);
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(getVfcImageData());
    vspCollectedData.setAllowedFlavors(null);
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String entryDefinitionServiceTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate mainServiceTemplate = serviceTemplates.get(entryDefinitionServiceTemplate);
    Assert.assertNotNull(mainServiceTemplate);
    String componentName = vspCollectedData.getComponents().get(COMPONENT_ID);
    Assert.assertNull(mainServiceTemplate.getTopology_template().getNode_templates()
        .get(componentName + GeneratorConstants.VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX)
        .getProperties());
  }

  @Test
  public void testGenerateToscaCompleteData() {
    VspModelInfo vspCollectedData = new VspModelInfo();
    vspCollectedData.setReleaseVendor(RELEASE_VENDOR);
    vspCollectedData.setComponents(getComponentData());
    vspCollectedData.setMultiFlavorVfcImages(getVfcImageData());
    vspCollectedData.setAllowedFlavors(getDeploymentFlavorData());
    vspCollectedData.setNics(getComponentNics());
    ToscaServiceModel toscaServiceModel =
        manualVspToscaManager.generateToscaModel(vspCollectedData);
    Assert.assertNotNull(toscaServiceModel);
    Assert.assertNotNull(toscaServiceModel.getServiceTemplates());
    Assert.assertEquals(22, toscaServiceModel.getServiceTemplates().size());
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    String entryDefinitionServiceTemplate = toscaServiceModel.getEntryDefinitionServiceTemplate();
    ServiceTemplate mainServiceTemplate = serviceTemplates.get(entryDefinitionServiceTemplate);
    Assert.assertNotNull(mainServiceTemplate);
    String componentName = vspCollectedData.getComponents().get(COMPONENT_ID);
    Assert.assertNotNull(mainServiceTemplate.getTopology_template().getNode_templates()
        .get(componentName + GeneratorConstants.VNF_NODE_TEMPLATE_ID_SUFFIX));
    //Validate vnf configuration node template
    validateVnfConfigurationNodeTemplate(mainServiceTemplate, componentName);
    //Validate vnf node template
    validateVnfNodeTemplate(mainServiceTemplate, componentName);
    //Validate substitution service template
    ServiceTemplate substitutionServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(componentName + GeneratorConstants.TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX);
    List<Nic> nics = vspCollectedData.getNics().get(COMPONENT_ID);
    validateSubstitutionServiceTemplate(substitutionServiceTemplate, nics, componentName);
    //Validate global substitution service template
    ServiceTemplate globalSubstitutionServiceTemplate = toscaServiceModel.getServiceTemplates()
        .get(ToscaUtil.getServiceTemplateFileName(GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME));
    validateGlobalSubstitutionServiceTemplate(globalSubstitutionServiceTemplate, nics,
        componentName);
  }

  private void validateVnfConfigurationNodeTemplate(ServiceTemplate mainServiceTemplate,
                                                    String componentName) {
    NodeTemplate vnfConfigNodeTemplate =
        mainServiceTemplate.getTopology_template().getNode_templates()
            .get(componentName + GeneratorConstants.VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX);
    Assert.assertNotNull(vnfConfigNodeTemplate);
    Assert.assertEquals(ToscaNodeType.VNF_CONFIG_NODE_TYPE, vnfConfigNodeTemplate.getType());
    Map<String, DeploymentFlavorModel> allowedFlavors = (Map<String, DeploymentFlavorModel>)
        vnfConfigNodeTemplate.getProperties().get(GeneratorConstants.ALLOWED_FLAVORS_PROPERTY);
    Map<String, DeploymentFlavorModel> deploymentFlavorData = getDeploymentFlavorData();
    Assert.assertNotNull(allowedFlavors);
    Assert.assertEquals(deploymentFlavorData.size(), allowedFlavors.size());
    Assert.assertEquals(deploymentFlavorData, allowedFlavors);
  }

  private void validateImagePropertyData(NodeTemplate vnfNodeTemplate, String componentName) {
    Map<String, MultiFlavorVfcImage> vfcImages = (Map<String, MultiFlavorVfcImage>)
        vnfNodeTemplate.getProperties().get(GeneratorConstants.IMAGES_PROPERTY);
    Assert.assertNotNull(vfcImages);
    Assert.assertEquals(2, vfcImages.size());
    MultiFlavorVfcImage image1 = vfcImages.get(IMAGE_VERSION_1);
    MultiFlavorVfcImage expectedImage1 = getImageData(IMAGE_VERSION_1, IMAGE_HASH_1,
        IMAGE_FILE_NAME_1, "md5");
    Assert.assertEquals(expectedImage1, image1);
    MultiFlavorVfcImage image2 = vfcImages.get(IMAGE_VERSION_2);
    MultiFlavorVfcImage expectedImage2 = getImageData(IMAGE_VERSION_2, IMAGE_HASH_2,
        IMAGE_FILE_NAME_2, "md5");
    Assert.assertEquals(expectedImage2, image2);
  }

  private void validateVnfNodeTemplate(ServiceTemplate mainServiceTemplate,
                                       String componentName) {
    NodeTemplate vnfNodeTemplate =
        mainServiceTemplate.getTopology_template().getNode_templates()
            .get(componentName + GeneratorConstants.VNF_NODE_TEMPLATE_ID_SUFFIX);
    Assert.assertNotNull(vnfNodeTemplate);
    Assert.assertEquals(ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE, vnfNodeTemplate.getType());
    Assert.assertNotNull(vnfNodeTemplate.getDirectives());
    Assert.assertEquals(true, vnfNodeTemplate.getDirectives().contains(ToscaConstants
        .NODE_TEMPLATE_DIRECTIVE_SUBSTITUTABLE));
    validateImagePropertyData(vnfNodeTemplate, componentName);
    Map<String, Object> serviceTemplateFilterProperty = (Map<String, Object>) vnfNodeTemplate
        .getProperties().get(SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);
    Assert.assertNotNull(serviceTemplateFilterProperty);
    String substituteServiceTemplate = serviceTemplateFilterProperty
            .get(SUBSTITUTE_SERVICE_TEMPLATE_PROPERTY_NAME).toString();
    String expectedServiceTemplateName = componentName + GeneratorConstants
        .TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX;
    Assert.assertEquals(expectedServiceTemplateName, substituteServiceTemplate);
    int count = (int) serviceTemplateFilterProperty.get(COUNT_PROPERTY_NAME);
    Assert.assertEquals(1, count);
  }

  private void validateSubstitutionServiceTemplate(ServiceTemplate substitutionServiceTemplate,
                                                   List<Nic> nics,
                                                   String componentName) {
    Assert.assertNotNull(substitutionServiceTemplate);
    int expectedNumberOfNodeTemplates = nics.size() + 1; //1 component node template
    Map<String, NodeTemplate> substitutionNodeTemplates =
        substitutionServiceTemplate.getTopology_template().getNode_templates();
    Assert.assertEquals(expectedNumberOfNodeTemplates, substitutionNodeTemplates.size());
    NodeTemplate componentNodeTemplate = substitutionNodeTemplates.get(componentName);
    Assert.assertNotNull(componentNodeTemplate);
    Assert.assertEquals(ToscaNodeType.VFC_NODE_TYPE_PREFIX + componentName, componentNodeTemplate
        .getType());
    for (Nic nic : nics) {
      String nicName = nic.getName();
      NodeTemplate nicNodeTemplate =
          substitutionNodeTemplates.get(nicName + PORT_NODE_TEMPLATE_ID_SUFFIX);
      validateNicNodeTemplate(nicNodeTemplate, componentName);
    }
    SubstitutionMapping substitutionMappings =
        substitutionServiceTemplate.getTopology_template().getSubstitution_mappings();
    validateSubstitutionMappings(substitutionMappings, nics, componentName);
  }

  private void validateNicNodeTemplate(NodeTemplate nicNodeTemplate,
                                       String componentName) {
    Assert.assertNotNull(nicNodeTemplate);
    Assert.assertEquals(ToscaNodeType.NETWORK_PORT, nicNodeTemplate.getType());
    List<Map<String, RequirementAssignment>> nicNodeTemplateRequirements =
        nicNodeTemplate.getRequirements();
    Assert.assertNotNull(nicNodeTemplateRequirements);
    Assert.assertEquals(1, nicNodeTemplateRequirements.size());
    RequirementAssignment expectedRequirementAssignment = new RequirementAssignment();
    expectedRequirementAssignment.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
    expectedRequirementAssignment.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
    expectedRequirementAssignment.setNode(componentName);
    Assert.assertEquals(true, new ToscaAnalyzerServiceImpl()
        .isRequirementExistInNodeTemplate(nicNodeTemplate, BINDING_REQUIREMENT_ID,
            expectedRequirementAssignment));
  }

  private void validateSubstitutionMappings(SubstitutionMapping substitutionMappings,
                                            List<Nic> nics,
                                            String componentName) {
    Assert.assertEquals(ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE, substitutionMappings
        .getNode_type());
    Map<String, List<String>> capabilities = substitutionMappings.getCapabilities();
    validateSubstitutionCapabilities(capabilities, componentName);
    Map<String, List<String>> requirements = substitutionMappings.getRequirements();
    validateSubstitutionRequirements(requirements, nics);
  }

  private void validateSubstitutionCapabilities(Map<String, List<String>> capabilities,
                                                String componentName) {
    List<String> supportedCapabilities = GeneratorUtils.supportedCapabilities;
    Assert.assertEquals(supportedCapabilities.size(), capabilities.size());
    for (String capability : supportedCapabilities) {
      String expectedCapabilityId = capability + "_" + componentName;
      Assert.assertEquals(true, capabilities.containsKey(expectedCapabilityId));
      List<String> expectedCapabilityValue = new ArrayList<>(2);
      expectedCapabilityValue.add(componentName);
      expectedCapabilityValue.add(capability);
      List<String> actualCapabilityValue = capabilities.get(expectedCapabilityId);
      Assert.assertEquals(expectedCapabilityValue, actualCapabilityValue);
    }
  }

  private void validateSubstitutionRequirements(Map<String, List<String>> requirements,
                                                List<Nic> nics) {
    List<String> supportedRequirements = GeneratorUtils.supportedRequirements;
    for(Nic nic : nics) {
      String nicNodeTemplateId = nic.getName() + PORT_NODE_TEMPLATE_ID_SUFFIX;
      for (String requirement : supportedRequirements) {
        String expectedRequirementId = requirement + "_" + nicNodeTemplateId;
        Assert.assertEquals(true, requirements.containsKey(expectedRequirementId));
        List<String> expectedRequirementValue = new ArrayList<>(2);
        expectedRequirementValue.add(nicNodeTemplateId);
        expectedRequirementValue.add(requirement);
        List<String> actualRequirementValue = requirements.get(expectedRequirementId);
        Assert.assertEquals(expectedRequirementValue, actualRequirementValue);
      }
    }
  }

  private void validateGlobalSubstitutionServiceTemplate(ServiceTemplate
                                                                globalSubstitutionServiceTemplate,
                                                         List<Nic> nics,
                                                         String componentName) {
    Assert.assertNotNull(globalSubstitutionServiceTemplate);
    Map<String, NodeType> nodeTypes = globalSubstitutionServiceTemplate.getNode_types();
    Assert.assertEquals(1, nodeTypes.size());
    NodeType deploymentFlavorNodeType =
        nodeTypes.get(ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE);
    Assert.assertNotNull(deploymentFlavorNodeType);
    Map<String, PropertyDefinition> properties = deploymentFlavorNodeType.getProperties();
    Assert.assertNotNull(properties);
    PropertyDefinition numCpusProperty = properties.get(GeneratorConstants.NUM_CPUS);
    Assert.assertNotNull(numCpusProperty);
    Assert.assertEquals(PropertyType.INTEGER.getDisplayName(), numCpusProperty.getType());
    Assert.assertEquals(true, numCpusProperty.getRequired());

    PropertyDefinition diskSizeProperty = properties.get(GeneratorConstants.DISK_SIZE);
    Assert.assertNotNull(diskSizeProperty);
    Assert.assertEquals(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(), diskSizeProperty.getType());
    Assert.assertEquals(true, diskSizeProperty.getRequired());

    PropertyDefinition memSizeProperty = properties.get(GeneratorConstants.MEM_SIZE);
    Assert.assertNotNull(memSizeProperty);
    Assert.assertEquals(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(), memSizeProperty.getType());
    Assert.assertEquals(true, memSizeProperty.getRequired());

    List<Map<String, RequirementDefinition>> requirements =
        deploymentFlavorNodeType.getRequirements();
    List<String> supportedRequirements = GeneratorUtils.supportedRequirements;
    for (Nic nic : nics) {
      boolean found = false;
      String nicNodeTemplateId = nic.getName() + PORT_NODE_TEMPLATE_ID_SUFFIX;
      for (String requirementId : supportedRequirements) {
        String expectedRequirementId = requirementId + "_" + nicNodeTemplateId;
        for (Map<String, RequirementDefinition> requirement : requirements) {
          if (requirement.containsKey(expectedRequirementId)) {
            found = true;
            break;
          }
        }
      }
      Assert.assertEquals(true, found);
    }

    Map<String, CapabilityDefinition> capabilities = deploymentFlavorNodeType.getCapabilities();
    List<String> supportedCapabilities = GeneratorUtils.supportedCapabilities;
    for (String capabilityId : supportedCapabilities) {
      String expectedCapabilityId = capabilityId + "_" + componentName;
      Assert.assertEquals (true, capabilities.containsKey(expectedCapabilityId));
    }
  }

  private Map<String, String> getComponentData() {
    Map<String, String> componentData = new HashMap<>();
    componentData.put(COMPONENT_ID, COMPONENT_NAME);
    return componentData;
  }

  private Map<String, List<MultiFlavorVfcImage>> getVfcImageData() {
    Map<String, List<MultiFlavorVfcImage>> imageData = new HashMap<>();
    List<MultiFlavorVfcImage> images = new ArrayList<>(2);
    MultiFlavorVfcImage image1 = getImageData(IMAGE_VERSION_1, IMAGE_HASH_1, IMAGE_FILE_NAME_1,
        "md5");
    MultiFlavorVfcImage image2 = getImageData(IMAGE_VERSION_2, IMAGE_HASH_2, IMAGE_FILE_NAME_2,
        "md5");
    images.add(image1);
    images.add(image2);
    imageData.put(COMPONENT_ID, images);
    return imageData;
  }

  private Map<String, DeploymentFlavorModel> getDeploymentFlavorData() {
    Map<String, DeploymentFlavorModel> deploymentFlavorData = new HashMap<>();
    ComputeFlavor computeFlavor1 = getComputeFlavorData(NUM_CPUS_1, DISK_SIZE_1, MEM_SIZE_1);
    LicenseFlavor licenseFlavor1 = getLicenseFlavor(FEATURE_GROUP_ID_1);
    VendorInfo vendorInfo1 = getVendorInfo(MANUFACTURER_REF_1, VENDOR_MODEL_1);
    DeploymentFlavorModel deploymentFlavor1 = getDeploymentFlavorModel(SP_PART_NUMBER_1,
        computeFlavor1, vendorInfo1, licenseFlavor1);

    ComputeFlavor computeFlavor2 = getComputeFlavorData(NUM_CPUS_2, DISK_SIZE_2, MEM_SIZE_2);
    LicenseFlavor licenseFlavor2 = getLicenseFlavor(FEATURE_GROUP_ID_2);
    VendorInfo vendorInfo2 = getVendorInfo(MANUFACTURER_REF_2, VENDOR_MODEL_2);

    DeploymentFlavorModel deploymentFlavor2 = getDeploymentFlavorModel(SP_PART_NUMBER_2,
        computeFlavor2, vendorInfo2, licenseFlavor2);

    deploymentFlavorData.put(SP_PART_NUMBER_1, deploymentFlavor1);
    deploymentFlavorData.put(SP_PART_NUMBER_2, deploymentFlavor2);
    return deploymentFlavorData;
  }

  private DeploymentFlavorModel getDeploymentFlavorModel(String spPartNumber, ComputeFlavor
      computeFlavor, VendorInfo vendorInfo, LicenseFlavor licenseFlavor) {
    DeploymentFlavorModel deploymentFlavor = new DeploymentFlavorModel();
    deploymentFlavor.setSp_part_number(spPartNumber);
    deploymentFlavor.setCompute_flavor(computeFlavor);
    deploymentFlavor.setLicense_flavor(licenseFlavor);
    deploymentFlavor.setVendor_info(vendorInfo);
    return deploymentFlavor;
  }

  private ComputeFlavor getComputeFlavorData(int numCpus, String diskSize, String memSize ) {
    ComputeFlavor computeFlavor = new ComputeFlavor();
    computeFlavor.setNum_cpus(numCpus);
    computeFlavor.setDisk_size(diskSize);
    computeFlavor.setMem_size(memSize);
    return computeFlavor;
  }

  private VendorInfo getVendorInfo(String manufacturerRefNumber, String deploymentFlavorModel) {
    VendorInfo vendorInfo = new VendorInfo();
    vendorInfo.setManufacturer_reference_number(manufacturerRefNumber);
    vendorInfo.setVendor_model(deploymentFlavorModel);
    return vendorInfo;
  }

  private LicenseFlavor getLicenseFlavor(String featureGroupId) {
    LicenseFlavor licenseFlavor = new LicenseFlavor();
    licenseFlavor.setFeature_group_uuid(featureGroupId);
    return licenseFlavor;
  }

  private MultiFlavorVfcImage getImageData(String imageVersion, String fileHash,
                                           String fileName, String fileHashType) {
    MultiFlavorVfcImage image = new MultiFlavorVfcImage();
    image.setSoftware_version(imageVersion);
    image.setFile_hash(fileHash);
    image.setFile_hash_type(fileHashType);
    image.setFile_name(fileName);
    return image;
  }

  private Map<String, List<Nic>> getComponentNics() {
    Map<String, List<Nic>> nicData = new HashMap<>();
    List<Nic> nicList = new ArrayList<>(2);
    Nic nic1 = new Nic();
    nic1.setName("Nic_1");

    Nic nic2 = new Nic();
    nic2.setName("Nic_2");

    nicList.add(nic1);
    nicList.add(nic2);
    nicData.put(COMPONENT_ID, nicList);
    return nicData;
  }

  */
}
