package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NicManagerImplTest {
  private static final String NIC_NOT_EXIST_MSG =
      "Vendor Software Product NIC with Id nic1 does not exist for Vendor Software Product with " +
          "id VSP_ID and version 0.1";

  private static final String USER = "nicTestUser";
  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version(0, 1);
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String NIC1_ID = "nic1";
  private static final String NIC2_ID = "nic2";
  private static final String NETWORK1_ID = "network1";
  private static final String NETWORK2_ID = "network2";

  @Mock
  private NicDao nicDao;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private NetworkManager networkManagerMock;
  @InjectMocks
  @Spy
  private NicManagerImpl nicManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListWhenNone() {
    Collection<NicEntity> nics = nicManager.listNics(VSP_ID, VERSION, COMPONENT_ID, USER);
    Assert.assertEquals(nics.size(), 0);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID),
        createNic(VSP_ID, VERSION, COMPONENT_ID, NIC2_ID, NETWORK2_ID)))
        .when(nicDao).list(anyObject());

    NetworkEntity network1 = NetworkManagerImplTest.createNetwork(VSP_ID, VERSION, NETWORK1_ID);
    NetworkEntity network2 = NetworkManagerImplTest.createNetwork(VSP_ID, VERSION, NETWORK2_ID);
    doReturn(Arrays.asList(network1, network2))
        .when(networkManagerMock).listNetworks(VSP_ID, VERSION, USER);

    Collection<NicEntity> nics = nicManager.listNics(VSP_ID, VERSION, COMPONENT_ID, USER);
    Assert.assertEquals(nics.size(), 2);
    for (NicEntity nic : nics) {
      Assert.assertEquals(nic.getNicCompositionData().getNetworkName(),
          NIC1_ID.equals(nic.getId())
              ? network1.getNetworkCompositionData().getName()
              : network2.getNetworkCompositionData().getName());
    }
  }

//    @Test(dependsOnMethods = "testListWhenNone")
//    public void testCreate() {
//        NIC1_ID = testCreate(VSP_ID, COMPONENT_ID, NETWORK1_ID, NETWORK1_ID.getNetworkCompositionData().getName());
//    }

/*    @Test(dependsOnMethods = {"testCreate"})
    public void testCreateWithExistingName_negative() {
        NicEntity nic = new NicEntity(VSP_ID, null, COMPONENT_ID, null);
        Nic nicData = new Nic();
        nicData.setName("nic1 name");
        nic.setNicCompositionData(nicData);
        testCreate_negative(nic, USER, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherComponent() {
//        ComponentEntity component12 = new ComponentEntity(VSP_ID, null, null);
//        ComponentData compData12 = new ComponentData();
//        compData12.setName("comp12 name");
//        compData12.setDescription("comp12 desc");
//        component12.setComponentCompositionData(compData12);
//
//        String component12Id = nicManager.createComponent(component12, USER).getId();
//        testCreate(VSP_ID, component12Id, NETWORK1_ID, NETWORK1_ID.getNetworkCompositionData().getName());
//    }

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherVsp() {
//        testCreate(vsp2Id, component21Id, network2.getId(), network2.getNetworkCompositionData().getName());
//    }

  @Test
  public void testCreateOnUploadVsp_negative() {

    testCreate_negative(new NicEntity(VSP_ID, VERSION, COMPONENT_ID, null), USER,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testUpdateNonExistingNicId_negative() {
    doReturn(null).when(nicDao).get(anyObject());

    testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, USER,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateOnUploadVsp() {
    doReturn(createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID))
        .when(nicDao).get(anyObject());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.nic, NIC1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    NicEntity nicEntity = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Nic nicData = new Nic();
    nicData.setName(NIC1_ID + " name");
    nicData.setDescription(NIC1_ID + " desc updated");
    nicData.setNetworkId(NETWORK1_ID);
    nicEntity.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData =
        nicManager.updateNic(nicEntity, USER);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(nicDao).update(nicEntity);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID))
        .when(nicDao).get(anyObject());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.nic, NIC1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    NicEntity nicEntity = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Nic nicData = new Nic();
    nicData.setName(NIC1_ID + " name updated");
    nicData.setDescription(NIC1_ID + " desc updated");
    nicData.setNetworkId(NETWORK1_ID);
    nicEntity.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData = nicManager.updateNic(nicEntity, USER);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(nicDao, never()).update(nicEntity);
  }


  @Test
  public void testGetNonExistingNicId_negative() {
    testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing nic id", USER,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }


  @Test
  public void testGet() {
    NicEntity expected = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    doReturn(expected).when(nicDao).get(anyObject());
    String compositionSchema = "schema string";
    doReturn(compositionSchema).when(nicManager).getNicCompositionSchema(anyObject());

    CompositionEntityResponse<Nic> response =
        nicManager.getNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, USER);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getNicCompositionData());
    Assert.assertEquals(response.getSchema(), compositionSchema);
  }

/*
    @Test(dependsOnMethods = {"testUpdateOnUploadVsp", "testList"})
    public void testCreateWithRemovedName() {
        testCreate(VSP_ID, COMPONENT_ID);
    }

    @Test
    public void testDeleteNonExistingNicId_negative() {
        testDelete_negative(VSP_ID, COMPONENT_ID, "non existing nic id", USER, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingComponentId_negative() {
        testDelete_negative(VSP_ID, "non existing component id", NIC1_ID, USER, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

/*
    @Test(dependsOnMethods = "testList")
    public void testDelete() {
        nicManager.deleteNic(VSP_ID, COMPONENT_ID, NIC1_ID, USER);
        NicEntity actual = vendorSoftwareProductDao.getNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
        Assert.assertNull(actual);
    }*/

  @Test
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, USER,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = NIC_NOT_EXIST_MSG)
  public void testGetNonExistingNicQuestionnaire() throws Exception {
    nicManager.getNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, USER);
  }

  @Test
  public void testGetQuestionnaire() throws Exception {
    NicEntity nic = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    nic.setQuestionnaireData("{}");
    doReturn(nic).when(nicDao).getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);

    String schema = "schema string";
    doReturn(schema).when(nicManager).getNicQuestionnaireSchema(anyObject());

    QuestionnaireResponse questionnaire =
        nicManager.getNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, USER);
    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), nic.getQuestionnaireData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = NIC_NOT_EXIST_MSG)
  public void testUpdateNonExistingNicQuestionnaire() throws Exception {
    doReturn(null).when(nicDao).get(anyObject());
    nicManager
        .updateNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, "questionnaire data", USER);
  }

  @Test
  public void testUpdateQuestionnaire() throws Exception {

  }

  private void testCreate_negative(NicEntity nic, String user, String expectedErrorCode) {
    try {
      nicManager.createNic(nic, user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String nicId,
                                String user, String expectedErrorCode) {
    try {
      nicManager.getNic(vspId, version, componentId, nicId, user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String componentId, String nicId,
                                   String user, String expectedErrorCode) {
    try {
      nicManager.updateNic(new NicEntity(vspId, version, componentId, nicId), user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String componentId, String nicId,
                                   String user,
                                   String expectedErrorCode) {
    try {
      nicManager.deleteNic(vspId, version, componentId, nicId, user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  static NicEntity createNic(String vspId, Version version, String compId, String nicId,
                             String networkId) {
    NicEntity nicEntity = new NicEntity(vspId, version, compId, nicId);
    Nic nicData = new Nic();
    nicData.setName(nicId + " name");
    nicData.setDescription(nicId + " desc");
    nicData.setNetworkId(networkId);
    nicEntity.setNicCompositionData(nicData);
    return nicEntity;
  }


}
