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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.AssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceInstanceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AssetRestUtils extends BaseRestUtils {
	static Gson gson = new Gson();
	static ObjectMapper objectMapper = new ObjectMapper();

	static Logger logger = LoggerFactory.getLogger(AssetRestUtils.class.getName());
	static final String contentTypeHeaderData = "application/json";
	static final String acceptHeaderDate = "application/json";
	static final String basicAuthentication = "Basic Y2k6MTIzNDU2";
	// /sdc/v1/catalog/{services/resources}/{componentUUID}/artifacts/{artifactUUID}
	static final String COMPONENT_ARTIFACT_URL = "/sdc/v1/catalog/%s/%s/artifacts/%s";
	// /sdc/v1/catalog/{services/resources}/{componentUUID}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}
	static final String RESOURCE_INSTANCE_ARTIFACT_URL = "/sdc/v1/catalog/%s/%s/resourceInstances/%s/artifacts/%s";

	public static HttpResponse getComponentToscaModel(AssetTypeEnum assetType, String uuid) throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = String.format(Urls.GET_TOSCA_MODEL, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue(), uuid);
		HttpGet httpGet = new HttpGet(url);

		httpGet.addHeader(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		httpGet.addHeader(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);

		logger.debug("Send GET request to get Tosca model: {}", url);

		return httpclient.execute(httpGet);
	}
	
	public static File getToscaModelCsarFile(AssetTypeEnum assetType, String uuid, String fileName) throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String url = String.format(Urls.GET_TOSCA_MODEL, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue(), uuid);
		HttpGet httpGet = new HttpGet(url);

		String csarDir = FileHandling.getCreateDirByName("outputCsar");
		File myFile = new File(csarDir+ File.separator + "tmpCSAR_" + fileName + ".csar");

		
		httpGet.addHeader(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		httpGet.addHeader(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);

		logger.debug("Send GET request to get Tosca model: {}", url);
		CloseableHttpResponse execute = httpclient.execute(httpGet);
		
	    HttpEntity entity = execute.getEntity();
	    if (entity != null) {
	        try (FileOutputStream outstream = new FileOutputStream(myFile)) {
	            entity.writeTo(outstream);
	            outstream.close();
	        }
	    }
		return myFile;
	}



	public static RestResponse getRestResponseComponentToscaModel(AssetTypeEnum assetType, String uuid) throws IOException {
		Config config = Utils.getConfig();
		
		String url = String.format(Urls.GET_TOSCA_MODEL, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue(), uuid);
		
		Map<String, String> headersMap = new HashMap<String,String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		
		HttpRequest http = new HttpRequest();

		logger.debug("Send GET request to get Resource Assets: {}", url);
		System.out.println("Send GET request to get Resource Assets: " + url);
		
		logger.debug("Request headers: {}", headersMap);
		System.out.println("Request headers: " + headersMap);

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;

	}

	public static RestResponse getComponentListByAssetType(boolean isBasicAuthentication, AssetTypeEnum assetType,
			String... filterArrayString) throws IOException {
		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		if (isBasicAuthentication) {
			headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);
		}
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ASSET_LIST, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue());
		if (filterArrayString != null && filterArrayString.length > 0) {
			url = buildUrlWithFilter(url, filterArrayString);
		}

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;
	}

	public static RestResponse getFilteredComponentList(AssetTypeEnum assetType, String query) throws IOException {
		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		HttpRequest http = new HttpRequest();

		String url = String.format(Urls.GET_FILTERED_ASSET_LIST, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue(), query);

		logger.debug("Send GET request to get Resource Assets: {}", url);
		logger.debug("Request headers: {}", headersMap);

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;
	}

	public static String buildUrlWithFilter(String url, String[] filterArrayString) {
		StringBuilder sb = new StringBuilder();
		int length = filterArrayString.length;
		int count = 0;
		for (String filterString : filterArrayString) {
			sb.append(filterString);
			count++;
			if (length != count) {
				sb.append("&");
			}
		}
		return url + "?" + sb;
	}

	public static RestResponse getAssetMetadataByAssetTypeAndUuid(boolean isBasicAuthentication,
			AssetTypeEnum assetType, String uuid) throws IOException {

		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		if (isBasicAuthentication) {
			headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);
		}
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ASSET_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				assetType.getValue(), uuid);

		logger.debug("Send GET request to get Resource Assets: {}", url);
		logger.debug("Request headers: {}", headersMap);

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;
	}

	public static List<ResourceAssetStructure> getResourceAssetList(RestResponse assetResponse) {
		List<ResourceAssetStructure> resourceAssetList = new ArrayList<>();

		JsonElement jelement = new JsonParser().parse(assetResponse.getResponse());
		JsonArray componenetArray = (JsonArray) jelement;
		for (JsonElement jElement : componenetArray) {
			ResourceAssetStructure resource = gson.fromJson(jElement, ResourceAssetStructure.class);
			resourceAssetList.add(resource);
		}
		return resourceAssetList;
	}

	public static ResourceDetailedAssetStructure getResourceAssetMetadata(RestResponse assetResponse) {

		List<ResourceInstanceAssetStructure> resourcesList = new ArrayList<>();
		List<ArtifactAssetStructure> artifactsList = new ArrayList<>();
		ResourceDetailedAssetStructure resourceAssetMetadata = new ResourceDetailedAssetStructure();
		String response = assetResponse.getResponse();

		JsonObject jObject = (JsonObject) new JsonParser().parse(response);
		resourceAssetMetadata = gson.fromJson(jObject, ResourceDetailedAssetStructure.class);

		setResourceInstanceAssetList(resourcesList, jObject);
		resourceAssetMetadata.setResources(resourcesList);

		setArtifactAssetList(artifactsList, jObject);
		resourceAssetMetadata.setArtifacts(artifactsList);

		return resourceAssetMetadata;
	}

	public static void generalMetadataFieldsValidatior(AssetStructure assetMetadata, Component component) {

		assertTrue("Expected resourceUuid is " + component.getUUID() + " actual: " + assetMetadata.getUuid(),
				assetMetadata.getUuid().equals(component.getUUID()));
		assertTrue(
				"Expected resourceInvariantUuid is " + component.getInvariantUUID() + " actual: "
						+ assetMetadata.getInvariantUUID(),
				assetMetadata.getInvariantUUID().equals(component.getInvariantUUID()));
		assertTrue("Expected asset name is " + component.getName() + " actual: " + assetMetadata.getName(),
				assetMetadata.getName().equals(component.getName()));
		assertTrue("Expected asset version is " + component.getVersion() + " actual: " + assetMetadata.getVersion(),
				assetMetadata.getVersion().equals(component.getVersion()));
		assertTrue(
				"Expected asset lastUpdaterUserId is " + component.getLastUpdaterUserId() + " actual: "
						+ assetMetadata.getLastUpdaterUserId(),
				assetMetadata.getLastUpdaterUserId().equals(component.getLastUpdaterUserId()));
		assertNotNull("Expected asset toscaModel is null", assetMetadata.getToscaModelURL());
		assertTrue(
				"Expected asset category is " + component.getCategories().get(0).getName() + " actual: "
						+ assetMetadata.getCategory(),
				assetMetadata.getCategory().equals(component.getCategories().get(0).getName()));
		assertTrue(
				"Expected asset lifeCycleState is " + component.getLifecycleState() + " actual: "
						+ assetMetadata.getLifecycleState(),
				assetMetadata.getLifecycleState().equals(component.getLifecycleState().toString()));

	}

	public static void resourceMetadataValidatior(ResourceDetailedAssetStructure resourceAssetMetadata,
			Resource resource, AssetTypeEnum assetType) throws Exception {

		generalMetadataFieldsValidatior(resourceAssetMetadata, resource);
		assertTrue(
				"Expected asset lastUpdaterFullName is " + resource.getLastUpdaterFullName() + " actual: "
						+ resourceAssetMetadata.getLastUpdaterFullName(),
				resourceAssetMetadata.getLastUpdaterFullName().equals(resource.getLastUpdaterFullName()));
		assertTrue(
				"Expected asset subCategory is " + resource.getCategories().get(0).getSubcategories().get(0).getName()
						+ " actual: " + resourceAssetMetadata.getSubCategory(),
				resourceAssetMetadata.getSubCategory()
						.equals(resource.getCategories().get(0).getSubcategories().get(0).getName()));
		assertTrue(
				"Expected asset toscaResourceName is " + resource.getToscaResourceName() + " actual: "
						+ resourceAssetMetadata.getToscaResourceName(),
				resourceAssetMetadata.getToscaResourceName().equals(resource.getToscaResourceName()));
		assertTrue(
				"Expected asset resourceType is " + resource.getResourceType() + " actual: "
						+ resourceAssetMetadata.getResourceType(),
				resourceAssetMetadata.getResourceType().equals(resource.getResourceType().toString()));
		resourceInstanceAssetValidator(resourceAssetMetadata.getResources(), resource, assetType);
		// resourceInstanceAssetValidator(resourceAssetMetadata.getResources(),
		// resource);
		artifactAssetValidator(resourceAssetMetadata.getArtifacts(), resource, assetType);

	}

	public static void serviceMetadataValidatior(ServiceDetailedAssetStructure serviceAssetMetadata, Service service,
			AssetTypeEnum assetType) throws Exception {

		generalMetadataFieldsValidatior(serviceAssetMetadata, service);
		assertTrue(
				"Expected asset lastUpdaterFullName is " + service.getLastUpdaterFullName() + " actual: "
						+ serviceAssetMetadata.getLastUpdaterFullName(),
				serviceAssetMetadata.getLastUpdaterFullName().equals(service.getLastUpdaterFullName()));
		assertTrue("Expected asset distributionStatus is " + service.getDistributionStatus() + " actual: "
						+ serviceAssetMetadata.getDistributionStatus(),
				serviceAssetMetadata.getDistributionStatus().equals(service.getDistributionStatus().toString()));
		resourceInstanceAssetValidator(serviceAssetMetadata.getResources(), service, assetType);
		// resourceInstanceAssetValidator(serviceAssetMetadata.getResources(),
		// service);
		artifactAssetValidator(serviceAssetMetadata.getArtifacts(), service, assetType);

	}

	private static void artifactAssetValidator(List<ArtifactAssetStructure> artifactAssetStructureList,
			Component component, AssetTypeEnum assetType) {
		Map<String, ArtifactDefinition> componentDeploymentArtifacts = component.getDeploymentArtifacts();
		validateArtifactMetadata(componentDeploymentArtifacts, artifactAssetStructureList, component.getUUID(),
				assetType, null);
	}

	private static void validateArtifactMetadata(Map<String, ArtifactDefinition> componentDeploymentArtifacts,
			List<ArtifactAssetStructure> artifactAssetStructureList, String componentUuid, AssetTypeEnum assetType,
			String resourceInstanceName) {
		if(componentDeploymentArtifacts != null){
			for (Entry<String, ArtifactDefinition> componentDeploymentArtifact : componentDeploymentArtifacts.entrySet()) {
				ArtifactAssetStructure artifactAssetStructure = getArtifactMetadata(artifactAssetStructureList,
						componentDeploymentArtifact.getValue().getArtifactUUID());
				ArtifactDefinition componentDeploymentArtifactValue = componentDeploymentArtifact.getValue();
				if (artifactAssetStructure != null) {
					assertTrue(
							"Expected artifact asset artifactName is " + componentDeploymentArtifactValue.getArtifactName()
									+ " actual: " + artifactAssetStructure.getArtifactName(),
							componentDeploymentArtifactValue.getArtifactName()
									.equals(artifactAssetStructure.getArtifactName()));
					assertTrue(
							"Expected artifact asset Type is " + componentDeploymentArtifactValue.getArtifactType()
									+ " actual: " + artifactAssetStructure.getArtifactType(),
							componentDeploymentArtifactValue.getArtifactType()
									.equals(artifactAssetStructure.getArtifactType()));
					// assertNotNull("Expected artifact asset resourceInvariantUUID
					// is null",
					// resourceInstanceAssetStructure.getResourceInvariantUUID());
					// String expectedArtifactUrl = "/sdc/v1/catalog/" +
					// assetType.getValue() + "/" + componentUuid + "/artifacts/" +
					// componentDeploymentArtifactValue.getArtifactUUID();
					String expectedArtifactUrl = "";
					if (resourceInstanceName == null) {
						expectedArtifactUrl = String.format(COMPONENT_ARTIFACT_URL, assetType.getValue(), componentUuid,
								componentDeploymentArtifactValue.getArtifactUUID());
					} else {
						expectedArtifactUrl = String.format(RESOURCE_INSTANCE_ARTIFACT_URL, assetType.getValue(),
								componentUuid, resourceInstanceName, componentDeploymentArtifactValue.getArtifactUUID());
					}
	
					assertTrue(
							"Expected artifact asset URL is " + expectedArtifactUrl + " actual: "
									+ artifactAssetStructure.getArtifactURL(),
							artifactAssetStructure.getArtifactURL().equals(expectedArtifactUrl));
					assertTrue(
							"Expected artifact asset description is " + componentDeploymentArtifactValue.getDescription()
									+ " actual: " + artifactAssetStructure.getArtifactDescription(),
							componentDeploymentArtifactValue.getDescription().toString()
									.equals(artifactAssetStructure.getArtifactDescription()));
					assertTrue(
							"Expected artifact asset checkSum is " + componentDeploymentArtifactValue.getArtifactChecksum()
									+ " actual: " + artifactAssetStructure.getArtifactChecksum(),
							componentDeploymentArtifactValue.getArtifactChecksum()
									.equals(artifactAssetStructure.getArtifactChecksum()));
					assertTrue(
							"Expected artifact asset version is " + componentDeploymentArtifactValue.getArtifactVersion()
									+ " actual: " + artifactAssetStructure.getArtifactVersion(),
							componentDeploymentArtifactValue.getArtifactVersion()
									.equals(artifactAssetStructure.getArtifactVersion()));
					if (componentDeploymentArtifactValue.getTimeout() > 0) {
						assertTrue(
								"Expected artifact asset timeout is " + componentDeploymentArtifactValue.getTimeout()
										+ " actual: " + artifactAssetStructure.getArtifactTimeout(),
								componentDeploymentArtifactValue.getTimeout()
										.equals(artifactAssetStructure.getArtifactTimeout()));
					}
	
				} else {
					assertTrue("artifact asset with UUID" + componentDeploymentArtifact.getValue().getArtifactUUID()
							+ " not found in get Metadata response", false);
				}
			}
		}else{
			System.out.println("componentDeploymentArtifacts is null");
			logger.debug("componentDeploymentArtifacts is null");
		}

	}

	private static ArtifactAssetStructure getArtifactMetadata(List<ArtifactAssetStructure> artifactAssetStructureList,
			String artifactUUID) {
		for (ArtifactAssetStructure artifactAssetStructure : artifactAssetStructureList) {
			if (artifactAssetStructure.getArtifactUUID().equals(artifactUUID)) {
				return artifactAssetStructure;
			}
		}
		return null;
	}

	private static void resourceInstanceAssetValidator(
			List<ResourceInstanceAssetStructure> resourceInstanceAssetStructures, Component component,
			AssetTypeEnum assetType) throws Exception {

		List<ComponentInstance> componentInstances = component.getComponentInstances();
		if (componentInstances != null) {
			for (ComponentInstance componentInstance : componentInstances) {
				ResourceInstanceAssetStructure resourceInstanceAssetStructure = getResourceInstanceMetadata(
						resourceInstanceAssetStructures, componentInstance.getName());
				if (resourceInstanceAssetStructure != null) {
					assertTrue(
							"Expected RI asset resourceName is " + componentInstance.getComponentName() + " actual: "
									+ resourceInstanceAssetStructure.getResourceName(),
							componentInstance.getComponentName()
									.equals(resourceInstanceAssetStructure.getResourceName()));
					assertTrue(
							"Expected RI asset Name is " + componentInstance.getName() + " actual: "
									+ resourceInstanceAssetStructure.getResourceInstanceName(),
							componentInstance.getName()
									.equals(resourceInstanceAssetStructure.getResourceInstanceName()));
					assertNotNull("Expected RI asset resourceInvariantUUID is null",
							resourceInstanceAssetStructure.getResourceInvariantUUID());
					assertTrue(
							"Expected RI asset resourceVersion is " + componentInstance.getComponentVersion()
									+ " actual: " + resourceInstanceAssetStructure.getResourceVersion(),
							componentInstance.getComponentVersion()
									.equals(resourceInstanceAssetStructure.getResourceVersion()));
					assertTrue(
							"Expected RI asset resourceType is " + componentInstance.getOriginType() + " actual: "
									+ resourceInstanceAssetStructure.getResoucreType(),
							componentInstance.getOriginType().toString()
									.equals(resourceInstanceAssetStructure.getResoucreType()));
					Resource resource = AtomicOperationUtils.getResourceObject(componentInstance.getComponentUid());
					assertTrue("Expected RI asset resourceUUID is " + resource.getUUID() + " actual: " + resourceInstanceAssetStructure.getResourceUUID(),
							resource.getUUID().equals(resourceInstanceAssetStructure.getResourceUUID()));
					validateArtifactMetadata(componentInstance.getDeploymentArtifacts(),
							resourceInstanceAssetStructure.getArtifacts(), component.getUUID(), assetType,
							componentInstance.getNormalizedName());
					// validateArtifactMetadata(componentInstance.getDeploymentArtifacts(),
					// resourceInstanceAssetStructure.getArtifacts(),
					// component.getUUID(), AssetTypeEnum.RESOURCES);
				} else {
					assertTrue("resourceInstance asset with UUID" + componentInstance.getComponentUid()
							+ " not found in get Metadata response", false);
				}
			}
		}

	}

	// private static ResourceInstanceAssetStructure
	// getResourceInstanceMetadata(List<ResourceInstanceAssetStructure>
	// resourceInstanceAssetStructures, String componentUid) {
	private static ResourceInstanceAssetStructure getResourceInstanceMetadata(
			List<ResourceInstanceAssetStructure> resourceInstanceAssetStructures, String name) {
		for (ResourceInstanceAssetStructure resourceInstanceAssetStructure : resourceInstanceAssetStructures) {
			if (resourceInstanceAssetStructure.getResourceInstanceName().equals(name)) {
				return resourceInstanceAssetStructure;
			}
		}
		return null;
	}

	public static ServiceDetailedAssetStructure getServiceAssetMetadata(RestResponse assetResponse) {

		List<ResourceInstanceAssetStructure> resourcesList = new ArrayList<>();
		List<ArtifactAssetStructure> artifactsList = new ArrayList<>();
		ServiceDetailedAssetStructure serviceAssetMetadata;

		JsonObject jObject = (JsonObject) new JsonParser().parse(assetResponse.getResponse());
		serviceAssetMetadata = gson.fromJson(jObject, ServiceDetailedAssetStructure.class);

		setResourceInstanceAssetList(resourcesList, jObject);
		serviceAssetMetadata.setResources(resourcesList);

		setArtifactAssetList(artifactsList, jObject);
		serviceAssetMetadata.setArtifacts(artifactsList);

		return serviceAssetMetadata;
	}

	public static void setArtifactAssetList(List<ArtifactAssetStructure> artifactsList, JsonObject jObject) {
		JsonArray artifactsArray = jObject.getAsJsonArray("artifacts");
		if (artifactsArray != null) {
			for (JsonElement jElement : artifactsArray) {
				ArtifactAssetStructure artifact = gson.fromJson(jElement, ArtifactAssetStructure.class);
				artifactsList.add(artifact);
			}
		}
	}

	public static void setResourceInstanceAssetList(List<ResourceInstanceAssetStructure> resourcesList,
			JsonObject jObject) {
		JsonArray resourcesArray = jObject.getAsJsonArray("resources");
		if (resourcesArray != null) {
			for (JsonElement jElement : resourcesArray) {
				ResourceInstanceAssetStructure resource = gson.fromJson(jElement, ResourceInstanceAssetStructure.class);
				resourcesList.add(resource);
			}
		}
	}

	public static List<ServiceAssetStructure> getServiceAssetList(RestResponse assetResponse) {
		List<ServiceAssetStructure> serviceAssetList = new ArrayList<>();

		JsonElement jelement = new JsonParser().parse(assetResponse.getResponse());
		JsonArray componenetArray = (JsonArray) jelement;
		for (JsonElement jElement : componenetArray) {
			ServiceAssetStructure service = gson.fromJson(jElement, ServiceAssetStructure.class);
			serviceAssetList.add(service);
		}
		return serviceAssetList;
	}

	public static List<String> getResourceNamesList(List<ResourceAssetStructure> resourceAssetList) {
		List<String> assetNamesList = new ArrayList<>();
		for (ResourceAssetStructure resourceAsset : resourceAssetList) {
			assetNamesList.add(resourceAsset.getName());
		}
		return assetNamesList;
	}
	
	public static Map<String,String> getResourceAssetMap(RestResponse assetResponse) {
		Map<String,String> resourceAssetMap =  new HashMap<>();

		JsonElement jelement = new JsonParser().parse(assetResponse.getResponse());
		JsonArray componenetArray = (JsonArray) jelement;
		for (JsonElement jElement : componenetArray) {
			ResourceAssetStructure resource = gson.fromJson(jElement, ResourceAssetStructure.class);
			resourceAssetMap.put(resource.getName(), resource.getVersion());
		}
		return resourceAssetMap;
	}
	
	public static Map<String,String> getResourceListFiltteredByWholeVersion(Map<String,String> resourceAssetList) {
		Map<String,String> assetNamesMap = new HashMap<>();
			for (Entry<String, String> entry : resourceAssetList.entrySet()) {
			    String key = entry.getKey();
			    String[] parts = entry.getValue().split("\\.");
			    String lastOne = parts[parts.length-1];
			    
			    if (key.contains("CinderVolume") ){
			    	assetNamesMap.put(key,entry.getValue());
			    }
			    
			    if (lastOne.equals("0") && !key.contains("Ci") ){
			    	assetNamesMap.put(key,entry.getValue());
			    }
			}
			
		return assetNamesMap;
	}

	public static List<String> getResourceObjectByNameAndVersionToscaNamesList(Map<String,String> resourceAssetList) throws Exception {
		List<String> assetNamesList = new ArrayList<>();
		for (Entry<String, String> entry : resourceAssetList.entrySet()) {
			System.out.println("fetch data---->"+entry.getKey()+entry.getValue());
			Resource resourceObjectByNameAndVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, entry.getKey(), entry.getValue());
		    	assetNamesList.add(resourceObjectByNameAndVersion.getToscaResourceName());
		    
		}
		
		return assetNamesList;
	}
	

	public static List<String> getServiceNamesList(List<ServiceAssetStructure> serviceAssetList) {
		List<String> assetNamesList = new ArrayList<>();
		for (ServiceAssetStructure serviceAsset : serviceAssetList) {
			assetNamesList.add(serviceAsset.getName());
		}
		return assetNamesList;
	}

	public static void checkResourceTypeInObjectList(List<ResourceAssetStructure> resourceAssetList, ResourceTypeEnum resourceType) {
		for (ResourceAssetStructure resourceAsset : resourceAssetList) {
			assertTrue("Expected resourceType is " + resourceType.toString() + " actual: " + resourceAsset.getResourceType(),
					resourceAsset.getResourceType().equals(resourceType.toString()));
		}
	}

	public static void checkComponentTypeInObjectList(List<ResourceAssetStructure> resourceAssetList, ComponentTypeEnum componentType) {
		ComponentTypeEnum actualComponentType;
		for (ResourceAssetStructure resourceAsset : resourceAssetList) {
			actualComponentType = detectComponentType(resourceAsset);
			assertTrue("Expected componentType is " + componentType.getValue() + " actual: " + actualComponentType.getValue(), actualComponentType.equals(componentType));
		}
	}

	private static ComponentTypeEnum detectComponentType(ResourceAssetStructure resourceAsset) {
		String resourceType = resourceAsset.getResourceType();
		if(ResourceTypeEnum.getType(resourceType) !=null){
			return ComponentTypeEnum.RESOURCE;
		}
		return null;
	}
	private static String getShortUUID() {
		return UUID.randomUUID().toString().split("-")[0];
	}
	
}
