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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRelationType;
import org.openecomp.sdcrests.vsp.rest.ComponentDependencies;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.when;

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

  private ComponentDependencies componentDependencies;

  @Before
  public void setUp() {
    try {
      openMocks(this);

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

      componentDependencies = new ComponentDependenciesImpl(componentDependencyModelManager);

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

    ResponseEntity rsp = componentDependencies.create(model, vspId, versionId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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

    ResponseEntity rsp = componentDependencies.list(vspId, versionId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatusCodeValue());
    Object e = rsp.getBody();
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

    ResponseEntity rsp = componentDependencies.delete(vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }


  @Test
  public void testUpdate() {

    ComponentDependencyModel model = new ComponentDependencyModel();
    model.setRelationType(ComponentRelationType.dependsOn.name());
    model.setSourceId("sourceid");
    model.setTargetId("targetid");

    ResponseEntity rsp = componentDependencies.update(model, vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatusCodeValue());
    Assert.assertNull(rsp.getBody());
  }

  @Test
  public void testGet() {

    ResponseEntity rsp = componentDependencies.get(vspId, versionId, entityId, user);
    Assert.assertEquals("Response should be 200", 200, rsp.getStatusCodeValue());
    Assert.assertNotNull(rsp.getBody());
    try {
      ComponentDependencyResponseDto dto = (ComponentDependencyResponseDto) rsp.getBody();
      Assert.assertEquals("resulting entityId must match", dto.getId(), entityId);
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + rsp.getClass().getClass().getName());
    }
  }
}
