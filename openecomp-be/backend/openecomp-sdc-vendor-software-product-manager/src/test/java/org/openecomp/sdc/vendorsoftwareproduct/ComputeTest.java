package org.openecomp.sdc.vendorsoftwareproduct;

public class ComputeTest {

  /*private static final String USER1 = "componentsTestUser1";
  private static final String USER2 = "componentsTestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory
          .getInstance().createInterface();

  private static String vsp1Id;
  private static String vsp2Id;
  private static String comp1 = "{\"displayName\": \"VFC_Manual\", " +
      "\"description\": \"desc manual\"}";
  private static String compute1 = "{\"name\": \"Compute_A\", " +
      "\"description\": \"desc manual compute\"}";
  private static String computeDelete = "{\"name\": \"Compute_Delete\", " +
      "\"description\": \"desc manual compute delete\"}";

  private static String comp1Id;
  private static String compute1Id;
  private ComputeEntity createdCompute;

  static ComponentEntity createComponent(String vspId, Version version, String compId) {
    ComponentEntity componentEntity = new ComponentEntity(vspId, version, compId);
    ComponentData compData = new ComponentData();
    compData.setName(compId + " name");
    compData.setDisplayName(compId + " display name");
    compData.setDescription(compId + " desc");
    componentEntity.setComponentCompositionData(compData);
    vendorSoftwareProductDao.createComponent(componentEntity);
    return componentEntity;
  }

  static ComputeEntity createComputeEntity(String vspId, String componentId, String data ){
    ComputeEntity comp = new ComputeEntity();
    comp.setVspId(vspId);
    comp.setComponentId(componentId);
    comp.setCompositionData(data);
    return comp;
  }

  @BeforeClass
  private void init() {
    VspDetails
        vsp1 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null,
            VSPCommon.OnboardingMethod.HEAT.name()), USER1
    );
    vsp1Id = vsp1.getId(); //HEAT onboarded

    VspDetails vsp2 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp3",
            "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null, VSPCommon
                .OnboardingMethod.Manual.name()), USER1);
    vsp2Id = vsp2.getId(); //MANUAL onboarded

    ComponentEntity component = new ComponentEntity();
    component.setVspId(vsp2Id);
    component.setCompositionData(comp1);
    ComponentEntity createdComp = vendorSoftwareProductManager.createComponent(component, USER1);
    comp1Id = createdComp.getId();
  }

  @Test
  public void testListWhenNone() {

    final Collection<ListComputeResponse> listComputeResponses =
        vendorSoftwareProductManager.listCompute(vsp2Id, null, comp1Id, USER1);
    Assert.assertEquals(listComputeResponses.size(), 0);
  }

  @Test
  public void testCreateComputeInHeatOnboardedVsp_negative() {
    ComputeEntity comp = createComputeEntity(vsp1Id,comp1Id,compute1);
    try {
      createdCompute = vendorSoftwareProductManager.createCompute(comp, USER1);
      Assert.fail();
    }
    catch(CoreException exception){
      Assert.assertEquals(exception.code().id(),ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    }
  }

  @Test(dependsOnMethods = "testListWhenNone")
  public void testCreateCompute() {
    ComputeEntity comp = createComputeEntity(vsp2Id,comp1Id,compute1);

    createdCompute = vendorSoftwareProductManager.createCompute(comp, USER1);
    compute1Id = createdCompute.getId();
    Assert.assertNotNull(compute1Id);
    Assert.assertNotNull(createdCompute.getCompositionData());
    Assert.assertNotNull(
        vendorSoftwareProductManager.getCompute(vsp2Id, VERSION01, comp1Id,compute1Id,
            USER1).getData());
  }

  @Test(dependsOnMethods = "testCreateCompute")
  public void testCreateComputeNegative() {
    ComputeEntity comp = createComputeEntity(vsp2Id,comp1Id,compute1);

    try {
      ComputeEntity createdCompute = vendorSoftwareProductManager.createCompute(comp, USER1);
      Assert.fail();
    }
    catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),DUPLICATE_COMPUTE_NAME_NOT_ALLOWED);
    }
  }

  @Test
  public void testGetNonExistingComponentId_negative() {
    testGet_negative(vsp1Id, null, "non existing component id", compute1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, comp1Id, compute1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testGetNonExistingComputeId_negative() {
    testGet_negative(vsp1Id, null, comp1Id, "non existing compute id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test(dependsOnMethods = "testCreateCompute")
  public void testGetCompute() {
    testGet(vsp2Id, VERSION01, comp1Id, compute1Id, USER1, createdCompute);
  }


  @Test(dependsOnMethods = "testCreateCompute")
  public void testListCompute() {

    final Collection<ListComputeResponse> actual =
        vendorSoftwareProductManager.listCompute(vsp2Id, null, comp1Id, USER1);
    Assert.assertEquals(actual.size(), 1);
    actual.forEach(listComputeResponse -> {
      Assert.assertEquals(listComputeResponse.isAssociatedWithDeploymentFlavor(), false);
    } );
  }


  @Test(dependsOnMethods = "testListCompute")
  public void testListComputeAssociatedWithDeployment() {

    //Create DF and associate compute1Id CF to it
    String deployment1Id = null;
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity(vsp2Id,
        VERSION01, deployment1Id);
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel("DF_testListComputeAssociatedWithDeployment");
    deploymentFlavor.setDescription("creating a deployment flavor with compute flavor association");
    ComponentComputeAssociation association = new ComponentComputeAssociation();
    association.setComponentId(comp1Id);
    association.setComputeFlavorId(compute1Id);
    List<ComponentComputeAssociation> associations = new ArrayList<ComponentComputeAssociation>();
    associations.add(association);
    deploymentFlavor.setComponentComputeAssociations(associations);
    deploymentFlavorEntity.setDeploymentFlavorCompositionData(deploymentFlavor);

    DeploymentFlavorEntity createddeployment = vendorSoftwareProductManager.createDeploymentFlavor
        (deploymentFlavorEntity, USER1);
    Assert.assertEquals((createddeployment.getId() != null), true);
    deployment1Id = createddeployment.getId();

    final Collection<ListComputeResponse> actual =
        vendorSoftwareProductManager.listCompute(vsp2Id, null, comp1Id, USER1);
    Assert.assertEquals(actual.size(), 1);
    actual.forEach(listComputeResponse -> {
      Assert.assertEquals(listComputeResponse.isAssociatedWithDeploymentFlavor(), true);
    } );
  }

  @Test
  public void testUpdateNonExistingComponentId_negative() {
    testUpdate_negative(vsp1Id, "non existing component id", USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateNonExistingVspId_negative() {
    testUpdate_negative("non existing vsp id", comp1Id, USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  @Test
  public void testDelete() {
    ComputeEntity comp = createComputeEntity(vsp2Id,comp1Id,computeDelete);

    ComputeEntity created = vendorSoftwareProductManager.createCompute(comp, USER1);

    vendorSoftwareProductManager.deleteCompute(vsp2Id,comp1Id,created.getId(),USER1);
    testGet_negative(vsp2Id,null, comp1Id, created.getId(),USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteNonExistingComputeId_negative() {
    testDelete_negative(vsp2Id,comp1Id,"InvalidComputeId",USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteNonExistingComponentId_negative() {
    testDelete_negative(vsp2Id,"InvalidComponentId",compute1Id,USER1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testDeleteNonExistingVspId_negative() {
    testDelete_negative("InvalidVspId",comp1Id,compute1Id,USER1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
  }

  private void testGet(String vspId, Version version, String componentId, String computeId, String
      user, ComputeEntity expected) {
    CompositionEntityResponse<ComputeData>
        response = vendorSoftwareProductManager.getCompute(vspId, null, componentId, computeId,
        user);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getComputeCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testGet_negative(String vspId, Version version, String componentId, String computeId,
                                String user, String expectedErrorCode) {
    try {
      vendorSoftwareProductManager.getCompute(vspId, version, componentId, computeId, user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, String componentId, String computeId, String user,
                                   String expectedErrorCode){
    try {
      vendorSoftwareProductManager.deleteCompute(vspId, componentId, computeId, user);
      Assert.fail();
    }
    catch(CoreException exception){
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, String componentId, String user,
                                   String expectedErrorCode) {
    try {
      vendorSoftwareProductManager
          .updateComponent(new ComponentEntity(vspId, null, componentId), user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }*/
}
