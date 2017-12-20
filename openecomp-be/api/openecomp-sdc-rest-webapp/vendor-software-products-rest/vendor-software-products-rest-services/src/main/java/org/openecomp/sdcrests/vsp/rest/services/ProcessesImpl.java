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
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerServiceName;
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

  private static final Logger logger = LoggerFactory.getLogger(ProcessesImpl.class);
  @Autowired
  private ComponentProcesses componentProcesses;

  @Override
  public Response list(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Processes.toString());
    return componentProcesses.list(vspId, versionId, null, user);
  }

  @Override
  public Response deleteList(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_List_Processes.toString());
    return componentProcesses.deleteList(vspId, versionId, null, user);
  }

  @Override
  public Response create(ProcessRequestDto request, String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Process.toString());
    return componentProcesses.create(request, vspId, versionId, null, user);
  }

  @Override
  public Response get(String vspId, String versionId, String processId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Processes.toString());
    return componentProcesses.get(vspId, versionId, null, processId, user);
  }

  @Override
  public Response delete(String vspId, String versionId, String processId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Processes.toString());
    return componentProcesses.delete(vspId, versionId, null, processId, user);
  }

  @Override
  public Response update(ProcessRequestDto request, String vspId, String versionId,
                         String processId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Process.toString());
    return componentProcesses.update(request, vspId, versionId, null, processId, user);
  }

  @Override
  public Response getUploadedFile(String vspId, String versionId, String processId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Uploaded_File_Processes.toString());
    return componentProcesses.getUploadedFile(vspId, versionId, null, processId, user);
  }

  @Override
  public Response deleteUploadedFile(String vspId, String versionId, String processId,
                                     String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Uploaded_File_Processes.toString());
    return componentProcesses.deleteUploadedFile(vspId, versionId, null, processId, user);
  }

  @Override
  public Response uploadFile(Attachment attachment, String vspId, String versionId,
                             String processId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Upload_File_Processes.toString());
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.UPLOAD_PROCESS_ARTIFACT + vspId);
    return componentProcesses.uploadFile(attachment, vspId, versionId, null, processId, user);
  }
}
