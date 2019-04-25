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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRequestDto;
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
@PrepareForTest({ComponentsImpl.class, ComponentManagerFactory.class})
public class ComponentImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentImplTest.class);


  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ComponentManagerFactory.class);
      when(ComponentManagerFactory.getInstance()).thenReturn(componentManagerFactory);
      when(componentManagerFactory.createInterface()).thenReturn(componentManager);

      ComponentEntity ce = new ComponentEntity();
      ce.setId(vspId);
      ce.setVspId(vspId);
      ce.setVersion(new Version(versionId));

      Collection<ComponentEntity> ceList = Collections.singletonList(ce);
      when(componentManager.listComponents(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any())).thenReturn(ceList);

      when(componentManager.createComponent(
              ArgumentMatchers.any())).thenReturn(ce);

      CompositionEntityResponse<ComponentData> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(componentManager.getComponent(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(componentManager.updateComponent(
              ArgumentMatchers.any())).thenReturn(data);


      QuestionnaireResponse qr = new QuestionnaireResponse();
      qr.setData("helloworld");
      when(componentManager.getQuestionnaire(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(qr);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ComponentsImpl ci = new ComponentsImpl();

    Response rsp = ci.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<ComponentDto> results = (GenericCollectionWrapper<ComponentDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }

  @Test
  public void testDeleteList() {
    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.deleteList(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }



  @Test
  public void testCreate() {

    ComponentRequestDto dto = new ComponentRequestDto();
    dto.setDescription("hello");
    dto.setName("name");
    dto.setDisplayName("world");

    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.create(dto, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      ComponentCreationDto ccdto = (ComponentCreationDto)e;
      Assert.assertEquals(vspId, ccdto.getVfcId());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.delete(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.get(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    ComponentsImpl ci = new ComponentsImpl();
    ComponentRequestDto dto = new ComponentRequestDto();
    Response rsp = ci.update(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetQuestionaire() {
    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.getQuestionnaire(vspId, versionId, componentId, user);
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
    ComponentsImpl ci = new ComponentsImpl();
    Response rsp = ci.updateQuestionnaire("helloworld", vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }
}
