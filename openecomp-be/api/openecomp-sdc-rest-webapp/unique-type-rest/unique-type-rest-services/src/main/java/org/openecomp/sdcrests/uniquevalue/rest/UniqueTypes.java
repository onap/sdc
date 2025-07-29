/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdcrests.uniquevalue.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.validation.constraints.NotNull;

import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1.0/unique-types")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Unique Types")})
@Validated
public interface UniqueTypes {

    @GetMapping({ "", "/" })
    @Operation(description = "Lists unique value types")
    ResponseEntity<GenericCollectionWrapper<String>> listUniqueTypes(@NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{type}/values/{value}")
    @Operation(description = "Gets unique value")
    @ApiResponse(responseCode = "200", description = "Indication whether the unique value is occupied")
    @ApiResponse(responseCode = "404", description = "Unsupported unique type")
    ResponseEntity getUniqueValue(@Parameter(description = "The unique value type, for example: 'VlmName'") @PathVariable("type") String type,
                            @Parameter(description = "The unique value") @PathVariable("value") String value,
                            @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
