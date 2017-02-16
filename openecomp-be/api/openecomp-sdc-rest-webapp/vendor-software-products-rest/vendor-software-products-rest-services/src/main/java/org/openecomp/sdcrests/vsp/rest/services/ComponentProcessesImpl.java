/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.openecomp.sdcrests.vsp.rest.ComponentProcesses;
import org.openecomp.sdcrests.vsp.rest.mapping.MapProcessEntityToProcessEntityDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapProcessRequestDtoToProcessEntity;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;


@Named
@Service("componentProcesses")
@Scope(value = "prototype")
public class ComponentProcessesImpl implements ComponentProcesses {
  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response list(String vspId, String componentId, String version, String user) {
    Collection<ProcessEntity> processes = vendorSoftwareProductManager
        .listProcesses(vspId, Version.valueOf(version), componentId, user);

    MapProcessEntityToProcessEntityDto mapper = new MapProcessEntityToProcessEntityDto();
    GenericCollectionWrapper<ProcessEntityDto> results = new GenericCollectionWrapper<>();
    for (ProcessEntity process : processes) {
      results.add(mapper.applyMapping(process, ProcessEntityDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response deleteList(String vspId, String componentId, String user) {
    vendorSoftwareProductManager.deleteProcesses(vspId, componentId, user);
    return Response.ok().build();
  }

  @Override
  public Response create(ProcessRequestDto request, String vspId, String componentId, String user) {
    ProcessEntity process =
        new MapProcessRequestDtoToProcessEntity().applyMapping(request, ProcessEntity.class);
    process.setVspId(vspId);
    process.setComponentId(componentId);

    ProcessEntity createdProcess = vendorSoftwareProductManager.createProcess(process, user);
    return Response
        .ok(createdProcess != null ? new StringWrapperResponse(createdProcess.getId()) : null)
        .build();
  }

  @Override
  public Response get(String vspId, String componentId, String processId, String version,
                      String user) {
    ProcessEntity process = vendorSoftwareProductManager
        .getProcess(vspId, Version.valueOf(version), componentId, processId, user);
    ProcessEntityDto result =
        new MapProcessEntityToProcessEntityDto().applyMapping(process, ProcessEntityDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response delete(String vspId, String componentId, String processId, String user) {
    vendorSoftwareProductManager.deleteProcess(vspId, componentId, processId, user);
    return Response.ok().build();
  }

  @Override
  public Response update(ProcessRequestDto request, String vspId, String componentId,
                         String processId, String user) {
    ProcessEntity processEntity =
        new MapProcessRequestDtoToProcessEntity().applyMapping(request, ProcessEntity.class);
    processEntity.setVspId(vspId);
    processEntity.setComponentId(componentId);
    processEntity.setId(processId);

    vendorSoftwareProductManager.updateProcess(processEntity, user);
    return Response.ok().build();
  }

  @Override
  public Response getUploadedFile(String vspId, String componentId, String processId,
                                  String version, String user) {
    File file = vendorSoftwareProductManager
        .getProcessArtifact(vspId, Version.valueOf(version), componentId, processId, user);

    Response.ResponseBuilder response = Response.ok(file);
    if (file == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header("Content-Disposition", "attachment; filename=" + file.getName());
    return response.build();
  }

  @Override
  public Response deleteUploadedFile(String vspId, String componentId, String processId,
                                     String user) {
    vendorSoftwareProductManager.deleteProcessArtifact(vspId, componentId, processId, user);
    return Response.ok().build();
  }

  @Override
  public Response uploadFile(Attachment attachment, String vspId, String componentId,
                             String processId, String user) {
    vendorSoftwareProductManager.uploadProcessArtifact(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, componentId, processId,
        user);
    return Response.ok().build();
  }
}
