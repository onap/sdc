/*
 * Copyright © 2016-2017 European Support Limited
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
 */
package org.openecomp.sdcrests.togglz.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Togglz")})
@Validated
@RestController
@RequestMapping("/v1.0/togglz")
public interface TogglzFeatures {

    @GetMapping
    @Operation(description = "Get TOGGLZ Features", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = FeatureSetDto.class)))))
    ResponseEntity getFeatures();

    @PutMapping("/state/{state}")
    @Operation(description = "Update feature toggle state for all features")
    ResponseEntity setAllFeatures(@PathVariable("state") boolean state);

    @PutMapping("/{featureName}/state/{state}")
    @Operation(description = "Update feature toggle state")
    ResponseEntity setFeatureState(@PathVariable("featureName") String featureName, @PathVariable("state") boolean state);

    @GetMapping("/{featureName}/state")
    @Operation(description = "Get feature toggle state", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = FeatureDto.class))))
    ResponseEntity getFeatureState(@PathVariable("featureName") String featureName);
}
