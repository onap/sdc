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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.CapabilityTypeImportManager;
import org.openecomp.sdc.be.components.impl.CategoriesImportManager;
import org.openecomp.sdc.be.components.impl.DataTypeImportManager;
import org.openecomp.sdc.be.components.impl.GroupTypeImportManager;
import org.openecomp.sdc.be.components.impl.InterfaceLifecycleTypeImportManager;
import org.openecomp.sdc.be.components.impl.PolicyTypeImportManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.ConsumerTwoParam;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/uploadType")
@Api(value = "Catalog Types Upload", description = "Upload Type from yaml")
@Singleton
public class TypesUploadServlet extends AbstractValidationsServlet {
	@Resource
	private CapabilityTypeImportManager capabilityTypeImportManager;

	@Resource
	private InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager;

	@Resource
	private CategoriesImportManager categoriesImportManager;

	@Resource
	private DataTypeImportManager dataTypeImportManager;

	@Resource
	private GroupTypeImportManager groupTypeImportManager;

	@Resource
	private PolicyTypeImportManager policyTypeImportManager;

	private static Logger log = LoggerFactory.getLogger(TypesUploadServlet.class.getName());

	@POST
	@Path("/capability")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Capability Type from yaml", httpMethod = "POST", notes = "Returns created Capability Type", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Capability Type created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "Capability Type already exist") })
	public Response uploadCapabilityType(@ApiParam("FileInputStream") @FormDataParam("capabilityTypeZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		capabilityTypeImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> CapabilityTypeImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createElementsType(responseWrapper, () -> capabilityTypeImportManager.createCapabilityTypes(ymlPayload));
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.CapabilityType.name());
	}

	@POST
	@Path("/interfaceLifecycle")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Interface Lyfecycle Type from yaml", httpMethod = "POST", notes = "Returns created Interface Lifecycle Type", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Interface Lifecycle Type created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "Interface Lifecycle Type already exist") })
	public Response uploadInterfaceLifecycleType(@ApiParam("FileInputStream") @FormDataParam("interfaceLifecycleTypeZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		interfaceLifecycleTypeImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> InterfaceLifecycleTypeImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createElementsType(responseWrapper, () -> interfaceLifecycleTypeImportManager.createLifecycleTypes(ymlPayload));
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, "Interface Types");
	}

	@POST
	@Path("/categories")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Categories from yaml", httpMethod = "POST", notes = "Returns created categories", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Categories created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "Category already exist") })
	public Response uploadCategories(@ApiParam("FileInputStream") @FormDataParam("categoriesZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		categoriesImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> CategoriesImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createElementsType(responseWrapper, () -> categoriesImportManager.createCategories(ymlPayload));
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, "categories");
	}

	@POST
	@Path("/datatypes")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Categories from yaml", httpMethod = "POST", notes = "Returns created data types", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Data types created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "Data types already exist") })
	public Response uploadDataTypes(@ApiParam("FileInputStream") @FormDataParam("dataTypesZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		dataTypeImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> DataTypeImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createDataTypes(responseWrapper, ymlPayload);
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.DataType.getName());
	}

