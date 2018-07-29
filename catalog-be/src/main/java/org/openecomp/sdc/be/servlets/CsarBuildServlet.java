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

import com.jcabi.aspects.Loggable;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/services")
public class CsarBuildServlet extends ToscaDaoServlet {

    private static final Logger log = Logger.getLogger(CsarBuildServlet.class);

    @GET
    @Path("/{serviceName}/{serviceVersion}")
    public Response getDefaultTemplate(@Context final HttpServletRequest request, @PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion) {

        return null;// buildToscaCsar(request, serviceName, serviceVersion);

    }

    @GET
    @Path("/{serviceName}/{serviceVersion}/csar")
    public Response getToscaCsarTemplate(@Context final HttpServletRequest request, @PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion) {

        return null; // buildToscaCsar(request, serviceName, serviceVersion);

    }


    public static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";

    protected String[] prepareToscaMetaHeader(String serviceName) {
        return new String[] { "TOSCA-Meta-File-Version: 1.0\n", "CSAR-Version: 1.1\n", "Created-By: INTERWISE\n", "\n", "Entry-Definitions: Definitions/" + serviceName + ".yaml\n", "\n", "Name: Definitions/" + serviceName + ".yaml\n",
                "Content-Type: application/vnd.oasis.tosca.definitions.yaml\n" };
    }

    protected String getAppliactionMime(String fileName) {
        String mimeType;
        if (fileName.contains(".sh")) {
            mimeType = "x-sh";
        } else if (fileName.contains(".yang")) {
            mimeType = "yang";
        }

        else if (fileName.contains(".rar")) {
            mimeType = "x-rar-compressed";
        }

        else if (fileName.contains(".zip")) {
            mimeType = "zip";
        }

        else if (fileName.contains(".tar")) {
            mimeType = "x-tar";
        }

        else if (fileName.contains(".7z")) {
            mimeType = "x-7z-compressed";
        }

        else {
            // Undefined
            mimeType = "undefined";
        }
        return mimeType;
    }

    protected String getArtifactPath(String nodeTamplateName, ESArtifactData artifactData) {
        return "Scripts/" + nodeTamplateName + "/" + artifactData.getId();
    }

    protected String getResourcePath(String resourceName) {
        return "Definitions/" + resourceName + ".yaml";
    }

    @Override
    public Logger getLogger() {
        return log;
    }

}
