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

package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fj.data.Either;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.GenericArtifactBrowserBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.MonitoringBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.RelationshipTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.RequirementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.components.upgrade.UpgradeBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintJacksonDeserializer;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

public class BeGenericServlet extends BasicServlet {

    @Context
    protected HttpServletRequest servletRequest;

    private static final Logger log = Logger.getLogger(BeGenericServlet.class);

    /******************** New error response mechanism
     * @param requestErrorWrapper **************/

    protected Response buildErrorResponse(ResponseFormat requestErrorWrapper) {
        return Response.status(requestErrorWrapper.getStatus()).entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
    }

    protected Response buildGeneralErrorResponse() {
        return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
    }

    protected Response buildOkResponse(Object entity) {
        return buildOkResponseStatic(entity);
    }

    private static Response buildOkResponseStatic(Object entity) {
        return Response.status(Response.Status.OK)
            .entity(entity)
            .build();
    }

    protected Response buildOkResponse(ResponseFormat errorResponseWrapper, Object entity) {
        return buildOkResponse(errorResponseWrapper, entity, null);
    }

    protected Response buildOkResponse(ResponseFormat errorResponseWrapper, Object entity, Map<String, String> additionalHeaders) {
        int status = errorResponseWrapper.getStatus();
        ResponseBuilder responseBuilder = Response.status(status);
        if (entity != null) {
            if (log.isTraceEnabled())
                log.trace("returned entity is {}", entity.toString());
            responseBuilder = responseBuilder.entity(entity);
        }
        if (additionalHeaders != null) {
            for (Entry<String, String> additionalHeader : additionalHeaders.entrySet()) {
                String headerName = additionalHeader.getKey();
                String headerValue = additionalHeader.getValue();
                if (log.isTraceEnabled())
                    log.trace("Adding header {} with value {} to the response", headerName, headerValue);
                responseBuilder.header(headerName, headerValue);
            }
        }
        return responseBuilder.build();
    }

