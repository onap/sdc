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

package org.openecomp.sdc.ci.tests.utils.rest;

import java.io.IOException;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

public class ComponentRestUtils extends BaseRestUtils {
	public static RestResponse getComponentRequirmentsCapabilities(User sdncModifierDetails,
			ComponentReqDetails componentReqDetails) throws IOException {
		Config config = Utils.getConfig();
		ComponentTypeEnum componentType = null;
		if (componentReqDetails instanceof ResourceReqDetails) {
			componentType = ComponentTypeEnum.RESOURCE;
		} else if (componentReqDetails instanceof ServiceReqDetails) {
			componentType = ComponentTypeEnum.SERVICE;
		} else if (componentReqDetails instanceof ProductReqDetails) {
			componentType = ComponentTypeEnum.PRODUCT;
		}
		String url = String.format(Urls.GET_COMPONENT_REQUIRMENTS_CAPABILITIES, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentType),
				componentReqDetails.getUniqueId());
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static CapReqDef getAndParseComponentRequirmentsCapabilities(User user, ComponentReqDetails componentDetails)
			throws IOException {
		RestResponse getComponentReqCap = getComponentRequirmentsCapabilities(user, componentDetails);
		ResourceRestUtils.checkSuccess(getComponentReqCap);
		CapReqDef capReqDef = ResponseParser.parseToObject(getComponentReqCap.getResponse(), CapReqDef.class);
		return capReqDef;
	}
}
