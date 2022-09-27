/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/data-types")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class DataTypeServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(DataTypeServlet.class);
    private final HealingJanusGraphGenericDao janusGraphGenericDao;

    public DataTypeServlet(final UserBusinessLogic userAdminManager, final ComponentsUtils componentsUtils,
                           final HealingJanusGraphGenericDao janusGraphGenericDao) {
        super(userAdminManager, componentsUtils);
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @GET
    @Path("{dataType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get data types", method = "GET", summary = "Returns data types", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = Response.class))),
        @ApiResponse(responseCode = "200", description = "datatype"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Data types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response fetchDataType(@Context final HttpServletRequest request,
                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                  @PathParam("dataType") String dataType,
                                  @QueryParam("model") String modelName) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} - modifier id is {}", url, userId);
        final Map<String, Object> props = new HashMap<>();
        props.put(GraphPropertiesDictionary.NAME.getProperty(), dataType);
        props.put(GraphPropertiesDictionary.MODEL.getProperty(), modelName);
        final Either<List<DataTypeData>, JanusGraphOperationStatus> getAllDataTypesByModel = janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.DataType, props, modelName, DataTypeData.class);
        if (getAllDataTypesByModel.isRight()) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT));
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), getAllDataTypesByModel.left().value().get(0));
    }

}
