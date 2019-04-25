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
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManager;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.*;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ComponentDependenciesImpl.class, ActivityLogManagerFactory.class, ComponentManagerFactory.class, ProcessManagerFactory.class})
public class ComponentProcessesImplTest {

  private Logger logger = LoggerFactory.getLogger(ComponentProcessesImplTest.class);

  @Mock
  private ActivityLogManager activityLogManager;
  @Mock
  private ActivityLogManagerFactory activityLogManagerFactory;

  @Mock
  private ProcessManagerFactory processManagerFactory;

  @Mock
  private ProcessManager processManager;

  @Mock
  private ComponentManagerFactory componentManagerFactory;

  @Mock
  private ComponentManager componentManager;

  private final String vspId = UUID.randomUUID().toString();
  private final String versionId = UUID.randomUUID().toString();
  private final String componentId = "" + System.currentTimeMillis();
  private final String processId = "" + System.currentTimeMillis();
  private final String user = "cs0008";

  @Before
  public void setUp() {
    try {
      initMocks(this);

      mockStatic(ProcessManagerFactory.class);
      when(ProcessManagerFactory.getInstance()).thenReturn(processManagerFactory);
      when(processManagerFactory.createInterface()).thenReturn(processManager);

      mockStatic(ActivityLogManagerFactory.class);
      when(ActivityLogManagerFactory.getInstance()).thenReturn(activityLogManagerFactory);
      when(activityLogManagerFactory.createInterface()).thenReturn(activityLogManager);

      mockStatic(ComponentManagerFactory.class);
      when(ComponentManagerFactory.getInstance()).thenReturn(componentManagerFactory);
      when(componentManagerFactory.createInterface()).thenReturn(componentManager);

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

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @Test
  public void testList() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();

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
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
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

    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
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
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
    Response rsp = cpi.delete(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testGet() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
    Response rsp = cpi.get(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotNull(rsp.getEntity());
  }

  @Test
  public void testUpdate() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
    ProcessRequestDto dto = new ProcessRequestDto();
    Response rsp = cpi.update(dto, vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }

  @Test
  public void testGetUploadedFile() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
    Response rsp = cpi.getUploadedFile(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNotEquals(rsp.getHeaderString("Content-Disposition").indexOf(vspId),-1);
  }


  @Test
  public void testDeleteUploadedFile() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();
    Response rsp = cpi.deleteUploadedFile(vspId, versionId, componentId, processId, user);
    Assert.assertEquals("Response should be 200", HttpStatus.SC_OK, rsp.getStatus());
    Assert.assertNull(rsp.getEntity());
  }


  @Test
  public void testUploadFile() {
    ComponentProcessesImpl cpi = new ComponentProcessesImpl();

    Attachment attachment = mock(Attachment.class);
    when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("test"));
    byte[] bytes = "Hello World".getBytes();
    when(attachment.getObject(ArgumentMatchers.any())).thenReturn(new ByteArrayInputStream(bytes));
    Response rsp = cpi.uploadFile(attachment, vspId, versionId, componentId, processId, user);
    Assert.assertNull(rsp.getEntity());
  }

}
