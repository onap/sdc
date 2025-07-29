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
package org.openecomp.sdcrests.applicationconfig.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/v1.0/application-configuration")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Application Configuration")})
@Validated
public interface ApplicationConfiguration {

    @PostMapping(
    value = { "", "/" },
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Insert JSON schema into application config table")
    ResponseEntity insertToTable(@RequestParam("namespace") String namespace, @RequestParam("key") String key,
                                 @RequestPart("description") MultipartFile fileContainingSchema);

    @GetMapping("/{namespace}/{key}")
    @Operation(description = "Get JSON schema by namespace and key", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ConfigurationDataDto.class))))
    ResponseEntity getFromTable(@PathVariable("namespace") String namespace, @PathVariable("key") String key);

    @GetMapping("/{namespace}")
    @Operation(description = "Get List of keys and descriptions by namespace", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationConfigDto.class)))))
    ResponseEntity getListOfConfigurationByNamespaceFromTable(@PathVariable("namespace") String namespace);
}
