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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/togglz")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Togglz")})
@Validated
public interface TogglzFeatures {

    @GET
    @Operation(description = "Get TOGGLZ Features", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = FeatureSetDto.class)))))
    Response getFeatures();

    @PUT
    @Path("/state/{state}")
    @Operation(description = "Update feature toggle state for all features")
    Response setAllFeatures(@PathParam("state") boolean state);

    @PUT
    @Path("/{featureName}/state/{state}")
    @Operation(description = "Update feature toggle state")
    Response setFeatureState(@PathParam("featureName") String featureName, @PathParam("state") boolean state);

    @GET
    @Path("/{featureName}/state")
    @Operation(description = "Get feature toggle state", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = FeatureDto.class))))
    Response getFeatureState(@PathParam("featureName") String featureName);
}
