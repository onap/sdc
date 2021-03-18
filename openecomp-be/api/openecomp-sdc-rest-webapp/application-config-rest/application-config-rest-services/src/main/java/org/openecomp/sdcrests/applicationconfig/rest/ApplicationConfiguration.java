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
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.applicationconfiguration.types.ApplicationConfigDto;
import org.openecomp.sdcrests.applicationconfiguration.types.ConfigurationDataDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@Path("/v1.0/application-configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Application Configuration")})
@Validated
public interface ApplicationConfiguration {

  @POST
  @Path("/")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(description = "Insert JSON schema into application config table")
  Response insertToTable(@QueryParam("namespace") String namespace, @QueryParam("key") String key,
                         @Multipart("description") InputStream fileContainingSchema);


  @GET
  @Path("/{namespace}/{key}")
  @Operation(description = "Get JSON schema by namespace and key", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ConfigurationDataDto.class))))
  Response getFromTable(@PathParam("namespace") String namespace, @PathParam("key") String key);


  @GET
  @Path("/{namespace}")
  @Operation(description = "Get List of keys and descriptions by namespace", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ApplicationConfigDto.class)))))
  Response getListOfConfigurationByNamespaceFromTable(@PathParam("namespace") String namespace);

}
