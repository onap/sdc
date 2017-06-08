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
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.MibManager;
import org.openecomp.sdc.vendorsoftwareproduct.MibManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MibUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.ComponentUploads;
import org.openecomp.sdcrests.vsp.rest.mapping.MapMibUploadStatusToDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Named
@Service("componentUploads")
@Scope(value = "prototype")
@Validated
public class ComponentUploadsImpl implements ComponentUploads {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private MibManager mibManager = MibManagerFactory.getInstance().createInterface();
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();
  private static final Logger logger =
          LoggerFactory.getLogger(ComponentUploadsImpl.class);

  @Override
  public Response uploadTrapMibFile(Attachment attachment, String vspId, String versionId, String componentId,
                                    String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    MdcUtil.initMdc(LoggerServiceName.Upload_MIB_File.toString());
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.UPLOAD_ARTIFACT_MIB_TRAP + vspId);

    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    mibManager.upload(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, version, componentId,
        ArtifactType.SNMP_TRAP, user);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  @Override
  public Response deleteTrapMibFile(String vspId, String versionId, String componentId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    MdcUtil.initMdc(LoggerServiceName.Delete_MIB_File.toString());

    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    mibManager.delete(vspId, version, componentId, ArtifactType.SNMP_TRAP, user);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  @Override
  public Response uploadPollMibFile(Attachment attachment, String vspId, String versionId, String componentId,
                                    String user) {

    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.UPLOAD_ARTIFACT_MIB_POLL + vspId);
    MdcUtil.initMdc(LoggerServiceName.Upload_MIB_File.toString());

    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    mibManager.upload(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, version, componentId,
        ArtifactType.SNMP_POLL, user);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  @Override
  public Response deletePollMibFile(String vspId, String versionId, String componentId, String
      user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    MdcUtil.initMdc(LoggerServiceName.Delete_MIB_File.toString());

    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    mibManager.delete(vspId, version, componentId, ArtifactType.SNMP_POLL, user);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  @Override
  public Response list(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_MIB_Files.toString());

    Version version = resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read);
    componentManager.validateComponentExistence(vspId, version, componentId, user);

    MibUploadStatus response = mibManager.listFilenames(vspId, version, componentId, user);

    MibUploadStatusDto returnEntity =
        new MapMibUploadStatusToDto().applyMapping(response, MibUploadStatusDto.class);
    return Response.status(Response.Status.OK).entity(returnEntity).build();

  }
}
