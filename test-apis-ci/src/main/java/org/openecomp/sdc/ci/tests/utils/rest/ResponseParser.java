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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintJacksonDeserialiser;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDistributionStatus;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseParser {

	// comment by Andrey, for test only
	// public static void main(String[] args) {
	// String response =
	// "{\"uniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2\",\"resourceName\":\"importResource4test\",\"resourceVersion\":\"0.1\",\"creatorUserId\":\"jh0003\",\"creatorFullName\":\"Jimmy
	// Hendrix\",\"lastUpdaterUserId\":\"jh0003\",\"lastUpdaterFullName\":\"Jimmy
	// Hendrix\",\"creationDate\":1446742241514,\"lastUpdateDate\":1446742241514,\"description\":\"Represents
	// a generic software component that can be managed and run by a Compute
	// Node
	// Type.\",\"icon\":\"defaulticon\",\"tags\":[\"importResource4test\"],\"category\":\"Generic/Infrastructure\",\"lifecycleState\":\"NOT_CERTIFIED_CHECKOUT\",\"derivedFrom\":[\"tosca.nodes.Root\"],\"artifacts\":{},\"deploymentArtifacts\":{},\"properties\":[{\"uniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2.port\",\"type\":\"integer\",\"required\":false,\"description\":\"the
	// port the DBMS service will listen to for data and
	// requests\",\"password\":false,\"name\":\"port\",\"parentUniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2\",\"definition\":true},{\"uniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2.root_password\",\"type\":\"string\",\"required\":false,\"description\":\"the
	// optional root password for the DBMS
	// service\",\"password\":false,\"name\":\"root_password\",\"parentUniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2\",\"definition\":true}],\"interfaces\":{\"standard\":{\"type\":\"tosca.interfaces.node.lifecycle.Standard\",\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard\",\"operations\":{\"stop\":{\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard.stop\",\"description\":\"Standard
	// lifecycle stop
	// operation.\",\"definition\":false},\"start\":{\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard.start\",\"description\":\"Standard
	// lifecycle start
	// operation.\",\"definition\":false},\"delete\":{\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard.delete\",\"description\":\"Standard
	// lifecycle delete
	// operation.\",\"definition\":false},\"create\":{\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard.create\",\"description\":\"Standard
	// lifecycle create
	// operation.\",\"definition\":false},\"configure\":{\"uniqueId\":\"tosca.interfaces.node.lifecycle.standard.configure\",\"description\":\"Standard
	// lifecycle configure
	// operation.\",\"definition\":false}},\"definition\":false}},\"capabilities\":{\"feature\":{\"uniqueId\":\"capability.8313348e-3623-4f4a-9b8f-d2fbadaf9a31.feature\",\"type\":\"tosca.capabilities.Node\"},\"feature2\":{\"uniqueId\":\"capability.52eb0139-a855-47b9-a0e6-c90f0a90b1d2.feature2\",\"type\":\"tosca.capabilities.Node\"}},\"requirements\":{\"dependency\":{\"uniqueId\":\"8313348e-3623-4f4a-9b8f-d2fbadaf9a31.dependency\",\"capability\":\"tosca.capabilities.Node\",\"node\":\"tosca.nodes.Root\",\"relationship\":\"tosca.relationships.DependsOn\"},\"dependency2\":{\"uniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2.dependency2\",\"capability\":\"tosca.capabilities.Node\",\"node\":\"tosca.nodes.importResource4test\",\"relationship\":\"tosca.relationships.DependsOn\"}},\"vendorName\":\"ATT
	// (Tosca)\",\"vendorRelease\":\"1.0.0.wd03\",\"contactId\":\"jh0003\",\"systemName\":\"Importresource4test\",\"additionalInformation\":[{\"uniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2.additionalinformation\",\"lastCreatedCounter\":0,\"parentUniqueId\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2\",\"parameters\":[]}],\"allVersions\":{\"0.1\":\"52eb0139-a855-47b9-a0e6-c90f0a90b1d2\"},\"abstract\":false,\"highestVersion\":true,\"uuid\":\"2e91a2df-b066-49bb-abde-4c1c01e409db\"}";
	// convertResourceResponseToJavaObject(response);
	// }

	private static final String INVARIANT_UUID = "invariantUUID";
	public static final String UNIQUE_ID = "uniqueId";
	public static final String VERSION = "version";
	public static final String UUID = "uuid";
	public static final String NAME = "name";
	public static final String ORIGIN_TYPE = "originType";
	public static final String TOSCA_RESOURCE_NAME = "toscaResourceName";

	static Logger logger = Logger.getLogger(ResponseParser.class.getName());

	public static String getValueFromJsonResponse(String response, String fieldName) {
		try {
			JSONObject jsonResp = (JSONObject) JSONValue.parse(response);
			Object fieldValue = jsonResp.get(fieldName);
			return fieldValue.toString();

		} catch (Exception e) {
			return null;
		}

	}

	public static String getUniqueIdFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), UNIQUE_ID);
	}

	public static String getInvariantUuid(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), INVARIANT_UUID);
	}

	public static String getUuidFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), UUID);
	}

	public static String getNameFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), NAME);
	}

	public static String getVersionFromResponse(RestResponse response) {
		return ResponseParser.getValueFromJsonResponse(response.getResponse(), VERSION);
	}

	public static String getComponentTypeFromResponse(RestResponse response) {
		return ResponseParser.getValueFromJsonResponse(response.getResponse(), ORIGIN_TYPE);
	}

	public static String getToscaResourceNameFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), TOSCA_RESOURCE_NAME);
	}

	@SuppressWarnings("unchecked")
	public static ResourceRespJavaObject parseJsonListReturnResourceDetailsObj(RestResponse restResponse,
			String resourceType, String searchPattern, String expectedResult) throws Exception {

		// Gson gson = new Gson;

		JsonElement jElement = new JsonParser().parse(restResponse.getResponse());
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray arrayOfObjects = (JsonArray) jObject.get(resourceType);
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();
		ResourceRespJavaObject jsonToJavaObject = new ResourceRespJavaObject();

		for (int counter = 0; counter < arrayOfObjects.size(); counter++) {
			JsonObject jHitObject = (JsonObject) arrayOfObjects.get(counter);

			map = (Map<String, Object>) gson.fromJson(jHitObject.toString(), map.getClass());
			if (map.get(searchPattern).toString().contains(expectedResult)) {

				jsonToJavaObject = gson.fromJson(jObject, ResourceRespJavaObject.class);
				break;
			}
		}
		return jsonToJavaObject;

	}

	public static Resource convertResourceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> deserializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, deserializer);

		mapper.registerModule(module);
		Resource resource = null;
		try {
//			TODO Andrey L. uncomment line below in case to ignore on unknown properties, not recommended  
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			resource = mapper.readValue(response, Resource.class);
			
			logger.debug(resource.toString());
		} catch (IOException e) {
			try {
				List<Resource> resources = Arrays.asList(mapper.readValue(response.toString(), Resource[].class));
				resource = resources.get(0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return resource;
	}

	public static ComponentInstanceProperty convertPropertyResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		ComponentInstanceProperty propertyDefinition = null;
		try {
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			propertyDefinition = mapper.readValue(response, ComponentInstanceProperty.class);
			logger.debug(propertyDefinition.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return propertyDefinition;
	}

	public static String toJson(Object object) {
		Gson gson = new Gson();
		return gson.toJson(object);
	}

	public static ArtifactDefinition convertArtifactDefinitionResponseToJavaObject(String response) {
		ObjectMapper mapper = new ObjectMapper();
		ArtifactDefinition artifactDefinition = null;
		try {
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			artifactDefinition = mapper.readValue(response, ArtifactDefinition.class);
			logger.debug(artifactDefinition.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return artifactDefinition;

	}

	public static ArtifactReqDetails convertArtifactReqDetailsToJavaObject(String response) {

		ArtifactReqDetails artifactReqDetails = null;
		Gson gson = new Gson();
		artifactReqDetails = gson.fromJson(response, ArtifactReqDetails.class);
		return artifactReqDetails;
	}

	public static <T> T parseToObject(String json, Class<T> clazz) {
		Gson gson = new Gson();
		T object;
		try {
			object = gson.fromJson(json, clazz);
		} catch (Exception e) {
			object = parseToObjectUsingMapper(json, clazz);
		}
		return object;
	}

	public static <T> T parseToObjectUsingMapper(String json, Class<T> clazz) {
		// Generic convert
		ObjectMapper mapper = new ObjectMapper();
		T object = null;
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);
		mapper.registerModule(module);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			object = mapper.readValue(json, clazz);
			// System.out.println("Class: "+clazz.getSimpleName()+", json:
			// "+json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object;
	}

	public static ArtifactReqDetails convertArtifactDefinitionToArtifactReqDetailsObject(
			ArtifactDefinition artifactDefinition) {

		ArtifactReqDetails artifactReqDetails = null;
		Gson gson = new Gson();
		String artDef = gson.toJson(artifactDefinition);
		artifactReqDetails = gson.fromJson(artDef, ArtifactReqDetails.class);
		return artifactReqDetails;
	}

	public static <T> void addDeserializer(SimpleModule module, Class<T> clazz,
			final JsonDeserializer<T> deserializer) {
		module.addDeserializer(clazz, deserializer);
	}

	public static Service convertServiceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> deserializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, deserializer);

		mapper.registerModule(module);
		Service service = null;
		try {
//			TODO Andrey L. uncomment line below in case to ignore on unknown properties, not recommended, added by Matvey
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			service = mapper.readValue(response, Service.class);
			logger.debug(service.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return service;
	}

	public static Product convertProductResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		
		Product product = null;
		try {
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			product = mapper.readValue(response, Product.class);
			logger.debug(product.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return product;
	}

	public static ComponentInstance convertComponentInstanceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		ComponentInstance componentInstance = null;
		try {
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			componentInstance = mapper.readValue(response, ComponentInstance.class);
			logger.debug(componentInstance.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return componentInstance;
	}

	public static List<String> getValuesFromJsonArray(RestResponse message) throws Exception {
		List<String> artifactTypesArrayFromApi = new ArrayList<String>();

		org.json.JSONObject responseObject = new org.json.JSONObject(message.getResponse());
		JSONArray jArr = responseObject.getJSONArray("artifactTypes");

		for (int i = 0; i < jArr.length(); i++) {
			org.json.JSONObject jObj = jArr.getJSONObject(i);
			String value = jObj.get("name").toString();

			artifactTypesArrayFromApi.add(value);
		}
		return artifactTypesArrayFromApi;
	}

	public static String calculateMD5Header(ArtifactReqDetails artifactDetails) {
		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
		// calculate MD5 for json body
		return calculateMD5(jsonBody);

	}

	public static String calculateMD5(String data) {
		String calculatedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
		// encode base-64 result
		byte[] encodeBase64 = Base64.encodeBase64(calculatedMd5.getBytes());
		String encodeBase64Str = new String(encodeBase64);
		return encodeBase64Str;

	}

	public static List<Map<String, Object>> getAuditFromMessage(Map auditingMessage) {
		List<Map<String, Object>> auditList = new ArrayList<Map<String, Object>>();
		// JsonElement jElement = new JsonParser().parse(auditingMessage);
		// JsonObject jObject = jElement.getAsJsonObject();
		// JsonObject hitsObject = (JsonObject) jObject.get("hits");
		// JsonArray hitsArray = (JsonArray) hitsObject.get("hits");
		//
		// Iterator<JsonElement> hitsIterator = hitsArray.iterator();
		// while(hitsIterator.hasNext())
		// {
		// JsonElement nextHit = hitsIterator.next();
		// JsonObject jHitObject = nextHit.getAsJsonObject();
		// JsonObject jSourceObject = (JsonObject) jHitObject.get("_source");
		//
		// Gson gson=new Gson();
		// String auditUnparsed = jSourceObject.toString();
		//
		// Map<String,Object> map = new HashMap<String,Object>();
		// map = (Map<String,Object>) gson.fromJson(auditUnparsed,
		// map.getClass());

		auditList.add(auditingMessage);
		// }
		return auditList;
	}

	public static List<CategoryDefinition> parseCategories(RestResponse getAllCategoriesRest) {

		List<CategoryDefinition> categories = new ArrayList<>();
		try {
			JsonElement jElement = new JsonParser().parse(getAllCategoriesRest.getResponse());
			JsonArray cagegories = jElement.getAsJsonArray();
			Iterator<JsonElement> iter = cagegories.iterator();
			while (iter.hasNext()) {
				JsonElement next = iter.next();
				CategoryDefinition category = ResponseParser.parseToObject(next.toString(), CategoryDefinition.class);
				categories.add(category);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return categories;
	}

	public static JSONArray getListFromJson(RestResponse res, String field) throws JSONException {
		String valueFromJsonResponse = getValueFromJsonResponse(res.getResponse(), field);
		JSONArray jArr = new JSONArray(valueFromJsonResponse);

		return jArr;
	}

	public static List<String> getDerivedListFromJson(RestResponse res) throws JSONException {
		JSONArray listFromJson = getListFromJson(res, "derivedList");
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < listFromJson.length(); i++) {
			lst.add(listFromJson.getString(i));
		}

		return lst;
	}

	public static Map<String, Object> convertStringToMap(String obj) {
		Map<String, Object> object = (Map<String, Object>) JSONValue.parse(obj);
		return object;
	}

	public static List<Map<String, Object>> getListOfMapsFromJson(RestResponse res, String field) throws Exception {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JSONArray listFromJson = getListFromJson(res, field);
		for (int i = 0; i < listFromJson.length(); i++) {
			Map<String, Object> convertStringToMap = convertStringToMap(listFromJson.getString(i));
			list.add(convertStringToMap);
		}
		return list;

	}

	public static Map<String, Object> getJsonValueAsMap(RestResponse response, String key) {
		String valueField = getValueFromJsonResponse(response.getResponse(), key);
		Map<String, Object> convertToMap = convertStringToMap(valueField);
		return convertToMap;
	}

	public static String getJsonObjectValueByKey(String metadata, String key) {
		JsonElement jelement = new JsonParser().parse(metadata);

		JsonObject jobject = jelement.getAsJsonObject();
		Object obj = jobject.get(key);
		if (obj == null) {
			return null;
		} else {
			return obj.toString();
		}
	}

	public static Map<String, List<Component>> convertCatalogResponseToJavaObject(String response) {

		// Map<String, ArrayList<Component>> map = new HashMap<String,
		// ArrayList<Component>>();
		Map<String, List<Component>> map = new HashMap<String, List<Component>>();

		JsonElement jElement = new JsonParser().parse(response);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray jArrReousrces = jObject.getAsJsonArray("resources");
		JsonArray jArrServices = jObject.getAsJsonArray("services");
		JsonArray jArrProducts = jObject.getAsJsonArray("products");
		
		if (jArrReousrces != null && jArrServices != null && jArrProducts != null){
			
		
		//////// RESOURCE/////////////////////////////
		ArrayList<Component> restResponseArray = new ArrayList<>();
		Component component = null;
		for (int i = 0; i < jArrReousrces.size(); i++) {
			String resourceString = (String) jArrReousrces.get(i).toString();
			component = ResponseParser.convertResourceResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("resources", restResponseArray);

		///////// SERVICE/////////////////////////////

		restResponseArray = new ArrayList<>();
		component = null;
		for (int i = 0; i < jArrServices.size(); i++) {
			String resourceString = (String) jArrServices.get(i).toString();
			component = ResponseParser.convertServiceResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("services", restResponseArray);

		///////// PRODUCT/////////////////////////////
		restResponseArray = new ArrayList<>();
		component = null;
		for (int i = 0; i < jArrProducts.size(); i++) {
			String resourceString = (String) jArrProducts.get(i).toString();
			component = ResponseParser.convertProductResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("products", restResponseArray);
		
    	}
		else {
			map.put("resources", new ArrayList<>());
			map.put("services", new ArrayList<>());
			map.put("products", new ArrayList<>());
		}

		return map;

	}
	
	
	public static Map<Long, ServiceDistributionStatus> convertServiceDistributionStatusToObject(String response) throws ParseException {

		Map<Long, ServiceDistributionStatus> serviceDistributionStatusMap = new HashMap<Long, ServiceDistributionStatus>();
		ServiceDistributionStatus serviceDistributionStatusObject = null;
		
		JsonElement jElement = new JsonParser().parse(response);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray jDistrStatusArray = jObject.getAsJsonArray("distributionStatusOfServiceList");
		
		for (int i = 0; i < jDistrStatusArray.size(); i++){
			Gson gson = new Gson();
			String servDistrStatus = gson.toJson(jDistrStatusArray.get(i));
			serviceDistributionStatusObject = gson.fromJson(servDistrStatus, ServiceDistributionStatus.class);
			serviceDistributionStatusMap.put(Utils.getEpochTimeFromUTC(serviceDistributionStatusObject.getTimestamp()), serviceDistributionStatusObject);
		}

		return serviceDistributionStatusMap;
		
	}
	
	public static Map<String, String> getPropertiesNameType(RestResponse restResponse)
			throws JSONException {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		JSONArray propertiesList = getListFromJson(restResponse, "properties");
		for (int i = 0; i < propertiesList.length() ; i ++){
			JSONObject  prop = (JSONObject) JSONValue.parse(propertiesList.get(i).toString());
			String propName = prop.get("name").toString();
			String propType = prop.get("type").toString();
			propertiesMap.put(propName, propType);
		}
		
		return propertiesMap;
	}
	
	public static ResourceAssetStructure getDataOutOfSearchExternalAPIResponseForResourceName(String response, String resourceName) {
		Gson gson = new Gson();
		JsonElement jsonElement = new JsonParser().parse(response);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		for(JsonElement jElement: jsonArray) {
			ResourceAssetStructure parsedResponse = gson.fromJson(jElement, ResourceAssetStructure.class);
			
			if(resourceName.contains(parsedResponse.getName()) && parsedResponse.getName().contains(resourceName)) {
				return parsedResponse;
			}
		}
		
		return null;
	}
}
