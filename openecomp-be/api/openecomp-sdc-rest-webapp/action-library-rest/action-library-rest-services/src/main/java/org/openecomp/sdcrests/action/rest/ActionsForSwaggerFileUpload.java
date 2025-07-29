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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/workflow/v1.0/actions")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Actions")})
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
