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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.*;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class ComponentProcessesImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentProcessesImplTest.class);

  @Mock
  private ActivityLogManager activityLogManager;

  @Mock
  private ProcessManager processManager;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String processId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  private ComponentProcessesImpl cpi;

  @Before
  public void setUp() {
    try {
      initMocks(this);

      ProcessEntity pe = new ProcessEntity();
      pe.setId(vspId);
      pe.setComponentId(componentId);
      pe.setVspId(vspId);
      pe.setVersion(new Version(versionId));

      Collection<ProcessEntity> peList = Collections.singletonList(pe);
      when(processManager.listProcesses(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(peList);

      when(processManager.createProcess(
              ArgumentMatchers.any())).thenReturn(pe);

      when(processManager.getProcess(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(processId))).thenReturn(pe);

      File processArtifact = new File(vspId);
      when(processManager.getProcessArtifact(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId),
              ArgumentMatchers.eq(processId))).thenReturn(processArtifact);

      cpi = new ComponentProcessesImpl(processManager, componentManager, activityLogManager);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    Response rsp = cpi.list(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    @SuppressWarnings("unchecked")
    GenericCollectionWrapper<ProcessEntityDto> results = (GenericCollectionWrapper<ProcessEntityDto>)e;
    Assert.assertEquals("result length", 1, results.getListCount());
  }

  @Test
  public void testDeleteList() {
    Response rsp = cpi.deleteList(vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }



  @Test
  public void testCreate() {

    ProcessRequestDto dto = new ProcessRequestDto();
    dto.setDescription("hello");
    dto.setName("name");
    dto.setType(ProcessType.Other);

    Response rsp = cpi.create(dto, vspId, versionId, componentId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Object e = rsp.getEntity();
    Assert.assertNotNull(e);
    try {
      StringWrapperResponse swr = (StringWrapperResponse)e;
      Assert.assertEquals(vspId, swr.getValue());
    } catch (ClassCastException ex) {
      Assert.fail("unexpected class for DTO " + e.getClass().getName());
    }
  }


  @Test
  public void testDelete() {
    Response rsp = cpi.delete(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    Response rsp = cpi.get(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    ProcessRequestDto dto = new ProcessRequestDto();
    Response rsp = cpi.update(dto, vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetUploadedFile() {
    Response rsp = cpi.getUploadedFile(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf(vspId),-1);
  }


  @Test
  public void testDeleteUploadedFile() {
    Response rsp = cpi.deleteUploadedFile(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testUploadFile() {

    Attachment attachment = mock(Attachment.class);
    when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
    byte[] bytes = "Hello World".getBytes();
    when(attachment.getObject(ArgumentMatchers.any())).thenReturn(new ByteArrayInputStream(bytes));
    Response rsp = cpi.uploadFile(attachment, vspId, versionId, componentId, processId, user);
    Assert.assertNull(rsp.getEntity());
  }

}
