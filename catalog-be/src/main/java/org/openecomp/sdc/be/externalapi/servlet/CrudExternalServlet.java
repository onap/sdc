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

package org.openecomp.sdc.be.externalapi.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoBase;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "CRUD External Servlet", description = "This Servlet serves external users for creating assets and changing their lifecycle state")
@Singleton
public class CrudExternalServlet extends AbstractValidationsServlet {

    @Context
    private HttpServletRequest request;

    private static final Logger log = Logger.getLogger(CrudExternalServlet.class);

    /**
     * Creates a new Resource
     *
     * @param assetType
     * @param data
     * @param userId
     * @param instanceIdHeader
     * @return
     */
    @POST
    @Path("/{assetType}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "creates a resource", httpMethod = "POST", notes = "Creates a resource")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "ECOMP component is authenticated and Asset created", response = Resource.class),
            @ApiResponse(code = 400, message = "Missing  X-ECOMP-InstanceID  HTTP header - POL5001"),
            @ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
            @ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
            @ApiResponse(code = 404, message = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
            @ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
            @ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
            @ApiResponse(code = 400, message = "The name provided for the newly created resource is already in use for another resource in SDC - SVC4050"),
            @ApiResponse(code = 400, message = "Invalid field format. One of the provided fields does not comply with the field rules - SVC4126"),
            @ApiResponse(code = 400, message = "Missing request body. The post request did not contain the expected body - SVC4500"),
            @ApiResponse(code = 400, message = "The resource name is missing in the request body - SVC4062"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT description has wrong format - SVC4064"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT description has wrong format (exceeds limit) - SVC4065"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT tags exceeds character limit - SVC4066"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT vendor name exceeds character limit - SVC4067"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT vendor release exceeds character limit - SVC4068"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT ATT Contact has wrong format - SVC4069"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT name has wrong format - SVC4070"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT vendor name has wrong format - SVC4071"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT vendor release has wrong format - SVC4072"),
            @ApiResponse(code = 400, message = "Create VFCMT request: VFCMT name exceeds character limit - SVC4073")})
    @ApiImplicitParams({@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.Resource", paramType = "body", value = "json describe the created resource")})
    public Response createResourceExternal(
            @ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
            @ApiParam(value = "The user id", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
            @ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
            @ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
            @ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
            @ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
            @ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType,
            @ApiParam( hidden = true) String data) {

        init();

        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("Start handle request of {}", url);
        Resource resource = null;
        User modifier = null;
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(ComponentTypeEnum.RESOURCE.getValue());

        ServletContext context = request.getSession().getServletContext();
        ResourceBusinessLogic resourceBL = getResourceBL(context);
        try {
            // Validate X-ECOMP-InstanceID Header
            if (responseWrapper.isEmpty()) {
                validateXECOMPInstanceIDHeader(instanceIdHeader, responseWrapper);
            }
            // Validate USER_ID Header
            if (responseWrapper.isEmpty()) {
                validateHttpCspUserIdHeader(userId, responseWrapper);
            }
            // Validate assetType
            if (responseWrapper.isEmpty()) {
                if( !AssetTypeEnum.RESOURCES.getValue().equals(assetType) ){
                    responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                }
            }
            //Validate resource type
            if(responseWrapper.isEmpty()){
                JSONParser parser = new JSONParser();
                JSONObject jsonObj = (JSONObject) parser.parse(data);
                String resourceType = (String) jsonObj.get(FilterKeyEnum.RESOURCE_TYPE.getName());
                if( StringUtils.isEmpty(resourceType) || !ResourceTypeEnum.containsName(resourceType) ){
                    resourceCommonInfo.setResourceName((String) jsonObj.get("name"));
                    responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
            }
            // Convert the user json to a resource
            if (responseWrapper.isEmpty()) {
                modifier = new User();
                modifier.setUserId(userId);
                Either<Resource, ResponseFormat> eitherResource = getComponentsUtils()
                        .convertJsonToObjectUsingObjectMapper(data, modifier, Resource.class,
                                null, ComponentTypeEnum.RESOURCE);
                if( eitherResource.isRight() ){
                    responseWrapper.setInnerElement(eitherResource.right().value());
                }
                else{
                    resource = eitherResource.left().value();
                }

            }
            //validate name exist
            if(responseWrapper.isEmpty()){
                if( Strings.isEmpty(resource.getName())){
                    responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
                            ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue()));

                }
            }

            if(responseWrapper.isEmpty()){
                resource.setDerivedFrom(Arrays.asList("tosca.nodes.Root"));
                resource.setSystemName(ValidationUtils.convertToSystemName(resource.getName()));
                resource.setToscaResourceName(CommonBeUtils.generateToscaResourceName(ResourceTypeEnum.VFCMT.name(),
                        resource.getSystemName()));
                handleCategories(context, data, resource, responseWrapper);
            }
            // Create the resource in the dataModel
            if (responseWrapper.isEmpty()) {
                resource = resourceBL.createResource(resource, null,
                        modifier, null, null);
                return buildCreatedResourceResponse(resource, context, responseWrapper);
            } else {
                return buildErrorResponse(responseWrapper.getInnerElement());
            }
        } catch (IOException|ParseException e) {
            final String message = "failed to create vfc monitoring template resource";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return buildErrorResponse(responseWrapper.getInnerElement());
        } catch (ComponentException e){
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(e);
            responseWrapper.setInnerElement(responseFormat);
            return buildErrorResponse(responseFormat);
        }
        finally{
            getComponentsUtils().auditCreateResourceExternalApi(responseWrapper.getInnerElement(), resourceCommonInfo, request, resource);
        }
    }

    /**
     * Changing the lifecycle of an asset
     * @param jsonChangeInfo    The description - request body
     * @param assetType The requested asset type.Valid values are: resources / services (for VFCMT – use "resources")
     * @param uuid The uuid of the desired resource to be changed
     * @param lifecycleTransition The lifecycle operation to be performed on the asset.Valid values are:Checkin / Checkout /  CERTIFICATION_REQUEST
     * @param userId
     * @return
     */
    @POST
    @Path("/{assetType}/{uuid}/lifecycleState/{lifecycleOperation}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Change Resource lifecycle State", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Resource state changed", response = AssetMetadata.class),
            @ApiResponse(code = 400, message = "Missing X-ECOMP-InstanceID HTTP header - POL5001"),
            @ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
            @ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
            @ApiResponse(code = 404, message = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
            @ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
            @ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
            @ApiResponse(code = 403, message = "Asset is already checked-out by another user - SVC4085"),
            @ApiResponse(code = 403, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4080")})
    @ApiImplicitParams({@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction", paramType = "body", value = "userRemarks - Short description (free text) about the asset version being changed")})
    public Response changeResourceStateExternal(
            @ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
            @ApiParam(value = "The user id", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
            @ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
            @ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
            @ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
            @ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
            @ApiParam(allowableValues = "checkout, checkin", required = true) @PathParam(value = "lifecycleOperation") final String lifecycleTransition,
            @ApiParam(value = "id of component to be changed") @PathParam(value = "uuid") final String uuid,
            @ApiParam(value = "validValues: resources / services ", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam(value = "assetType") final String assetType,
            @ApiParam( hidden = true) String jsonChangeInfo) {

        Response response = null;

        init();

        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("Start handle request of {}", url);

        //get the business logic
        ServletContext context = request.getSession().getServletContext();
        LifecycleBusinessLogic businessLogic = getLifecycleBL(context);

        Wrapper<ResponseFormat> responseWrapper = runValidations(assetType);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        Component component = null;
        Component responseObject = null;
        User modifier = null;

        try{
            // Validate X-ECOMP-InstanceID Header
            if (responseWrapper.isEmpty()) {
                validateXECOMPInstanceIDHeader(instanceIdHeader, responseWrapper);
            }

            if (responseWrapper.isEmpty()) {
                //get user
                Either<User, ResponseFormat> eitherGetUser = getUser(request, userId);
                if (eitherGetUser.isRight()) {
                    ResponseFormat responseFormat = eitherGetUser.right().value();
                    responseWrapper.setInnerElement(responseFormat);
                    return buildErrorResponse(responseFormat);
                }
                modifier = eitherGetUser.left().value();

                //get the component id from the uuid
                Either<Component, ResponseFormat> latestVersion = businessLogic.getLatestComponentByUuid(componentType, uuid);
                if (latestVersion.isRight()) {
                    ResponseFormat responseFormat = latestVersion.right().value();
                    responseWrapper.setInnerElement(responseFormat);
                    return buildErrorResponse(responseFormat);
                }
                component = latestVersion.left().value();
                String componentId = component.getUniqueId();

                //validate the transition is valid
                Either<LifeCycleTransitionEnum, ResponseFormat> validateEnum = validateTransitionEnum(lifecycleTransition, modifier);
                if (validateEnum.isRight()) {
                    ResponseFormat responseFormat = validateEnum.right().value();
                    responseWrapper.setInnerElement(responseFormat);
                    return buildErrorResponse(responseFormat);
                }
                LifeCycleTransitionEnum transitionEnum = validateEnum.left().value();

                //create changeInfo
                LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction();
                try {
                    if (jsonChangeInfo != null && !jsonChangeInfo.isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        changeInfo = new LifecycleChangeInfoWithAction(mapper.readValue(jsonChangeInfo, LifecycleChangeInfoBase.class).getUserRemarks());
                    }
                }
                catch (IOException e) {
                    BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
                    log.debug("failed to convert from json {}", jsonChangeInfo, e);
                    ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(modifier, componentId, AuditingActionEnum.CHECKOUT_RESOURCE);
                    responseWrapper.setInnerElement(responseFormat);
                    return buildErrorResponse(responseFormat);
                }

                //execute business logic
                Either<? extends Component, ResponseFormat> actionResponse = businessLogic.changeComponentState(componentType, componentId, modifier, transitionEnum, changeInfo, false, true);
                if (actionResponse.isRight()) {
                    log.info("failed to change resource state");
                    ResponseFormat responseFormat = actionResponse.right().value();
                    responseWrapper.setInnerElement(responseFormat);
                    return buildErrorResponse(responseFormat);
                }

                log.debug("change state successful !!!");
                responseObject = actionResponse.left().value();
                response = buildCreatedResourceResponse(responseObject, context, responseWrapper);
            } else {
                response = buildErrorResponse(responseWrapper.getInnerElement());
            }

            return response;
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Change Lifecycle State");
            log.debug("change lifecycle state failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(responseFormat);
            return buildErrorResponse(responseFormat);
        } catch (ComponentException e){
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(e);
            responseWrapper.setInnerElement(responseFormat);
            return buildErrorResponse(responseFormat);
        }
        finally{
            getComponentsUtils().auditChangeLifecycleAction(responseWrapper.getInnerElement(), componentType, requestId,
                    component, responseObject, new DistributionData(instanceIdHeader, requestURI), modifier);
        }
    }

    private Response buildCreatedResourceResponse(Component resource, ServletContext context,
            Wrapper<ResponseFormat> responseWrapper) throws IOException {
        ResponseFormat responseFormat;
        Response response;
        AssetMetadataConverter assetMetadataUtils = getAssetUtils(context);
        Either<? extends AssetMetadata, ResponseFormat> resMetadata = assetMetadataUtils
                .convertToSingleAssetMetadata(resource, request.getRequestURL().toString(),
                        true);
        if (resMetadata.isRight()) {
            log.debug("Asset conversion Failed");
            responseFormat = resMetadata.right().value();
            responseWrapper.setInnerElement(responseFormat);
            response = buildErrorResponse(responseFormat);
        }else{
            final AssetMetadata assetData = resMetadata.left().value();
            assetData.setToscaModelURL(null);

            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.CREATED));
            Object representation = RepresentationUtils.toRepresentation(assetData);
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
        }
        return response;
    }

    private void handleCategories(ServletContext context, String data, Resource resource,
            Wrapper<ResponseFormat> responseWrapper) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(data);
            String category = (String) jsonObj.get(CategoryTypeEnum.CATEGORY.getValue());
            String subcategory = (String) jsonObj.get(CategoryTypeEnum.SUBCATEGORY.getValue());
            if (Strings.isEmpty(category)) {
                responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
                        ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
            }
            else if (Strings.isEmpty(subcategory)) {
                responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
                        ActionStatus.COMPONENT_MISSING_SUBCATEGORY));
            }
            if (responseWrapper.isEmpty()) {
                ElementBusinessLogic elementLogic = getElementBL(context);
                // get All Categories
                Either<List<CategoryDefinition>, ActionStatus> allResourceCategories = elementLogic
                        .getAllResourceCategories();
                // Error fetching categories
                if (allResourceCategories.isRight()) {
                    responseWrapper.setInnerElement(
                            getComponentsUtils().getResponseFormat(allResourceCategories.right().value()));
                } else {
                    addCategories(resource, category, subcategory, allResourceCategories, responseWrapper);
                }
            }
        } catch (ParseException e) {
            log.debug("Exception occured in addCategories: {}", e.getMessage(), e);
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    private void addCategories(Resource resource, String category, String subcategory,
            Either<List<CategoryDefinition>, ActionStatus> allResourceCategories,
            Wrapper<ResponseFormat> responseWrapper) {
        Optional<CategoryDefinition> optionalCategory =
                // Stream of all the categories
                allResourceCategories.left().value().stream()
                        // filter in only relevant category
                        .filter(e -> e.getName().equals(category))
                        // get the result
                        .findAny();
        if (!optionalCategory.isPresent()) {
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
                    ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
        } else {
            CategoryDefinition categoryDefinition = optionalCategory.get();

            List<SubCategoryDefinition> subCaregories =
                    // Stream of all sub-categories of the relevant
                    // category
                    categoryDefinition.getSubcategories().stream()
                            // filter in only relevant sub-category
                            .filter(e -> e.getName().equals(subcategory))
                            // get the result
                            .collect(Collectors.toList());

            if( subCaregories.isEmpty() ){
                responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
                        ActionStatus.COMPONENT_INVALID_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
            }
            else{
                categoryDefinition.setSubcategories(subCaregories);
                resource.setCategories(Arrays.asList(categoryDefinition));
            }

        }
    }






    private Wrapper<ResponseFormat> runValidations(final String assetType) {
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();

        // Validate X-ECOMP-InstanceID Header
        if (responseWrapper.isEmpty()) {
            String instanceId = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
            validateXECOMPInstanceIDHeader(instanceId,responseWrapper);
        }
        // Validate USER_ID Header
        if (responseWrapper.isEmpty()) {
            validateHttpCspUserIdHeader(request.getHeader(Constants.USER_ID_HEADER),responseWrapper);
        }
        // Validate assetType
        if (responseWrapper.isEmpty()) {
            if( !AssetTypeEnum.RESOURCES.getValue().equals(assetType) &&  !AssetTypeEnum.SERVICES.getValue().equals(assetType)){
                responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            }
        }

        return responseWrapper;
    }

    private Either<LifeCycleTransitionEnum, ResponseFormat> validateTransitionEnum(final String lifecycleTransition, User user) {
        LifeCycleTransitionEnum transitionEnum = LifeCycleTransitionEnum.CHECKOUT;
        try {
            transitionEnum = LifeCycleTransitionEnum.getFromDisplayName(lifecycleTransition);
        } catch (IllegalArgumentException e) {
            log.info("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString(), e);
            ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, "", AuditingActionEnum.CHECKOUT_RESOURCE);
            return Either.right(error);
        }
        return Either.left(transitionEnum);
    }

}
