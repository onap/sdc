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
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
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
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "Product Catalog", description = "Product Catalog"))
@Singleton
public class ProductServlet extends BeGenericServlet {
    private static final Logger log = Logger.getLogger(ProductServlet.class);
    private final ProductBusinessLogic productBusinessLogic;

    @Inject
    public ProductServlet(UserBusinessLogic userBusinessLogic,
        ProductBusinessLogic productBusinessLogic,
        ComponentsUtils componentsUtils) {
        super(userBusinessLogic, componentsUtils);
        this.productBusinessLogic = productBusinessLogic;
    }

    @POST
    @Path("/products")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create product", method = "POST", summary = "Returns created product",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation / Empty USER_ID header"),
            @ApiResponse(responseCode = "400", description = "Invalid/missing content"),
            @ApiResponse(responseCode = "409", description = "Product already exists / User not found / Wrong user role")})
    public Response createProduct(@Parameter(description = "Product object to be created", required = true) String data,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of product strategist user",
                    required = true) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;
        try {
            Product product = RepresentationUtils.fromRepresentation(data, Product.class);
            Either<Product, ResponseFormat> actionResponse = productBusinessLogic.createProduct(product, modifier);

            if (actionResponse.isRight()) {
                log.debug("Failed to create product");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Object result = RepresentationUtils.toRepresentation(actionResponse.left().value());
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), result);
            return response;

        } catch (Exception e) {
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
    @Operation(description = "Retrieve product", method = "GET", summary = "Returns product according to productId",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "403", description = "Missing information"),
            @ApiResponse(responseCode = "409", description = "Restricted operation"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
            @ApiResponse(responseCode = "404", description = "Product not found"),})
    public Response getProductById(@PathParam("productId") final String productId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;

        try {
            log.trace("get product with id {}", productId);
            Either<Product, ResponseFormat> actionResponse = productBusinessLogic.getProduct(productId, modifier);

            if (actionResponse.isRight()) {
                log.debug("Failed to get product");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Object product = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), product);

        } catch (Exception e) {
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
    @Operation(description = "Retrieve Service", method = "GET",
            summary = "Returns product according to name and version",responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public Response getServiceByNameAndVersion(@PathParam("productName") final String productName,
            @PathParam("productVersion") final String productVersion, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;
        try {
            Either<Product, ResponseFormat> actionResponse =
                    productBusinessLogic.getProductByNameAndVersion(productName, productVersion, userId);

            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Product product = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(product);

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get product by name and version");
            log.debug("get product failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @DELETE
    @Path("/products/{productId}")
    public Response deleteProduct(@PathParam("productId") final String productId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;

        try {
            log.trace("delete product with id {}", productId);
            Either<Product, ResponseFormat> actionResponse = productBusinessLogic.deleteProduct(productId, modifier);

            if (actionResponse.isRight()) {
                log.debug("Failed to delete product");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Object product = RepresentationUtils.toRepresentation(actionResponse.left().value());
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), product);
            return response;

        } catch (Exception e) {
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
    @Operation(description = "Update Product Metadata", method = "PUT", summary = "Returns updated product",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Product.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Product Updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateProductMetadata(@PathParam("productId") final String productId,
            @Parameter(description = "Product object to be Updated", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        Response response = null;

        try {
            String productIdLower = productId.toLowerCase();
            Product updatedProduct = RepresentationUtils.fromRepresentation(data, Product.class);
            Either<Product, ResponseFormat> actionResponse =
                    productBusinessLogic.updateProductMetadata(productIdLower, updatedProduct, modifier);

            if (actionResponse.isRight()) {
                log.debug("failed to update product");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            Product product = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(product);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            log.debug("update product metadata failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    @GET
    @Path("/products/validate-name/{productName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "validate product name", method = "GET",
            summary = "checks if the chosen product name is available ",responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation")})
    public Response validateServiceName(@PathParam("productName") final String productName,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        Response response = null;
        try {
            Either<Map<String, Boolean>, ResponseFormat> actionResponse =
                    productBusinessLogic.validateProductNameExists(productName, userId);

            if (actionResponse.isRight()) {
                log.debug("failed to get validate service name");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Product Name");
            log.debug("validate product name failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

}
