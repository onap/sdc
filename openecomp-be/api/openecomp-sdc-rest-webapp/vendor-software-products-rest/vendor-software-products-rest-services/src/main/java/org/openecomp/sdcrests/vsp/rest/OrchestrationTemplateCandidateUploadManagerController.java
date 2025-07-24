/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.vsp.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SDCE-1 APIs
 * Package Upload Manager
 */
@RestController
@Tag(name = "SDCE-1 APIs")
@Tag(name = "Package Upload Manager")
@Validated
@RequestMapping(OrchestrationTemplateCandidateUploadManagerController.URL)
public interface OrchestrationTemplateCandidateUploadManagerController extends VspEntities {

    String URL = "/v1.0/vendor-software-products/{vspId}/versions/{versionId}/orchestration-template-candidate/upload";

    /**
     * Gets the latest package upload status for a Vendor Software Product version.
     *
     * @param vspId     the vsp id
     * @param versionId the vsp version id
     * @param user      the username accessing the API
     * @return if successful, an OK response with the latest VspUploadStatus information
     */
    @GetMapping("/")
    ResponseEntity getLatestStatus(
            @Parameter(description = "Vendor Software Product id") @PathVariable("vspId") String vspId,
            @Parameter(description = "Vendor Software Product version id") @PathVariable("versionId") String versionId,
            @RequestHeader(name = USER_ID_HEADER_PARAM)
            @NotNull(message = USER_MISSING_ERROR_MSG) String user);

    /**
     * Creates the upload lock, setting the status to upload in progress.
     *
     * @param vspId     the vsp id
     * @param versionId the vsp version id
     * @param user      the username accessing the API
     * @return if successful, an OK response with the created VspUploadStatus information
     */
    @PostMapping("/")
    ResponseEntity createUploadLock(
            @Parameter(description = "Vendor Software Product id") @PathVariable("vspId") String vspId,
            @Parameter(description = "Vendor Software Product version id") @PathVariable("versionId") String versionId,
            @RequestHeader(name = USER_ID_HEADER_PARAM)
            @NotNull(message = USER_MISSING_ERROR_MSG) String user);
}
