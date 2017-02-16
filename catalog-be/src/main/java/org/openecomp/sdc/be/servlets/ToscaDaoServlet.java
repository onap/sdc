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

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.impl.DownloadArtifactLogic;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.info.ServletJsonResponse;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.springframework.web.context.WebApplicationContext;

public abstract class ToscaDaoServlet extends BeGenericServlet {
	public abstract Logger getLogger();

	protected IResourceUploader getResourceUploader(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		if (webApplicationContextWrapper == null) {
			getLogger().error("Failed to get web application context from context.");
			return null;
		}

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

		return webApplicationContext.getBean(IResourceUploader.class);

	}

	// protected IToscaYamlBuilder getToscaYamlBuilder(ServletContext context){
	// WebAppContextWrapper webApplicationContextWrapper =
	// (WebAppContextWrapper) context
	// .getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
	//
	// if (webApplicationContextWrapper == null) {
	// getLogger().error("Failed to get web application context from context.");
	// return null;
	// }
	//
	// WebApplicationContext webApplicationContext =
	// webApplicationContextWrapper
	// .getWebAppContext(context);
	//
	// return webApplicationContext.getBean(IToscaYamlBuilder.class);
	//
	// }

	protected DownloadArtifactLogic getLogic(ServletContext context) {
		DownloadArtifactLogic downloadLogic = (DownloadArtifactLogic) context.getAttribute(Constants.DOWNLOAD_ARTIFACT_LOGIC_ATTR);

		if (downloadLogic == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInitializationError, "DownloadArtifactLogic from context");
			BeEcompErrorManager.getInstance().logBeInitializationError("DownloadArtifactLogic from context");
			return null;
		}
		return downloadLogic;
	}

	protected Response buildResponse(int status, String errorMessage) {

		ServletJsonResponse jsonResponse = new ServletJsonResponse();
		jsonResponse.setDescription(errorMessage);
		jsonResponse.setSource(Constants.CATALOG_BE);

		Response response = Response.status(status).entity(jsonResponse).build();

		return response;
	}
}
