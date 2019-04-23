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
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicCreationResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;
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
@PrepareForTest({NicsImpl.class, ComponentManagerFactory.class, NicManagerFactory.class})
public class NicsImplTest {

  private Logger logger = LoggerFactory.getLogger(NicsImplTest.class);

  @Mock
  private NicManagerFactory nicManagerFactory;

  @Mock
  private NicManager nicManager;

  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String nicId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ComponentManagerFactory.class);
      when(ComponentManagerFactory.getInstance()).thenReturn(componentManagerFactory);
      when(componentManagerFactory.createInterface()).thenReturn(componentManager);

      mockStatic(NicManagerFactory.class);
      when(NicManagerFactory.getInstance()).thenReturn(nicManagerFactory);
      when(nicManagerFactory.createInterface()).thenReturn(nicManager);



      NicEntity e = new NicEntity();
      e.setComponentId(componentId);
      e.setId(nicId);
      e.setCompositionData("{\"name\":\"nm\",\"description\":\"d\"}");


      Collection<NicEntity> lst = Collections.singletonList(e);
      when(nicManager.listNics(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(lst);

      when(nicManager.createNic(
              ArgumentMatchers.any())).thenReturn(e);

      CompositionEntityResponse<Nic> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(nicManager.getNic(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(nicId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(nicManager.updateNic(
              ArgumentMatchers.any())).thenReturn(data);


      QuestionnaireResponse qr = new QuestionnaireResponse();
      qr.setData("helloworld");
      when(nicManager.getNicQuestionnaire(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(nicId))).thenReturn(qr);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    NicsImpl bean = new NicsImpl();

    Response rsp = bean.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<NicDto> results = (GenericCollectionWrapper<NicDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }


  @Test
  public void testCreate() {

    NicRequestDto dto = new NicRequestDto();
    dto.setDescription("hello");
    dto.setName("name");
    dto.setNetworkDescription("nd");
    dto.setNetworkId(nicId);
    dto.setNetworkType("nt");

    NicsImpl bean = new NicsImpl();
    Response rsp = bean.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      NicCreationResponseDto creationDto = (NicCreationResponseDto)e;
      Assert.assertEquals(nicId, creationDto.getNicId());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    NicsImpl bean = new NicsImpl();
    Response rsp = bean.delete(vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    NicsImpl bean = new NicsImpl();
    Response rsp = bean.get(vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    NicsImpl bean = new NicsImpl();
    NicRequestDto dto = new NicRequestDto();
    Response rsp = bean.update(dto, vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetQuestionaire() {
    NicsImpl bean = new NicsImpl();
    Response rsp = bean.getQuestionnaire(vspId, versionId, componentId, nicId, user);
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
    NicsImpl bean = new NicsImpl();
    Response rsp = bean.updateQuestionnaire("helloworld", vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }
}
