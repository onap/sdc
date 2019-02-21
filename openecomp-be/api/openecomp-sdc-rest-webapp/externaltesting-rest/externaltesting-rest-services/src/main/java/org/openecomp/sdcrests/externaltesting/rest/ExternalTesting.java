/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.sdcrests.externaltesting.rest;

import io.swagger.annotations.Api;
import org.openecomp.core.externaltesting.api.TestExecutionRequest;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1.0/externaltesting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "External-Testing")
@Validated

public interface ExternalTesting {

  @GET
  @Path("/config")
  Response getConfig();


  @GET
  @Path("/")
  Response listTests();

  @GET
  @Path("/{testId}")
  Response getTestDefinition(@PathParam("testId") String testId);


  @GET
  @Path("/{endpointId}/{testId}")
  Response getTestDefinition(@PathParam("endpointId") String endpointId,
                             @PathParam("testId") String testId);


  @POST
  @Path("run")
  Response run(TestExecutionRequest req);

}
