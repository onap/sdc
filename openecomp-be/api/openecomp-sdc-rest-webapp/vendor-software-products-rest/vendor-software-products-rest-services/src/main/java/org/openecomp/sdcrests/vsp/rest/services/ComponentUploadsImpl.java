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
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MibUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.ComponentUploads;
import org.openecomp.sdcrests.vsp.rest.mapping.MapMibUploadStatusToDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("componentUploads")
@Scope(value = "prototype")
@Validated
public class ComponentUploadsImpl implements ComponentUploads {
  @Autowired
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Override
  public Response uploadTrapMibFile(Attachment attachment, String vspId, String componentId,
                                    String user) {
    vendorSoftwareProductManager.uploadComponentMib(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, componentId, true,
        user);
    return Response.ok().build();
  }

  @Override
  public Response deleteTrapMibFile(String vspId, String componentId, String user) {
    vendorSoftwareProductManager.deleteComponentMib(vspId, componentId, true, user);
    return Response.ok().build();
  }

  @Override
  public Response uploadPollMibFile(Attachment attachment, String vspId, String componentId,
                                    String user) {
    vendorSoftwareProductManager.uploadComponentMib(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, componentId, false,
        user);
    return Response.ok().build();
  }

  @Override
  public Response deletePollMibFile(String vspId, String componentId, String user) {
    vendorSoftwareProductManager.deleteComponentMib(vspId, componentId, false, user);
    return Response.ok().build();
  }

  @Override
  public Response list(String vspId, String componentId, String user) {
    MibUploadStatus response =
        vendorSoftwareProductManager.listMibFilenames(vspId, componentId, user);

    MibUploadStatusDto returnEntity =
        new MapMibUploadStatusToDto().applyMapping(response, MibUploadStatusDto.class);
    return Response.status(Response.Status.OK).entity(returnEntity).build();

  }
}
