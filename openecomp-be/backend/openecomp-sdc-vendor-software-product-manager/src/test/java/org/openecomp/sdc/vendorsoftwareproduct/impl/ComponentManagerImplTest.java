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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
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

public class ComponentManagerImplTest {
  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version("version_id");
  private static final String COMP1_ID = "comp1";
  private static final String COMP2_ID = "comp2";
  private static final String COMP_NOT_EXIST_MSG =
      "Vendor Software Product Component with Id comp1 does not exist " +
          "for Vendor Software Product with id VSP_ID and version version_id";

  @Mock
  private ComponentDao componentDaoMock;
  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;
  @Mock
  private NicManager nicManagerMock;
  @Mock
  private VendorSoftwareProductInfoDao vspInfoDao;
  @InjectMocks
  @Spy
  private ComponentManagerImpl componentManager;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() {
    componentManager = null;
  }

  @Test
  public void testListWhenNone() {
    Collection<ComponentEntity> components = componentManager.listComponents(VSP_ID, VERSION);
    Assert.assertEquals(components.size(), 0);
  }

  @Test(expected = CoreException.class)
  public void validateExceptionWhenTryingToRetriveNotExistingComponentEntity() {
    doReturn(null).when(componentDaoMock).get(any());
    componentManager.validateComponentExistence(VSP_ID, VERSION, COMP1_ID);
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createComponent(VSP_ID, VERSION, COMP1_ID),
        createComponent(VSP_ID, VERSION, COMP2_ID)))
        .when(componentDaoMock).list(any());

    Collection<ComponentEntity> actual = componentManager.listComponents(VSP_ID, VERSION);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test
  public void testDeleteListOnUploadVsp_negative() {
    testDeleteList_negative(VSP_ID, VERSION,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test
  public void testCreate() {
    ComponentEntity expected = new ComponentEntity(VSP_ID, null, null);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name");
    compData.setDescription("comp1 desc");
    expected.setComponentCompositionData(compData);

    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    doReturn(vspComponentList).when(componentDaoMock).list(any());
    doReturn(expected).when(compositionEntityDataManagerMock).createComponent(any(), ArgumentMatchers.anyBoolean());

    ComponentEntity created = componentManager.createComponent(expected);
    Assert.assertNotNull(created);
    //expected.setId(created.getId());
    //expected.setVersion(VERSION);

    //ComponentEntity actual = componentDaoMock.getComponent(VSP_ID, VERSION, created.getId());

    //Assert.assertEquals(actual, expected);
    //return created.getId();
  }

  @Test
  public void testCreateWithVspCompListMoreThanOne() {
    ComponentEntity expected = new ComponentEntity(VSP_ID, null, null);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name");
    compData.setDescription("comp1 desc");
    expected.setComponentCompositionData(compData);

    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    doReturn(vspComponentList).when(componentDaoMock).list(any());

    try {
       componentManager.createComponent(expected);
    } catch (CoreException exception) {
      Assert.assertEquals("Creation of only one VFC per VSP allowed.", exception.code().message());
      Assert.assertEquals(VendorSoftwareProductErrorCodes.VSP_VFC_COUNT_EXCEED,
          exception.code().id());
    }
  }

  @Test
  public void testUpdateComp() {
    ComponentEntity expected = new ComponentEntity(VSP_ID, null, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name");
    compData.setDescription("comp1 desc");
    expected.setComponentCompositionData(compData);

    doReturn(expected).when(componentDaoMock).get(any());
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    doReturn(vspComponentList).when(componentDaoMock).list(any());
    doReturn(new CompositionEntityValidationData(null, null)).when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    CompositionEntityValidationData created = componentManager.updateComponent(expected);
    Assert.assertNotNull(created);
  }

  @Test
  public void testUpdateCompWithSameVfcDisplayName() {
    ComponentEntity expected = new ComponentEntity(VSP_ID, null, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name");
    compData.setDescription("comp1 desc");
    compData.setDisplayName("comp1 displayname");
    expected.setComponentCompositionData(compData);

    doReturn(expected).when(componentDaoMock).get(any());
    doReturn(true).when(vspInfoDao).isManual(any(), any());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    ComponentEntity expected2 = new ComponentEntity(VSP_ID + "2", null, COMP1_ID + "2");
    expected2.setComponentCompositionData(compData);
    vspComponentList.add(expected2);
    doReturn(vspComponentList).when(componentDaoMock).list(any());
    doReturn(new CompositionEntityValidationData(null, null)).when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    try {
       componentManager.updateComponent(expected);
    } catch (CoreException exception) {
      Assert.assertEquals("VFC with specified name already present in given VSP.",
          exception.code().message());
      Assert.assertEquals(VendorSoftwareProductErrorCodes.VSP_VFC_DUPLICATE_NAME,
          exception.code().id());
    }
  }

/*    @Test
    public void testCreateWithExistingName_negative() {
        ComponentEntity component = new ComponentEntity(VSP_ID, null, null);
        ComponentData compData = new ComponentData();
        compData.setName("comp1 name");
        compData.setDescription("comp1 desc");
        component.setComponentCompositionData(compData);
        testCreate_negative(component, USER, UniqueValueUtil.UNIQUE_VALUE_VIOLATION);
    }*/

/*    @Test
    public void testCreateWithExistingNameUnderOtherVsp() {
        testCreate(vsp2Id);
    }*/

  @Test
  public void testCreateOnUploadVsp_negative() {
    testCreate_negative(new ComponentEntity(VSP_ID, VERSION, null),
        VendorSoftwareProductErrorCodes.VFC_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testUpdateNonExistingComponentId_negative() {
    String componentId = "non existing component id";
    doReturn(null).when(componentDaoMock).get(any());

    testUpdate_negative(VSP_ID, VERSION, componentId,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateOnUploadVsp() {
    doReturn(createComponent(VSP_ID, VERSION, COMP1_ID)).when(componentDaoMock)
        .get(any());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.component, COMP1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName(COMP1_ID + " name");                // no change
    compData.setDisplayName(COMP1_ID + " display name"); // no change
    compData.setDescription(COMP1_ID + " desc updated"); // allowed change
    component.setComponentCompositionData(compData);


    CompositionEntityValidationData validationData =
        componentManager.updateComponent(component);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(componentDaoMock).update(component);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createComponent(VSP_ID, VERSION, COMP1_ID))
        .when(componentDaoMock).get(any());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.component, COMP1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(any(), any(), any());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name updated");// not allowed: changed name + omitted display name
    component.setComponentCompositionData(compData);

    CompositionEntityValidationData validationData =
        componentManager.updateComponent(component);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(componentDaoMock, never()).update(component);
  }

  @Test
  public void testGetNonExistingComponentId_negative() {
    String componentId = "non existing component id";
    doReturn(null).when(componentDaoMock).get(any());

    testGet_negative(VSP_ID, VERSION, componentId,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet() {
    ComponentEntity expected = createComponent(VSP_ID, VERSION, COMP1_ID);
    doReturn(expected).when(componentDaoMock).get(any());

    doReturn("schema string").when(componentManager).getComponentCompositionSchema(any());

    testGet(VSP_ID, VERSION, COMP1_ID, expected);
  }




/*
    @Test(dependsOnMethods = {"testUpdateOnUploadVsp", "testList"})
    public void testCreateWithERemovedName() {
        testCreate(VSP_ID);
    }

    @Test(dependsOnMethods = "testList")
    public void testDeleteNonExistingComponentId_negative() {
        testDelete_negative(VSP_ID, "non existing component id", USER, VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }*/



/*
    @Test(dependsOnMethods = "testList")
    public void testDelete() {
        componentManager.deleteComponent(VSP_ID, COMP1_ID, USER);
        ComponentEntity actual = componentDaoMock.getComponent(VSP_ID, VERSION, COMP1_ID);
        Assert.assertNull(actual);
    }*/

  @Test
  public void testDeleteOnUploadVsp_negative() {
    testDelete_negative(VSP_ID, VERSION, COMP1_ID,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test(expected = CoreException.class)
  public void testGetNonExistingComponentQuestionnaire() throws Exception {
    componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID);
  }

  @Test
  public void testComponentNullQuestionnaire() {
    doReturn(new ComponentEntity(VSP_ID, VERSION, COMP1_ID)).when(componentDaoMock)
        .getQuestionnaireData(VSP_ID, VERSION, COMP1_ID);
    String schema = "schema string";
    doReturn(schema).when(componentManager).getComponentQuestionnaireSchema(any());

    QuestionnaireResponse questionnaire =
        componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID);
    Assert.assertNotNull(questionnaire);
    Assert.assertNull(questionnaire.getData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }


  @Test
  public void testGetQuestionnaire() throws Exception {
    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, COMP1_ID);
    component.setQuestionnaireData("{}");
    doReturn(component).when(componentDaoMock).getQuestionnaireData(VSP_ID, VERSION, COMP1_ID);

    NicEntity nicEntity1 = new NicEntity();
    Nic nic1 = new Nic();
    nic1.setName("nic1");
    nicEntity1.setNicCompositionData(nic1);

    NicEntity nicEntity2 = new NicEntity();
    Nic nic2 = new Nic();
    nic2.setName("nic2");
    nicEntity2.setNicCompositionData(nic2);

    doReturn(Arrays.asList(nicEntity1, nicEntity2))
        .when(nicManagerMock).listNics(VSP_ID, VERSION, COMP1_ID);

    String schema = "schema string";
    doReturn(schema).when(componentManager).getComponentQuestionnaireSchema(any());

    QuestionnaireResponse questionnaire =
        componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID);
    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), component.getQuestionnaireData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }

  @Test(expected = CoreException.class)
  public void testUpdateNonExistingComponentQuestionnaire() throws Exception {
    doReturn(null).when(componentDaoMock).get(any());
    componentManager.updateQuestionnaire(VSP_ID, VERSION, COMP1_ID, "questionnaire data");
  }

  @Test
  public void testUpdateQuestionnaire() throws Exception {
    ComponentEntity component = createComponent(VSP_ID, VERSION, COMP1_ID);
    doReturn(component).when(componentDaoMock).get(any());

    componentManager.updateQuestionnaire(VSP_ID, VERSION, COMP1_ID, "questionnaire data");

    verify(componentDaoMock)
        .updateQuestionnaireData(VSP_ID, VERSION, COMP1_ID, "questionnaire data");
  }

  private void testGet(String vspId, Version version, String componentId,
                       ComponentEntity expected) {

    CompositionEntityResponse<ComponentData>
        response = componentManager.getComponent(vspId, version, componentId);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getComponentCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(ComponentEntity component,
                                   String expectedErrorCode) {
    try {
      componentManager.createComponent(component);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId,
                                String expectedErrorCode) {
    try {
      componentManager.getComponent(vspId, version, componentId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String componentId,
                                   String expectedErrorCode) {
    try {
      componentManager.updateComponent(new ComponentEntity(vspId, version, componentId));
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteList_negative(String vspId, Version version,
                                       String expectedErrorCode) {
    try {
      componentManager.deleteComponents(vspId, version);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String componentId,
                                   String expectedErrorCode) {
    try {
      componentManager.deleteComponent(vspId, version, componentId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }


  private static ComponentEntity createComponent(String vspId, Version version, String compId) {
    ComponentEntity componentEntity = new ComponentEntity(vspId, version, compId);
    ComponentData compData = new ComponentData();
    compData.setName(compId + " name");
    compData.setDisplayName(compId + " display name");
    compData.setDescription(compId + " desc");
    componentEntity.setComponentCompositionData(compData);
    componentEntity.setQuestionnaireData("{}");
    return componentEntity;
  }
}
