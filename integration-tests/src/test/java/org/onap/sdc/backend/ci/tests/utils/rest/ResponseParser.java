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

package org.onap.sdc.backend.ci.tests.utils.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceAssetStructure;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceRespJavaObject;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceDistributionStatus;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.VfModuleDefinition;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

public class ResponseParser {

    private static final String INVARIANT_UUID = "invariantUUID";
    public static final String UNIQUE_ID = "uniqueId";
    public static final String VERSION = "version";
    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String ORIGIN_TYPE = "originType";
    public static final String TOSCA_RESOURCE_NAME = "toscaResourceName";

    static Logger logger = LoggerFactory.getLogger(ResponseParser.class);

    public static String getValueFromJsonResponse(String response, String fieldName) {
        try {
            String[] split = fieldName.split(":");
            String fieldValue = response;

            for (int i = 0; i < split.length; i++) {
                fieldValue = parser(fieldValue, split[i]);
            }
            return fieldValue;
        } catch (Exception e) {
            return null;
        }

    }

    private static String parser(String response, String field) {
        JSONObject fieldValue = (JSONObject) JSONValue.parse(response);
        return fieldValue.get(field).toString();
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
        Map<String, Object> map = new HashMap<>();
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

    private static ObjectMapper newObjectMapper() {
        SimpleModule module = new SimpleModule("customDeserializationModule");
        module.addDeserializer(PropertyConstraint.class, new PropertyOperation.PropertyConstraintJacksonDeserializer());
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .registerModule(module);
    }

    public static Resource convertResourceResponseToJavaObject(String response) {
        ObjectMapper mapper = newObjectMapper();
        Resource resource = null;
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            resource = mapper.readValue(response, Resource.class);

            logger.debug(resource.toString());
        } catch (IOException e) {
            try {
                List<Resource> resources = Arrays.asList(mapper.readValue(response.toString(), Resource[].class));
                resource = resources.get(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return resource;
    }

    public static ComponentInstanceProperty convertPropertyResponseToJavaObject(String response) {

        ObjectMapper mapper = newObjectMapper();
        ComponentInstanceProperty propertyDefinition = null;
        try {
            propertyDefinition = mapper.readValue(response, ComponentInstanceProperty.class);
            logger.debug(propertyDefinition.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propertyDefinition;
    }

    public static GroupDefinition convertPropertyResponseToObject(String response) {

        ObjectMapper mapper = newObjectMapper();
        GroupDefinition groupDefinition = null;
        try {
            groupDefinition = mapper.readValue(response, GroupDefinition.class);
            logger.debug(groupDefinition.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupDefinition;
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static ArtifactDefinition convertArtifactDefinitionResponseToJavaObject(String response) {
        ObjectMapper mapper = new ObjectMapper();
        ArtifactDefinition artifactDefinition = null;
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            artifactDefinition = mapper.readValue(response, ArtifactDefinition.class);
            logger.debug(artifactDefinition.toString());
        } catch (IOException e) {
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
        ObjectMapper mapper = newObjectMapper();
        T object = null;
        try {
            object = mapper.readValue(json, clazz);
        } catch (IOException e) {
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

    public static Service convertServiceResponseToJavaObject(String response) {

        ObjectMapper mapper = newObjectMapper();
        Service service = null;
        try {
            service = mapper.readValue(response, Service.class);
            logger.debug(service.toString());
            //Temporary catch until bug with distribution status fixed
        } catch (InvalidFormatException e) {
            System.out.println("broken service with invalid distribution status : " + response);
            logger.debug("broken service with invalid distribution status : " + response);
            return service;
        } catch (IOException e) {

            e.printStackTrace();
        }

        return service;
    }

    public static Product convertProductResponseToJavaObject(String response) {

        ObjectMapper mapper = newObjectMapper();
        Product product = null;
        try {
            product = mapper.readValue(response, Product.class);
            logger.debug(product.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return product;
    }

    public static ComponentInstance convertComponentInstanceResponseToJavaObject(String response) {

        ObjectMapper mapper = newObjectMapper();
        ComponentInstance componentInstance = null;
        try {
            componentInstance = mapper.readValue(response, ComponentInstance.class);
            logger.debug(componentInstance.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return componentInstance;
    }

    public static List<String> getValuesFromJsonArray(RestResponse message) throws Exception {
        List<String> artifactTypesArrayFromApi = new ArrayList<>();

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

    public static List<Map<String, Object>> getAuditFromMessage(Map<String, Object> auditingMessage) {
        List<Map<String, Object>> auditList = new ArrayList<>();
        auditList.add(auditingMessage);
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
        JSONArray listFromJson = getListFromJson(res, "derivedFrom");
        List<String> lst = new ArrayList<>();
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
        List<Map<String, Object>> list = new ArrayList<>();
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
        Map<String, List<Component>> map = new HashMap<>();

        JsonElement jElement = new JsonParser().parse(response);
        JsonObject jObject = jElement.getAsJsonObject();
        JsonArray jArrReousrces = jObject.getAsJsonArray("resources");
        JsonArray jArrServices = jObject.getAsJsonArray("services");

        if (jArrReousrces != null && jArrServices != null) {
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

        } else {
            map.put("resources", new ArrayList<>());
            map.put("services", new ArrayList<>());
        }

        return map;

    }

    public static Map<Long, ServiceDistributionStatus> convertServiceDistributionStatusToObject(String response) throws ParseException {

        Map<Long, ServiceDistributionStatus> serviceDistributionStatusMap = new HashMap<>();
        ServiceDistributionStatus serviceDistributionStatusObject = null;

        JsonElement jElement = new JsonParser().parse(response);
        JsonObject jObject = jElement.getAsJsonObject();
        JsonArray jDistrStatusArray = jObject.getAsJsonArray("distributionStatusOfServiceList");

        for (int i = 0; i < jDistrStatusArray.size(); i++) {
            Gson gson = new Gson();
            String servDistrStatus = gson.toJson(jDistrStatusArray.get(i));
            serviceDistributionStatusObject = gson.fromJson(servDistrStatus, ServiceDistributionStatus.class);
            serviceDistributionStatusMap.put(Utils.getEpochTimeFromUTC(serviceDistributionStatusObject.getTimestamp()), serviceDistributionStatusObject);
        }

        return serviceDistributionStatusMap;

    }

    public static Map<String, String> getPropertiesNameType(RestResponse restResponse)
            throws JSONException {
        Map<String, String> propertiesMap = new HashMap<>();
        JSONArray propertiesList = getListFromJson(restResponse, "properties");
        for (int i = 0; i < propertiesList.length(); i++) {
            JSONObject prop = (JSONObject) JSONValue.parse(propertiesList.get(i).toString());
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
        for (JsonElement jElement : jsonArray) {
            ResourceAssetStructure parsedResponse = gson.fromJson(jElement, ResourceAssetStructure.class);

            if (resourceName.contains(parsedResponse.getName()) && parsedResponse.getName().contains(resourceName)) {
                return parsedResponse;
            }
        }

        return null;
    }

    public static Map<String, VfModuleDefinition> convertVfModuleJsonResponseToJavaObject(String response) {

        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        inputStream = new ByteArrayInputStream(response.getBytes());
        List<?> list = (List<?>) yaml.load(inputStream);
        ObjectMapper mapper = new ObjectMapper();

        VfModuleDefinition moduleDefinition;
        Map<String, VfModuleDefinition> vfModulesMap = new HashMap<>();
        for (Object obj : list) {
//			TODO Andrey L. uncomment line below in case to ignore on unknown properties, not recommended
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            moduleDefinition = mapper.convertValue(obj, VfModuleDefinition.class);
            vfModulesMap.put(moduleDefinition.vfModuleModelName, moduleDefinition);
        }
        return vfModulesMap;
    }

    public static InterfaceDefinition convertInterfaceDefinitionResponseToJavaObject(String response) {
        ObjectMapper mapper = new ObjectMapper();
        InterfaceDefinition interfaceDefinition = null;
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            interfaceDefinition = mapper.readValue(response, InterfaceDefinition.class);
            logger.debug(interfaceDefinition.toString());
        } catch (IOException e) {
            logger.debug("Failed to convertInterfaceDefinitionResponseToJavaObject", e);
        }
        return interfaceDefinition;
    }

}
