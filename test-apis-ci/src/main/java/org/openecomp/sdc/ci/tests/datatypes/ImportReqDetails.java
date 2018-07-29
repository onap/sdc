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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static org.testng.AssertJUnit.*;

public class ImportReqDetails extends ResourceReqDetails {

	private static final String CAPS = "capabilities";
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
	
	public ImportReqDetails(Resource resource, String payloadName, String payloadData){
		super(resource);
		this.payloadData = payloadData;
		this.payloadName = payloadName;
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

	public void setReqirementsAndCapabilities(String path, String fileName, User user, String derivedFromSource) throws IOException, JSONException{
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

	public void setRequirements(String path, String fileName, User user, String derivedFromSource) throws IOException, JSONException {
		Map<String, Object> requirementsFromFile = getRequirementsMapFromFile(path + File.separator + fileName,
				toscaResourceName, "requirements");
		Map<String, Object> reqs = organizeRequirementsMap(requirementsFromFile);
		getDerivedReqCap(user, reqs, "requirements", derivedFromSource);
		this.requirements = reqs;
	}

	@SuppressWarnings("unchecked")
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

			if (field.equals(CAPS)) {
				convertListToMap.replace("capabilitySources", derivedList);
				lst = new ArrayList<>(Arrays.asList(convertListToMap));
			}

			Object existingValue = reqCapMap.get(type);
			if (existingValue != null) {
				Map<String, Object> convertedExistingValue = convertListToMap((List<Object>) existingValue);
				if (convertedExistingValue.get("name").toString().equalsIgnoreCase(convertListToMap.get("name").toString())) {
					lst = new ArrayList<>(Arrays.asList(convertedExistingValue));
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

	public void setCapabilities(String path, String fileName, User user, String derivedFromSource) throws IOException, JSONException {
		Map<String, Object> capabilitiesFromFile = getCapabilitiesMapFromFile(path + File.separator + fileName,
				toscaResourceName, CAPS);
		Map<String, Object> caps = organizeCapabilitiesMap(capabilitiesFromFile);
		getDerivedReqCap(user, caps, CAPS, derivedFromSource);
		this.capabilities = caps;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> organizeCapabilitiesMap(Map<String, Object> capabilitiesFromFile) {
		Iterator<String> iterator = capabilitiesFromFile.keySet().iterator();
		Map<String, Object> capMap = new HashMap<>();
		while (iterator.hasNext()) {
			List<Object> valueList = new ArrayList<>();
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
				valueList = new ArrayList<>(Arrays.asList(convertValue, valuesMap));
			}
			capMap.put(key, valueList);
		}
		return capMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getCapabilitiesMapFromFile(String fileName, String toscaResourceName,
			String fieldToTest) throws FileNotFoundException {
		Map<String, Object> resourceToscaMap = getToscaResourceFromFile(fileName, toscaResourceName);
		Object capMap = resourceToscaMap.get(fieldToTest);
		if (capMap == null) {
			return new HashMap<>();
		}
		return (Map<String, Object>) capMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> organizeRequirementsMap(Map<String, Object> requirementsFromFile) {
		Map<String, Object> reqMap = new HashMap<>();
		List<Object> valueList = new ArrayList<>();
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> getRequirementsMapFromFile(String fileName, String toscaResourceName,
			String fieldToTest) throws FileNotFoundException {
		Map<String, Object> resourceToscaMap = getToscaResourceFromFile(fileName, toscaResourceName);
		List<Object> reqListFromFile = (List<Object>) resourceToscaMap.get(fieldToTest);
		if (reqListFromFile == null) {
			return new HashMap<>();
		}
		return convertListToMap(reqListFromFile);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getToscaResourceFromFile(String fullFileName, String toscaResourceName) throws FileNotFoundException{
		Map<String, Object> nodesTypesMap = getNodesTypesMapFromFile(fullFileName);
		Map<String, Object> resourceToscaMap = (Map<String, Object>) nodesTypesMap.get(toscaResourceName);

		derivedFromField = resourceToscaMap.get("derived_from").toString();

		return resourceToscaMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getNodesTypesMapFromFile(String fullFileName) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		File file = new File(fullFileName);
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> mapFromFile = (Map<?, ?>) yaml.load(inputStream);
		return (Map<String, Object>) mapFromFile.get("node_types");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertListToMap(List<Object> testedListFromFile) {
		Map<String, Object> testedMapFromFile = new HashMap<>();
		for (int i = 0; i < testedListFromFile.size(); i++) {
			Object req = testedListFromFile.get(i);
			ObjectMapper m = new ObjectMapper();
			Map<? extends String, ? extends String> mappedObject = m.convertValue(req, Map.class);
			testedMapFromFile.putAll(mappedObject);
		}
		return testedMapFromFile;
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> convertListToMapList(List<Object> testedListFromFile) {
		List<Map<String, Object>> listOfMaps = new ArrayList<>();
		for (int i = 0; i < testedListFromFile.size(); i++) {
			Object req = testedListFromFile.get(i);
			ObjectMapper m = new ObjectMapper();
			Map<? extends String, ? extends String> mappedObject = m.convertValue(req, Map.class);
			mappedObject.remove("uniqueId");
			Map<String, Object> testedMapFromFile = new HashMap<>();
			testedMapFromFile.putAll(mappedObject);
			listOfMaps.add(testedMapFromFile);
		}
		return listOfMaps;
	}

}
