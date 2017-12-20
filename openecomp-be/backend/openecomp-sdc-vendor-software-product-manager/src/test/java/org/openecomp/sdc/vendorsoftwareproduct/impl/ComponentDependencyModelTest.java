package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class ComponentDependencyModelTest {

  private static final String VSP_ID = "vsp_id";
  private static final Version VERSION = new Version("version_id");
  private static final String COMP_DEP_ID = "comp_dep_id";

  private static final String COMP_ID_1 = "comp_id_1";
  private static final String COMP_ID_2 = "comp_id_2";
  private static final String COMP_ID_3 = "comp_id_3";
  private static final String COMP_ID_4 = "comp_id_4";

  @Spy
  @InjectMocks
  private ComponentDependencyModelManagerImpl componentDependencyModelManager;
  @Mock
  private ComponentManager componentManager;
  @Mock
  private ComponentDependencyModelDao componentDependencyModelDao;

  @BeforeMethod
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListDependency() {
    List<ComponentDependencyModelEntity> entities = new ArrayList<>();
    entities.add(createModelEntity(COMP_ID_1, COMP_ID_2));
    entities.add(createModelEntity(COMP_ID_3, COMP_ID_4));

    Mockito.when(componentDependencyModelDao
        .list(new ComponentDependencyModelEntity(VSP_ID, VERSION, null)))
        .thenReturn(entities);

    Collection<ComponentDependencyModelEntity> list =
        componentDependencyModelManager.list(VSP_ID, VERSION);

    Mockito.verify(componentDependencyModelDao, Mockito.times(1))
        .list(new ComponentDependencyModelEntity(VSP_ID, VERSION, null));

    Assert.assertEquals(2, list.size());
  }

  @Test
  public void testCreateDependency() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_2);

    componentDependencyModelManager.createComponentDependency(modelEntity, VSP_ID, VERSION);
    Mockito.verify(componentDependencyModelDao, Mockito.times(1)).create(modelEntity);
  }

  @Test
  public void testCreateDependencyNegative_SameSourceTarget() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_1);
    testCreateDependency_negative(modelEntity, VSP_ID, VERSION,
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().message());
  }

  @Test
  public void testCreateDependencyNegative_NoSourceId() {

    ComponentDependencyModelEntity modelEntity = createModelEntity(null, COMP_ID_1);
    testCreateDependency_negative(modelEntity, VSP_ID, VERSION,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());


    ComponentDependencyModelEntity modelEntity1 = createModelEntity("", COMP_ID_1);
    testCreateDependency_negative(modelEntity1, VSP_ID, VERSION,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());
  }

  @Test
  public void testUpdateDependency() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_2);
    modelEntity.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity);

    componentDependencyModelManager.update(modelEntity);
    Mockito.verify(componentDependencyModelDao, Mockito.times(1)).update(modelEntity);
  }

  @Test
  public void testUpdateDependencyNegative_NoSourceId() {

    ComponentDependencyModelEntity modelEntity = createModelEntity(null, COMP_ID_1);
    modelEntity.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity);

    testUpdateDependency_negative(modelEntity,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());

    ComponentDependencyModelEntity modelEntity1 = createModelEntity("", COMP_ID_1);
    modelEntity1.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity1);

    testUpdateDependency_negative(modelEntity1,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());
  }

  @Test
  public void testUpdateDependencyNegative_SameSourceTarget() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_1);
    modelEntity.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity);
    testUpdateDependency_negative(modelEntity,
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().message());
  }

  @Test
  public void testDeleteDependency() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_2);
    modelEntity.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity);

    componentDependencyModelManager.delete(VSP_ID, VERSION, COMP_DEP_ID);
    Mockito.verify(componentDependencyModelDao, Mockito.times(1)).delete(modelEntity);
  }

  @Test
  public void testDeleteInvalidDependency() {
    ComponentDependencyModelEntity delModelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_2);
    delModelEntity.setId(COMP_DEP_ID);

    try {
      componentDependencyModelManager.delete(VSP_ID, VERSION, COMP_DEP_ID);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), "VERSIONABLE_SUB_ENTITY_NOT_FOUND");
      Assert.assertEquals(exception.getMessage(),
          String.format("Vendor Software Product Component Dependency Model with Id %s " +
                  "does not exist for Vendor Software Product with id %s and version %s",
              COMP_DEP_ID, VSP_ID, VERSION.getId()));
    }
  }


  @Test
  public void testGetDependency() {
    ComponentDependencyModelEntity modelEntity =
        createModelEntity(COMP_ID_1, COMP_ID_2);
    modelEntity.setId(COMP_DEP_ID);

    when(componentDependencyModelDao.get(anyObject())).thenReturn(modelEntity);

    ComponentDependencyModelEntity retrieved =
        componentDependencyModelManager.get(VSP_ID, VERSION, COMP_DEP_ID);

    Assert.assertEquals(retrieved.getSourceComponentId(), COMP_ID_1);

  }

  private ComponentDependencyModelEntity createModelEntity(String sourceId, String targetId) {
    ComponentDependencyModelEntity entity =
        new ComponentDependencyModelEntity(VSP_ID, VERSION, COMP_DEP_ID);
    entity.setSourceComponentId(sourceId);
    entity.setTargetComponentId(targetId);
    entity.setRelation("dependsOn");
    return entity;
  }

  private void testCreateDependency_negative(ComponentDependencyModelEntity entity, String vspId,
                                             Version version, String expectedErrorCode,
                                             String expectedErrorMsg) {
    try {
      componentDependencyModelManager.createComponentDependency(entity, vspId, version);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }

  private void testUpdateDependency_negative(ComponentDependencyModelEntity entity,
                                             String expectedErrorCode, String expectedErrorMsg) {
    try {
      componentDependencyModelManager.update(entity);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }
}
