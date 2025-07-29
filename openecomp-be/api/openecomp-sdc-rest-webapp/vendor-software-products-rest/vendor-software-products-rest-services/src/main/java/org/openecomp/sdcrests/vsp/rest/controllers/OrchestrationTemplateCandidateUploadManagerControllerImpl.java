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

package org.openecomp.sdcrests.vsp.rest.controllers;

import java.util.Optional;

import javax.inject.Named;

import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.OrchestrationTemplateCandidateUploadManagerController;
import org.openecomp.sdcrests.vsp.rest.services.OrchestrationTemplateCandidateUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Named
@Service("packageUploadManagerController")
public class OrchestrationTemplateCandidateUploadManagerControllerImpl implements OrchestrationTemplateCandidateUploadManagerController {

    private final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager;

    @Autowired
    public OrchestrationTemplateCandidateUploadManagerControllerImpl(
        final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager) {
        this.orchestrationTemplateCandidateUploadManager = orchestrationTemplateCandidateUploadManager;
    }

    @Override
    public ResponseEntity getLatestStatus(String vspId, String versionId, String user) {
        vspId = ValidationUtils.sanitizeInputString(vspId);
        versionId = ValidationUtils.sanitizeInputString(versionId);
        user = ValidationUtils.sanitizeInputString(user);

        Optional<VspUploadStatusDto> vspUploadStatus = orchestrationTemplateCandidateUploadManager.findLatestStatus(vspId, versionId, user);
        return vspUploadStatus
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Override
    public ResponseEntity createUploadLock(String vspId, String versionId, String user) {
        vspId = ValidationUtils.sanitizeInputString(vspId);
        versionId = ValidationUtils.sanitizeInputString(versionId);
        user = ValidationUtils.sanitizeInputString(user);

        VspUploadStatusDto vspUploadStatus = orchestrationTemplateCandidateUploadManager.putUploadInProgress(vspId, versionId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(vspUploadStatus);
    }

    /**
     * Builds the string representing the get API url.
     *
     * @param vspId the vsp id
     * @param vspVersionId the vsp version id
     * @return the string representing the get API url
     */
    public static String buildGetUrl(final String vspId, final String vspVersionId) {
        return OrchestrationTemplateCandidateUploadManagerController.URL
                .replace("{vspId}", vspId)
                .replace("{versionId}", vspVersionId);
    }
}
