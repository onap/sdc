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

package org.openecomp.sdc.ci.tests.verificator;

import static org.testng.AssertJUnit.assertTrue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;

public class ServiceVerificator {

	private ServiceVerificator() {
	}

	public static void verifyNumOfComponentInstances(ComponentReqDetails component, String version, int numOfVFC,
			User user) {
		String responseAfterDrag = null;
		component.setVersion(version);
		if (component instanceof ServiceReqDetails) {
			responseAfterDrag = RestCDUtils.getService((ServiceReqDetails) component, user).getResponse();
		} else if (component instanceof ResourceReqDetails) {
			responseAfterDrag = RestCDUtils.getResource((ResourceReqDetails) component, user).getResponse();
		}
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		int size = ((JSONArray) jsonResource.get("componentInstances")).size();
		assertTrue(size == numOfVFC);
	}
	
	public static void verifyLinkCreated(ServiceReqDetails createServiceInUI, User user, int expectedRelationsSize) {
		String responseAfterDrag = RestCDUtils.getService(createServiceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		assertTrue(((JSONArray) jsonResource.get("componentInstancesRelations")).size() == expectedRelationsSize);

	}

}
