package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;
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
@PrepareForTest({DeploymentFlavorsImpl.class, DeploymentFlavorManagerFactory.class})
public class DeploymentFlavorsImplTest {

  private Logger logger = LoggerFactory.getLogger(DeploymentFlavorsImplTest.class);


  @Mock
  private DeploymentFlavorManagerFactory deploymentFlavorManagerFactory;

  @Mock
  private DeploymentFlavorManager deploymentFlavorManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String deploymentFlavorId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(DeploymentFlavorManagerFactory.class);
      when(DeploymentFlavorManagerFactory.getInstance()).thenReturn(deploymentFlavorManagerFactory);
      when(deploymentFlavorManagerFactory.createInterface()).thenReturn(deploymentFlavorManager);

      DeploymentFlavorEntity e = new DeploymentFlavorEntity();
      e.setId(deploymentFlavorId);
      e.setVspId(vspId);
      e.setVersion(new Version(versionId));

      Collection<DeploymentFlavorEntity> lst = Collections.singletonList(e);
      when(deploymentFlavorManager.listDeploymentFlavors(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any())).thenReturn(lst);

      when(deploymentFlavorManager.createDeploymentFlavor(
              ArgumentMatchers.any())).thenReturn(e);

      CompositionEntityResponse<DeploymentFlavor> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(deploymentFlavorManager.getDeploymentFlavor(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(deploymentFlavorId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(deploymentFlavorManager.updateDeploymentFlavor(
              ArgumentMatchers.any())).thenReturn(data);



      when(deploymentFlavorManager.getDeploymentFlavorSchema(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any())).thenReturn(r);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();

    Response rsp = dfi.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<DeploymentFlavorCreationDto> results = (GenericCollectionWrapper<DeploymentFlavorCreationDto>) e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }

  @Test
  public void testCreate() {

    DeploymentFlavorRequestDto dto = new DeploymentFlavorRequestDto();
    dto.setDescription("hello");
    dto.setModel("model");
    dto.setFeatureGroupId("fgi");

    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();
    Response rsp = dfi.create(dto, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      DeploymentFlavorCreationDto responseDto = (DeploymentFlavorCreationDto)e;
      Assert.assertEquals(deploymentFlavorId, responseDto.getDeploymentFlavorId());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();
    Response rsp = dfi.delete(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();
    Response rsp = dfi.get(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testGetSchema() {
    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();
    Response rsp = dfi.get(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    DeploymentFlavorsImpl dfi = new DeploymentFlavorsImpl();
    DeploymentFlavorRequestDto dto = new DeploymentFlavorRequestDto();
    Response rsp = dfi.update(dto, vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

}
