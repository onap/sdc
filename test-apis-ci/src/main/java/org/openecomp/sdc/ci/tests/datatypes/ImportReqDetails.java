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

package org.openecomp.sdc.ci.tests.datatypes;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.yaml.snakeyaml.Yaml;

public class ImportReqDetails extends ResourceReqDetails {

	private String payloadName;
	private String payloadData;

	private Map<String, Object> requirements;
	private Map<String, Object> capabilities;

	private List<String> derivedList;
	private String derivedFromField;

	public ImportReqDetails(String resourceName, String description, List<String> tags, List<String> derivedFrom,
			String vendorName, String vendorRelease, String contactId, String icon) {
		super(resourceName, description, tags, null, derivedFrom, vendorName, vendorRelease, contactId, icon);
	}

	public String getPayloadName() {
		return payloadName;
	}

	public void setPayloadName(String payloadName) {
		this.payloadName = payloadName;
	}

	public String getPayloadData() {
		return payloadData;
	}

	public void setPayloadData(String payloadData) {
		this.payloadData = payloadData;
	}

	@Override
	public String toString() {
		return "ImportReqDetails [payloadName=" + payloadName + ", payloadData=" + payloadData + "]";
	}

	public void setReqirementsAndCapabilities(String path, String fileName, User user, String derivedFromSource)
			throws Exception {
		setRequirements(path, fileName, user, derivedFromSource);
		setCapabilities(path, fileName, user, derivedFromSource);
	}

	public List<String> getDerivedList() {
		return derivedList;
	}

	public void setDerivedList(List<String> derivedList) {
		this.derivedList = derivedList;
	}

	public String getDerivedFromField() {
		return derivedFromField;
	}

	public void setDerivedFromField(String derivedFromField) {
		this.derivedFromField = derivedFromField;
	}

	public Map<String, Object> getRequirements() {
		return requirements;
	}

	public void setRequirements(String path, String fileName, User user, String derivedFromSource) throws Exception {
		Map<String, Object> requirementsFromFile = getRequirementsMapFromFile(path + File.separator + fileName,
				toscaResourceName, "requirements");
		Map<String, Object> requirements = organizeRequirementsMap(requirementsFromFile);
		getDerivedReqCap(user, requirements, "requirements", derivedFromSource);
		this.requirements = requirements;
	}

	private void getDerivedReqCap(User user, Map<String, Object> reqCapMap, String field, String derivedFromResource)
			throws IOException, JSONException {

		if (derivedFromResource == null) {
			derivedFromResource = "Root";
		}

		RestResponse rest = getResourceSource(user, derivedFromResource);
		Map<String, Object> parsedFieldFromResponseAsMap = ResponseParser.getJsonValueAsMap(rest, field);
		Iterator<String> iterator = parsedFieldFromResponseAsMap.keySet().iterator();
		Map<String, Object> convertListToMap = null;
		while (iterator.hasNext()) {
			String type = iterator.next();
			List<Object> lst = (List<Object>) parsedFieldFromResponseAsMap.get(type);
			convertListToMap = convertListToMap(lst);

			if (field.equals("capabilities")) {
				convertListToMap.replace("capabilitySources", derivedList);
				lst = new ArrayList<Object>(Arrays.asList(convertListToMap));
			}

			Object existingValue = reqCapMap.get(type);
			if (existingValue != null) {
				Map<String, Object> convertedExistingValue = convertListToMap((List<Object>) existingValue);
				if (convertedExistingValue.get("name").toString().toLowerCase()
						.equals(convertListToMap.get("name").toString().toLowerCase())) {
					lst = new ArrayList<Object>(Arrays.asList(convertedExistingValue));
				} else {
					lst.add(convertedExistingValue);
				}
			}

			reqCapMap.put(type, lst);
		}
	}

	private RestResponse getResourceSource(User user, String source) throws IOException, JSONException {
		org.codehaus.jettison.json.JSONObject getResourceJSONObject = null;
		RestResponse rest = ResourceRestUtils.getResourceByNameAndVersion(user.getUserId(), source, "1.0");
		if (rest.getErrorCode().intValue() == 200) {
			JSONArray jArray = new JSONArray(rest.getResponse());
			for (int i = 0; i < jArray.length(); i++) {
				getResourceJSONObject = jArray.getJSONObject(i);
				String resourceType = getResourceJSONObject.get("resourceType").toString();
				if (!resourceType.equals("VF")) {
					rest.setResponse(getResourceJSONObject.toString());
				}
			}
		}
		return rest;
	}

	public Map<String, Object> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(String path, String fileName, User user, String derivedFromSource) throws Exception {
		Map<String, Object> capabilitiesFromFile = getCapabilitiesMapFromFile(path + File.separator + fileName,
				toscaResourceName, "capabilities");
		Map<String, Object> capabilities = organizeCapabilitiesMap(capabilitiesFromFile);
		getDerivedReqCap(user, capabilities, "capabilities", derivedFromSource);
		this.capabilities = capabilities;
	}

	private Map<String, Object> organizeCapabilitiesMap(Map<String, Object> capabilitiesFromFile) {
		Iterator<String> iterator = capabilitiesFromFile.keySet().iterator();
		Map<String, Object> capMap = new HashMap<String, Object>();
		while (iterator.hasNext()) {
			List<Object> valueList = new ArrayList<Object>();
			String next = iterator.next();
			Map<String, Object> valuesMap = (Map<String, Object>) capabilitiesFromFile.get(next);
			String key = valuesMap.remove("type").toString();
			valuesMap.put("name", next);
			valuesMap.put("capabilitySources", derivedList);
			valuesMap.put("type", key);

			if (!valuesMap.containsKey("occurrences")) {
				valuesMap.put("minOccurrences", "1");
				valuesMap.put("maxOccurrences", "UNBOUNDED");
			}

			Object tempValue = capMap.get(key);
			if (tempValue == null) {
				valueList.add(valuesMap);
			} else {
				Map<String, Object> convertValue = convertListToMap((List<Object>) tempValue);
				valueList = new ArrayList<Object>(Arrays.asList(convertValue, valuesMap));
			}
			capMap.put(key, valueList);
		}
		return capMap;
	}

