package org.openecomp.sdc.vendorsoftwareproduct;

public class DeploymentFlavorTest {
  /*private static final String USER1 = "deploymentTestUser1";
  private static final String USER2 = "deploymentTestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();

  private static String vsp1Id;
  private static String vsp2Id;
  private static String vsp3Id;
  private static String component11Id;
  private static String component21Id;
  private static String deployment1Id = "deployment1";
  private static String deployment2Id = "deployment2";

  @BeforeClass
  private void init() {
    List<String> featureGroups = new ArrayList<>();
    featureGroups.add("fg01");
    featureGroups.add("fg02");
    vsp1Id = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", featureGroups,
            VSPCommon.OnboardingMethod.
                Manual.name()), USER1).getId();
    component11Id = ComponentsTest.createComponent(vsp1Id, VERSION01, "component11").getId();

    vsp2Id = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp2", "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null,
            VSPCommon.OnboardingMethod.Manual.name()), USER1).getId();
    component21Id = ComponentsTest.createComponent(vsp2Id, VERSION01, "component21").getId();

    vsp3Id = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp3forDeployment",
            "vendorName1",
            "vlm1Id", "icon", "category", "subCategory", "123", null,
            VSPCommon.OnboardingMethod.HEAT.name()), USER1).getId();
  }

    static DeploymentFlavorEntity createDeploymentFlavor(String vspId, Version version, String deploymentFlavorId) {
        DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vspId, version, deploymentFlavorId);
        DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
        deploymentFlavor.setModel(deploymentFlavorId + " name");
        deploymentFlavor.setDescription(deploymentFlavorId + " desc");
        deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
        DeploymentFlavorEntity createdDeployment = vendorSoftwareProductManager
                .createDeploymentFlavor(deploymentFlavorEntity, USER1);
        deployment2Id = createdDeployment.getId();
        return deploymentFlavorEntity;
    }

  @Test
  public void testCreate() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("TestDeploymentcreatewithoutFG");
    deploymentFlavor.setDescription("creating a deployment flavor without any FG and any " +
        "association");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    DeploymentFlavorEntity createddeployment = vendorSoftwareProductManager.createDeploymentFlavor
        (deploymentFlavorEntity, USER1);
    Assert.assertEquals((createddeployment.getId() != null), true);
    deployment1Id = createddeployment.getId();
  }

  @Test(dependsOnMethods = "testCreate")
  public void testUniqueModelCreate() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("TestDeploymentcreatewithoutFG");
    deploymentFlavor.setDescription("creating a deployment flavor without any FG and any " +
        "association");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED);
    }
  }

  @Test
  public void testInvalidFeatureGroup() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("TestInvalidFeatureGroup");
    deploymentFlavor.setDescription("creating a deployment flavor with invalid FG and without any" +
        " " +
        "association");
    deploymentFlavor.setFeatureGroupId("fg01");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), FEATURE_GROUP_NOT_EXIST_FOR_VSP);
    }
  }

  @Test
  public void testInvalidAssociation() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("testInvalidAssociation");
    deploymentFlavor.setDescription("creating a deployment flavor with wrong association with " +
        "null compute flavor id");
    List<ComponentComputeAssociation> componentComputeAssociationList = new ArrayList<>();
    ComponentComputeAssociation componentComputeAssociation = new ComponentComputeAssociation();
    componentComputeAssociation.setComputeFlavorId("72138712");
    componentComputeAssociationList.add(componentComputeAssociation);
    deploymentFlavor.setComponentComputeAssociations(componentComputeAssociationList);
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), INVALID_COMPONENT_COMPUTE_ASSOCIATION);
    }

  }

  @Test
  public void testInvalidComputeFlavorIdAssociation() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("testInvalidComputeFlavorIdAssociation");
    deploymentFlavor.setDescription("creating a deployment flavor with wrong compute flavor id in" +
        " assocation");
    List<ComponentComputeAssociation> componentComputeAssociationList = new ArrayList<>();
    ComponentComputeAssociation componentComputeAssociation = new ComponentComputeAssociation();
    componentComputeAssociation.setComponentId(component21Id);
    componentComputeAssociation.setComputeFlavorId("123123");
    componentComputeAssociationList.add(componentComputeAssociation);
    deploymentFlavor.setComponentComputeAssociations(componentComputeAssociationList);
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), INVALID_COMPUTE_FLAVOR_ID);
    }

  }

  @Test
  public void testInvalidVfcIdAssociation() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("testInvalidVfcIdAssociation");
    deploymentFlavor.setDescription("creating a deployment flavor with invalid vfcid association");
    List<ComponentComputeAssociation> componentComputeAssociationList = new ArrayList<>();
    ComponentComputeAssociation componentComputeAssociation = new ComponentComputeAssociation();
    componentComputeAssociation.setComponentId("WRONGVFCID");
    componentComputeAssociation.setComputeFlavorId("123123");
    componentComputeAssociationList.add(componentComputeAssociation);
    deploymentFlavor.setComponentComputeAssociations(componentComputeAssociationList);
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }
  }

  @Test
  public void testNegativeforVspHeatOnboarded() {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vsp3Id, VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("TestDeploymentcreatewithoutFG");
    deploymentFlavor.setDescription("creating a deployment flavor for VSP onboarded with HEAT");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    try {
      vendorSoftwareProductManager.createDeploymentFlavor
          (deploymentFlavorEntity, USER1);
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),
          CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    }
  }

  @Test
  public void testGetNegative_InvalidVspId(){
      testGetNegative("InvalidVspId", VERSION01, deployment1Id, USER1, VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testGetNegative_InvalidDeploymentFlavorId(){
      testGetNegative(vsp1Id, VERSION01, deployment1Id, USER1, VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet(){
      DeploymentFlavorEntity expected = createDeploymentFlavor(vsp1Id, VERSION01,deployment2Id);
      testGet(vsp1Id, VERSION01, deployment2Id, USER1, expected);
  }

  private void testGet(String vspId, Version version, String componentId, String user,
                       DeploymentFlavorEntity expected) {
      CompositionEntityResponse<DeploymentFlavor> response = vendorSoftwareProductManager
              .getDeploymentFlavor(vspId, null, componentId, user);
      Assert.assertEquals(response.getId(), expected.getId());
      Assert.assertEquals(response.getData().getDescription(),
              expected.getDeploymentFlavorCompositionData().getDescription());
      Assert.assertEquals(response.getData().getModel(),
              expected.getDeploymentFlavorCompositionData().getModel());
      Assert.assertEquals(response.getData().getFeatureGroupId(),
              expected.getDeploymentFlavorCompositionData().getFeatureGroupId());
      Assert.assertEquals(response.getData().getComponentComputeAssociations(),
              expected.getDeploymentFlavorCompositionData().getComponentComputeAssociations());
      Assert.assertNotNull(response.getSchema());
  }

  private void testGetNegative(String vspId, Version version, String deploymentFlavorId, String User,
                               String expectedErrorCode){
      try{
          vendorSoftwareProductManager.getDeploymentFlavor(vspId, version, deploymentFlavorId, User);
          Assert.fail();
      } catch (CoreException exception){
          Assert.assertEquals(exception.code().id(), expectedErrorCode);
      }
  }

  @Test
  public void testUpdateNegative_InvalidVspId(){
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity("InvalidVspId",
            VERSION01,deployment2Id);
    DeploymentFlavor deploymentFlavorData = new DeploymentFlavor();
    deploymentFlavorData.setModel(deployment2Id);
    deploymentFlavorData.setDescription("updating a deployment flavor with invalid VspId and without any" +
            "association");
    deploymentFlavorData.setFeatureGroupId("fg01");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavorData);

    testUpdateNegative(deploymentFlavorEntity, USER1, VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testUpdateNegative_InvalidDeploymentFlavorId(){
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vsp1Id,
            VERSION01,"InvalidDeploymentFlavorId");
    testUpdateNegative(deploymentFlavorEntity, USER1, VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testUpdateNegative_InvalidFeatureGroup(){
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vsp2Id,
            VERSION01,deployment1Id);
    DeploymentFlavor deploymentFlavorData = new DeploymentFlavor();
    deploymentFlavorData.setModel("TestDeploymentCreateWithoutFG");
    deploymentFlavorData.setDescription("updating a deployment flavor with invalid FeatureGroupId and without any" +
            "association");
    deploymentFlavorData.setFeatureGroupId("fg01");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavorData);
    String expectedError = "#/featureGroupId: "
            +deploymentFlavorData.getFeatureGroupId()+" is not a valid value. Possible values: ";

      final CompositionEntityValidationData validationData = vendorSoftwareProductManager
              .updateDeploymentFlavor(deploymentFlavorEntity, USER1);
      final Collection<String> errors = validationData.getErrors();
      final Object[] objects = errors.toArray();
      Assert.assertEquals(errors.size(), 1);
      Assert.assertEquals(objects[0], expectedError);
  }

  @Test(dependsOnMethods = "testCreate")
  public void testUpdateNegative_InvalidComputeFlavorIdAssociation(){
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vsp2Id,
            VERSION01,deployment1Id);
    DeploymentFlavor deploymentFlavorData = new DeploymentFlavor();
    deploymentFlavorData.setModel("TestDeploymentcreatewithInvalidComputFlavorId");
    deploymentFlavorData.setDescription("updating a deployment flavor with wrong compute flavor id in" +
            " assocation");
    List<ComponentComputeAssociation> componentComputeAssociationList = new ArrayList<>();
    ComponentComputeAssociation componentComputeAssociation = new ComponentComputeAssociation();
    componentComputeAssociation.setComponentId(component21Id);
    componentComputeAssociation.setComputeFlavorId("123123");
    componentComputeAssociationList.add(componentComputeAssociation);
    deploymentFlavorData.setComponentComputeAssociations(componentComputeAssociationList);
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavorData);

    testUpdateNegative(deploymentFlavorEntity, USER1, INVALID_COMPUTE_FLAVOR_ID);
  }

  @Test
  public void testUpdate(){
    //Creating a separate deployment flavor for testing deletion
    DeploymentFlavorEntity deploymentFlavorEntity =
            new DeploymentFlavorEntity(vsp1Id, VERSION01, deployment2Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("TestDeploymentCreateforUpdate");
    deploymentFlavor.setDescription("creating a deployment flavor for updation");
    deploymentFlavor.setFeatureGroupId("fg01");
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
    DeploymentFlavorEntity createdDeployment = vendorSoftwareProductManager.createDeploymentFlavor
            (deploymentFlavorEntity, USER1);
    deployment2Id = createdDeployment.getId();

    final DeploymentFlavor deploymentFlavorCompositionData = createdDeployment.getDeploymentFlavorCompositionData();
    deploymentFlavorCompositionData.setModel("TestDeploymentCreateforUpdate"); //no change
    deploymentFlavorCompositionData.setDescription("updating deployment flavor"); //allow change
    deploymentFlavorCompositionData.setFeatureGroupId("fg01"); //no change
    createdDeployment.setDeploymentFlavorCompositionData(deploymentFlavorCompositionData);

    vendorSoftwareProductManager.updateDeploymentFlavor(createdDeployment, USER1);
    CompositionEntityResponse<DeploymentFlavor> deploymentFlavorCompositionEntityResponse = vendorSoftwareProductManager
            .getDeploymentFlavor(vsp1Id, VERSION01, deployment2Id, USER1);
    final DeploymentFlavor data = deploymentFlavorCompositionEntityResponse.getData();

    Assert.assertEquals(
            data.getDescription(),createdDeployment.getDeploymentFlavorCompositionData().getDescription());
  }

  @Test
  public void testUpdateVspWithNoFeatureGroup(){
      //Creating a separate deployment flavor for testing deletion
      DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment1Id);
      DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
      deploymentFlavor.setModel("TestDeploymentCreateforUpdate");
      deploymentFlavor.setDescription("creating a deployment flavor for updation");
      deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
      DeploymentFlavorEntity createdDeployment = vendorSoftwareProductManager
              .createDeploymentFlavor(deploymentFlavorEntity, USER1);
      deployment1Id = createdDeployment.getId();

      final DeploymentFlavor deploymentFlavorCompositionData = createdDeployment.getDeploymentFlavorCompositionData();
      deploymentFlavorCompositionData.setModel("TestDeploymentCreateforUpdate"); //no change
      deploymentFlavorCompositionData.setDescription("updating deployment flavor"); //allow change
      createdDeployment.setDeploymentFlavorCompositionData(deploymentFlavorCompositionData);

      vendorSoftwareProductManager.updateDeploymentFlavor(createdDeployment, USER1);
      CompositionEntityResponse<DeploymentFlavor> deploymentFlavorCompositionEntityResponse =
              vendorSoftwareProductManager.getDeploymentFlavor(vsp2Id, VERSION01, deployment1Id, USER1);
      final DeploymentFlavor data = deploymentFlavorCompositionEntityResponse.getData();

      Assert.assertEquals(data.getDescription(),createdDeployment.getDeploymentFlavorCompositionData()
              .getDescription());
    }

  private void testUpdateNegative(DeploymentFlavorEntity deploymentFlavorEntity, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
              .updateDeploymentFlavor(deploymentFlavorEntity, user);
      System.out.print("updated");
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  @Test
  public void testDelete(){
      //Creating a separate deployment flavor for testing deletion
      DeploymentFlavorEntity deploymentFlavorEntity =
              new DeploymentFlavorEntity(vsp2Id, VERSION01, deployment2Id);
      DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
      deploymentFlavor.setModel("TestDeploymentcreateWithoutFG");
      deploymentFlavor.setDescription("creating a deployment flavor without any FG and any " +
              "association");
      deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);
      DeploymentFlavorEntity createddeployment = vendorSoftwareProductManager.createDeploymentFlavor
              (deploymentFlavorEntity, USER1);
      deployment2Id = createddeployment.getId();
      vendorSoftwareProductManager.deleteDeploymentFlavor(vsp2Id, deployment2Id, USER1);
      testDeleteNegative(vsp2Id, deployment2Id, USER1,
              VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteNegative_InvalidVspId(){
    testDeleteNegative("InvalidVspId", deployment2Id, USER1,
            VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testDeleteNegative_NonExistingDeploymentFlavorId(){
    testDeleteNegative(vsp2Id, "NonExistingDeploymentFlavorId", USER1,
            VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  private void testDeleteNegative(String vspId, String deploymentId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
              .deleteDeploymentFlavor(vspId, deploymentId, user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }*/
}
