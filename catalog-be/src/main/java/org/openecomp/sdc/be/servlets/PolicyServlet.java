package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTargetDTO;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Provides REST API to create, retrieve, update, delete a policy
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Policy Servlet")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PolicyServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(PolicyServlet.class);
    private final PolicyBusinessLogic policyBusinessLogic;

    public PolicyServlet(PolicyBusinessLogic policyBusinessLogic, ServletUtils servletUtils, ResourceImportManager resourceImportManager, ComponentsUtils componentsUtils) {
        this.policyBusinessLogic = policyBusinessLogic;
        this.servletUtils = servletUtils;
        this.resourceImportManager = resourceImportManager;
        this.componentsUtils = componentsUtils;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/policies/{policyTypeName}")
    @ApiOperation(value = "Create Policy", httpMethod = "POST", notes = "Returns created Policy", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Policy created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Policy already exist"),  @ApiResponse(code = 404, message = "Component not found")})
    public Response createPolicy(@PathParam("componentId") final String containerComponentId,
                                 @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyTypeName") final String policyTypeName,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
                                 @Context final HttpServletRequest request) {
        init();

        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            if (responseWrapper.isEmpty()) {
                responseWrapper.setInnerElement(policyBusinessLogic.createPolicy(componentTypeWrapper.getInnerElement(), containerComponentId, policyTypeName, userId, true)
                        .either(l -> buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), l),
                                this::buildErrorResponse));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Policy");
            log.error("Failed to create policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @ApiOperation(value = "Update Policy metadata", httpMethod = "PUT", notes = "Returns updated Policy", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Policy updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / policy Not found")})
    public Response updatePolicy(@PathParam("componentId") final String containerComponentId,
                                 @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyId") final String policyId, @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
                                 @ApiParam(value = "PolicyDefinition", required = true) String policyData, @Context final HttpServletRequest request) {
        init();

        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            Wrapper<PolicyDefinition> policyWrapper = new Wrapper<>();
            if (responseWrapper.isEmpty()) {
                convertJsonToObjectOfClass(policyData, policyWrapper, PolicyDefinition.class, responseWrapper);
                if (policyWrapper.isEmpty()) {
                    responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT)));
                }
            }
            if (!policyWrapper.isEmpty()) {
                policyWrapper.getInnerElement().setUniqueId(policyId);
                responseWrapper.setInnerElement(policyBusinessLogic.updatePolicy(componentTypeWrapper.getInnerElement(), containerComponentId, policyWrapper.getInnerElement(), userId, true)
                        .either(this::buildOkResponse,
                                this::buildErrorResponse));
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Policy");
            log.error("Failed to update policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @ApiOperation(value = "Get Policy", httpMethod = "GET", notes = "Returns Policy", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Policy was found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / policy Not found")})
    public Response getPolicy(@PathParam("componentId") final String containerComponentId,
                              @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                              @PathParam("policyId") final String policyId, @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
                              @Context final HttpServletRequest request) {
        init();

        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            if (responseWrapper.isEmpty()) {
                responseWrapper.setInnerElement(policyBusinessLogic.getPolicy(componentTypeWrapper.getInnerElement(), containerComponentId, policyId, userId)
                        .either(this::buildOkResponse,
                                this::buildErrorResponse));
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Policy");
            log.error("Failed to retrieve policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}")
    @ApiOperation(value = "Delete Policy", httpMethod = "DELETE", notes = "No body", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Policy was deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / policy Not found")})
    public Response deletePolicy(@PathParam("componentId") final String containerComponentId,
                                 @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("policyId") final String policyId, @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
                                 @Context final HttpServletRequest request) {
        init();

        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            if (responseWrapper.isEmpty()) {
                responseWrapper.setInnerElement(policyBusinessLogic.deletePolicy(componentTypeWrapper.getInnerElement(), containerComponentId, policyId, userId, true)
                        .either(this::buildOkResponse,
                                this::buildErrorResponse));
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Policy");
            log.error("Failed to delete policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/undeclare")
    @ApiOperation(value = "undeclare Policy", httpMethod = "PUT", notes = "No body", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 204, message = "Policy was undeclared"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / policy Not found")})
    public Response undeclarePolicy(@PathParam("componentId") final String containerComponentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("policyId") final String policyId, @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
            @Context final HttpServletRequest request) {
        init();

        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            if (responseWrapper.isEmpty()) {
                responseWrapper.setInnerElement(policyBusinessLogic.undeclarePolicy(componentTypeWrapper.getInnerElement(), containerComponentId, policyId, userId, true)
                                                        .either(this::buildOkResponse,
                                                                this::buildErrorResponse));
            }

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Undeclare Policy");
            log.error("Failed to undeclare policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/properties")
    @ApiOperation(value = "Get component policy properties", httpMethod = "GET", notes = "Returns component policy properties", response = PropertyDataDefinition.class, responseContainer="List")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Properties found"),@ApiResponse(code = 400, message = "invalid content - Error: containerComponentType is invalid"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Componentorpolicy  not found"),
    @ApiResponse(code = 500, message = "The GET request failed due to internal SDC problem.") })public Response getPolicyProperties(@ApiParam(value = "the id of the component which is the container of the policy") @PathParam("componentId") final String containerComponentId,
                                        @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                        @ApiParam(value = "the id of the policy which its properties are to return") @PathParam("policyId") final String policyId,
                                        @ApiParam(value = "the userid", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                        @Context final HttpServletRequest request) {
        init();
        try {
            return convertToComponentType(containerComponentType)
                    .left()
                    .bind(cmptType -> policyBusinessLogic.getPolicyProperties(cmptType, containerComponentId, policyId, userId))
                    .either(this::buildOkResponse,
                            this::buildErrorResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("get Policy properties");
            log.debug("#getPolicyProperties - get Policy properties has failed.", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }


    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/policies/{policyId}/properties")
    @ApiOperation(value = "Update Policy properties", httpMethod = "PUT", notes = "Returns updated Policy", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Policy properties updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / policy Not found")})
    public Response updatePolicyProperties(@PathParam("componentId") final String containerComponentId,
                                           @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                           @PathParam("policyId") final String policyId, @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
                                           @ApiParam(value = "PolicyDefinition", required = true) String policyData, @Context final HttpServletRequest request) {
        init();
        Wrapper<Response> responseWrapper = new Wrapper<>();
        try {
            Wrapper<ComponentTypeEnum> componentTypeWrapper = validateComponentTypeAndUserId(containerComponentType, userId, responseWrapper);
            Wrapper<PropertyDataDefinition[]> propertiesWrapper = new Wrapper<>();
            if (responseWrapper.isEmpty()) {
                convertJsonToObjectOfClass(policyData, propertiesWrapper, PropertyDataDefinition[].class, responseWrapper);
                if (propertiesWrapper.isEmpty()) {
                    responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT)));
                }
            }
            if (!propertiesWrapper.isEmpty()) {
                responseWrapper.setInnerElement(
                        policyBusinessLogic.updatePolicyProperties(componentTypeWrapper.getInnerElement(), containerComponentId, policyId, propertiesWrapper.getInnerElement(), userId, true)
                                .either(this::buildOkResponse, this::buildErrorResponse));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Policy");
            log.error("Failed to update policy. The exception {} occurred. ", e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        }
        return responseWrapper.getInnerElement();
    }

    private Wrapper<ComponentTypeEnum> validateComponentTypeAndUserId(final String containerComponentType, String userId, Wrapper<Response> responseWrapper) {
        Wrapper<ComponentTypeEnum> componentTypeWrapper = new Wrapper<>();
        if (StringUtils.isEmpty(userId)) {
            log.error("Missing userId HTTP header. ");
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID)));
        }
        if (responseWrapper.isEmpty()) {
            validateComponentType(responseWrapper, componentTypeWrapper, containerComponentType);
        }
        return componentTypeWrapper;
    }

	@POST
	@Path("/{containerComponentType}/{componentId}/policies/{policyId}/targets")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "update policy targets", httpMethod = "POST", notes = "Returns updated Policy", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 201, message = "Policy target updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content")})
	public Response updatePolicyTargets(@PathParam("componentId") final String containerComponentId,
										@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
										@PathParam("policyId") final String policyId,
										@HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId,
										@Context final HttpServletRequest request,
										List<PolicyTargetDTO> requestJson) {
		try {

		    return updatePolicyTargetsFromDTO(requestJson)
                    .left()
                    .bind(policyTarget -> updatePolicyTargetsFromMap(policyTarget, containerComponentType, containerComponentId, policyId, userId))
                    .either(this::buildOkResponse, this::buildErrorResponse);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Policy");
            log.debug("Policy target update has been failed with the exception{}. ", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{componentType}/{componentId}/create/policies")
    @ApiOperation(value = "Create policies on service", httpMethod = "POST", notes = "Return policies list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response declareProperties(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "ComponentIns policies Object to be created", required = true) String componentInstPoliciesMapObj) {

        return super.declareProperties(userId, componentId, componentType, componentInstPoliciesMapObj,
                DeclarationTypeEnum.POLICY, request);
    }

    private Either<PolicyDefinition, ResponseFormat> updatePolicyTargetsFromMap(Map<PolicyTargetType, List<String>> policyTarget, String containerComponentType, String containerComponentId, String policyId, String userId) {
        return convertToComponentType(containerComponentType)
                .left()
                .bind(cmptType -> policyBusinessLogic.updatePolicyTargets(cmptType, containerComponentId, policyId, policyTarget, userId));
    }

    private Either<Map<PolicyTargetType, List<String>>, ResponseFormat> updatePolicyTargetsFromDTO(List<PolicyTargetDTO> targetDTOList) {
        Map<PolicyTargetType, List<String>> policyTarget = new HashMap<>();
        for (PolicyTargetDTO currentTarget : targetDTOList) {
            if(!addTargetsByType(policyTarget, currentTarget.getType(), currentTarget.getUniqueIds())){
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.POLICY_TARGET_TYPE_DOES_NOT_EXIST, currentTarget.getType()));
            }
        }
        return Either.left(policyTarget);
    }


    public boolean addTargetsByType(Map<PolicyTargetType, List<String>> policyTarget, String type, List<String> uniqueIds) {
        PolicyTargetType targetTypeEnum = PolicyTargetType.getByNameIgnoreCase(type);
        if(targetTypeEnum != null){
            policyTarget.put(targetTypeEnum, validateUniquenessOfIds(uniqueIds));
            return true;
        }
        else{
            return false;
        }
    }

    private List<String> validateUniquenessOfIds(List<String> uniqueIds) {
        return uniqueIds.stream().distinct().collect(Collectors.toList());
    }
}
