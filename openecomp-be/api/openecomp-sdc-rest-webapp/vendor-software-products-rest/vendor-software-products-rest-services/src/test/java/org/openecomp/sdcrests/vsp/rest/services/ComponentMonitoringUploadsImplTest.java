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
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;


import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;

public class ComponentMonitoringUploadsImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentMonitoringUploadsImplTest.class);

  @Mock
  private ComponentManager componentManager;

  @Mock
  private MonitoringUploadsManager uploadsManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  private ComponentMonitoringUploadsImpl bean;

  @Before
  public void setUp() {
    try {
      initMocks(this);

      MonitoringUploadStatus result = new MonitoringUploadStatus();
      result.setSnmpPoll("p");
      result.setSnmpTrap("t");
      result.setVesEvent("v");
      when(uploadsManager.listFilenames(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(result);

      this.bean = new ComponentMonitoringUploadsImpl(uploadsManager, componentManager);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testUpload() {
    byte[] bytes = "Hello".getBytes();
    Attachment a = new Attachment("foo", new ByteArrayInputStream(bytes), new ContentDisposition("filename"));
    String type = MonitoringUploadType.SNMP_POLL.toString();
    try {
      Response rsp = bean.upload(a, vspId, versionId, componentId, type, user);
      Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
      Assert.assertNull(rsp.getEntity());
    }
    catch (Exception ex) {
      logger.error("test failed due to exception", ex);
      Assert.fail("exception caught " + ex.getMessage());
    }
  }


  @Test
  public void testDelete() {
    String type = MonitoringUploadType.SNMP_POLL.toString();
    try {
      Response rsp = bean.delete(vspId, versionId, componentId, type, user);
      Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
      Assert.assertNull(rsp.getEntity());
    }
    catch (Exception ex) {
      logger.error("test failed due to exception", ex);
      Assert.fail("exception caught " + ex.getMessage());
    }
  }

  @Test
  public void testList() {
    try {
      Response rsp = bean.list(vspId, versionId, componentId, user);
      Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
      Assert.assertNotNull(rsp.getEntity());
      MonitoringUploadStatusDto dto = (MonitoringUploadStatusDto)rsp.getEntity();
      Assert.assertEquals("p",dto.getSnmpPoll());
      Assert.assertEquals("v",dto.getVesEvent());
      Assert.assertEquals("t",dto.getSnmpTrap());
    }
    catch (Exception ex) {
      logger.error("test failed due to exception", ex);
      Assert.fail("exception caught " + ex.getMessage());
    }
  }

}
