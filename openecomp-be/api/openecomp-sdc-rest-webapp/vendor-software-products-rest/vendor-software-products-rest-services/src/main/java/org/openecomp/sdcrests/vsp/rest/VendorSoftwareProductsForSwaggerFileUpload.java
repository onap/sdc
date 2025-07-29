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
package org.openecomp.sdcrests.vsp.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.validation.constraints.NotNull;

import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1.0/vendor-software-products")
@Validated
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Products")})
public interface VendorSoftwareProductsForSwaggerFileUpload {

    @PostMapping(value = "/{vspId}/versions/{versionId}/orchestration-template-candidate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        description = "Uploads a HEAT package to translate",
        responses = @ApiResponse(
            content = @Content(schema = @Schema(implementation = UploadFileResponseDto.class))
        )
    )
    ResponseEntity uploadOrchestrationTemplateCandidate(
        @PathVariable("vspId") String vspId,
        @PathVariable("versionId") String versionId,
        @RequestPart("upload") MultipartFile heatFileToUpload,
        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user
    );
}
