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
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
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
@PrepareForTest({ComputeImpl.class, ComponentManagerFactory.class, ComputeManagerFactory.class})
public class ComputeImplTest {

  private Logger logger = LoggerFactory.getLogger(ComputeImplTest.class);

  @Mock
  private ComputeManagerFactory computeManagerFactory;

  @Mock
  private ComputeManager computeManager;

  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String computeId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ComponentManagerFactory.class);
      when(ComponentManagerFactory.getInstance()).thenReturn(componentManagerFactory);
      when(componentManagerFactory.createInterface()).thenReturn(componentManager);

      mockStatic(ComputeManagerFactory.class);
      when(ComputeManagerFactory.getInstance()).thenReturn(computeManagerFactory);
      when(computeManagerFactory.createInterface()).thenReturn(computeManager);


      ListComputeResponse lcr = new ListComputeResponse();
      lcr.setAssociatedWithDeploymentFlavor(false);
      lcr.setComputeEntity(new ComputeEntity());
      lcr.getComputeEntity().setComponentId(componentId);
      lcr.getComputeEntity().setCompositionData("{\"name\":\"nm\",\"description\":\"d\"}");


      Collection<ListComputeResponse> cList = Collections.singletonList(lcr);
      when(computeManager.listComputes(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(cList);

      ComputeEntity ce = new ComputeEntity();
      ce.setComponentId(componentId);
      ce.setId(computeId);
      ce.setCompositionData("data");
      when(computeManager.createCompute(
              ArgumentMatchers.any())).thenReturn(ce);

      CompositionEntityResponse<ComputeData> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(computeManager.getCompute(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(computeId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(computeManager.updateCompute(
              ArgumentMatchers.any())).thenReturn(data);


      QuestionnaireResponse qr = new QuestionnaireResponse();
      qr.setData("helloworld");
      when(computeManager.getComputeQuestionnaire(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(computeId))).thenReturn(qr);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ComputeImpl ci = new ComputeImpl();

    Response rsp = ci.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<ComputeDto> results = (GenericCollectionWrapper<ComputeDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }


  @Test
  public void testCreate() {

    ComputeDetailsDto dto = new ComputeDetailsDto();
    dto.setDescription("hello");
    dto.setName("name");

    ComputeImpl ci = new ComputeImpl();
    Response rsp = ci.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      ComputeCreationDto ccdto = (ComputeCreationDto)e;
      Assert.assertEquals(computeId, ccdto.getId());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    ComputeImpl ci = new ComputeImpl();
    Response rsp = ci.delete(vspId, versionId, componentId, computeId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    ComputeImpl ci = new ComputeImpl();
    Response rsp = ci.get(vspId, versionId, componentId, computeId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    ComputeImpl ci = new ComputeImpl();
    ComputeDetailsDto dto = new ComputeDetailsDto();
    Response rsp = ci.update(dto, vspId, versionId, componentId, computeId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetQuestionaire() {
    ComputeImpl ci = new ComputeImpl();
    Response rsp = ci.getQuestionnaire(vspId, versionId, componentId, computeId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    try {
      QuestionnaireResponseDto dto = (QuestionnaireResponseDto)rsp.getEntity();
      Assert.assertEquals("helloworld", dto.getData());
    }
    catch (Exception ex) {
      logger.error("caught exception", ex);
      Assert.fail(ex.getMessage());
    }
  }


  @Test
  public void testUpdateQuestionaire() {
    ComputeImpl ci = new ComputeImpl();
    Response rsp = ci.updateQuestionnaire("helloworld", vspId, versionId, componentId, computeId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }
}
