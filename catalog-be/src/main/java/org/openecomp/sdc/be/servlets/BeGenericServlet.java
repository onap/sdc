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

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.openecomp.sdc.be.components.clean.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.MonitoringBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.VFComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.api.IElementDAO;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

public class BeGenericServlet extends BasicServlet {

	@Context
	protected HttpServletRequest servletRequest;

	private static Logger log = LoggerFactory.getLogger(BeGenericServlet.class.getName());

	/******************** New error response mechanism 
	 * @param additionalParams **************/
	
	protected Response buildErrorResponse(ResponseFormat requestErrorWrapper) {
		return Response.status(requestErrorWrapper.getStatus()).entity(gson.toJson(requestErrorWrapper.getRequestError())).build();		
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

			getComponentsUtils().auditResource(errorResponse, user, null, "", "", AuditingActionEnum.CHECKOUT_RESOURCE, null);
			return Either.right(errorResponse);
		}
		return Either.left(eitherCreator.left().value());

	}

	protected UserBusinessLogic getUserAdminManager(ServletContext context) {
		return getClassFromWebAppContext(context, () -> UserBusinessLogic.class);
	}

	protected ResourceBusinessLogic getResourceBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ResourceBusinessLogic.class);
	}

	protected ComponentsCleanBusinessLogic getComponentCleanerBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ComponentsCleanBusinessLogic.class);
	}

	protected ServiceBusinessLogic getServiceBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ServiceBusinessLogic.class);
	}

	protected ProductBusinessLogic getProductBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ProductBusinessLogic.class);
	}

	protected ArtifactsBusinessLogic getArtifactBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ArtifactsBusinessLogic.class);
	}

	protected ElementBusinessLogic getElementBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> ElementBusinessLogic.class);
	}

	protected MonitoringBusinessLogic getMonitoringBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> MonitoringBusinessLogic.class);
	}

	protected AssetMetadataConverter getAssetUtils(ServletContext context) {
		return getClassFromWebAppContext(context, () -> AssetMetadataConverter.class);
	}
	
	protected LifecycleBusinessLogic getLifecycleBL(ServletContext context) {
		return getClassFromWebAppContext(context, () -> LifecycleBusinessLogic.class);
	}
	
	protected <SomeClass> SomeClass getClassFromWebAppContext(ServletContext context, Supplier<Class<SomeClass>> businessLogicClassGen) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		SomeClass monitoringBusinessLogic = webApplicationContext.getBean(businessLogicClassGen.get());
		return monitoringBusinessLogic;
	}

	protected GroupBusinessLogic getGroupBL(ServletContext context) {

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		GroupBusinessLogic groupBusinessLogic = webApplicationContext.getBean(GroupBusinessLogic.class);
		return groupBusinessLogic;
	}

	protected ComponentInstanceBusinessLogic getComponentInstanceBL(ServletContext context, ComponentTypeEnum containerComponentType) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		if (containerComponentType == ComponentTypeEnum.RESOURCE) {
			return webApplicationContext.getBean(VFComponentInstanceBusinessLogic.class);
		}
		if (containerComponentType == ComponentTypeEnum.SERVICE) {
			return webApplicationContext.getBean(ServiceComponentInstanceBusinessLogic.class);
		}
		if (containerComponentType == ComponentTypeEnum.PRODUCT) {
			return webApplicationContext.getBean(ProductComponentInstanceBusinessLogic.class);
		}
		return null;
	}

	protected IElementDAO getElementDao(Class<? extends IElementDAO> clazz, ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

		return webApplicationContext.getBean(clazz);
	}

	protected ComponentsUtils getComponentsUtils() {
		ServletContext context = this.servletRequest.getSession().getServletContext();

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ComponentsUtils componentsUtils = webApplicationContext.getBean(ComponentsUtils.class);
		return componentsUtils;
	}

	/**
	 * Used to support Unit Test.<br>
	 * Header Params are not supported in Unit Tests
	 * 
	 * @return
	 */
	protected String initHeaderParam(String headerValue, HttpServletRequest request, String headerName) {
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
		case RESOURCE: {
			businessLogic = getResourceBL(context);
			break;
		}
		case SERVICE: {
			businessLogic = getServiceBL(context);
			break;
		}
		case PRODUCT: {
			businessLogic = getProductBL(context);
			break;
		}
		case RESOURCE_INSTANCE: {
			businessLogic = getResourceBL(context);
			break;
		}
		default: {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "getComponentBL");
			BeEcompErrorManager.getInstance().logBeSystemError("getComponentBL");
			throw new IllegalArgumentException("Illegal component type:" + componentTypeEnum.getValue());
		}
		}
		return businessLogic;
	}
}
