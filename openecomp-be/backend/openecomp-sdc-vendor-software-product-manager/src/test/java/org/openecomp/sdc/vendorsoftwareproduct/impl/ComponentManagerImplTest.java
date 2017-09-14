package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ComponentManagerImplTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  private static final String COMP_NOT_EXIST_MSG =
      "Vendor Software Product Component with Id 1 does not exist for Vendor Software Product with id VSP_ID and version 0.1";
  private static final String USER = "componentsTestUser";
  private static final String VSP_ID = "VSP_ID";
  private static final Version VERSION = new Version(0, 1);
  private static final String COMP1_ID = "1";
  private static final String COMP2_ID = "2";

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

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListWhenNone() {
    Collection<ComponentEntity> components =
        componentManager.listComponents(VSP_ID, VERSION, USER);
    Assert.assertEquals(components.size(), 0);
  }

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = "Vendor Software Product Component with Id dummyComponentId " +
          "does not exist for Vendor Software Product with id dummyVsp and version 1.0")
  public void validateExceptionWhenTryingToRetriveNotExistingComponentEntity() {
    Version version = Mockito.mock(Version.class);
    doReturn("1.0").when(version).toString();
    doReturn(null).when(componentDaoMock).get(anyObject());
    componentManager.validateComponentExistence("dummyVsp", version, "dummyComponentId",
        "dummyUser");
  }

  @Test
  public void testList() {
    doReturn(Arrays.asList(
        createComponent(VSP_ID, VERSION, COMP1_ID),
        createComponent(VSP_ID, VERSION, COMP2_ID)))
        .when(componentDaoMock).list(anyObject());

    Collection<ComponentEntity> actual =
        componentManager.listComponents(VSP_ID, VERSION, USER);
    Assert.assertEquals(actual.size(), 2);
  }

  @Test
  public void testDeleteListOnUploadVsp_negative() {
    testDeleteList_negative(VSP_ID, VERSION, USER,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

/*    @Test
    public void testCreate() {
        COMP1_ID = testCreate(VSP_ID);
    }*/
  @Test
  public void testCreate() {
    ComponentEntity expected = new ComponentEntity(VSP_ID, null, null);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name");
    compData.setDescription("comp1 desc");
    expected.setComponentCompositionData(compData);

    doReturn(true).when(vspInfoDao).isManual(anyObject(),anyObject());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    doReturn(vspComponentList).when(componentDaoMock).list(anyObject());
    doReturn(expected).when(compositionEntityDataManagerMock).createComponent(anyObject());

    ComponentEntity created = componentManager.createComponent(expected, USER);
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

    doReturn(true).when(vspInfoDao).isManual(anyObject(),anyObject());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    doReturn(vspComponentList).when(componentDaoMock).list(anyObject());

    try {
      ComponentEntity created = componentManager.createComponent(expected, USER);
    }  catch (CoreException exception) {
      log.debug("",exception);
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

    doReturn(expected).when(componentDaoMock).get(anyObject());
    doReturn(true).when(vspInfoDao).isManual(anyObject(),anyObject());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    doReturn(vspComponentList).when(componentDaoMock).list(anyObject());
    doReturn(new CompositionEntityValidationData(null,null)).when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(),anyObject(),anyObject());

    CompositionEntityValidationData created = componentManager.updateComponent(expected, USER);
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

    doReturn(expected).when(componentDaoMock).get(anyObject());
    doReturn(true).when(vspInfoDao).isManual(anyObject(),anyObject());
    Collection<ComponentEntity> vspComponentList = new ArrayList<>();
    vspComponentList.add(expected);
    ComponentEntity expected2 = new ComponentEntity(VSP_ID+"2", null, COMP1_ID+"2");
    expected2.setComponentCompositionData(compData);
    vspComponentList.add(expected2);
    doReturn(vspComponentList).when(componentDaoMock).list(anyObject());
    doReturn(new CompositionEntityValidationData(null,null)).when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(),anyObject(),anyObject());

    try {
      CompositionEntityValidationData created = componentManager.updateComponent(expected, USER);
    }  catch (CoreException exception) {
      log.debug("",exception);
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
    testCreate_negative(new ComponentEntity(VSP_ID, VERSION, null), USER,
        VendorSoftwareProductErrorCodes.VFC_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING);
  }

  @Test
  public void testUpdateNonExistingComponentId_negative() {
    String componentId = "non existing component id";
    doReturn(null).when(componentDaoMock).get(anyObject());

    testUpdate_negative(VSP_ID, VERSION, componentId, USER,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testUpdateOnUploadVsp() {
    doReturn(createComponent(VSP_ID, VERSION, COMP1_ID)).when(componentDaoMock)
        .get(anyObject());

    doReturn(new CompositionEntityValidationData(CompositionEntityType.component, COMP1_ID))
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName(COMP1_ID + " name");                // no change
    compData.setDisplayName(COMP1_ID + " display name"); // no change
    compData.setVfcCode(COMP1_ID + " display name"); // no change
    compData.setNfcCode(COMP1_ID + " display name"); // no change
    compData.setNfcFunction(COMP1_ID + " display name"); // no change
    compData.setDescription(COMP1_ID + " desc updated"); // allowed change
    component.setComponentCompositionData(compData);


    CompositionEntityValidationData validationData =
        componentManager.updateComponent(component, USER);
    Assert.assertTrue(validationData == null || validationData.getErrors() == null);
    verify(componentDaoMock).update(component);
  }

  @Test
  public void testIllegalUpdateOnUploadVsp() {
    doReturn(createComponent(VSP_ID, VERSION, COMP1_ID))
        .when(componentDaoMock).get(anyObject());

    CompositionEntityValidationData toBeReturned =
        new CompositionEntityValidationData(CompositionEntityType.component, COMP1_ID);
    toBeReturned.setErrors(Arrays.asList("error1", "error2"));
    doReturn(toBeReturned)
        .when(compositionEntityDataManagerMock)
        .validateEntity(anyObject(), anyObject(), anyObject());

    ComponentEntity component = new ComponentEntity(VSP_ID, VERSION, COMP1_ID);
    ComponentData compData = new ComponentData();
    compData.setName("comp1 name updated");// not allowed: changed name + omitted display name
    component.setComponentCompositionData(compData);

    CompositionEntityValidationData validationData =
        componentManager.updateComponent(component, USER);
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getErrors().size(), 2);

    verify(componentDaoMock, never()).update(component);
  }

  @Test
  public void testGetNonExistingComponentId_negative() {
    String componentId = "non existing component id";
    doReturn(null).when(componentDaoMock).get(anyObject());

    testGet_negative(VSP_ID, VERSION, componentId, USER,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
  }

  @Test
  public void testGet() {
    ComponentEntity expected = createComponent(VSP_ID, VERSION, COMP1_ID);
    doReturn(expected).when(componentDaoMock).get(anyObject());

    doReturn("schema string").when(componentManager).getComponentCompositionSchema(anyObject());

    testGet(VSP_ID, VERSION, COMP1_ID, USER, expected);
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
    testDelete_negative(VSP_ID, VERSION, COMP1_ID, USER,
        VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
  }

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = COMP_NOT_EXIST_MSG)
  public void testGetNonExistingComponentQuestionnaire() throws Exception {
    componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID, USER);
  }

  @Test
  public void testComponentNullQuestionnaire() throws Exception {
    doReturn(new ComponentEntity(VSP_ID, VERSION, COMP1_ID)).when(componentDaoMock)
        .getQuestionnaireData(VSP_ID, VERSION, COMP1_ID);
    String schema = "schema string";
    doReturn(schema).when(componentManager).getComponentQuestionnaireSchema(anyObject());

    QuestionnaireResponse questionnaire =
        componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID, USER);
    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), null);
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
        .when(nicManagerMock).listNics(VSP_ID, VERSION, COMP1_ID, USER);

    String schema = "schema string";
    doReturn(schema).when(componentManager).getComponentQuestionnaireSchema(anyObject());

    QuestionnaireResponse questionnaire =
        componentManager.getQuestionnaire(VSP_ID, VERSION, COMP1_ID, USER);
    Assert.assertNotNull(questionnaire);
    Assert.assertEquals(questionnaire.getData(), component.getQuestionnaireData());
    Assert.assertEquals(questionnaire.getSchema(), schema);
    Assert.assertNull(questionnaire.getErrorMessage());
  }

  @Test(expectedExceptions = CoreException.class,
      expectedExceptionsMessageRegExp = COMP_NOT_EXIST_MSG)
  public void testUpdateNonExistingComponentQuestionnaire() throws Exception {
    doReturn(null).when(componentDaoMock).get(anyObject());
    componentManager.updateQuestionnaire(VSP_ID, VERSION, COMP1_ID, "questionnaire data", USER);
  }

  @Test
  public void testUpdateQuestionnaire() throws Exception {
    ComponentEntity component = createComponent(VSP_ID, VERSION, COMP1_ID);
    doReturn(component).when(componentDaoMock).get(anyObject());

    componentManager.updateQuestionnaire(VSP_ID, VERSION, COMP1_ID, "questionnaire data", USER);

    verify(componentDaoMock)
        .updateQuestionnaireData(VSP_ID, VERSION, COMP1_ID, "questionnaire data");
  }

