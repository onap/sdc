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

import java.util.Iterator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

public final class VfVerificator {
	private VfVerificator() {
	}

	public static void verifyNumOfComponentInstances(ResourceReqDetails createResourceInUI, int numOfVFC, User user) {
		String responseAfterDrag = RestCDUtils.getResource(createResourceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		int size = ((JSONArray) jsonResource.get("componentInstances")).size();
		assertTrue(size == numOfVFC);
	}

	public static void verifyRILocationChanged(ResourceReqDetails createResourceInUI,
			ImmutablePair<String, String> prevRIPos, User user) {

		ImmutablePair<String, String> currRIPos = ResourceUIUtils.getFirstRIPos(createResourceInUI, user);
		assertTrue(!prevRIPos.left.equals(currRIPos.left) || !prevRIPos.right.equals(currRIPos.right));
	}

	public static void verifyLinkCreated(ResourceReqDetails createResourceInUI, User user, int expectedRelationsSize) {
		String responseAfterDrag = RestCDUtils.getResource(createResourceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		assertTrue(((JSONArray) jsonResource.get("componentInstancesRelations")).size() == expectedRelationsSize);

	}

	public static void verifyVFUpdatedInUI(ResourceReqDetails vf) {
		assertTrue(vf.getName().equals(ResourceGeneralPage.getNameText()));
		assertTrue(vf.getDescription().equals(ResourceGeneralPage.getDescriptionText()));
		assertTrue(vf.getVendorName().equals(ResourceGeneralPage.getVendorNameText()));
		assertTrue(vf.getVendorRelease().equals(ResourceGeneralPage.getVendorReleaseText()));
		assertTrue(vf.getContactId().equals(ResourceGeneralPage.getUserIdContactText()));
	}

	public static void verifyVFUpdated(ResourceReqDetails vf, User user) {
		String response = RestCDUtils.getResource(vf, user).getResponse();
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response);
		assertTrue(vf.getName().equals(resource.getName()));
		assertTrue(vf.getDescription().equals(resource.getDescription()));
		assertTrue(vf.getCategories().equals(resource.getCategories()));
		assertTrue(vf.getVendorName().equals(resource.getVendorName()));
		assertTrue(vf.getVendorRelease().equals(resource.getVendorRelease()));
		assertTrue(vf.getTags().equals(resource.getTags()));
		assertTrue(vf.getContactId().equals(resource.getContactId()));
	}

	public static void verifyVFLifecycle(ResourceReqDetails vf, User user, LifecycleStateEnum expectedLifecycleState) {
		String responseAfterDrag = RestCDUtils.getResource(vf, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		String actualLifecycleState = jsonResource.get("lifecycleState").toString();
		assertTrue("actual: " + actualLifecycleState + "--expected: " + expectedLifecycleState,
				expectedLifecycleState.name().equals(actualLifecycleState));
	}
}