	@POST
	@Path("/grouptypes")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create GroupTypes from yaml", httpMethod = "POST", notes = "Returns created group types", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "group types created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "group types already exist") })
	public Response uploadGroupTypes(@ApiParam("FileInputStream") @FormDataParam("groupTypesZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		groupTypeImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> GroupTypeImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createGroupTypes(responseWrapper, ymlPayload);
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.GroupType.getName());
	}

	@POST
	@Path("/policytypes")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create PolicyTypes from yaml", httpMethod = "POST", notes = "Returns created policy types", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "policy types created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
	@ApiResponse(code = 409, message = "policy types already exist") })
	public Response uploadPolicyTypes(@ApiParam("FileInputStream") @FormDataParam("policyTypesZip") File file, @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
		policyTypeImportManager = initElementTypeImportManager(request.getSession().getServletContext(), () -> PolicyTypeImportManager.class);
		ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod = (responseWrapper, ymlPayload) -> createPolicyTypes(responseWrapper, ymlPayload);
		return uploadElementTypeServletLogic(createElementsMethod, file, request, creator, NodeTypeEnum.PolicyType.getName());
	}

	private Response uploadElementTypeServletLogic(ConsumerTwoParam<Wrapper<Response>, String> createElementsMethod, File file, final HttpServletRequest request, String creator, String elementTypeName) {
		init(log);
		String userId = initHeaderParam(creator, request, Constants.USER_ID_HEADER);
		try {
			Wrapper<Response> responseWrapper = new Wrapper<>();
			Wrapper<User> userWrapper = new Wrapper<>();
			Wrapper<String> yamlStringWrapper = new Wrapper<>();

			String url = request.getMethod() + " " + request.getRequestURI();
			log.debug("Start handle request of {}", url);

			validateUserExist(responseWrapper, userWrapper, userId);

			if (responseWrapper.isEmpty()) {
				validateUserRole(responseWrapper, userWrapper.getInnerElement());
			}

			if (responseWrapper.isEmpty()) {
				validateDataNotNull(responseWrapper, file);
			}

			if (responseWrapper.isEmpty()) {
				fillZipContents(yamlStringWrapper, file);
			}

			if (responseWrapper.isEmpty()) {
				createElementsMethod.accept(responseWrapper, yamlStringWrapper.getInnerElement());
			}

			return responseWrapper.getInnerElement();

		} catch (Exception e) {
			log.debug("create {} failed with exception:", elementTypeName, e);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create " + elementTypeName);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	private <T> void createElementsType(Wrapper<Response> responseWrapper, Supplier<Either<T, ResponseFormat>> elementsCreater) {
		Either<T, ResponseFormat> eitherResult = elementsCreater.get();
		if (eitherResult.isRight()) {
			Response response = buildErrorResponse(eitherResult.right().value());
			responseWrapper.setInnerElement(response);
		} else {
			try {
				Response response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), RepresentationUtils.toRepresentation(eitherResult.left().value()));
				responseWrapper.setInnerElement(response);
			} catch (Exception e) {
				Response response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
				responseWrapper.setInnerElement(response);
			} 
		}
	}

	// data types
	private void createDataTypes(Wrapper<Response> responseWrapper, String dataTypesYml) {
		final Supplier<Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () -> dataTypeImportManager.createDataTypes(dataTypesYml);
		buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.DATA_TYPE_ALREADY_EXIST, NodeTypeEnum.DataType.name());
	}

	// group types
	private void createGroupTypes(Wrapper<Response> responseWrapper, String groupTypesYml) {
		final Supplier<Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () -> groupTypeImportManager.createGroupTypes(groupTypesYml);
		buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.GROUP_TYPE_ALREADY_EXIST, NodeTypeEnum.GroupType.name());
	}

	// policy types
	private void createPolicyTypes(Wrapper<Response> responseWrapper, String policyTypesYml) {
		final Supplier<Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml = () -> policyTypeImportManager.createPolicyTypes(policyTypesYml);
		buildStatusForElementTypeCreate(responseWrapper, generateElementTypeFromYml, ActionStatus.POLICY_TYPE_ALREADY_EXIST, NodeTypeEnum.PolicyType.name());
	}

	// data types
	private <ElementTypeDefinition> void buildStatusForElementTypeCreate(Wrapper<Response> responseWrapper, Supplier<Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat>> generateElementTypeFromYml, ActionStatus alreadyExistStatus, String elementTypeName) {
		Either<List<ImmutablePair<ElementTypeDefinition, Boolean>>, ResponseFormat> eitherResult = generateElementTypeFromYml.get();

		if (eitherResult.isRight()) {
			Response response = buildErrorResponse(eitherResult.right().value());
			responseWrapper.setInnerElement(response);
		} else {
			Object representation;
			try {
				List<ImmutablePair<ElementTypeDefinition, Boolean>> list = eitherResult.left().value();
				ActionStatus status = ActionStatus.OK;
				if (list != null) {

					// Group result by the right value - true or false.
					// I.e., get the number of data types which are new and
					// which are old.
					Map<Boolean, List<ImmutablePair<ElementTypeDefinition, Boolean>>> collect = list.stream().collect(Collectors.groupingBy(ImmutablePair<ElementTypeDefinition, Boolean>::getRight));
					if (collect != null) {
						Set<Boolean> keySet = collect.keySet();
						if (keySet.size() == 1) {
							Boolean isNew = keySet.iterator().next();
							if (isNew.booleanValue() == true) {
								// all data types created at the first time
								status = ActionStatus.CREATED;
							} else {
								// All data types already exists

								status = alreadyExistStatus;
							}
						}
					}
				}
				representation = RepresentationUtils.toRepresentation(eitherResult.left().value());

				Response response = buildOkResponse(getComponentsUtils().getResponseFormat(status), representation);
				responseWrapper.setInnerElement(response);

			} catch (IOException e) {
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create " + elementTypeName);
				log.debug("failed to convert {} to json", elementTypeName, e);
				Response response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
				responseWrapper.setInnerElement(response);
			}
		}
	}

	private <T> T initElementTypeImportManager(ServletContext context, Supplier<Class<T>> classGetter) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		T elementTypeImortManager = webApplicationContext.getBean(classGetter.get());
		return elementTypeImortManager;
	}

}
