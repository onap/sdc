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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.impl.DownloadArtifactLogic;
import org.openecomp.sdc.be.info.ArtifactAccessInfo;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/resources/available")
public class ResourceArtifactDownloadServlet extends ToscaDaoServlet {

	private static Logger log = LoggerFactory.getLogger(ResourceArtifactDownloadServlet.class.getName());

	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@GET
	@Path("/{resourceName}/{resourceVersion}/artifacts/{artifactName}")
	// @Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getResourceArtifactByName(@PathParam("resourceName") final String resourceName, @PathParam("resourceVersion") final String resourceVersion, @PathParam("artifactName") final String artifactName,
			@Context final HttpServletRequest request) {

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		Response response = null;
		try {
			// get the artifact data
			String artifactId = String.format(Constants.ARTIFACT_ID_FORMAT, resourceName, resourceVersion, artifactName);

			IResourceUploader resouceUploader = getResourceUploader(request.getSession().getServletContext());
			if (resouceUploader == null) {
				return buildResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "");

			}
			Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = resouceUploader.getArtifact(artifactId);

			DownloadArtifactLogic logic = getLogic(request.getSession().getServletContext());
			if (logic == null) {
				return buildResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "");

			}
			response = logic.downloadArtifact(resourceName, resourceVersion, artifactName, getArtifactStatus, artifactId);

			log.info("Finish handle request of {} | result = {}", url, response.getStatus());
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Resource Artifact By Name");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource Artifact By Name");
			log.debug("getResourceArtifactByName failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@GET
	@Path("/{resourceName}/{resourceVersion}/artifacts/{artifactName}/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceArtifactMetadata(@PathParam("resourceName") final String resourceName, @PathParam("resourceVersion") final String resourceVersion, @PathParam("artifactName") final String artifactName,
			@Context final HttpServletRequest request) {

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		Response response = null;
		try {
			IResourceUploader resourceDao = getResourceUploader(request.getSession().getServletContext());
			if (resourceDao == null) {
				log.error("resource dao cannot be found");
				response = buildResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Resource dao cannot be found");
				return response;
			}

			String artifactId = String.format(Constants.ARTIFACT_ID_FORMAT, resourceName, resourceVersion, artifactName);
			Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = resourceDao.getArtifact(artifactId);

			if (getArtifactStatus.isRight()) {
				ResourceUploadStatus status = getArtifactStatus.right().value();
				if (status == ResourceUploadStatus.COMPONENT_NOT_EXIST) {
					response = Response.status(HttpStatus.SC_NOT_FOUND).build();
					log.debug("Could not find artifact for with id: {}", artifactId);
				} else {
					response = Response.status(HttpStatus.SC_NO_CONTENT).build();
					log.debug("Could not find artifact for with id: {}", artifactId);
				}
				return response;
			} else {
				ESArtifactData artifactData = getArtifactStatus.left().value();
				log.debug("found artifact with id: {}", artifactId);
				ArtifactAccessInfo artifactInfo = new ArtifactAccessInfo(artifactData);
				String artifactDataJson = gson.toJson(artifactInfo);
				response = Response.status(HttpStatus.SC_OK).entity(artifactDataJson).type(MediaType.APPLICATION_JSON_TYPE).build();

				log.info("Finish handle request of {} | result = {}", url, response.getStatus());
				return response;
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Resource Artifact Metadata");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource Artifact Metadata");
			log.debug("getResourceArtifactMetadata failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}

	}

	@Override
	public Logger getLogger() {
		return log;
	}
}
