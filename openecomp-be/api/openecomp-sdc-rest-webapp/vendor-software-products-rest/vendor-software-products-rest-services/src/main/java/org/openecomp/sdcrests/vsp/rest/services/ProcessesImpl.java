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

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.openecomp.sdcrests.vsp.rest.ComponentProcesses;
import org.openecomp.sdcrests.vsp.rest.Processes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("processes")
@Scope(value = "prototype")
public class ProcessesImpl implements Processes {

  @Autowired
  private ComponentProcesses componentProcesses;

  @Override
  public Response list(String vspId, String version, String user) {
    return componentProcesses.list(vspId, GENERAL_COMPONENT_ID, version, user);
  }

  @Override
  public Response deleteList(String vspId, String user) {
    return componentProcesses.deleteList(vspId, GENERAL_COMPONENT_ID, user);
  }

  @Override
  public Response create(ProcessRequestDto request, String vspId, String user) {
    return componentProcesses.create(request, vspId, GENERAL_COMPONENT_ID, user);
  }

  @Override
  public Response get(String vspId, String processId, String version, String user) {
    return componentProcesses.get(vspId, GENERAL_COMPONENT_ID, processId, version, user);
  }

  @Override
  public Response delete(String vspId, String processId, String user) {
    return componentProcesses.delete(vspId, GENERAL_COMPONENT_ID, processId, user);
  }

  @Override
  public Response update(ProcessRequestDto request, String vspId, String processId, String user) {
    return componentProcesses.update(request, vspId, GENERAL_COMPONENT_ID, processId, user);
  }

  @Override
  public Response getUploadedFile(String vspId, String processId, String version, String user) {
    return componentProcesses
        .getUploadedFile(vspId, GENERAL_COMPONENT_ID, processId, version, user);
  }

  @Override
  public Response deleteUploadedFile(String vspId, String processId, String user) {
    return componentProcesses.deleteUploadedFile(vspId, GENERAL_COMPONENT_ID, processId, user);
  }

  @Override
  public Response uploadFile(Attachment attachment, String vspId, String processId, String user) {
    return componentProcesses.uploadFile(attachment, vspId, GENERAL_COMPONENT_ID, processId, user);
  }
}
