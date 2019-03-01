/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.ci.tests.utils.rest;

import com.google.gson.Gson;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.RequirementDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;

import java.util.List;

public class RequirementsUtils extends BaseRestUtils {
    private static Gson gson = new Gson();
    private static final String COMPONENT_TYPE = "services";

    public static RestResponse createRequirement(String componentId,
                                                 List<RequirementDetails> requirementDefinitionList,
                                                 User user) throws Exception{
        Config config = Config.instance();
        String url = String.format(Urls.CREATE_REQUIREMENT, config.getCatalogBeHost(), config.getCatalogBePort(),
                COMPONENT_TYPE, componentId);

        String data = "{ \"requirements\" : {" + "\"" +requirementDefinitionList.get(0).getCapability()+ "\"" +" : "
                + gson.toJson(requirementDefinitionList) + "  } }";

        return sendPost(url, data , user.getUserId(), acceptHeaderData);
    }

    public static RestResponse updateRequirement(String componentId,
                                                 List<RequirementDetails> requirementDefinitionList,
                                                 User user) throws Exception{
        Config config = Config.instance();
        String url = String.format(Urls.UPDATE_REQUIREMENT, config.getCatalogBeHost(), config.getCatalogBePort(),
                COMPONENT_TYPE, componentId);

        String data = "{ \"requirements\" : {" + "\"" +requirementDefinitionList.get(0).getCapability()+ "\"" +" : "
                + gson.toJson(requirementDefinitionList) + "  } }";

        return sendPost(url, data , user.getUserId(), acceptHeaderData);
    }

    public static RestResponse deleteRequirement(String componentId,
                                                 String requirementId,
                                                 User user) throws Exception{
        Config config = Config.instance();
        String url = String.format(Urls.DELETE_REQUIREMENT, config.getCatalogBeHost(), config.getCatalogBePort(),
                COMPONENT_TYPE, componentId, requirementId);
        return sendDelete(url, user.getUserId());
    }

    public static RestResponse getRequirement(String componentId,
                                                 String requirementId,
                                                 User user) throws Exception{
        Config config = Config.instance();
        String url = String.format(Urls.GET_REQUIREMENT, config.getCatalogBeHost(), config.getCatalogBePort(),
                COMPONENT_TYPE, componentId, requirementId);
        return sendDelete(url, user.getUserId());
    }
}
