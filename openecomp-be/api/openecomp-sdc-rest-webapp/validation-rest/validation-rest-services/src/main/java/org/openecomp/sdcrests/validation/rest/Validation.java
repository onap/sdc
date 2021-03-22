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

package org.openecomp.sdcrests.validation.rest;

import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;


@Path("/v1.0/validation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Validation")})
@Validated

public interface Validation {


  @POST
  @Path("{type}/validate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response validateFile(@PathParam("type") String type,
                        @Multipart("validate") InputStream fileToValidate);
}
