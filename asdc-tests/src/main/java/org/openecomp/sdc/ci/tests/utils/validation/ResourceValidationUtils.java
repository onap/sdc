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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.RespJsonKeysEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

import com.google.gson.Gson;

public class ResourceValidationUtils {

	public static void validateResourceReqVsResp(ResourceReqDetails resourceDetails,
			ResourceRespJavaObject resourceRespJavaObject) {

		String expected;

		expected = resourceDetails.getDescription();
		assertEquals("resource description - ", expected, resourceRespJavaObject.getDescription());

		expected = resourceDetails.getVendorName();
		assertEquals("resource vendorName - ", expected, resourceRespJavaObject.getVendorName());

		expected = resourceDetails.getVendorRelease();
		assertEquals("resource vendorReleaseName - ", expected, resourceRespJavaObject.getVendorRelease());

		expected = resourceDetails.getContactId();
		assertEquals("resource contactId - ", expected, resourceRespJavaObject.getContactId());

	}

	public static void validateResourceReqVsResp(ResourceReqDetails resourceDetails, Resource resourceRespJavaObject) {

		String expected;

		expected = resourceDetails.getDescription();
		assertEquals("resource description - ", expected, resourceRespJavaObject.getDescription());

		expected = resourceDetails.getVendorName();
		assertEquals("resource vendorName - ", expected, resourceRespJavaObject.getVendorName());

		expected = resourceDetails.getVendorRelease();
		assertEquals("resource vendorReleaseName - ", expected, resourceRespJavaObject.getVendorRelease());

		expected = resourceDetails.getContactId();
		assertEquals("resource contactId - ", expected, resourceRespJavaObject.getContactId());

		// Validating deduplication of tags
		List<String> expectedTags = resourceDetails.getTags();
		if (expectedTags != null) {
			Set<String> hs = new LinkedHashSet<>(expectedTags);
			expectedTags.clear();
			expectedTags.addAll(hs);
			List<String> receivedTags = resourceRespJavaObject.getTags();
			assertEquals("resource tags - ", expectedTags, receivedTags);
		}

	}

	public static void validateModelObjects(Resource expected, Resource actual) throws Exception {

		compareElements(expected.getUniqueId(), actual.getUniqueId());
		compareElements(expected.getName(), actual.getName());
		compareElements(expected.getVersion(), actual.getVersion());
		compareElements(expected.getCreatorUserId(), actual.getCreatorUserId());
		compareElements(expected.getCreatorFullName(), actual.getCreatorFullName());
		compareElements(expected.getLastUpdaterUserId(), actual.getLastUpdaterUserId());
		compareElements(expected.getLastUpdaterFullName(), actual.getLastUpdaterFullName());
		compareElements(expected.getCreatorFullName(), actual.getCreatorFullName());
		compareElements(expected.getCreationDate(), actual.getCreationDate());
		compareElements(expected.getLastUpdateDate(), actual.getLastUpdateDate());
		compareElements(expected.getDescription(), actual.getDescription());
		compareElements(expected.getIcon(), actual.getIcon());
		compareElements(expected.getLastUpdateDate(), actual.getLastUpdateDate());
		// TODO compare tags
		compareElements(expected.getCategories(), actual.getCategories());
		compareElements(expected.getLifecycleState(), actual.getLifecycleState());
		compareElements(expected.getVendorName(), actual.getVendorName());
		compareElements(expected.getVendorRelease(), actual.getVendorRelease());
		compareElements(expected.getContactId(), actual.getContactId());
		compareElements(expected.getUUID(), actual.getUUID());
		compareElements(expected.getVersion(), actual.getVersion());

	}

	public static void validateResp(RestResponse restResponse, ResourceRespJavaObject resourceRespJavaObject)
			throws Exception {

		Gson gson = new Gson();
		String response = restResponse.getResponse();

		validateResp(response, resourceRespJavaObject, gson);

	}

