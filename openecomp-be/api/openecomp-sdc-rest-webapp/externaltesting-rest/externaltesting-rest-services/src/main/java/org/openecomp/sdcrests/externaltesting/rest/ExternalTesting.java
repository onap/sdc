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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.core.externaltesting.api.ClientConfiguration;
import org.openecomp.core.externaltesting.api.RemoteTestingEndpointDefinition;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/externaltesting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "External-Testing"))
@Validated

public interface ExternalTesting {

    @GET
    @Path("/config")
    Response getConfig();

    @PUT
    @Path("/config")
    Response setConfig(ClientConfiguration config);

    @GET
    @Path("/testcasetree")
    Response getTestCasesAsTree();

    @GET
    @Path("/endpoints")
    Response getEndpoints();

    @PUT
    @Path("/endpoints")
    Response setEndpoints(List<RemoteTestingEndpointDefinition> endpoints);

    @GET
    @Path("/endpoints/{endpointId}/scenarios")
    Response getScenarios(@PathParam("endpointId") String endpointId);

    @GET
    @Path("/endpoints/{endpointId}/scenarios/{scenario}/testsuites")
    Response getTestsuites(@PathParam("endpointId") String endpointId, @PathParam("scenario") String scenario);

    @GET
    @Path("/endpoints/{endpointId}/scenarios/{scenario}/testcases")
    Response getTestcases(@PathParam("endpointId") String endpointId, @PathParam("scenario") String scenario);

    @GET
    @Path("/endpoints/{endpointId}/scenarios/{scenario}/testsuites/{testsuite}/testcases/{testcase}")
    Response getTestcase(@PathParam("endpointId") String endpointId, @PathParam("scenario") String scenario,
            @PathParam("testsuite") String testsuite, @PathParam("testcase") String testcase);

    @POST
    @Path("/endpoints/{endpointId}/executions/{executionId}")
    Response getExecution(@PathParam("endpointId") String endpointId, @PathParam("executionId") String executionId);


    @POST
    @Path("/executions")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response execute(@QueryParam("vspId") String vspId, @QueryParam("vspVersionId") String vspVersionId,
            @QueryParam("requestId") String requestId,
            @Multipart(value = "files", required = false) List<Attachment> files,
            @Multipart(value = "testdata", required = false) String testData);

    @GET
    @Path("/executions")
    Response getValidationResult(@QueryParam("requestId") String requestId,
            @QueryParam("endPoint") List<String> endPoints);
}
