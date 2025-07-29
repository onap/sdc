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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdcrests.action.types.ActionResponseDto;
import org.openecomp.sdcrests.action.types.ListResponseWrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Defines various CRUD API that can be performed on Action.
 */
@RestController
@RequestMapping("/workflow/v1.0/actions")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Actions")})
@Validated
public interface Actions {

    /**
     * List All Major, Last Minor and Candidate version if any for Given Action Invariant UuId
     *
     * @return List of All Major, Last Minor and Candidate version if any Of Action with given actionInvariantUuId. If actionUuId is provided then
     * only action with given actionInvariantUuId and actionUuId
     */
    @GetMapping("/{actionInvariantUuId}")
    @Operation(description = "List Actions For Given Action Invariant UuId", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ListResponseWrapper.class))))
    ResponseEntity getActionsByActionInvariantUuId(@PathVariable("actionInvariantUuId") String actionInvariantUuId, @RequestParam("version") String actionUuId,
                                                   HttpServletRequest servletRequest);

    /**
     * Get list of actions based on a filter criteria. If no filter is sent all actions will be returned
     *
     * @return List Of Last Major and Last Minor of All Actions based on filter criteria
     */
    @GetMapping({ "", "/" })
    @Operation(description = "List Filtered Actions ", summary = "Get list of actions based on a filter criteria | If no filter is sent all actions "
        + "will be returned", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ListResponseWrapper.class))))
    ResponseEntity getFilteredActions(@RequestParam("vendor") String vendor, @RequestParam("category") String category, @RequestParam("name") String name,
                                @RequestParam("modelId") String modelId, @RequestParam("componentId") String componentId,
                                HttpServletRequest servletRequest);

    /**
     * List OPENECOMP Components supported by Action Library.
     *
     * @return List of OPENECOMP Components supported by Action Library
     */
    @GetMapping("/components")
    @Operation(description = "List OPENECOMP Components supported by Action Library", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ListResponseWrapper.class))))
    ResponseEntity getOpenEcompComponents(HttpServletRequest servletRequest);

    /**
     * Create a new Action based on request JSON.
     *
     * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action
     */
    @PostMapping({ "", "/" })
    @Operation(description = "Create a new Action")
    ResponseEntity createAction(String requestJson, HttpServletRequest servletRequest);

    /**
     * Update an existing action with parameters provided in requestJson.
     *
     * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action
     */
    @PutMapping("/{actionInvariantUuId}")
    @Operation(description = "Update an existing action")
    ResponseEntity updateAction(@PathVariable("actionInvariantUuId") String actionInvariantUuId, @RequestBody String requestJson,
                          HttpServletRequest servletRequest);

    /**
     * Delete an action.
     *
     * @param actionInvariantUuId Invariant UuId of the action to be deleted
     * @param servletRequest      Servlet request object
     * @return Empty response object
     */
    @DeleteMapping("/{actionInvariantUuId}")
    @Operation(description = "Delete Action")
    ResponseEntity deleteAction(@PathVariable("actionInvariantUuId") String actionInvariantUuId, HttpServletRequest servletRequest);

    /**
     * Performs Checkout/Undo_Checkout/Checkin/Submit Operation on Action.
     *
     * @return Metadata object {@link ActionResponseDto ActionResponseDto} object for created Action
     */
    @PostMapping("/{actionInvariantUuId}")
    @Operation(description = "Actions on a action", summary = "Performs one of the following actions on a action: |"
        + "Checkout: Locks it for edits by other users. Only the locking user sees the edited " + "version.|"
        + "Undo_Checkout: Unlocks it and deletes the edits that were done.|" + "Checkin: Unlocks it and activates the edited version to all users.| "
        + "Submit: Finalize its active version.|")
    ResponseEntity actOnAction(@PathVariable("actionInvariantUuId") String actionInvariantUuId, String requestJson,
                        HttpServletRequest servletRequest);

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
    @PostMapping(
    value = "/{actionInvariantUuId}/artifacts",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Upload new Artifact")
    ResponseEntity uploadArtifact(
            @PathVariable("actionInvariantUuId") String actionInvariantUuId,
            @RequestPart(value = "artifactName", required = false) String artifactName,
            @RequestPart(value = "artifactLabel", required = false) String artifactLabel,
            @RequestPart(value = "artifactCategory", required = false) String artifactCategory,
            @RequestPart(value = "artifactDescription", required = false) String artifactDescription,
            @RequestPart(value = "artifactProtection", required = false) String artifactProtection,
            @RequestHeader("Content-MD5") String checksum,
            @RequestPart("uploadArtifact") MultipartFile artifactToUpload,
            HttpServletRequest servletRequest
    );

    @GetMapping(value = "/{actionUuId}/artifacts/{artifactUuId}", 
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(description = "Downloads artifact for action")
    ResponseEntity downloadArtifact(@PathVariable("actionUuId") String actionUuId, @PathVariable("artifactUuId") String artifactUuId,
                              HttpServletRequest servletRequest);

    @DeleteMapping("/{actionInvariantUuId}/artifacts/{artifactUuId}")
    @Operation(description = "Delete Artifact")
    ResponseEntity deleteArtifact(@PathVariable("actionInvariantUuId") String actionInvariantUuId, @PathVariable("artifactUuId") String artifactUuId,
                            HttpServletRequest servletRequest);

    @PutMapping(
    value = "/{actionInvariantUuId}/artifacts/{artifactUuId}",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Update an existing artifact")
    ResponseEntity updateArtifact(
            @PathVariable("actionInvariantUuId") String actionInvariantUuId,
            @PathVariable("artifactUuId") String artifactUuId,
            @RequestPart(value = "artifactName", required = false) String artifactName,
            @RequestPart(value = "artifactLabel", required = false) String artifactLabel,
            @RequestPart(value = "artifactCategory", required = false) String artifactCategory,
            @RequestPart(value = "artifactDescription", required = false) String artifactDescription,
            @RequestPart(value = "artifactProtection", required = false) String artifactProtection,
            @RequestHeader("Content-MD5") String checksum,
            @RequestPart(value = "updateArtifact") MultipartFile artifactToUpdate,
            HttpServletRequest servletRequest);
}
