/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.utils.rest;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceOperationsRestUtils extends BaseRestUtils {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(InterfaceOperationsRestUtils.class.getName());

    public static RestResponse addInterfaceOperations(Component component, Map<String, Object> interfaceDefinitionMap,
            User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.ADD_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(),
                ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId());
        String jsonBody = new Gson().toJson(interfaceDefinitionMap);
        return sendPost(url, jsonBody, user.getUserId(), acceptHeaderData);
    }

    public static RestResponse updateInterfaceOperations(Component component,
            Map<String, Object> interfaceDefinitionMap, User user) throws IOException {
        Config config = Utils.getConfig();
        String url =
                String.format(Urls.UPDATE_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(),
                        ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId());
        String jsonBody = new Gson().toJson(interfaceDefinitionMap);
        return sendPut(url, jsonBody, user.getUserId(), acceptHeaderData);
    }

    public static RestResponse getInterfaceOperations(Component component, String interfaceId, String operationIds,
            User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(),
                ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId(), interfaceId,
                operationIds);
        return sendGet(url, user.getUserId());
    }

    public static RestResponse deleteInterfaceOperations(Component component, String interfaceId, String operationIds,
            User user) throws IOException {
        Config config = Utils.getConfig();
        String url =
                String.format(Urls.DELETE_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(),
                        ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId(),
                        interfaceId, operationIds);
        return sendDelete(url, user.getUserId());
    }

}
