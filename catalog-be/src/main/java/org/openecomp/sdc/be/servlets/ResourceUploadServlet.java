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

import java.io.File;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/upload")
@Api(value = "Resources Catalog Upload", description = "Upload resource yaml")
@Singleton
public class ResourceUploadServlet extends AbstractValidationsServlet {

	private static Logger log = LoggerFactory.getLogger(ResourceUploadServlet.class.getName());
	public static final String NORMATIVE_TYPE_RESOURCE = "multipart";
	public static final String CSAR_TYPE_RESOURCE = "csar";
	public static final String USER_TYPE_RESOURCE = "user-resource";
	public static final String USER_TYPE_RESOURCE_UI_IMPORT = "user-resource-ui-import";

	public enum ResourceAuthorityTypeEnum {
		NORMATIVE_TYPE_BE(NORMATIVE_TYPE_RESOURCE, true, false), USER_TYPE_BE(USER_TYPE_RESOURCE, true, true), USER_TYPE_UI(USER_TYPE_RESOURCE_UI_IMPORT, false, true), CSAR_TYPE_BE(CSAR_TYPE_RESOURCE, true, true);

		private String urlPath;
		private boolean isBackEndImport, isUserTypeResource;

		public static ResourceAuthorityTypeEnum findByUrlPath(String urlPath) {
			ResourceAuthorityTypeEnum found = null;
			for (ResourceAuthorityTypeEnum curr : ResourceAuthorityTypeEnum.values()) {
				if (curr.getUrlPath().equals(urlPath)) {
					found = curr;
					break;
				}
			}
			return found;
		}

		private ResourceAuthorityTypeEnum(String urlPath, boolean isBackEndImport, boolean isUserTypeResource) {
			this.urlPath = urlPath;
			this.isBackEndImport = isBackEndImport;
			this.isUserTypeResource = isUserTypeResource;
		}

		public String getUrlPath() {
			return urlPath;
		}

		public boolean isBackEndImport() {
			return isBackEndImport;
		}

		public boolean isUserTypeResource() {
			return isUserTypeResource;
		}
	}

	@Resource
	private ResourceImportManager resourceImportManager;

	@POST
	@Path("/{resourceAuthority}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource from yaml", httpMethod = "POST", notes = "Returns created resource", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Resource created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Resource already exist") })
	public Response uploadMultipart(
			@ApiParam(value = "validValues: normative-resource / user-resource", allowableValues = NORMATIVE_TYPE_RESOURCE + "," + USER_TYPE_RESOURCE + ","
					+ USER_TYPE_RESOURCE_UI_IMPORT) @PathParam(value = "resourceAuthority") final String resourceAuthority,
			@ApiParam("FileInputStream") @FormDataParam("resourceZip") File file, @ApiParam("ContentDisposition") @FormDataParam("resourceZip") FormDataContentDisposition contentDispositionHeader,
			@ApiParam("resourceMetadata") @FormDataParam("resourceMetadata") String resourceInfoJsonString, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
			// updateResourse Query Parameter if false checks if already exist
			@DefaultValue("true") @QueryParam("createNewVersion") boolean createNewVersion) {

		init(request.getSession().getServletContext());
		try {

			Wrapper<Response> responseWrapper = new Wrapper<>();
			Wrapper<User> userWrapper = new Wrapper<>();
			Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
			Wrapper<String> yamlStringWrapper = new Wrapper<>();

			String url = request.getMethod() + " " + request.getRequestURI();
			log.debug("Start handle request of {}", url);

			// When we get an errorResponse it will be filled into the
			// responseWrapper
			validateAuthorityType(responseWrapper, resourceAuthority);

			ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.findByUrlPath(resourceAuthority);

			commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, resourceInfoJsonString);

			fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), resourceInfoJsonString, resourceAuthorityEnum, file);

			// PayLoad Validations
			if(!resourceAuthorityEnum.equals(ResourceAuthorityTypeEnum.CSAR_TYPE_BE)){
				commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement());

				specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request, resourceInfoJsonString, resourceAuthorityEnum);
			}

			if (responseWrapper.isEmpty()) {
				handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(), yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, createNewVersion, null);
			}

			return responseWrapper.getInnerElement();

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Upload Resource");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Upload Resource");
			log.debug("upload resource failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	/********************************************************************************************************************/

	private void init(ServletContext context) {
		init(log);
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		resourceImportManager = webApplicationContext.getBean(ResourceImportManager.class);
		resourceImportManager.init(context);
	}
}
