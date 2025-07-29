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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.validation.constraints.NotNull;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/uploads/")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Component Uploads")})
@Validated
public interface ComponentMonitoringUploads extends VspEntities {

    @PostMapping("types/{type}/")
    @Operation(description = "Upload file for component by type")
    ResponseEntity upload(@RequestPart("upload") MultipartFile multipartFile,
                          @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                          @Parameter(description = "Component Id") @PathVariable("componentId") String componentId,
                          @Parameter(description = "Upload Type") @PathVariable("type") String type,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws Exception;

    @DeleteMapping("types/{type}")
    @Operation(description = "Delete file uploaded for component")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Component Id") @PathVariable("componentId") String componentId,
                    @Parameter(description = "Upload Type") @PathVariable("type") String type,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws Exception;


    @GetMapping("")
    @Operation(description = "Get the filenames of uploaded files by type", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = MonitoringUploadStatusDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                  @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
