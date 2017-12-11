/*-
 *
 * Copyright © 2016-2017 European Support Limited *
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.action.rest;

import com.sun.jersey.multipart.FormDataParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/workflow/v1.0/actions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Actions")
@Validated
public interface ActionsForSwaggerFileUpload {

  /**
   * Upload an artifact to an action.
   *
   * @param actionInvariantUuId Invariant UuId of the action to which the artifact is uploaded
   * @param artifactName        Name of the artifact
   * @param artifactLabel       Label of the artifact
   * @param artifactCategory    Category of the artifact
   * @param artifactDescription Description  of the artifact
   * @param artifactProtection  Artifact protection mode
   * @param checksum            Checksum of the artifact
   * @param artifactToUpload    Artifact content object
   * @param servletRequest      Servlet request object
   * @return Generated UuId of the uploaded artifact
   */
  @POST
  @Path("/{actionInvariantUuId}/artifacts")
  @ApiOperation(value = "Upload new Artifact")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response uploadArtifact(@PathParam("actionInvariantUuId") String actionInvariantUuId,
                          @Multipart(value = "artifactName", required = false) String artifactName,
                          @Multipart(value = "artifactLabel", required = false)
                              String artifactLabel,
                          @Multipart(value = "artifactCategory", required = false)
                              String artifactCategory,
                          @Multipart(value = "artifactDescription", required = false)
                              String artifactDescription,
                          @Multipart(value = "artifactProtection", required = false)
                              String artifactProtection,
                          @HeaderParam("Content-MD5") String checksum,
                          @FormDataParam(value = "uploadArtifact") InputStream artifactToUpload,
                          @Context HttpServletRequest servletRequest);


  @PUT
  @Path("/{actionInvariantUuId}/artifacts/{artifactUuId}")
  @ApiOperation(value = "Update an existing artifact")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response updateArtifact(@PathParam("actionInvariantUuId") String actionInvariantUuId,
                          @PathParam("artifactUuId") String artifactUuId,
                          @Multipart(value = "artifactName", required = false) String artifactName,
                          @Multipart(value = "artifactLabel", required = false)
                              String artifactLabel,
                          @Multipart(value = "artifactCategory", required = false)
                              String artifactCategory,
                          @Multipart(value = "artifactDescription", required = false)
                              String artifactDescription,
                          @Multipart(value = "artifactProtection", required = false)
                              String artifactProtection,
                          @HeaderParam("Content-MD5") String checksum,
                          @FormDataParam(value = "updateArtifact") InputStream artifactToUpdate,
                          @Context HttpServletRequest servletRequest);

}
