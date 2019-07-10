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

import javax.inject.Inject;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.DownloadArtifactLogic;
import org.openecomp.sdc.be.info.ServletJsonResponse;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.ws.rs.core.Response;

public abstract class ToscaDaoServlet extends BeGenericServlet {
    public abstract Logger getLogger();
    protected final IResourceUploader resourceUploader;
    protected final DownloadArtifactLogic logic;

    @Inject
    public ToscaDaoServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        IResourceUploader resourceUploader,
        DownloadArtifactLogic logic) {
        super(userBusinessLogic, componentsUtils);
        this.resourceUploader = resourceUploader;
        this.logic = logic;
    }

    protected Response buildResponse(int status, String errorMessage) {

        ServletJsonResponse jsonResponse = new ServletJsonResponse();
        jsonResponse.setDescription(errorMessage);
        jsonResponse.setSource(Constants.CATALOG_BE);

        return Response.status(status).entity(jsonResponse).build();
    }
}
