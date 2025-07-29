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
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.when;

public class DeploymentFlavorsImplTest {

  private Logger logger = LoggerFactory.getLogger(DeploymentFlavorsImplTest.class);

  @Mock
  private DeploymentFlavorManager deploymentFlavorManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String deploymentFlavorId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  private DeploymentFlavorsImpl dfi;

  @Before
  public void setUp() {
    try {
      openMocks(this);

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

      dfi = new DeploymentFlavorsImpl(deploymentFlavorManager);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ResponseEntity rsp = dfi.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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

    ResponseEntity rsp = dfi.create(dto, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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
    ResponseEntity rsp = dfi.delete(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }


  @Test
  public void testGet() {
    ResponseEntity rsp = dfi.get(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
  }

  @Test
  public void testGetSchema() {
    ResponseEntity rsp = dfi.get(vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
  }

  @Test
  public void testUpdate() {
    DeploymentFlavorRequestDto dto = new DeploymentFlavorRequestDto();
    ResponseEntity rsp = dfi.update(dto, vspId, versionId, deploymentFlavorId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }

}