/*
    @Test(dependsOnMethods = "testDelete")
    public void testDeleteList() {
        ComponentEntity comp3 = new ComponentEntity(VSP_ID, null, null);
        comp3.setName("comp3 name");
        comp3.setDescription("comp3 desc");
        componentManager.createComponent(comp3, USER);

        componentManager.deleteComponents(VSP_ID, USER);

        Collection<ComponentEntity> actual = componentManager.listComponents(VSP_ID, null, USER);
        Assert.assertEquals(actual.size(), 0);
    }*/

  private void testGet(String vspId, Version version, String componentId, String user,
                       ComponentEntity expected) {

    CompositionEntityResponse<ComponentData>
        response = componentManager.getComponent(vspId, version, componentId, user);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(response.getData(), expected.getComponentCompositionData());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(ComponentEntity component, String user,
                                   String expectedErrorCode) {
    try {
      componentManager.createComponent(component, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String user,
                                String expectedErrorCode) {
    try {
      componentManager.getComponent(vspId, version, componentId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testUpdate_negative(String vspId, Version version, String componentId, String user,
                                   String expectedErrorCode) {
    try {
      componentManager
          .updateComponent(new ComponentEntity(vspId, version, componentId), user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testList_negative(String vspId, Version version, String user,
                                 String expectedErrorCode) {
    try {
      componentManager.listComponents(vspId, version, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDeleteList_negative(String vspId, Version version, String user,
                                       String expectedErrorCode) {
    try {
      componentManager.deleteComponents(vspId, version, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testDelete_negative(String vspId, Version version, String componentId, String user,
                                   String expectedErrorCode) {
    try {
      componentManager.deleteComponent(vspId, version, componentId, user);
      Assert.fail();
    } catch (CoreException exception) {
      log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }


  public static ComponentEntity createComponent(String vspId, Version version, String compId) {
    ComponentEntity componentEntity = new ComponentEntity(vspId, version, compId);
    ComponentData compData = new ComponentData();
    compData.setName(compId + " name");
    compData.setDisplayName(compId + " display name");
    compData.setVfcCode(compId + " display name");
    compData.setDescription(compId + " desc");
    componentEntity.setComponentCompositionData(compData);
    componentEntity.setQuestionnaireData("{}");
    return componentEntity;
  }
}