package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.MonitoringUploadsManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitoringUploadsManagerImpl.class, MonitoringUploadsManagerFactory.class, ComponentManagerFactory.class})
public class ComponentMonitoringUploadsImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentMonitoringUploadsImplTest.class);

  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  @Mock
  private MonitoringUploadsManagerFactory uploadsFactory;

  @Mock
  private MonitoringUploadsManager uploadsManager;

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

      mockStatic(MonitoringUploadsManagerFactory.class);
      when(MonitoringUploadsManagerFactory.getInstance()).thenReturn(uploadsFactory);
      when(uploadsFactory.createInterface()).thenReturn(uploadsManager);

      MonitoringUploadStatus result = new MonitoringUploadStatus();
      result.setSnmpPoll("p");
      result.setSnmpTrap("t");
      result.setVesEvent("v");
      when(uploadsManager.listFilenames(
              ArgumentMatchers.eq(vspId),
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(componentId))).thenReturn(result);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testUpload() {
    ComponentMonitoringUploadsImpl bean = new ComponentMonitoringUploadsImpl();
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
    ComponentMonitoringUploadsImpl bean = new ComponentMonitoringUploadsImpl();
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
    ComponentMonitoringUploadsImpl bean = new ComponentMonitoringUploadsImpl();
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
