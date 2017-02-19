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

package org.openecomp.sdcrests.action.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.action.types.ActionResponseDto;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Defines various CRUD API that can be performed on Action.
 */
@Path("/workflow/v1.0/actions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Actions")
@Validated
public interface Actions {

  /**
   * List All Major, Last Minor and Candidate version if any for Given Action Invariant UUID
   *
   * @return List of All Major, Last Minor and Candidate version if any Of Action with given
    actionInvariantUuId. If actionUUID is provided then only action with given actionInvariantUuId
    and actionUUID
   */
  @GET
  @Path("/{actionInvariantUUID}")
  @ApiOperation(value = "List Actions For Given Action Invariant UUID", responseContainer = "List")
  Response getActionsByActionInvariantUuId(
      @PathParam("actionInvariantUUID") String actionInvariantUuId,
      @QueryParam("version") String actionUuId, @Context HttpServletRequest servletRequest);

  /**
   * Get list of actions based on a filter criteria. If no filter is sent all actions will
    be returned
   *
   * @return List Of Last Major and Last Minor of All Actions based on filter criteria
   */
  @GET
  @ApiOperation(value = "List Filtered Actions ",
      notes = "Get list of actions based on a filter criteria | If no filter is sent all actions "
          + "will be returned",
      responseContainer = "List")
  Response getFilteredActions(@QueryParam("vendor") String vendor,
                              @QueryParam("category") String category,
                              @QueryParam("name") String name,
                              @QueryParam("modelId") String modelId,
                              @QueryParam("componentId") String componentId,
                              @Context HttpServletRequest servletRequest);

  /**
   * List ECOMP Components supported by Action Library.
   *
   * @return List of ECOMP Components supported by Action Library.
   */
  @GET
  @Path("/components")
  @ApiOperation(value = "List ECOMP Components supported by Action Library",
      responseContainer = "List")
  Response getEcompComponents(@Context HttpServletRequest servletRequest);

  /**
   * Create a new Action based on request JSON.
   *
   * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action.
   */
  @POST
  @ApiOperation(value = "Create a new Action")
  Response createAction(String requestJson, @Context HttpServletRequest servletRequest);

  /**
   * Update an existing action with parameters provided in requestJson.
   *
   * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action.
   */
  @PUT
  @Path("/{actionInvariantUUID}")
  @ApiOperation(value = "Update an existing action")
  Response updateAction(@PathParam("actionInvariantUUID") String actionInvariantUuId,
                        String requestJson, @Context HttpServletRequest servletRequest);

  /**
   * Delete an action.
   *
   * @param actionInvariantUuId Invariant UUID of the action to be deleted.
   * @param servletRequest      Servlet request object.
   * @return Empty response object.
   */
  @DELETE
  @Path("/{actionInvariantUUID}")
  @ApiOperation(value = "Delete Action")
  Response deleteAction(@PathParam("actionInvariantUUID") String actionInvariantUuId,
                        @Context HttpServletRequest servletRequest);

  /**
   * Performs Checkout/Undo_Checkout/Checkin/Submit Operation on Action.
   *
   * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action.
   */
  @POST
  @Path("/{actionInvariantUUID}")
  @ApiOperation(value = "Actions on a action",
      notes = "Performs one of the following actions on a action: |"
          + "Checkout: Locks it for edits by other users. Only the locking user sees the edited "
          + "version.|"
          + "Undo_Checkout: Unlocks it and deletes the edits that were done.|"
          + "Checkin: Unlocks it and activates the edited version to all users.| "
          + "Submit: Finalize its active version.|")
  Response actOnAction(@PathParam("actionInvariantUUID") String actionInvariantUuId,
                       String requestJson, @Context HttpServletRequest servletRequest);

  /**
   * Upload an artifact to an action.
   *
   * @param actionInvariantUuId Invariant UUID of the action to which the artifact is uploaded.
   * @param artifactName        Name of the artifact.
   * @param artifactLabel       Label of the artifact.
   * @param artifactCategory    Category of the artifact.
   * @param artifactDescription Description  of the artifact.
   * @param artifactProtection  Artifact protection mode.
   * @param checksum            Checksum of the artifact.
   * @param artifactToUpload    Artifact content object.
   * @param servletRequest      Servlet request object.
   * @return Generated UUID of the uploaded artifact.
   */
  @POST
  @Path("/{actionInvariantUUID}/artifacts")
  @ApiOperation(value = "Upload new Artifact")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response uploadArtifact(@PathParam("actionInvariantUUID") String actionInvariantUuId,
      @Multipart(value = "artifactName", required = false) String artifactName,
      @Multipart(value = "artifactLabel", required = false) String artifactLabel,
      @Multipart(value = "artifactCategory", required = false) String artifactCategory,
      @Multipart(value = "artifactDescription", required = false) String artifactDescription,
      @Multipart(value = "artifactProtection", required = false) String artifactProtection,
      @HeaderParam("Content-MD5") String checksum,
      @Multipart(value = "uploadArtifact", required = false) Attachment artifactToUpload,
      @Context HttpServletRequest servletRequest);

  @GET
  @Path("/{actionUUID}/artifacts/{artifactUUID}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Downloads artifact for action")
  Response downloadArtifact(@PathParam("actionUUID") String actionUuId,
                            @PathParam("artifactUUID") String artifactUuId,
                            @Context HttpServletRequest servletRequest);

  @DELETE
  @Path("/{actionInvariantUUID}/artifacts/{artifactUUID}")
  @ApiOperation(value = "Delete Artifact")
  Response deleteArtifact(@PathParam("actionInvariantUUID") String actionInvariantUuId,
                          @PathParam("artifactUUID") String artifactUuId,
                          @Context HttpServletRequest servletRequest);

  @PUT
  @Path("/{actionInvariantUUID}/artifacts/{artifactUUID}")
  @ApiOperation(value = "Update an existing artifact")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response updateArtifact(@PathParam("actionInvariantUUID") String actionInvariantUuId,
                          @PathParam("artifactUUID") String artifactUuId,
      @Multipart(value = "artifactName", required = false) String artifactName,
      @Multipart(value = "artifactLabel", required = false) String artifactLabel,
      @Multipart(value = "artifactCategory", required = false) String artifactCategory,
      @Multipart(value = "artifactDescription", required = false) String artifactDescription,
      @Multipart(value = "artifactProtection", required = false) String artifactProtection,
      @HeaderParam("Content-MD5") String checksum,
      @Multipart(value = "updateArtifact", required = false) Attachment artifactToUpdate,
      @Context HttpServletRequest servletRequest);

}
