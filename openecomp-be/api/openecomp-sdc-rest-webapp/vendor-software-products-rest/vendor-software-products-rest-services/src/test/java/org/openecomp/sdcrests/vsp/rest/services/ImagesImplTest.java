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

package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.when;

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

  private ImagesImpl ii;

  @Before
  public void setUp() {
    try {
      openMocks(this);

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

      ii = new ImagesImpl(imageManager, componentManager);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {

    ResponseEntity rsp = ii.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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

    ResponseEntity rsp = ii.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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
    ResponseEntity rsp = ii.delete(vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }


  @Test
  public void testGet() {
    ResponseEntity rsp = ii.get(vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
  }

  @Test
  public void testUpdate() {
    ImageRequestDto dto = new ImageRequestDto();
    ResponseEntity rsp = ii.update(dto, vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }

  @Test
  public void testGetQuestionaire() {
    ResponseEntity rsp = ii.getQuestionnaire(vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    try {
      QuestionnaireResponseDto dto = (QuestionnaireResponseDto)rsp.getBody();
      Assert.assertEquals("helloworld", dto.getData());
    }
    catch (Exception ex) {
      logger.error("caught exception", ex);
      Assert.fail(ex.getMessage());
    }
  }


  @Test
  public void testUpdateQuestionaire() {
    ResponseEntity rsp = ii.updateQuestionnaire("helloworld", vspId, versionId, componentId, imageId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }
}
