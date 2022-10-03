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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * Root service (exposed at "/" path)
 */

//upload Service model by Shiyong1989@hotmail.com
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/uploadservice")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Singleton
public class ServiceUploadServlet extends AbstractValidationsServlet {

    public static final String NORMATIVE_TYPE_SERVICE = "multipart";
    public static final String CSAR_TYPE_SERVICE = "csar";
    public static final String USER_TYPE_SERVICE = "user-service";
    public static final String USER_TYPE_SERVICE_UI_IMPORT = "user-servcie-ui-import";
    private static final Logger log = Logger.getLogger(ServiceUploadServlet.class);

    public ServiceUploadServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
    }

    public enum ServiceAuthorityTypeEnum {
        NORMATIVE_TYPE_BE(NORMATIVE_TYPE_SERVICE, true, false), USER_TYPE_BE(USER_TYPE_SERVICE, true, true), USER_TYPE_UI(USER_TYPE_SERVICE_UI_IMPORT,
            false, true), CSAR_TYPE_BE(CSAR_TYPE_SERVICE, true, true);
        private String urlPath;
        private boolean isBackEndImport, isUserTypeService;

        private ServiceAuthorityTypeEnum(String urlPath, boolean isBackEndImport, boolean isUserTypeService) {
            this.urlPath = urlPath;
            this.isBackEndImport = isBackEndImport;
            this.isUserTypeService = isUserTypeService;
        }

        public static ServiceAuthorityTypeEnum findByUrlPath(String urlPath) {
            ServiceAuthorityTypeEnum found = null;
            for (ServiceAuthorityTypeEnum curr : ServiceAuthorityTypeEnum.values()) {
                if (curr.getUrlPath().equals(urlPath)) {
                    found = curr;
                    break;
                }
            }
            return found;
        }

        public String getUrlPath() {
            return urlPath;
        }

        public boolean isBackEndImport() {
            return isBackEndImport;
        }

        public boolean isUserTypeService() {
            return isUserTypeService;
        }
    }
}