    /*******************************************************************************************************/
    protected Either<User, ResponseFormat> getUser(final HttpServletRequest request, String userId) {
        Either<User, ActionStatus> eitherCreator = getUserAdminManager(request.getSession().getServletContext()).getUser(userId, false);
        if (eitherCreator.isRight()) {
            log.info("createResource method - user is not listed. userId= {}", userId);
            ResponseFormat errorResponse = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_INFORMATION);
            User user = new User("", "", userId, "", null, null);

            getComponentsUtils().auditResource(errorResponse, user, "", AuditingActionEnum.CHECKOUT_RESOURCE);
            return Either.right(errorResponse);
        }
        return Either.left(eitherCreator.left().value());

    }

    UserBusinessLogic getUserAdminManager(ServletContext context) {
        return getClassFromWebAppContext(context, () -> UserBusinessLogic.class);
    }

    protected GenericArtifactBrowserBusinessLogic getGenericArtifactBrowserBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> GenericArtifactBrowserBusinessLogic.class);
    }

    protected ResourceBusinessLogic getResourceBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ResourceBusinessLogic.class);
    }

    protected InterfaceOperationBusinessLogic getInterfaceOperationBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> InterfaceOperationBusinessLogic.class);
    }

    protected CapabilitiesBusinessLogic getCapabilitiesBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> CapabilitiesBusinessLogic.class);
    }

    protected RelationshipTypeBusinessLogic getRelationshipTypeBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> RelationshipTypeBusinessLogic.class);
    }
    protected RequirementBusinessLogic getRequirementBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> RequirementBusinessLogic.class);
    }
    ComponentsCleanBusinessLogic getComponentCleanerBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ComponentsCleanBusinessLogic.class);
    }

    protected ServiceBusinessLogic getServiceBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ServiceBusinessLogic.class);
    }

    ProductBusinessLogic getProductBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ProductBusinessLogic.class);
    }

    protected ArtifactsBusinessLogic getArtifactBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ArtifactsBusinessLogic.class);
    }
    protected UpgradeBusinessLogic getUpgradeBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> UpgradeBusinessLogic.class);
    }

    protected ElementBusinessLogic getElementBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ElementBusinessLogic.class);
    }

    MonitoringBusinessLogic getMonitoringBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> MonitoringBusinessLogic.class);
    }

    protected AssetMetadataConverter getAssetUtils(ServletContext context) {
        return getClassFromWebAppContext(context, () -> AssetMetadataConverter.class);
    }

    protected LifecycleBusinessLogic getLifecycleBL(ServletContext context) {
        return getClassFromWebAppContext(context, () -> LifecycleBusinessLogic.class);
    }

    <T> T getClassFromWebAppContext(ServletContext context, Supplier<Class<T>> businessLogicClassGen) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(businessLogicClassGen.get());
    }

    GroupBusinessLogic getGroupBL(ServletContext context) {

        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(GroupBusinessLogic.class);
    }

    protected ComponentInstanceBusinessLogic getComponentInstanceBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ComponentInstanceBusinessLogic.class);
    }

    protected ComponentsUtils getComponentsUtils() {
        ServletContext context = this.servletRequest.getSession().getServletContext();

        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ComponentsUtils.class);
    }

    /**
     * Used to support Unit Test.<br>
     * Header Params are not supported in Unit Tests
     *
     * @return
     */
    String initHeaderParam(String headerValue, HttpServletRequest request, String headerName) {
        String retValue;
        if (headerValue != null) {
            retValue = headerValue;
        } else {
            retValue = request.getHeader(headerName);
        }
        return retValue;
    }

    protected String getContentDispositionValue(String artifactFileName) {
        return new StringBuilder().append("attachment; filename=\"").append(artifactFileName).append("\"").toString();
    }



    protected ComponentBusinessLogic getComponentBL(ComponentTypeEnum componentTypeEnum, ServletContext context) {
        ComponentBusinessLogic businessLogic;
        switch (componentTypeEnum) {
            case RESOURCE:
                businessLogic = getResourceBL(context);
                break;
            case SERVICE:
                businessLogic = getServiceBL(context);
                break;
            case PRODUCT:
                businessLogic = getProductBL(context);
                break;
            case RESOURCE_INSTANCE:
                businessLogic = getResourceBL(context);
                break;
            default:
                BeEcompErrorManager.getInstance().logBeSystemError("getComponentBL");
                throw new IllegalArgumentException("Illegal component type:" + componentTypeEnum.getValue());
        }
        return businessLogic;
    }

    <T> void convertJsonToObjectOfClass(String json, Wrapper<T> policyWrapper, Class<T> clazz, Wrapper<Response> errorWrapper) {
        T object = null;
        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            log.trace("Starting to convert json to object. Json=\n{}", json);

            SimpleModule module = new SimpleModule("customDeserializationModule");
            module.addDeserializer(PropertyConstraint.class, new PropertyConstraintJacksonDeserializer());
            mapper.registerModule(module);

            object = mapper.readValue(json, clazz);
            if (object != null) {
                policyWrapper.setInnerElement(object);
            } else {
                BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
                log.debug("The object of class {} is null after converting from json. ", clazz);
                errorWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT)));
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
            log.debug("The exception {} occured upon json to object convertation. Json=\n{}", e, json);
            errorWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT)));
        }
    }

    protected Either<Map<String, PropertyDefinition>, ActionStatus> getPropertyModel(String componentId,
                                                                                     String data) {
        JSONParser parser = new JSONParser();
        JSONObject root;
        try {
            Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
            root = (JSONObject) parser.parse(data);

            Set entrySet = root.entrySet();
            Iterator iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry next = (Entry) iterator.next();
                String propertyName = (String) next.getKey();
                JSONObject value = (JSONObject) next.getValue();
                String jsonString = value.toJSONString();
                Either<PropertyDefinition, ActionStatus> convertJsonToObject = convertJsonToObject(jsonString, PropertyDefinition.class);
                if (convertJsonToObject.isRight()) {
                    return Either.right(convertJsonToObject.right().value());
                }
                PropertyDefinition propertyDefinition = convertJsonToObject.left().value();
                String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(componentId, (String) propertyName);
                propertyDefinition.setUniqueId(uniqueId);
                properties.put(propertyName, propertyDefinition);
            }

            return Either.left(properties);
        } catch (ParseException e) {
            log.info("Property conetnt is invalid - {}", data);
            return Either.right(ActionStatus.INVALID_CONTENT);
        }
    }

    protected Either<Map<String, PropertyDefinition>, ActionStatus> getPropertiesListForUpdate(String data) {

        Map<String, PropertyDefinition> properties = new HashMap<>();
        JSONParser parser = new JSONParser();
        JSONArray jsonArray;

        try {
            jsonArray = (JSONArray) parser.parse(data);
            for (Object jsonElement : jsonArray) {
                String propertyAsString = jsonElement.toString();
                Either<PropertyDefinition, ActionStatus> convertJsonToObject = convertJsonToObject(propertyAsString, PropertyDefinition.class);

                if (convertJsonToObject.isRight()) {
                    return Either.right(convertJsonToObject.right().value());
                }

                PropertyDefinition propertyDefinition = convertJsonToObject.left().value();
                properties.put(propertyDefinition.getName(), propertyDefinition);
            }

            return Either.left(properties);
        } catch (Exception e) {
            log.info("Property content is invalid - {}", data);
            return Either.right(ActionStatus.INVALID_CONTENT);
        }

    }


    protected String propertyToJson(Map.Entry<String, PropertyDefinition> property) {
        JSONObject root = new JSONObject();
        String propertyName = property.getKey();
        PropertyDefinition propertyDefinition = property.getValue();
        JSONObject propertyDefinitionO = getPropertyDefinitionJSONObject(propertyDefinition);
        root.put(propertyName, propertyDefinitionO);
        propertyDefinition.getType();
        return root.toString();
    }

    private JSONObject getPropertyDefinitionJSONObject(PropertyDefinition propertyDefinition) {

        Either<String, ActionStatus> either = convertObjectToJson(propertyDefinition);
        if (either.isRight()) {
            return new JSONObject();
        }
        String value = either.left().value();
        try {
            JSONObject root = (JSONObject) new JSONParser().parse(value);
            return root;
        } catch (ParseException e) {
            log.info("failed to convert input to json");
            log.error("failed to convert to json", e);
            return new JSONObject();
        }

    }

    protected  <T> Either<T, ActionStatus> convertJsonToObject(String data, Class<T> clazz) {
        T t = null;
        Type constraintType = new TypeToken<PropertyConstraint>() {
        }.getType();
        Gson
            gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyOperation.PropertyConstraintDeserialiser()).create();
        try {
            log.trace("convert json to object. json=\n {}", data);
            t = gson.fromJson(data, clazz);
            if (t == null) {
                log.info("object is null after converting from json");
                return Either.right(ActionStatus.INVALID_CONTENT);
            }
        } catch (Exception e) {
            // INVALID JSON
            log.info("failed to convert from json");
            log.error("failed to convert from json", e);
            return Either.right(ActionStatus.INVALID_CONTENT);
        }
        return Either.left(t);
    }

    private Either<String, ActionStatus> convertObjectToJson(PropertyDefinition propertyDefinition) {
        Type constraintType = new TypeToken<PropertyConstraint>() {
        }.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyOperation.PropertyConstraintSerialiser()).create();
        try {
            log.trace("convert object to json. propertyDefinition= {}", propertyDefinition);
            String json = gson.toJson(propertyDefinition);
            if (json == null) {
                log.info("object is null after converting to json");
                return Either.right(ActionStatus.INVALID_CONTENT);
            }
            return Either.left(json);
        } catch (Exception e) {
            // INVALID JSON
            log.info("failed to convert to json");
            log.debug("failed to convert fto json", e);
            return Either.right(ActionStatus.INVALID_CONTENT);
        }

    }

    protected PropertyBusinessLogic getPropertyBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        PropertyBusinessLogic propertytBl = webApplicationContext.getBean(PropertyBusinessLogic.class);
        return propertytBl;
    }

    protected InputsBusinessLogic getInputBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(InputsBusinessLogic.class);
    }

    protected PolicyBusinessLogic getPolicyBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(PolicyBusinessLogic.class);
    }

    protected Either<ComponentInstInputsMap, ResponseFormat> parseToComponentInstanceMap(String componentJson, User user, ComponentTypeEnum componentType) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(componentJson, user, ComponentInstInputsMap.class, AuditingActionEnum.CREATE_RESOURCE, componentType);
    }

    protected Response declareProperties(String userId, String componentId, String componentType,
            String componentInstInputsMapObj, DeclarationTypeEnum typeEnum, HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response = null;

        try {
            BaseBusinessLogic businessLogic = getBlForPropertyDeclaration(typeEnum, context);

            // get modifier id
            User modifier = new User();
            modifier.setUserId(userId);
            log.debug("modifier id is {}", userId);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            Either<ComponentInstInputsMap, ResponseFormat> componentInstInputsMapRes = parseToComponentInstanceMap(componentInstInputsMapObj, modifier, componentTypeEnum);
            if (componentInstInputsMapRes.isRight()) {
                log.debug("failed to parse componentInstInputsMap");
                response = buildErrorResponse(componentInstInputsMapRes.right().value());
                return response;
            }

            Either<List<ToscaDataDefinition>, ResponseFormat> propertiesAfterDeclaration = businessLogic
                                                                               .declareProperties(userId, componentId,
                                                                                       componentTypeEnum,
                                                                                       componentInstInputsMapRes.left().value());
            if (propertiesAfterDeclaration.isRight()) {
                log.debug("failed to create inputs  for service: {}", componentId);
                return buildErrorResponse(propertiesAfterDeclaration.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(propertiesAfterDeclaration.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create inputs for service with id: " + componentId);
            log.debug("Properties declaration failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    public BaseBusinessLogic getBlForPropertyDeclaration(DeclarationTypeEnum typeEnum,
                                                          ServletContext context) {
        if(typeEnum.equals(DeclarationTypeEnum.POLICY)) {
            return getPolicyBL(context);
        }

        return getInputBL(context);
    }
}