	private Map<String, Object> getCapabilitiesMapFromFile(String fileName, String toscaResourceName,
			String fieldToTest) throws Exception {
		Map<String, Object> resourceToscaMap = getToscaResourceFromFile(fileName, toscaResourceName);
		Object capMap = resourceToscaMap.get(fieldToTest);
		if (capMap == null) {
			return new HashMap<String, Object>();
		}
		return (Map<String, Object>) capMap;
	}

	private Map<String, Object> organizeRequirementsMap(Map<String, Object> requirementsFromFile) {
		Map<String, Object> reqMap = new HashMap<String, Object>();
		List<Object> valueList = new ArrayList<Object>();
		Iterator<String> iterator = requirementsFromFile.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Map<String, Object> valuesMap = (Map<String, Object>) requirementsFromFile.get(key);
			valuesMap.put("name", key);
			String capability = valuesMap.get("capability").toString();

			List<Object> occurencesList = (List<Object>) valuesMap.remove("occurrences");
			if (occurencesList != null) {
				valuesMap.put("minOccurrences", occurencesList.get(0).toString());
				valuesMap.put("maxOccurrences", occurencesList.get(1).toString());
			}

			valueList.add(valuesMap);
			reqMap.put(capability, valueList);
		}

		return reqMap;
	}

	private Map<String, Object> getRequirementsMapFromFile(String fileName, String toscaResourceName,
			String fieldToTest) throws Exception {
		Map<String, Object> resourceToscaMap = getToscaResourceFromFile(fileName, toscaResourceName);
		List<Object> reqListFromFile = (List<Object>) resourceToscaMap.get(fieldToTest);
		if (reqListFromFile == null) {
			return new HashMap<String, Object>();
		}
		Map<String, Object> testedMapFromFile = convertListToMap(reqListFromFile);
		return testedMapFromFile;
	}

	private Map<String, Object> getToscaResourceFromFile(String fullFileName, String toscaResourceName)
			throws Exception {
		Map<String, Object> nodesTypesMap = getNodesTypesMapFromFile(fullFileName);
		Map<String, Object> resourceToscaMap = (Map<String, Object>) nodesTypesMap.get(toscaResourceName);

		derivedFromField = resourceToscaMap.get("derived_from").toString();

		return resourceToscaMap;
	}

	private Map<String, Object> getNodesTypesMapFromFile(String fullFileName) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		File file = new File(fullFileName);
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> mapFromFile = (Map<?, ?>) yaml.load(inputStream);
		Map<String, Object> nodesTypesMap = (Map<String, Object>) mapFromFile.get("node_types");
		return nodesTypesMap;
	}

	private Map<String, Object> convertListToMap(List<Object> testedListFromFile) {
		Map<String, Object> testedMapFromFile = new HashMap<String, Object>();
		for (int i = 0; i < testedListFromFile.size(); i++) {
			Object req = testedListFromFile.get(i);
			ObjectMapper m = new ObjectMapper();
			Map<? extends String, ? extends String> mappedObject = m.convertValue(req, Map.class);
			testedMapFromFile.putAll(mappedObject);
		}
		return testedMapFromFile;
	}

	public void compareRequirementsOrCapabilities(Map<String, Object> exepectedReq, Map<String, Object> actualReq) {
		Iterator<String> iterator = exepectedReq.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			List<Object> expectedValues = (List<Object>) exepectedReq.get(key);
			List<Object> actualValues = (List<Object>) actualReq.get(key);
			assertNotNull(actualValues);

			List<Map<String, Object>> expectedMapsList = convertListToMapList(expectedValues);
			List<Map<String, Object>> actualMapsList = convertListToMapList(actualValues);
			assertEquals(expectedMapsList.size(), actualMapsList.size());

			for (int i = 0; i < expectedMapsList.size(); i++) {
				Map<String, Object> expectedMap = expectedMapsList.get(i);
				Map<String, Object> actualdMap = actualMapsList.get(i);
				if (expectedMap.get("name").equals(actualdMap.get("name"))) {
					Iterator<String> iterator2 = expectedMap.keySet().iterator();
					while (iterator2.hasNext()) {
						String innerKey = iterator2.next();
						assertTrue(
								"check " + innerKey + " in " + key + ":\nexpected: "
										+ expectedMap.get(innerKey).toString() + "\nactual: "
										+ actualdMap.get(innerKey).toString(),
								expectedMap.get(innerKey).equals(actualdMap.get(innerKey)));

					}

				}
			}
		}
	}

	private List<Map<String, Object>> convertListToMapList(List<Object> testedListFromFile) {
		List<Map<String, Object>> listOfMaps = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < testedListFromFile.size(); i++) {
			Object req = testedListFromFile.get(i);
			ObjectMapper m = new ObjectMapper();
			Map<? extends String, ? extends String> mappedObject = m.convertValue(req, Map.class);
			mappedObject.remove("uniqueId");
			Map<String, Object> testedMapFromFile = new HashMap<String, Object>();
			testedMapFromFile.putAll(mappedObject);
			listOfMaps.add(testedMapFromFile);
		}
		return listOfMaps;
	}

}
