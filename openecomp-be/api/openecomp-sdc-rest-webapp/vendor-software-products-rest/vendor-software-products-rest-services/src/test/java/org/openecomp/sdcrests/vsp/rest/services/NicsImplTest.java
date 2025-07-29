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
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
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
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.when;

public class NicsImplTest {

  private Logger logger = LoggerFactory.getLogger(NicsImplTest.class);

  @Mock
  private NicManager nicManager;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String nicId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  private NicsImpl bean;

  @Before
  public void setUp() {
    try {
      openMocks(this);

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

      bean = new NicsImpl(nicManager, componentManager);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {

    ResponseEntity rsp = bean.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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
    dto.setNetworkType("External");

    ResponseEntity rsp = bean.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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
    ResponseEntity rsp = bean.delete(vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }


  @Test
  public void testGet() {
    ResponseEntity rsp = bean.get(vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
  }

  @Test
  public void testUpdate() {
    NicRequestDto dto = new NicRequestDto();
    dto.setDescription("hello");
    dto.setName("name");
    dto.setNetworkDescription("nd");
    dto.setNetworkId(nicId);
    dto.setNetworkType("External");

    ResponseEntity rsp = bean.update(dto, vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }

  @Test
  public void testGetQuestionaire() {
    ResponseEntity rsp = bean.getQuestionnaire(vspId, versionId, componentId, nicId, user);
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
    ResponseEntity rsp = bean.updateQuestionnaire("helloworld", vspId, versionId, componentId, nicId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }
}
