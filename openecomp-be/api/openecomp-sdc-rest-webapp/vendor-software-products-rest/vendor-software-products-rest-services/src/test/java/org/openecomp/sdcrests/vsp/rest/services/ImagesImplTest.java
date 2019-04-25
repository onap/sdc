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
import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;
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
@PrepareForTest({ComputeImpl.class, ComponentManagerFactory.class, ImageManagerFactory.class})
public class ImagesImplTest {

  private Logger logger = LoggerFactory.getLogger(ImagesImplTest.class);

  @Mock
  private ImageManagerFactory imageManagerFactory;

  @Mock
  private ImageManager imageManager;

  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String imageId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ComponentManagerFactory.class);
      when(ComponentManagerFactory.getInstance()).thenReturn(componentManagerFactory);
      when(componentManagerFactory.createInterface()).thenReturn(componentManager);

      mockStatic(ImageManagerFactory.class);
      when(ImageManagerFactory.getInstance()).thenReturn(imageManagerFactory);
      when(imageManagerFactory.createInterface()).thenReturn(imageManager);



      ImageEntity ie = new ImageEntity();
      ie.setComponentId(componentId);
      ie.setId(imageId);
      ie.setCompositionData("{\"name\":\"nm\",\"description\":\"d\"}");


      Collection<ImageEntity> cList = Collections.singletonList(ie);
      when(imageManager.listImages(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(cList);

      when(imageManager.createImage(
              ArgumentMatchers.any())).thenReturn(ie);

      CompositionEntityResponse<Image> r = new CompositionEntityResponse<>();
      r.setId(vspId);
      when(imageManager.getImage(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(imageId))).thenReturn(r);

      CompositionEntityType tpe = CompositionEntityType.component;
      CompositionEntityValidationData data = new CompositionEntityValidationData(tpe, vspId);
      when(imageManager.updateImage(
              ArgumentMatchers.any())).thenReturn(data);


      QuestionnaireResponse qr = new QuestionnaireResponse();
      qr.setData("helloworld");
      when(imageManager.getImageQuestionnaire(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(imageId))).thenReturn(qr);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ImagesImpl ii = new ImagesImpl();

    Response rsp = ii.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<ImageDto> results = (GenericCollectionWrapper<ImageDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }


  @Test
  public void testCreate() {

    ImageRequestDto dto = new ImageRequestDto();
    dto.setDescription("hello");
    dto.setFileName("name");

    ImagesImpl ii = new ImagesImpl();
    Response rsp = ii.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      ImageCreationDto creationDto = (ImageCreationDto)e;
      Assert.assertEquals(imageId, creationDto.getId());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    ImagesImpl ii = new ImagesImpl();
    Response rsp = ii.delete(vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    ImagesImpl ii = new ImagesImpl();
    Response rsp = ii.get(vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    ImagesImpl ii = new ImagesImpl();
    ImageRequestDto dto = new ImageRequestDto();
    Response rsp = ii.update(dto, vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetQuestionaire() {
    ImagesImpl ii = new ImagesImpl();
    Response rsp = ii.getQuestionnaire(vspId, versionId, componentId, imageId, user);
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
    ImagesImpl ii = new ImagesImpl();
    Response rsp = ii.updateQuestionnaire("helloworld", vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }
}
