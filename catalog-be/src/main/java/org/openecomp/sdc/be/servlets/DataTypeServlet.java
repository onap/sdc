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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.DataTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.dto.PropertyDefinitionDto;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.DataTypeOperation;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/data-types")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class DataTypeServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(DataTypeServlet.class);
    private final DataTypeOperation dataTypeOperation;
    private final DataTypeBusinessLogic dataTypeBusinessLogic;

    public DataTypeServlet(final ComponentsUtils componentsUtils,
                           final DataTypeOperation dataTypeOperation, DataTypeBusinessLogic dataTypeBusinessLogic) {
        super(componentsUtils);
        this.dataTypeOperation = dataTypeOperation;
        this.dataTypeBusinessLogic = dataTypeBusinessLogic;
    }

    @GET
    @Path("{dataTypeUid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get data types", method = "GET", summary = "Returns data types", responses = {
        @ApiResponse(content = @Content(schema = @Schema(implementation = DataTypeDataDefinition.class))),
        @ApiResponse(responseCode = "200", description = "Data type found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Data types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response fetchDataType(@Context final HttpServletRequest request,
                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                  @PathParam("dataTypeUid") String dataTypeUid) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} - modifier id is {}", url, userId);
        final Optional<DataTypeDataDefinition> dataTypeFoundOptional;
        try {
            dataTypeFoundOptional = dataTypeOperation.getDataTypeByUid(dataTypeUid);
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception ex) {
            final String errorMsg = "Unexpected error while listing the Tosca DataType";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, this.getClass().getName(), errorMsg, ex);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        var dataType = dataTypeFoundOptional.orElseThrow(() -> new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, dataTypeUid));
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataType);
    }

    @GET
    @Path("{id}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get a data type properties", method = "GET", summary = "Returns the data type properties", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = PropertyDefinition.class)))),
        @ApiResponse(responseCode = "200", description = "Data type found, properties may be empty"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Data type not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response fetchProperties(@Parameter(in = ParameterIn.PATH, required = true, description = "The data type id")
                                    @PathParam("id") final String id) {
        final List<PropertyDefinition> allProperties = dataTypeOperation.findAllProperties(id);
        return buildOkResponse(allProperties);
    }

    @POST
    @Path("{id}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a property in the given data type", method = "POST", description = "Create a property in the given data type",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = PropertyDefinitionDto.class))),
            @ApiResponse(responseCode = "201", description = "Property created in the data type"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "409", description = "Property already exists in the data type"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Data type not found")
        })
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createProperty(@Parameter(in = ParameterIn.PATH, required = true, description = "The data type id")
                                   @PathParam("id") final String id,
                                   @RequestBody(description = "Property to add", required = true) final PropertyDefinitionDto propertyDefinitionDto) {
        Optional<DataTypeDataDefinition> dataTypeOptional = dataTypeOperation.getDataTypeByUid(id);
        dataTypeOptional.orElseThrow(() -> {
            throw new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, String.format("Failed to find data type '%s'", id));
        });
        DataTypeDataDefinition dataType = dataTypeOptional.get();
        String model = dataType.getModel();
        Optional<DataTypeDataDefinition> propertyDataType = dataTypeOperation.getDataTypeByNameAndModel(propertyDefinitionDto.getType(), model);
        if (propertyDataType.isEmpty()) {
            if (StringUtils.isEmpty(model)) {
                model = Constants.DEFAULT_MODEL_NAME;
            }
            throw new OperationException(ActionStatus.INVALID_MODEL,
                String.format("Property model is not the same as the data type model. Must be '%s'", model));
        }
        if (StringUtils.isEmpty(dataType.getModel())) {
            dataType.setModel(Constants.DEFAULT_MODEL_NAME);
        }
        final PropertyDefinitionDto property = dataTypeOperation.createProperty(id, propertyDefinitionDto);
        dataTypeOperation.updatePropertyInAdditionalTypeDataType(dataType, property, true);
        dataTypeBusinessLogic.updateApplicationDataTypeCache(id);
        return Response.status(Status.CREATED).entity(property).build();
    }

    @PUT
    @Path("{id}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update a property in the given data type", method = "POST", description = "Update a property in the given data type",
        responses = {
            @ApiResponse(content = @Content(schema = @Schema(implementation = PropertyDefinitionDto.class))),
            @ApiResponse(responseCode = "201", description = "Property updated in the data type"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Data type not found")
        })
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateProperty(@Parameter(in = ParameterIn.PATH, required = true, description = "The data type id")
                                   @PathParam("id") final String id,
                                   @RequestBody(description = "Property to update", required = true)
                                   final PropertyDefinitionDto propertyDefinitionDto) {
        Optional<DataTypeDataDefinition> dataTypeOptional = dataTypeOperation.getDataTypeByUid(id);
        dataTypeOptional.orElseThrow(() -> {
            throw new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, String.format("Failed to find data type '%s'", id));
        });
        DataTypeDataDefinition dataType = dataTypeOptional.get();
        String model = dataType.getModel();
        Optional<DataTypeDataDefinition> propertyDataType = dataTypeOperation.getDataTypeByNameAndModel(propertyDefinitionDto.getType(), model);
        if (propertyDataType.isEmpty()) {
            if (StringUtils.isEmpty(model)) {
                model = Constants.DEFAULT_MODEL_NAME;
            }
            throw new OperationException(ActionStatus.INVALID_MODEL,
                String.format("Property model is not the same as the data type model. Must be '%s'", model));
        }
        if (StringUtils.isEmpty(dataType.getModel())) {
            dataType.setModel(Constants.DEFAULT_MODEL_NAME);
        }
        final PropertyDefinitionDto property = dataTypeOperation.updateProperty(id, propertyDefinitionDto);
        dataTypeOperation.updatePropertyInAdditionalTypeDataType(dataType, property, true);
        dataTypeBusinessLogic.updateApplicationDataTypeCache(id);
        return Response.status(Status.CREATED).entity(property).build();
    }

    @GET
    @Path("{dataTypeName}/models")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get models for type", method = "GET", summary = "Returns list of models for type", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "dataTypeModels"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Data type not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getDataTypeModels(@PathParam("dataTypeName") String dataTypeName) {
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
            gson.toJson(dataTypeOperation.getAllDataTypeModels(dataTypeName)));
    }

    @DELETE
    @Path("{dataTypeId}/{propertyId}")
    public Response deleteProperty(@Parameter(in = ParameterIn.PATH, required = true, description = "The data type id")
                                   @PathParam("dataTypeId") final String dataTypeId,
                                   @Parameter(in = ParameterIn.PATH, required = true, description = "The property id to delete")
                                   @PathParam("propertyId") final String propertyId) {
        final Optional<DataTypeDataDefinition> dataTypeOptional = dataTypeOperation.getDataTypeByUid(dataTypeId);
        dataTypeOptional.orElseThrow(() -> {
            throw new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, String.format("Failed to find data type '%s'", dataTypeId));
        });
        final DataTypeDataDefinition dataTypeDataDefinition = dataTypeOptional.get();
        if (StringUtils.isEmpty(dataTypeDataDefinition.getModel())) {
            dataTypeDataDefinition.setModel(Constants.DEFAULT_MODEL_NAME);
        }
        final PropertyDefinitionDto propertyDefinitionDto;
        try {
            propertyDefinitionDto = dataTypeOperation.deleteProperty(dataTypeDataDefinition, propertyId);
            dataTypeOperation.updatePropertyInAdditionalTypeDataType(dataTypeDataDefinition, propertyDefinitionDto, false);
        } catch (OperationException e) {
            final PropertyDefinitionDto dto = new PropertyDefinitionDto();
            dto.setName(extractNameFromPropertyId(propertyId));
            dataTypeOperation.updatePropertyInAdditionalTypeDataType(dataTypeDataDefinition, dto, false);
            throw e;
        } finally {
            dataTypeBusinessLogic.updateApplicationDataTypeCache(dataTypeId);
        }
        return Response.status(Status.OK).entity(propertyDefinitionDto).build();
    }

    @DELETE
    @Path("{dataTypeId}")
    public Response deleteDatatype(@Parameter(in = ParameterIn.PATH, required = true, description = "The data type id")
                                   @PathParam("dataTypeId") final String dataTypeId) {
        final Optional<DataTypeDataDefinition> dataTypeOptional = dataTypeOperation.getDataTypeByUid(dataTypeId);
        dataTypeOptional.orElseThrow(() -> {
            throw new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, String.format("Failed to find data type '%s'", dataTypeId));
        });
        final DataTypeDataDefinition dataTypeDataDefinition = dataTypeOptional.get();
        if (dataTypeDataDefinition.isNormative()) {
            throw new OperationException(ActionStatus.CANNOT_DELETE_SYSTEM_DEPLOYED_RESOURCES, ElementTypeEnum.DATA_TYPE.getToscaEntryName(),
                dataTypeId);
        }
        if (StringUtils.isEmpty(dataTypeDataDefinition.getModel())) {
            dataTypeDataDefinition.setModel(Constants.DEFAULT_MODEL_NAME);
        }
        try {
            dataTypeOperation.deleteDataTypesByDataTypeId(dataTypeId);
            dataTypeOperation.removeDataTypeFromAdditionalType(dataTypeDataDefinition);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Datatype");
            log.debug("delete datatype failed with exception ", e);
            throw e;
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
    }

    private String extractNameFromPropertyId(final String propertyId) {
        final String[] split = propertyId.split("\\.");
        return split[split.length - 1];
    }
}
