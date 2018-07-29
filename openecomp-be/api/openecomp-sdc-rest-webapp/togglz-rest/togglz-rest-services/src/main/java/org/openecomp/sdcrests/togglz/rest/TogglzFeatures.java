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

import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.togglz.types.FeatureDto;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1.0/togglz")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Validated
public interface TogglzFeatures {

  @GET
  @ApiOperation(value = "Get TOGGLZ Features",
      response = FeatureSetDto.class,
      responseContainer = "List")
  Response getFeatures();


  @PUT
  @Path("/state/{state}")
  @ApiOperation(value = "Update feature toggle state for all features")
  Response setAllFeatures(@PathParam("state") boolean state);


  @PUT
  @Path("/{featureName}/state/{state}")
  @ApiOperation(value = "Update feature toggle state")
  Response setFeatureState(@PathParam("featureName") String featureName, @PathParam("state") boolean state);

  @GET
  @Path("/{featureName}/state")
  @ApiOperation(value = "Get feature toggle state",
          response = FeatureDto.class)
  Response getFeatureState(@PathParam("featureName") String featureName);
}
