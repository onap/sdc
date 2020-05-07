/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManager;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.openecomp.sdcrests.vsp.rest.ComponentProcesses;
import org.openecomp.sdcrests.vsp.rest.mapping.MapProcessEntityToProcessEntityDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapProcessRequestDtoToProcessEntity;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

@Named
@Service("componentProcesses")
@Scope(value = "prototype")
public class ComponentProcessesImpl implements ComponentProcesses {

  private final ProcessManager processManager;
  private final ComponentManager componentManager;
  private final ActivityLogManager activityLogManager;

  public ComponentProcessesImpl() {
    this.processManager = ProcessManagerFactory.getInstance().createInterface();
    this.componentManager = ComponentManagerFactory.getInstance().createInterface();
    this.activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
  }

  public ComponentProcessesImpl(ProcessManager processManager,
      ComponentManager componentManager, ActivityLogManager activityLogManager) {
    this.processManager = processManager;
    this.componentManager = componentManager;
    this.activityLogManager = activityLogManager;
  }

  @Override
  public Response list(String vspId, String versionId, String componentId, String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    Collection<ProcessEntity> processes = processManager.listProcesses(vspId, version, componentId);

    MapProcessEntityToProcessEntityDto mapper = new MapProcessEntityToProcessEntityDto();
    GenericCollectionWrapper<ProcessEntityDto> results = new GenericCollectionWrapper<>();
    for (ProcessEntity process : processes) {
      results.add(mapper.applyMapping(process, ProcessEntityDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response deleteList(String vspId, String versionId, String componentId, String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    processManager.deleteProcesses(vspId, version, componentId);

    return Response.ok().build();
  }

  @Override
  public Response create(ProcessRequestDto request, String vspId, String versionId,
                         String componentId, String user) {

    ProcessEntity process =
        new MapProcessRequestDtoToProcessEntity().applyMapping(request, ProcessEntity.class);
    process.setVspId(vspId);
    process.setVersion(new Version(versionId));
    process.setComponentId(componentId);

    validateComponentExistence(vspId, process.getVersion(), componentId, user);
    ProcessEntity createdProcess = processManager.createProcess(process);

    return Response
        .ok(createdProcess != null ? new StringWrapperResponse(createdProcess.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String versionId, String componentId, String processId,
                      String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    ProcessEntity process = processManager.getProcess(vspId, version, componentId, processId);
    ProcessEntityDto result =
        new MapProcessEntityToProcessEntityDto().applyMapping(process, ProcessEntityDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId, String processId,
                         String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    processManager.deleteProcess(vspId, version, componentId, processId);
    return Response.ok().build();
  }

  @Override
  public Response update(ProcessRequestDto request, String vspId, String versionId,
                         String componentId,
                         String processId, String user) {

    ProcessEntity process =
        new MapProcessRequestDtoToProcessEntity().applyMapping(request, ProcessEntity.class);
    process.setVspId(vspId);
    process.setVersion(new Version(versionId));
    process.setComponentId(componentId);
    process.setId(processId);
    validateComponentExistence(vspId, process.getVersion(), componentId, user);
    processManager.updateProcess(process);
    return Response.ok().build();
  }

  @Override
  public Response getUploadedFile(String vspId, String versionId, String componentId,
                                  String processId, String user) {

    Version vspVersion = new Version(versionId);
    validateComponentExistence(vspId, vspVersion, componentId, user);
    File file = processManager.getProcessArtifact(vspId, vspVersion, componentId, processId);

    Response.ResponseBuilder response = Response.ok(file);
    if (file == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header("Content-Disposition", "attachment; filename=" + file.getName());
    return response.build();
  }

  @Override
  public Response deleteUploadedFile(String vspId, String versionId, String componentId,
                                     String processId,
                                     String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    processManager.deleteProcessArtifact(vspId, version, componentId, processId);
    return Response.ok().build();
  }

  @Override
  public Response uploadFile(Attachment attachment, String vspId, String versionId,
                             String componentId,
                             String processId, String user) {

    Version version = new Version(versionId);
    validateComponentExistence(vspId, version, componentId, user);
    processManager.uploadProcessArtifact(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, version, componentId,
        processId);
    activityLogManager.logActivity(new ActivityLogEntity(vspId, version,
            ActivityType.Upload_Artifact, user, true, "", ""));

    return Response.ok().build();
  }


  private void validateComponentExistence(String vspId, Version version, String componentId,
                                          String user) {
    if (componentId == null) {
      return;
    }
    componentManager.validateComponentExistence(vspId, version, componentId);
  }
}
