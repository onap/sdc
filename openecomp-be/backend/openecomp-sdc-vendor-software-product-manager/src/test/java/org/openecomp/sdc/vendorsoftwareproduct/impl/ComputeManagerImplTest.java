package org.openecomp.sdc.vendorsoftwareproduct.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComputeManagerImplTest {

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

    private static final String COMPUTE_NOT_EXIST_MSG =
            "Vendor Software Product COMPUTE with Id compute1 does not exist for Vendor Software Product with " +
                    "id VSP_ID and version 0.1";

    private static final String USER = "computeTestUser";
    private static final String VSP_ID = "VSP_ID";
    private static final Version VERSION = new Version(0, 1);
    private static final String COMPONENT_ID = "COMPONENT_ID";
    private static final String COMPUTE1_ID = "compute1";
    private static final String COMPUTE2_ID = "compute2";

  @Mock
  private ComputeDao computeDao;

  @Mock
  private CompositionEntityDataManager compositionEntityDataManagerMock;

  @Mock
  private NetworkManager networkManagerMock;

  @Mock
  private VendorSoftwareProductInfoDao vspInfoDao;

  @Mock
  private ComputeEntity computeEntity;

  @Mock
  private ListComputeResponse listComputeResponse;

  @Mock
  private DeploymentFlavorEntity deploymentFlavorEntity;

  @Mock
  private DeploymentFlavorDao deploymentFlavorDao;

  @Mock
  private ComponentComputeAssociation componentComputeAssociation;

  @Mock
  DeploymentFlavor deploymentFlavor;

  @InjectMocks
  @Spy
  private ComputeManagerImpl computeManager;

  @BeforeMethod
  public void setUp() throws Exception {
      MockitoAnnotations.initMocks(this);
  }

   @Test
  public void testListWhenNone() {
    Collection<ListComputeResponse> computes = computeManager.listCompute(VSP_ID, VERSION, COMPONENT_ID, USER);
    Assert.assertEquals(computes.size(), 0);
  }

  @Test
  public void testList(){
      doReturn(Arrays.asList(
        createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID),
        createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE2_ID)))
              .when(computeDao).list(anyObject());


      Collection<ListComputeResponse> computes = computeManager.listCompute(VSP_ID, VERSION, COMPONENT_ID, USER);
      Assert.assertEquals(computes.size(), 2);
      for (ListComputeResponse compute : computes) {
          Assert.assertEquals(compute.getComputeEntity().getComputeCompositionData().getName(),
                  COMPUTE1_ID.equals(compute.getComputeEntity().getId())
                          ? "compute1name"
                          : "compute2name");
      };
  }

    @Test
    public void testCreateOnNotManualCompute_negative() {

        testCreate_negative(new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, null), USER,
                VendorSoftwareProductErrorCodes.ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    }

    @Test
    public void testCreateManualCompute() {
        ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());
        computeManager.createCompute(expected, USER);
        verify(compositionEntityDataManagerMock).createCompute(expected);
        verify(compositionEntityDataManagerMock).createCompute(expected);
    }

    @Test
    public void testCreateManualComputeWithDuplicateName() {
        ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

        ComputeEntity expectedDiffName = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        expectedDiffName.setId(COMPUTE1_ID + "Name");
        ComputeData computeData = expectedDiffName.getComputeCompositionData();
        computeData.setName(COMPUTE1_ID + "Name");
        expectedDiffName.setComputeCompositionData(computeData);
        List<ComputeEntity> vfcImageList = new ArrayList<ComputeEntity>();
        vfcImageList.add(expectedDiffName);
        doReturn(vfcImageList).when(computeDao).list(anyObject());

        try {
            computeManager.createCompute(expected, USER);
            Assert.fail();
        }
        catch (CoreException ex) {
          log.debug("",ex);
            Assert.assertEquals(VendorSoftwareProductErrorCodes.DUPLICATE_COMPUTE_NAME_NOT_ALLOWED,
                    ex.code().id());
        }
    }

    @Test
    public void testUpdateNonExistingComputeId_negative() {

        testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER,
                VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test
    public void testUpdateCompute() {
        doReturn(createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID))
                .when(computeDao).get(anyObject());

        doReturn(new CompositionEntityValidationData(CompositionEntityType.compute, COMPUTE1_ID))
                .when(compositionEntityDataManagerMock)
                .validateEntity(anyObject(), anyObject(), anyObject());

        ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        ComputeData computeData = new ComputeData();
        computeData.setName(COMPUTE1_ID + "name");
        computeData.setDescription(COMPUTE1_ID + "desc updated");
        computeEntity.setComputeCompositionData(computeData);

        CompositionEntityValidationData validationData =
                computeManager.updateCompute(computeEntity, USER);
        Assert.assertTrue(validationData == null || validationData.getErrors() == null);
        verify(computeDao).update(computeEntity);
    }

    @Test
    public void testIllegalComputeUpdate() {
        doReturn(createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID))
                .when(computeDao).get(anyObject());

        doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());

        CompositionEntityValidationData toBeReturned =
                new CompositionEntityValidationData(CompositionEntityType.compute, COMPUTE1_ID);
        toBeReturned.setErrors(Arrays.asList("error1", "error2"));
        doReturn(toBeReturned)
                .when(compositionEntityDataManagerMock)
                .validateEntity(anyObject(), anyObject(), anyObject());

        ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        ComputeData computeData = new ComputeData();
        computeData.setName(COMPUTE1_ID + "_name_updated");
        computeData.setDescription(COMPUTE1_ID + " desc updated");
        computeEntity.setComputeCompositionData(computeData);

        CompositionEntityValidationData validationData = computeManager.updateCompute(computeEntity, USER);
        Assert.assertNotNull(validationData);
        Assert.assertEquals(validationData.getErrors().size(), 2);

        verify(computeDao, never()).update(computeEntity);
    }

    @Test
    public void testUpdateHEATComputeName() throws Exception {
        doReturn(createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID))
                .when(computeDao).get(anyObject());
        ComputeEntity computeEntity = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        ComputeData computeData = new ComputeData();
        computeData.setName(COMPUTE1_ID + " name updated");
        computeData.setDescription(COMPUTE1_ID + " desc updated");
        computeEntity.setComputeCompositionData(computeData);

        try {
            computeManager.updateCompute(computeEntity, USER);
        }
        catch (CoreException ex) {
          log.debug("",ex);
            Assert.assertEquals(ex.code().id(), VendorSoftwareProductErrorCodes.UPDATE_COMPUTE_NOT_ALLOWED);
        }

    }

    @Test
    public void testUpdateManualComputeQuestionnaire() throws Exception {
        String json = "{\"md5\" :\"FFDSD33SS\"}";
        doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());
        doReturn(new ComputeEntity(null,null,null,null)).when(computeDao).get(anyObject());

        computeManager.updateComputeQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, json, USER);
        verify(computeDao).updateQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, json);
    }

    @Test
    public void testGetNonExistingComputeId_negative() {
        testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing compute id", USER,
                VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test
    public void testGet() {
        ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        doReturn(expected).when(computeDao).get(anyObject());
        String compositionSchema = "schema string";
        doReturn(compositionSchema).when(computeManager).getComputeCompositionSchema(anyObject());

        CompositionEntityResponse<ComputeData> response =
                computeManager.getCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER);
        Assert.assertEquals(response.getId(), expected.getId());
        Assert.assertEquals(response.getData().getName(), expected.getComputeCompositionData().getName());
        Assert.assertEquals(response.getData().getDescription(), expected.getComputeCompositionData().
                getDescription());
        Assert.assertEquals(response.getSchema(), compositionSchema);
    }

    @Test
    public void testGetQuestionnaire() throws Exception {
        ComputeEntity compute = new ComputeEntity(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        compute.setQuestionnaireData("{}");
        doReturn(compute).when(computeDao).getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);

        String schema = "schema string";


        doReturn(schema).when(computeManager).getComputeQuestionnaireSchema(anyObject());

        QuestionnaireResponse questionnaire =
                computeManager.getComputeQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER);

        Assert.assertNotNull(questionnaire);
        Assert.assertEquals(questionnaire.getData(), compute.getQuestionnaireData());
        Assert.assertEquals(questionnaire.getSchema(), schema);
        Assert.assertNull(questionnaire.getErrorMessage());
    }

    @Test
    public void testDeleteOnNotManualCompute() {
        ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        doReturn(expected).when(computeDao).get(anyObject());
        testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER,
                VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
    }

    @Test
    public void testDeleteOnManualCompute() {
        ComputeEntity expected = createCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID);
        doReturn(expected).when(computeDao).get(anyObject());
        doReturn(true).when(vspInfoDao).isManual(anyObject(), anyObject());
        computeManager.deleteCompute(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER);
        verify(computeDao).delete(anyObject());
    }

    @Test
    public void testDeleteOnNotExistCompute() {
        testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, COMPUTE1_ID, USER,
                VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
    }


    private void testDelete_negative(String vspId, Version version, String componentId, String computeId,
                                     String user,
                                     String expectedErrorCode) {
        try {
            computeManager.deleteCompute(vspId, version, componentId, computeId, user);
            Assert.fail();
        } catch (CoreException exception) {
          log.debug("",exception);
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

    private void testGet_negative(String vspId, Version version, String componentId, String computeId,
                                String user, String expectedErrorCode) {
      try {
          computeManager.getCompute(vspId, version, componentId, computeId, user);
          Assert.fail();
      } catch (CoreException exception) {
        log.debug("",exception);
          Assert.assertEquals(exception.code().id(), expectedErrorCode);
      }
  }

    private void testList_negative(String vspId, Version version, String componentId, String user,
                                   String expectedErrorCode, String expectedErrorMsg) {
        try {
            computeManager.listCompute(vspId, version, componentId, user);
            Assert.fail();
        } catch (CoreException exception) {
          log.debug("",exception);
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
            Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
        }
    }



    private void testUpdate_negative(String vspId, Version version, String componentId, String computeId,
                                 String user, String expectedErrorCode) {
  try {
    computeManager.updateCompute(new ComputeEntity(vspId, version, componentId, computeId), user);
    Assert.fail();
  } catch (CoreException exception) {
    log.debug("",exception);
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
    }
  }

  private void testCreate_negative(ComputeEntity computeEntity1, String user, String expectedErrorCode) {
        try {
            computeManager.createCompute(computeEntity1, user);
            Assert.fail();
        } catch (CoreException exception) {
          log.debug("",exception);
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

  static ComputeEntity createCompute(String vspId, Version version, String compId, String computeId){
      ComputeEntity computeEntity1 = new ComputeEntity(vspId, version, compId, computeId);
      ComputeData computeData = new ComputeData();
      computeData.setName(computeId+"name");
      computeData.setDescription(computeId+"desc");
      computeEntity1.setComputeCompositionData(computeData);
      return computeEntity1;
  }
}