	public static void validateResp(String response, ResourceRespJavaObject resourceRespJavaObject, Gson gson) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map<String, Object>) gson.fromJson(response, map.getClass());

		// De-duplicating the tags list for comparison
		List<String> tags = resourceRespJavaObject.getTags();
		if (tags != null) {
			Set<String> hs = new LinkedHashSet<>(tags);
			tags = new ArrayList<String>(hs);
			resourceRespJavaObject.setTags(tags);
			tags = new ArrayList<String>(hs);
			resourceRespJavaObject.setTags(tags);
		}

		validateField(map, RespJsonKeysEnum.RESOURCE_NAME.getRespJsonKeyName(), resourceRespJavaObject.getName());
		validateField(map, RespJsonKeysEnum.RESOURCE_DESC.getRespJsonKeyName(),
				resourceRespJavaObject.getDescription());
		// validateField(map, RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName(),
		// resourceRespJavaObject.getCategories());
		validateField(map, RespJsonKeysEnum.VENDOR_NAME.getRespJsonKeyName(), resourceRespJavaObject.getVendorName());
		validateField(map, RespJsonKeysEnum.VENDOR_RELEASE.getRespJsonKeyName(),
				resourceRespJavaObject.getVendorRelease());
		validateField(map, RespJsonKeysEnum.CONTACT_ID.getRespJsonKeyName(), resourceRespJavaObject.getContactId());
		validateField(map, RespJsonKeysEnum.ICON.getRespJsonKeyName(), resourceRespJavaObject.getIcon());
		validateField(map, RespJsonKeysEnum.IS_ABSTRACT.getRespJsonKeyName(),
				Boolean.valueOf(resourceRespJavaObject.getAbstractt()));
		validateField(map, RespJsonKeysEnum.HIGHEST_VERSION.getRespJsonKeyName(),
				Boolean.valueOf(resourceRespJavaObject.getIsHighestVersion()));
		validateField(map, RespJsonKeysEnum.UNIQUE_ID.getRespJsonKeyName(), resourceRespJavaObject.getUniqueId());
		validateField(map, RespJsonKeysEnum.RESOURCE_VERSION.getRespJsonKeyName(), resourceRespJavaObject.getVersion());
		validateField(map, RespJsonKeysEnum.LIFE_CYCLE_STATE.getRespJsonKeyName(),
				resourceRespJavaObject.getLifecycleState());
		validateField(map, RespJsonKeysEnum.TAGS.getRespJsonKeyName(), tags);
		validateField(map, RespJsonKeysEnum.CREATOR_USER_ID.getRespJsonKeyName(),
				resourceRespJavaObject.getCreatorUserId());
		validateField(map, RespJsonKeysEnum.CREATOR_FULL_NAME.getRespJsonKeyName(),
				resourceRespJavaObject.getCreatorFullName());
		validateField(map, RespJsonKeysEnum.LAST_UPDATER_ATT_UID.getRespJsonKeyName(),
				resourceRespJavaObject.getLastUpdaterUserId());
		validateField(map, RespJsonKeysEnum.LAST_UPDATER_FULL_NAME.getRespJsonKeyName(),
				resourceRespJavaObject.getLastUpdaterFullName());
		validateField(map, RespJsonKeysEnum.COST.getRespJsonKeyName(), resourceRespJavaObject.getCost());
		validateField(map, RespJsonKeysEnum.LICENSE_TYPE.getRespJsonKeyName(), resourceRespJavaObject.getLicenseType());
		validateField(map, RespJsonKeysEnum.RESOURCE_TYPE.getRespJsonKeyName(),
				resourceRespJavaObject.getResourceType().toString());
		if (resourceRespJavaObject.getResourceType().equals("VF")) {
			validateField(map, RespJsonKeysEnum.DERIVED_FROM.getRespJsonKeyName(), null);
		} else {
			validateField(map, RespJsonKeysEnum.DERIVED_FROM.getRespJsonKeyName(),
					resourceRespJavaObject.getDerivedFrom());
		}

		validateCategories(resourceRespJavaObject, map);

		String uuid = ResponseParser.getValueFromJsonResponse(response, RespJsonKeysEnum.UUID.getRespJsonKeyName());
		assertTrue("UUID is empty", uuid != null && !uuid.isEmpty());
	}

	private static void validateCategories(ResourceRespJavaObject resourceRespJavaObject, Map<String, Object> map) {
		assertTrue(RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName() + " is missing",
				map.containsKey(RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName()));
		Object foundValue = map.get(RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName());
		List<Map<String, Object>> foundList = (List<Map<String, Object>>) foundValue;
		List<CategoryDefinition> excpectedList = resourceRespJavaObject.getCategories();

		assertTrue(foundList.size() == excpectedList.size());
		for (int i = 0; i < foundList.size(); i++) {
			CategoryDefinition expCat = excpectedList.get(i);
			Map<String, Object> foun = foundList.get(i);
			assertTrue("expected " + expCat.getName() + " not equal to actual " + foundValue,
					foun.get("name").equals(expCat.getName()));
		}
	}

	public static void validateField(Map<String, Object> map, String jsonField, Object expectedValue) {
		if (expectedValue == null) {
			assertTrue(jsonField + " is expected to be null", !map.containsKey(jsonField));
		} else {
			assertTrue(jsonField + " is missing", map.containsKey(jsonField));
			Object foundValue = map.get(jsonField);
			compareElements(expectedValue, foundValue);
		}
	}

	public static void compareElements(Object expectedValue, Object foundValue) {
		if (expectedValue instanceof String) {
			assertTrue(foundValue instanceof String);
			assertTrue("expected " + expectedValue + " not equal to actual " + foundValue,
					foundValue.equals(expectedValue));
		}
		else if (expectedValue instanceof Boolean) {
			assertTrue(foundValue instanceof Boolean);
			assertTrue(foundValue == expectedValue);
		} else if (expectedValue instanceof Map) {
			assertTrue(foundValue instanceof Map);
			Map<String, Object> foundMap = (Map<String, Object>) foundValue;
			Map<String, Object> excpectedMap = (Map<String, Object>) expectedValue;
			assertTrue(foundMap.size() == excpectedMap.size());
			Iterator<String> foundkeyItr = foundMap.keySet().iterator();
			while (foundkeyItr.hasNext()) {
				String foundKey = foundkeyItr.next();
				assertTrue(excpectedMap.containsKey(foundKey));
				compareElements(excpectedMap.get(foundKey), foundMap.get(foundKey));
			}

		} else if (expectedValue instanceof List) {
			assertTrue(foundValue instanceof List);
			List<Object> foundList = (List<Object>) foundValue;
			List<Object> excpectedList = (List<Object>) expectedValue;
			assertTrue(foundList.size() == excpectedList.size());
			for (int i = 0; i < foundList.size(); i++) {
				compareElements(excpectedList.get(i), foundList.get(i));
			}

		} else {
			assertTrue(foundValue.equals(expectedValue));
		}
	}

	public static boolean validateUuidAfterChangingStatus(String oldUuid, String newUuid) {
		return oldUuid.equals(newUuid);

	}

	public static void validateRespArt(RestResponse restResponse, ResourceRespJavaObject resourceRespJavaObject,
			String interfaze) throws Exception {

		Gson gson = new Gson();
		String response = restResponse.getResponse();

		Map<String, Object> map = new HashMap<String, Object>();
		map = (Map<String, Object>) gson.fromJson(response, map.getClass());

		Resource resource = gson.fromJson(response, Resource.class);

		Map<String, ArtifactDefinition> artifacts = resource.getArtifacts();
		Map<String, InterfaceDefinition> interfaces = null;

		if (interfaze != null) {
			interfaces = resource.getInterfaces();
			Map<String, Operation> operation = interfaces.get(interfaze).getOperations();
			// operation.get("configure").getUniqueId();
		}

		validateField(map, RespJsonKeysEnum.RESOURCE_NAME.getRespJsonKeyName(), resourceRespJavaObject.getName());
		validateField(map, RespJsonKeysEnum.RESOURCE_DESC.getRespJsonKeyName(),
				resourceRespJavaObject.getDescription());
		// validateField(map, RespJsonKeysEnum.CATEGORIES.getRespJsonKeyName(),
		// resourceRespJavaObject.getCategories());
		validateField(map, RespJsonKeysEnum.DERIVED_FROM.getRespJsonKeyName(), resourceRespJavaObject.getDerivedFrom());
		validateField(map, RespJsonKeysEnum.VENDOR_NAME.getRespJsonKeyName(), resourceRespJavaObject.getVendorName());
		validateField(map, RespJsonKeysEnum.VENDOR_RELEASE.getRespJsonKeyName(),
				resourceRespJavaObject.getVendorRelease());
		validateField(map, RespJsonKeysEnum.CONTACT_ID.getRespJsonKeyName(), resourceRespJavaObject.getContactId());
		validateField(map, RespJsonKeysEnum.ICON.getRespJsonKeyName(), resourceRespJavaObject.getIcon());
		validateField(map, RespJsonKeysEnum.IS_ABSTRACT.getRespJsonKeyName(),
				Boolean.valueOf(resourceRespJavaObject.getAbstractt()));
		validateField(map, RespJsonKeysEnum.HIGHEST_VERSION.getRespJsonKeyName(),
				Boolean.valueOf(resourceRespJavaObject.getIsHighestVersion()));
		validateField(map, RespJsonKeysEnum.UNIQUE_ID.getRespJsonKeyName(), resourceRespJavaObject.getUniqueId());
		validateField(map, RespJsonKeysEnum.RESOURCE_VERSION.getRespJsonKeyName(), resourceRespJavaObject.getVersion());
		validateField(map, RespJsonKeysEnum.LIFE_CYCLE_STATE.getRespJsonKeyName(),
				resourceRespJavaObject.getLifecycleState());
		validateField(map, RespJsonKeysEnum.TAGS.getRespJsonKeyName(), resourceRespJavaObject.getTags());
		validateField(map, RespJsonKeysEnum.CREATOR_USER_ID.getRespJsonKeyName(),
				resourceRespJavaObject.getCreatorUserId());
		validateField(map, RespJsonKeysEnum.CREATOR_FULL_NAME.getRespJsonKeyName(),
				resourceRespJavaObject.getCreatorFullName());
		validateField(map, RespJsonKeysEnum.LAST_UPDATER_ATT_UID.getRespJsonKeyName(),
				resourceRespJavaObject.getLastUpdaterUserId());
		validateField(map, RespJsonKeysEnum.LAST_UPDATER_FULL_NAME.getRespJsonKeyName(),
				resourceRespJavaObject.getLastUpdaterFullName());

		// validate number of artifacts
		if (resourceRespJavaObject.getArtifacts() != null) {

			// assertEquals("check number of artifacts",
			// resourceRespJavaObject.getArtifacts().size(), artifacts.size());
			int iterNum = -1;
			ArrayList<String> myArtifacats = new ArrayList<String>();
			Iterator it = artifacts.entrySet().iterator();
			while (it.hasNext()) {
				iterNum++;
				Map.Entry pair = (Map.Entry) it.next();
				// System.out.println(pair.getKey() + " = " + pair.getValue());
				ArtifactDefinition myArtifact = artifacts.get(pair.getKey());
				myArtifacats.add(myArtifact.getEsId());
				it.remove(); // avoids a ConcurrentModificationException
			}
			// assertTrue("check service contains
			// artifacts",myArtifacats.containsAll(resourceRespJavaObject.getArtifacts()));
		}

		// validate number of interfaces:

		if (interfaze != null) {
			assertEquals("check number of interfaces", resourceRespJavaObject.getInterfaces().size(),
					interfaces.size());
		}

	}

	public static boolean validateResourceIsAbstartct(List<Resource> resourceList, Boolean bool) {
		if (resourceList != null && resourceList.size() > 0) {
			for (Resource resource : resourceList) {
				if (resource.isAbstract().equals(bool))
					continue;
				else
					return false;
			}
		} else
			return false;
		return true;
	}

	public static void validateResourceVersion(Resource resource, String expectedVersion) {
		if (resource != null && !resource.equals("")) {
			assertTrue("expected resource version is: " + expectedVersion + ", but actual is: " + resource.getVersion(),
					resource.getVersion().equals(expectedVersion));
		}
	}
}
