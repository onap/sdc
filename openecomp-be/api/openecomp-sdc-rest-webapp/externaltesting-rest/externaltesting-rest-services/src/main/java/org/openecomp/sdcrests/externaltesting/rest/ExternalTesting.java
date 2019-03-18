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
import org.openecomp.core.externaltesting.api.VtpTestExecutionRequest;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


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
  @Path("/testcasetree")
  Response getTestCasesAsTree();

  @GET
  @Path("/endpoints")
  Response getEndpoints();

  @GET
  @Path("/endpoints/{endpointId}/scenarios")
  Response getScenarios(@PathParam("endpointId") String endpointId);

  @GET
  @Path("/endpoints/{endpointId}/scenarios/{scenario}/testsuites")
  Response getTestsuites(@PathParam("endpointId") String endpointId, @PathParam("scenario") String scenario);

  @GET
  @Path("/endpoints/{endpointId}/scenarios/{scenario}/testcases")
  Response getTestcases(@PathParam("endpointId") String endpointId,
                        @PathParam("scenario") String scenario);

  @GET
  @Path("/endpoints/{endpointId}/scenarios/{scenario}/testsuites/{testsuite}/testcases/{testcase}")
  Response getTestcase(@PathParam("endpointId") String endpointId,
                       @PathParam("scenario") String scenario,
                       @PathParam("testsuite") String testsuite,
                       @PathParam("testcase") String testcase);

  @POST
  @Path("/endpoints/{endpointId}/executions/{executionId}")
  Response getExecution(@PathParam("endpointId") String endpointId,
                        @PathParam("executionId") String executionId);


  @POST
  @Path("/executions")
  Response execute(List<VtpTestExecutionRequest> req,
                   @QueryParam("requestId") String requestId);

}
