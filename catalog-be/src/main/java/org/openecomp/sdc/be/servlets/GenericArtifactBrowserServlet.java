/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.GenericArtifactBrowserBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.GenericArtifactQueryInfo;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.owasp.esapi.ESAPI;
import org.springframework.stereotype.Controller;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/gab")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Generic Artifact Browser"))
@Controller
public class GenericArtifactBrowserServlet extends BeGenericServlet {

    private static final Logger LOGGER = Logger.getLogger(GenericArtifactBrowserServlet.class);
    private final GenericArtifactBrowserBusinessLogic gabLogic;
    private final ArtifactsBusinessLogic artifactsBusinessLogic;

    @Inject
    public GenericArtifactBrowserServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        ArtifactsBusinessLogic artifactsBusinessLogic,
        GenericArtifactBrowserBusinessLogic gabLogic) {
        super(userBusinessLogic, componentsUtils);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
        this.gabLogic = gabLogic;
    }

    @POST
    @Path("/searchFor")
    @Operation(description = "Search json paths inside the yaml", method = "POST", summary = "Returns found entries of json paths",responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returned yaml entries"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response searchFor(
        @Parameter(description = "Generic Artifact search model", required = true) GenericArtifactQueryInfo query,
        @Context final HttpServletRequest request) {
        try {
            Either<ImmutablePair<String, byte[]>, ResponseFormat> immutablePairResponseFormatEither = artifactsBusinessLogic
                .downloadArtifact(ESAPI.encoder().canonicalize(query.getParentId()), ESAPI.encoder().canonicalize(query.getArtifactUniqueId()));
            if (immutablePairResponseFormatEither.isLeft()){
                GABQuery gabQuery = prepareGabQuery(query, immutablePairResponseFormatEither);
                return buildOkResponse(gabLogic.searchFor(gabQuery));
            }else{
                throw new IOException(immutablePairResponseFormatEither.right().value().getFormattedMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Cannot search for a given queries in the yaml file", e);
            return buildGeneralErrorResponse();
        }
    }

    private GABQuery prepareGabQuery(GenericArtifactQueryInfo query,
        Either<ImmutablePair<String, byte[]>, ResponseFormat> immutablePairResponseFormatEither) {
        byte[] content = immutablePairResponseFormatEither.left().value().getRight();
        Set<String> queryFields = query.getFields().stream().map(ESAPI.encoder()::canonicalize).collect(Collectors.toSet());
        return new GABQuery(queryFields, new String(content), GABQueryType.CONTENT);
    }
}
