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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.togglz.types.FeatureSetDto;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/togglz")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "TOGGLZ")
@Validated
public interface TogglzFeatures {

  @GET
  @ApiOperation(value = "Get TOGGLZ Features",
      response = FeatureSetDto.class,
      responseContainer = "List")
  Response getFeatures();

}
