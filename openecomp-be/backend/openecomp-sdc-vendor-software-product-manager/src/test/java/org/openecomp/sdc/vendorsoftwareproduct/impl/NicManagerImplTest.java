/*
 * Copyright © 2018 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NicManagerImplTest {

  private static final String NIC_NOT_EXIST_MSG =
      "Vendor Software Product NIC with Id nic1 does not exist for Vendor Software Product with " +
          "id VSP_ID and version version_id";

  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version("version_id");
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
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDao;
  @InjectMocks
  @Spy
  private NicManagerImpl nicManager;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() {
    nicManager = null;
  }

  @Test
  public void testListWhenNone() {
    Collection<NicEntity> nics = nicManager.listNics(VSP_ID, VERSION, COMPONENT_ID);
    Assert.assertEquals(nics.size(), 0);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID),
        createNic(VSP_ID, VERSION, COMPONENT_ID, NIC2_ID, NETWORK2_ID)))
        .when(nicDao).list(any());

    NetworkEntity network1 = NetworkManagerImplTest.createNetwork(VSP_ID, VERSION, NETWORK1_ID);
    NetworkEntity network2 = NetworkManagerImplTest.createNetwork(VSP_ID, VERSION, NETWORK2_ID);
    doReturn(Arrays.asList(network1, network2))
        .when(networkManagerMock).listNetworks(VSP_ID, VERSION);

    Collection<NicEntity> nics = nicManager.listNics(VSP_ID, VERSION, COMPONENT_ID);
    Assert.assertEquals(nics.size(), 2);
    for (NicEntity nic : nics) {
      Assert.assertEquals(nic.getNicCompositionData().getNetworkName(),
          NIC1_ID.equals(nic.getId())
              ? network1.getNetworkCompositionData().getName()
              : network2.getNetworkCompositionData().getName());
    }
  }

  @Test
  public void testCreate() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic nic = nicEntity.getNicCompositionData();
    nic.setNetworkType(NetworkType.Internal);
    nicEntity.setNicCompositionData(nic);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<NicEntity> nicEntities = new ArrayList<>();
    doReturn(nicEntities).when(nicDao).list(any());
    doReturn(nicEntity).when(compositionEntityDataManagerMock).createNic(any());

    NicEntity created = nicManager.createNic(nicEntity);
    Assert.assertNotNull(created);
  }

  @Test
  public void testCreateWithIncorrectNicNameFormat() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic nic = nicEntity.getNicCompositionData();
    nic.setNetworkType(NetworkType.Internal);
    nicEntity.setNicCompositionData(nic);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<NicEntity> nicEntities = new ArrayList<>();

    NicEntity nicEntityDiffName = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic newNameNic = nicEntityDiffName.getNicCompositionData();
    newNameNic.setName(NIC1_ID + "_Name/*");
    nicEntityDiffName.setNicCompositionData(newNameNic);
    nicEntities.add(nicEntityDiffName);
    doReturn(nicEntities).when(nicDao).list(any());
    doReturn(nicEntity).when(compositionEntityDataManagerMock).createNic(any());

    try {
      NicEntity created = nicManager.createNic(nicEntity);
    } catch (CoreException exception) {
      Assert.assertEquals(VendorSoftwareProductErrorCodes.NIC_NAME_FORMAT_NOT_ALLOWED,
          exception.code().id());
    }
  }

  @Test
  public void testCreateWithDupNicName() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic nic = nicEntity.getNicCompositionData();
    nic.setNetworkType(NetworkType.Internal);
    nicEntity.setNicCompositionData(nic);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<NicEntity> nicEntities = new ArrayList<>();

    NicEntity nicEntityDiffName = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic newNameNic = nicEntityDiffName.getNicCompositionData();
    newNameNic.setName(NIC1_ID + "_Name");
    nicEntityDiffName.setNicCompositionData(newNameNic);
    nicEntities.add(nicEntityDiffName);
    doReturn(nicEntities).when(nicDao).list(any());
    doReturn(nicEntity).when(compositionEntityDataManagerMock).createNic(any());

    try {
      NicEntity created = nicManager.createNic(nicEntity);
    } catch (CoreException exception) {
      Assert.assertEquals("Invalid request, NIC with name " + nic.getName() +
              " already exist for component with ID " + nicEntity.getComponentId() + ".",
          exception.code().message());
      Assert.assertEquals(VendorSoftwareProductErrorCodes.DUPLICATE_NIC_NAME_NOT_ALLOWED,
          exception.code().id());
    }
  }

  @Test
  public void testCreateWithExternalNetworkType() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic nic = nicEntity.getNicCompositionData();
    nic.setNetworkType(NetworkType.External);
    nicEntity.setNicCompositionData(nic);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<NicEntity> nicEntities = new ArrayList<>();
    doReturn(nicEntities).when(nicDao).list(any());
    doReturn(nicEntity).when(compositionEntityDataManagerMock).createNic(any());

    try {
      NicEntity created = nicManager.createNic(nicEntity);
    } catch (CoreException exception) {
      Assert.assertEquals("Invalid request,NetworkId not allowed for External Networks",
          exception.code().message());
      Assert
          .assertEquals(VendorSoftwareProductErrorCodes.NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK,
              exception.code().id());
    }
  }

  @Test
  public void testCreateWithNetworkDesc() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    Nic nic = nicEntity.getNicCompositionData();
    nic.setNetworkType(NetworkType.Internal);
    nic.setNetworkDescription(NIC1_ID);
    nicEntity.setNicCompositionData(nic);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<NicEntity> nicEntities = new ArrayList<>();
    doReturn(nicEntities).when(nicDao).list(any());
    doReturn(nicEntity).when(compositionEntityDataManagerMock).createNic(any());

    try {
      NicEntity created = nicManager.createNic(nicEntity);
    } catch (CoreException exception) {
      Assert.assertEquals("Invalid request, Network Description not allowed for Internal Networks",
          exception.code().message());
      Assert.assertEquals(VendorSoftwareProductErrorCodes
          .NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK, exception.code().id());
    }
  }

  @Test
  public void testDeleteNic() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    doReturn(nicEntity).when(nicDao).get(any());

    nicManager.deleteNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);

  }

  @Test
  public void testUpdateNicQuestionnaire() {
    NicEntity nicEntity = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);

    doReturn(nicEntity).when(nicDao).get(any());
    doReturn("{}").when(nicManager).getNicCompositionSchema(any());

    nicManager
        .updateNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, "Ques");

    verify(nicDao).updateQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, "Ques");
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
        testCreate_negative(nic, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherComponent() {
//        ComponentEntity component12 = new ComponentEntity(VSP_ID, null, null);
//        ComponentData compData12 = new ComponentData();
//        compData12.setName("comp12 name");
//        compData12.setDescription("comp12 desc");
//        component12.setComponentCompositionData(compData12);
//
//        String component12Id = nicManager.createComponent(component12).getId();
//        testCreate(VSP_ID, component12Id, NETWORK1_ID, NETWORK1_ID.getNetworkCompositionData().getName());
//    }

//    @Test(dependsOnMethods = {"testCreate"})
//    public void testCreateWithExistingNameUnderOtherVsp() {
//        testCreate(vsp2Id, component21Id, network2.getId(), network2.getNetworkCompositionData().getName());
//    }

  @Test
  public void testCreateOnUploadVsp_negative() {

    testCreate_negative(new NicEntity(VSP_ID, VERSION, COMPONENT_ID, null),
        VendorSoftwareProductErrorCodes.ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testUpdateNonExistingNicId_negative() {
    doReturn(null).when(nicDao).get(any());

    testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateOnUploadVsp() {
    doReturn(createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID))
        .when(nicDao).get(any());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.nic, NIC1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    NicEntity nicEntity = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Nic nicData = new Nic();
    nicData.setName(NIC1_ID + " name");
    nicData.setDescription(NIC1_ID + " desc updated");
    nicData.setNetworkId(NETWORK1_ID);
    nicEntity.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData =
        nicManager.updateNic(nicEntity);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(nicDao).update(nicEntity);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID))
        .when(nicDao).get(any());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.nic, NIC1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    NicEntity nicEntity = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Nic nicData = new Nic();
    nicData.setName(NIC1_ID + "_name_updated");
    nicData.setDescription(NIC1_ID + " desc updated");
    nicData.setNetworkId(NETWORK1_ID);
    nicEntity.setNicCompositionData(nicData);

    CompositionEntityValidationData validationData = nicManager.updateNic(nicEntity);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(nicDao, never()).update(nicEntity);
  }

  @Test
  public void testUpdateIncorrectNameFormat() {
    doReturn(createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID))
        .when(nicDao).get(any());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.nic, NIC1_ID);
    toBeReturned.setErrors(Arrays.asList("Field does not conform to predefined criteria:name : must match [a-zA-Z0-9_]*$"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());
    doReturn(true).when(vspInfoDao).isManual(any(), any());

    NicEntity nicEntity = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Nic nicData = new Nic();
    nicData.setName(NIC1_ID + "_name_updated/*");
    nicData.setDescription(NIC1_ID + " desc updated");
    nicData.setNetworkId(NETWORK1_ID);
    nicEntity.setNicCompositionData(nicData);

    CompositionEntityValidationData output = nicManager.updateNic(nicEntity);
    Assert.assertEquals(output.getErrors(),toBeReturned.getErrors());

  }


  @Test
  public void testGetNonExistingNicId_negative() {
    testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing nic id",
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }


  @Test
  public void testGet() {
    NicEntity expected = createNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, NETWORK1_ID);
    doReturn(expected).when(nicDao).get(any());
    String compositionSchema = "schema string";
    doReturn(compositionSchema).when(nicManager).getNicCompositionSchema(any());

    CompositionEntityResponse<Nic> response =
        nicManager.getNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
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
        testDelete_negative(VSP_ID, COMPONENT_ID, "non existing nic id", VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingComponentId_negative() {
        testDelete_negative(VSP_ID, "non existing component id", NIC1_ID, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/

/*
    @Test(dependsOnMethods = "testList")
    public void testDelete() {
        nicManager.deleteNic(VSP_ID, COMPONENT_ID, NIC1_ID);
        NicEntity actual = vendorSoftwareProductDao.getNic(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
        Assert.assertNull(actual);
    }*/

  @Test
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID,
        VendorSoftwareProductErrorCodes.DELETE_NIC_NOT_ALLOWED);
  }

  @Test(expected = CoreException.class)
  public void testGetNonExistingNicQuestionnaire() throws Exception {
    nicManager.getNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
  }

  @Test
  public void testGetQuestionnaire() throws Exception {
    NicEntity nic = new NicEntity(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    nic.setQuestionnaireData("{}");
    doReturn(nic).when(nicDao).getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);

    String schema = "schema string";
    doReturn(schema).when(nicManager).getNicQuestionnaireSchema(any());

    QuestionnaireResponse questionnaire =
        nicManager.getNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID);
    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), nic.getQuestionnaireData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }

  @Test(expected = CoreException.class)
  public void testUpdateNonExistingNicQuestionnaire() throws Exception {
    doReturn(null).when(nicDao).get(any());
    nicManager
        .updateNicQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, NIC1_ID, "questionnaire data");
  }

  @Test
  public void testUpdateQuestionnaire() throws Exception {

  }

  private void testCreate_negative(NicEntity nic, String expectedErrorCode) {
    try {
      nicManager.createNic(nic);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String nicId,
                                String expectedErrorCode) {
    try {
      nicManager.getNic(vspId, version, componentId, nicId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String componentId, String nicId,
                                   String expectedErrorCode) {
    try {
      nicManager.updateNic(new NicEntity(vspId, version, componentId, nicId));
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String componentId, String nicId,
                                   String expectedErrorCode) {
    try {
      nicManager.deleteNic(vspId, version, componentId, nicId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  static NicEntity createNic(String vspId, Version version, String compId, String nicId,
                             String networkId) {
    NicEntity nicEntity = new NicEntity(vspId, version, compId, nicId);
    Nic nicData = new Nic();
    nicData.setName(nicId + "_name");
    nicData.setDescription(nicId + " desc");
    nicData.setNetworkId(networkId);
    nicEntity.setNicCompositionData(nicData);
    return nicEntity;
  }


}
