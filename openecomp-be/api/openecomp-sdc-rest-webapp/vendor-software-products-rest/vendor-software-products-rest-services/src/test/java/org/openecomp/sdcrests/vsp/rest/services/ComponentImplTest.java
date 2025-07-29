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
import org.springframework.http.ResponseEntity;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.when;

public class ComponentImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentImplTest.class);

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  private ComponentsImpl ci;

  @Before
  public void setUp() {
    try {
      openMocks(this);

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

      ci = new ComponentsImpl(componentManager);


    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ResponseEntity rsp = ci.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<ComponentDto> results = (GenericCollectionWrapper<ComponentDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }

  @Test
  public void testDeleteList() {
    ResponseEntity rsp = ci.deleteList(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }



  @Test
  public void testCreate() {

    ComponentRequestDto dto = new ComponentRequestDto();
    dto.setDescription("hello");
    dto.setName("name");
    dto.setDisplayName("world");

    ResponseEntity rsp = ci.create(dto, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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
    ResponseEntity rsp = ci.delete(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }


  @Test
  public void testGet() {
    ResponseEntity rsp = ci.get(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
  }

  @Test
  public void testUpdate() {
    ComponentRequestDto dto = new ComponentRequestDto();
    ResponseEntity rsp = ci.update(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }

  @Test
  public void testGetQuestionaire() {
    ResponseEntity rsp = ci.getQuestionnaire(vspId, versionId, componentId, user);
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
    ResponseEntity rsp = ci.updateQuestionnaire("helloworld", vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }
}
