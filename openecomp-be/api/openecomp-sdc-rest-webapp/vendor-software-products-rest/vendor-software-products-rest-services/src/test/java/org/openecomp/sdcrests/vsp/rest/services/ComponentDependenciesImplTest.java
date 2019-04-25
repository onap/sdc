package org.openecomp.sdcrests.vsp.rest.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRelationType;
import org.openecomp.sdcrests.vsp.rest.ComponentDependencies;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentDependenciesImpl.class, ComponentDependencyModelManagerFactory.class})
public class ComponentDependenciesImplTest {

  private Logger logger = LoggerFactory.getLogger(org.openecomp.sdcrests.vsp.rest.services.ComponentDependenciesImplTest.class);

  @Mock
  private ComponentDependencyModelManagerFactory componentDependencyModelManagerFactory;

  @Mock
  private ComponentDependencyModelManager componentDependencyModelManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String entityId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ComponentDependencyModelManagerFactory.class);
      when(ComponentDependencyModelManagerFactory.getInstance()).thenReturn(componentDependencyModelManagerFactory);
      when(componentDependencyModelManagerFactory.createInterface()).thenReturn(componentDependencyModelManager);

      ComponentDependencyModelEntity e = new ComponentDependencyModelEntity();
      e.setSourceComponentId("sourceid");
      e.setTargetComponentId("targetid");
      e.setVspId(vspId);
      e.setVersion(new Version(versionId));
      e.setRelation(ComponentRelationType.dependsOn.name());
      e.setId(entityId);

      // create
      when(componentDependencyModelManager.createComponentDependency(
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(vspId),
          ArgumentMatchers.any())).thenReturn(e);

      // list
      Collection<ComponentDependencyModelEntity> entities =
          Collections.singletonList(e);
      when(componentDependencyModelManager.list(
          ArgumentMatchers.eq(vspId),
          ArgumentMatchers.any())).thenReturn(entities);

      // get
      when(componentDependencyModelManager.get(
          ArgumentMatchers.eq(vspId),
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(entityId)
          )).thenReturn(e);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testCreate() {
    ComponentDependencyModel model = new ComponentDependencyModel();
    model.setRelationType(ComponentRelationType.dependsOn.name());
    model.setSourceId("sourceid");
    model.setTargetId("targetid");


    ComponentDependencies componentDependencies = new ComponentDependenciesImpl();
    Response rsp = componentDependencies.create(model, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      ComponentDependencyCreationDto dto = (ComponentDependencyCreationDto) e;
      Assert.assertEquals("resulting entityId must match", dto.getId(), entityId);
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }

  @Test
  public void testList() {

    ComponentDependencies componentDependencies = new ComponentDependenciesImpl();
    Response rsp = componentDependencies.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      @SuppressWarnings("unchecked")
      GenericCollectionWrapper<ComponentDependencyResponseDto> results =
          (GenericCollectionWrapper<ComponentDependencyResponseDto>) e;

      Assert.assertEquals("result length", 1, results.getListCount());
      Assert.assertEquals("resulting entityId must match", results.getResults().get(0).getId(), entityId);
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {

    ComponentDependencies componentDependencies = new ComponentDependenciesImpl();

    Response rsp = componentDependencies.delete(vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testUpdate() {

    ComponentDependencies componentDependencies = new ComponentDependenciesImpl();

    ComponentDependencyModel model = new ComponentDependencyModel();
    model.setRelationType(ComponentRelationType.dependsOn.name());
    model.setSourceId("sourceid");
    model.setTargetId("targetid");

    Response rsp = componentDependencies.update(model, vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGet() {

    ComponentDependencies componentDependencies = new ComponentDependenciesImpl();
    Response rsp = componentDependencies.get(vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
    try {
      ComponentDependencyResponseDto dto = (ComponentDependencyResponseDto) rsp.getEntity();
      Assert.assertEquals("resulting entityId must match", dto.getId(), entityId);
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + rsp.getEntity().getClass().getName());
    }



  }
}
