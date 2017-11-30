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

import javax.inject.Singleton;
import javax.servlet.ServletContext;
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

import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Product Catalog", description = "Product Servlet")
@Singleton
public class ProductServlet extends BeGenericServlet {
	private static Logger log = LoggerFactory.getLogger(ProductServlet.class.getName());

	@POST
	@Path("/products")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create product", httpMethod = "POST", notes = "Returns created product", response = Product.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Product created"), @ApiResponse(code = 403, message = "Restricted operation / Empty USER_ID header"), @ApiResponse(code = 400, message = "Invalid/missing content"),
	@ApiResponse(code = 409, message = "Product already exists / User not found / Wrong user role") })
	public Response createProduct(@ApiParam(value = "Product object to be created", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of product strategist user", required = true) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ProductBusinessLogic businessLogic = getProductBL(context);
			Product product = RepresentationUtils.fromRepresentation(data, Product.class);
			Either<Product, ResponseFormat> actionResponse = businessLogic.createProduct(product, modifier);

			if (actionResponse.isRight()) {
				log.debug("Failed to create product");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Object result = RepresentationUtils.toRepresentation(actionResponse.left().value());
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), result);
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Create Product");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Product");
			log.debug("create product failed with error ", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@GET
	@Path("/products/{productId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve product", httpMethod = "GET", notes = "Returns product according to productId", response = Product.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Product found"), @ApiResponse(code = 403, message = "Missing information"), @ApiResponse(code = 409, message = "Restricted operation"),
			@ApiResponse(code = 500, message = "Internal Server Error"), @ApiResponse(code = 404, message = "Product not found"), })
	public Response getProductById(@PathParam("productId") final String productId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			ProductBusinessLogic businessLogic = getProductBL(context);
			log.trace("get product with id {}", productId);
			Either<Product, ResponseFormat> actionResponse = businessLogic.getProduct(productId, modifier);

			if (actionResponse.isRight()) {
				log.debug("Failed to get product");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Object product = RepresentationUtils.toRepresentation(actionResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), product);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Product");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Product");
			log.debug("get product failed with error ", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@GET
	@Path("/products/productName/{productName}/productVersion/{productVersion}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve Service", httpMethod = "GET", notes = "Returns product according to name and version", response = Product.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Product found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Product not found") })
	public Response getServiceByNameAndVersion(@PathParam("productName") final String productName, @PathParam("productVersion") final String productVersion, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		// get modifier id
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ProductBusinessLogic businessLogic = getProductBL(context);
			Either<Product, ResponseFormat> actionResponse = businessLogic.getProductByNameAndVersion(productName, productVersion, userId);

			if (actionResponse.isRight()) {
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Product product = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(product);

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get product by name and version");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get product by name and version");
			log.debug("get product failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

		}
	}

	@DELETE
	@Path("/products/{productId}")
	public Response deleteProduct(@PathParam("productId") final String productId, @Context final HttpServletRequest request) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			ProductBusinessLogic businessLogic = getProductBL(context);
			log.trace("delete product with id {}", productId);
			Either<Product, ResponseFormat> actionResponse = businessLogic.deleteProduct(productId, modifier);

			if (actionResponse.isRight()) {
				log.debug("Failed to delete product");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Object product = RepresentationUtils.toRepresentation(actionResponse.left().value());
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), product);
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Delete Resource");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource");
			log.debug("delete resource failed with error ", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@PUT
	@Path("/products/{productId}/metadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Product Metadata", httpMethod = "PUT", notes = "Returns updated product", response = Product.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Product Updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateProductMetadata(@PathParam("productId") final String productId, @ApiParam(value = "Product object to be Updated", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);
		Response response = null;

		try {
			String productIdLower = productId.toLowerCase();
			ProductBusinessLogic businessLogic = getProductBL(context);
			Product updatedProduct = RepresentationUtils.fromRepresentation(data, Product.class);
			Either<Product, ResponseFormat> actionResponse = businessLogic.updateProductMetadata(productIdLower, updatedProduct, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to update product");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Product product = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(product);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Update Product Metadata");
			log.debug("update product metadata failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@GET
	@Path("/products/validate-name/{productName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "validate product name", httpMethod = "GET", notes = "checks if the chosen product name is available ", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation") })
	public Response validateServiceName(@PathParam("productName") final String productName, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);
		Response response = null;
		try {
			ProductBusinessLogic businessLogic = getProductBL(context);

			Either<Map<String, Boolean>, ResponseFormat> actionResponse = businessLogic.validateProductNameExists(productName, userId);

			if (actionResponse.isRight()) {
				log.debug("failed to get validate service name");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Validate Service Name");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Product Name");
			log.debug("validate product name failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

}
