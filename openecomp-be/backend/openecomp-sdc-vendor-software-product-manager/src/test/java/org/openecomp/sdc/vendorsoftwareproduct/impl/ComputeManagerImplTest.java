/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
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
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


public class ComputeManagerImplTest {

  private static final String COMPUTE_NOT_EXIST_MSG =
      "Vendor Software Product COMPUTE with Id compute1 does not exist for Vendor Software Product with " +
          "id VSP_ID and version version_id";

  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version("version_id");
  private static final String COMPONENT_ID = "COMPONENT_ID";
  private static final String COMPUTE1_ID = "compute1";
  private static final String COMPUTE2_ID = "compute2";

  @Mock
  private ComputeDao computeDao;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDao;
  @Mock
  private DeploymentFlavorDao deploymentFlavorDao;
  @InjectMocks
  @Spy
  private ComputeManagerImpl computeManager;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() {
    computeManager = null;
  }

  @Test
  public void testListWhenNone() {
    Collection<ListComputeResponse> computes =
        computeManager.listComputes(VSP_ID, VERSION, COMPONENT_ID);
    Assert.assertEquals(computes.size(), 0);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID),
        createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE2_ID)))
        .when(computeDao).list(any());


    Collection<ListComputeResponse> computes =
        computeManager.listComputes(VSP_ID, VERSION, COMPONENT_ID);
    Assert.assertEquals(computes.size(), 2);
    for (ListComputeResponse compute : computes) {
      Assert.assertEquals(compute.getComputeEntity().getComputeCompositionData().getName(),
          COMPUTE1_ID.equals(compute.getComputeEntity().getId())
              ? "compute1name"
              : "compute2name");
    }
  }

  @Test
  public void testCreateOnNotManualCompute_negative() {
    testCreate_negative(new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, null),
        VendorSoftwareProductErrorCodes.ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testCreateManualCompute() {
    ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    doNothing().when(computeManager)
        .validateUniqueName(VSP_ID, VERSION, COMPONENT_ID,
            expected.getComputeCompositionData().getName());
    doNothing().when(computeManager)
        .createUniqueName(VSP_ID, VERSION, COMPONENT_ID,
            expected.getComputeCompositionData().getName());
    String questionnaireSchema = "{}";
    doReturn(questionnaireSchema).when(computeManager).getComputeQuestionnaireSchema(any());

    computeManager.createCompute(expected);
    verify(computeDao).create(expected);
  }

  @Test(expected = CoreException.class)
  public void testCreateManualComputeWithDuplicateName() {
    ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(true).when(vspInfoDao).isManual(any(), any());

    doThrow(new CoreException(
        new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).build()))
        .when(computeManager).validateUniqueName(VSP_ID, VERSION, COMPONENT_ID,
        expected.getComputeCompositionData().getName());

    computeManager.createCompute(expected);
  }

  @Test
  public void testUpdateNonExistingComputeId_negative() {
    testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateCompute() {
    ComputeEntity retrieved = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(retrieved).when(computeDao).get(any());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.compute, COMPUTE1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    ComputeData computeData = new ComputeData();
    computeData.setName(COMPUTE1_ID + "name");
    computeData.setDescription(COMPUTE1_ID + "desc updated");
    computeEntity.setComputeCompositionData(computeData);

    doNothing().when(computeManager)
        .updateUniqueName(VSP_ID, VERSION, COMPONENT_ID, retrieved.getComputeCompositionData().getName(),
            computeData.getName());

    CompositionEntityValidationData validationData =
        computeManager.updateCompute(computeEntity);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(computeDao).update(computeEntity);
  }

  @Test
  public void testIllegalComputeUpdate() {
    doReturn(createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID))
        .when(computeDao).get(any());

    doReturn(true).when(vspInfoDao).isManual(any(), any());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.compute, COMPUTE1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    ComputeData computeData = new ComputeData();
    computeData.setName(COMPUTE1_ID + "_name_updated");
    computeData.setDescription(COMPUTE1_ID + " desc updated");
    computeEntity.setComputeCompositionData(computeData);

    CompositionEntityValidationData validationData =
        computeManager.updateCompute(computeEntity);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(computeDao, never()).update(computeEntity);
  }

  @Test
  public void testUpdateHEATComputeName() throws Exception {
    doReturn(createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID))
        .when(computeDao).get(any());

    String updatedName = COMPUTE1_ID + " name updated";
    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.compute, COMPUTE1_ID);

    toBeReturned.setErrors(Arrays.asList("#/name: "+updatedName+" is not a valid value."+
        COMPUTE1_ID+"is the only possible value for this field"));
    doReturn(toBeReturned).when(compositionEntityDataManagerMock).validateEntity(any(),any(),any());

    ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    ComputeData computeData = new ComputeData();
    computeData.setName(updatedName);
    computeData.setDescription(COMPUTE1_ID + " desc updated");
    computeEntity.setComputeCompositionData(computeData);

    CompositionEntityValidationData output = computeManager.updateCompute(computeEntity);

    Assert.assertEquals(output.getErrors(), toBeReturned.getErrors());
  }

  @Test
  public void testUpdateManualComputeQuestionnaire() throws Exception {
    String json = "{\"md5\" :\"FFDSD33SS\"}";
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    doReturn(new ComputeEntity(null, null, null, null)).when(computeDao).get(any());

    computeManager
        .updateComputeQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, json);
    verify(computeDao).updateQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, json);
  }

  @Test
  public void testGetNonExistingComputeId_negative() {
    testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing compute id",
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet() {
    ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(expected).when(computeDao).get(any());
    String compositionSchema = "schema string";
    doReturn(compositionSchema).when(computeManager).getComputeCompositionSchema(any());

    CompositionEntityResponse<ComputeData> response =
        computeManager.getCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert
        .assertEquals(response.getData().getName(), expected.getComputeCompositionData().getName());
    Assert.assertEquals(response.getData().getDescription(), expected.getComputeCompositionData().
        getDescription());
    Assert.assertEquals(response.getSchema(), compositionSchema);
  }

  @Test
  public void testGetQuestionnaire() throws Exception {
    ComputeEntity compute = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    compute.setQuestionnaireData("{}");
    doReturn(compute).when(computeDao)
        .getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);

    String schema = "schema string";

    doReturn(schema).when(computeManager).getComputeQuestionnaireSchema(any());

    QuestionnaireResponse questionnaire =
        computeManager.getComputeQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);

    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), compute.getQuestionnaireData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }

  @Test
  public void testDeleteOnNotManualCompute() {
    ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(expected).when(computeDao).get(any());
    testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testDeleteOnManualCompute() {
    ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    doReturn(expected).when(computeDao).get(any());
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    doNothing().when(computeManager).deleteUniqueValue(VSP_ID, VERSION, COMPONENT_ID,
        expected.getComputeCompositionData().getName());

    computeManager.deleteCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
    verify(computeDao).delete(any());
  }

  @Test
  public void testDeleteOnNotExistCompute() {
    testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }


  private void testDelete_negative(String vspId, Version version, String componentId,
                                   String computeId,
                                   String expectedErrorCode) {
    try {
      computeManager.deleteCompute(vspId, version, componentId, computeId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String computeId,
                                String expectedErrorCode) {
    try {
      computeManager.getCompute(vspId, version, componentId, computeId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String componentId,
                                 String expectedErrorCode, String expectedErrorMsg) {
    try {
      computeManager.listComputes(vspId, version, componentId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }


  private void testUpdate_negative(String vspId, Version version, String componentId,
                                   String computeId, String expectedErrorCode) {
    try {
      computeManager.updateCompute(new ComputeEntity(vspId, version, componentId, computeId));
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testCreate_negative(ComputeEntity computeEntity1, String expectedErrorCode) {
    try {
      computeManager.createCompute(computeEntity1);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private static ComputeEntity createCompute(String vspId, Version version, String compId,
                                             String computeId) {
    ComputeEntity computeEntity1 = new ComputeEntity(vspId, version, compId, computeId);
    ComputeData computeData = new ComputeData();
    computeData.setName(computeId + "name");
    computeData.setDescription(computeId + "desc");
    computeEntity1.setComputeCompositionData(computeData);
    return computeEntity1;
  }
}
