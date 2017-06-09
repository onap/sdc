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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;

import com.google.gson.Gson;

public class ArtifactRestUtils extends BaseRestUtils {
	private static Logger logger = LoggerFactory.getLogger(ArtifactRestUtils.class.getName());
	
	
	// External API
	// Delete Artifact on rI of the asset 
	public static RestResponse externalAPIDeleteArtifactOfComponentInstanceOnAsset(Component component, User user, ComponentInstance resourceInstance, String artifactUUID) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		String resourceInstanceName = resourceInstance.getNormalizedName();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.DELETE_EXTRNAL_API_DELETE_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, resourceInstanceName, artifactUUID);
		
		return deleteInformationalArtifact(user, url);
	}
	
	// Delete Artifact of the asset 
	public static RestResponse externalAPIDeleteArtifactOfTheAsset(Component component, User user, String artifactUUID) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.DELETE_EXTRNAL_API_DELETE_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, artifactUUID);
		
		RestResponse restResponse = deleteInformationalArtifact(user, url);
		
		return restResponse;
	}
	
	
	// Update Artifact on rI of the asset 
	public static RestResponse externalAPIUpdateArtifactOfComponentInstanceOnAsset(Component component, User user, ArtifactReqDetails artifactReqDetails, ComponentInstance resourceInstance, String artifactUUID) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		String resourceInstanceName = resourceInstance.getNormalizedName();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPDATE_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, resourceInstanceName, artifactUUID);
		
		return updateInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails), url);
	}
	
	// Update Artifact of the asset 
	public static RestResponse externalAPIUpdateArtifactOfTheAsset(Component component, User user, ArtifactReqDetails artifactReqDetails, String artifactUUID) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPDATE_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, artifactUUID);
		
		return updateInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails), url);
	}
	
	
	// Upload Artifact on rI of the asset 
	public static RestResponse externalAPIUploadArtifactOfComponentInstanceOnAsset(Component component, User user, ArtifactReqDetails artifactReqDetails, ComponentInstance resourceInstance) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		String resourceInstanceName = resourceInstance.getNormalizedName();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, resourceInstanceName);
		
		return uploadInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails), url);
	}
	
	// Upload Artifact of the asset 
	public static RestResponse externalAPIUploadArtifactOfTheAsset(Component component, User user, ArtifactReqDetails artifactReqDetails) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID);
		
		return uploadInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails), url);
	}
	
	
	// Upload Artifact of the asset with invalid checksum
	public static RestResponse externalAPIUploadArtifactWithInvalidCheckSumOfComponentInstanceOnAsset(Component component, User user, ArtifactReqDetails artifactReqDetails, ComponentInstance resourceInstance) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		String resourceInstanceName = resourceInstance.getNormalizedName();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_COMPONENTINSTANCE_ON_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID, resourceInstanceName);
		
		return uploadInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails) + "123", url);
	}
		
	// Upload Artifact of the asset with invalid checksum
	public static RestResponse externalAPIUploadArtifactWithInvalidCheckSumOfTheAsset(Component component, User user, ArtifactReqDetails artifactReqDetails) throws IOException {
		Config config = Utils.getConfig();
		String resourceType = null;
		String resourceUUID = component.getUUID();
		
		System.out.println(component.getComponentType());
		
		if(component.getComponentType().toString().toLowerCase().equals("resource")) {
			resourceType = "resources";
		} else {
			resourceType = "services";
		}
		
		String url = String.format(Urls.POST_EXTERNAL_API_UPLOAD_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceType, resourceUUID);
		
		return uploadInformationalArtifact(artifactReqDetails, user, calculateChecksum(artifactReqDetails) + "123", url);
	}
	
	
	//
	// Testing
	//
	public static RestResponse getResourceDeploymentArtifactExternalAPI(String resourceUUID, String artifactUUID,User sdncModifierDetails, String resourceType) throws IOException {
		Config config = Utils.getConfig();
		String url = null;
		
		if (resourceType.toUpperCase().equals("SERVICE")) {
			url = String.format(Urls.GET_DOWNLOAD_SERVICE_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUUID, artifactUUID);

		} else {
			url = String.format(Urls.GET_DOWNLOAD_RESOURCE_ARTIFACT_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUUID, artifactUUID);
		}
		
		Map<String, String> headersMap = new HashMap<String,String>();
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		
		HttpRequest http = new HttpRequest();

		logger.debug("Send GET request to get Resource Assets: {}",url);
		System.out.println("Send GET request to get Resource Assets: " + url);
		
		logger.debug("Request headers: {}",headersMap);
		System.out.println("Request headers: " + headersMap);

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;

	}
	
	
	
	public static RestResponse getComponentInstanceDeploymentArtifactExternalAPI(String resourceUUID, String componentNormalizedName, String artifactUUID,User sdncModifierDetails, String resourceType) throws IOException {
		Config config = Utils.getConfig();
		String url = null;
		
		if (resourceType.toLowerCase().equals("service")) {
			url = String.format(Urls.GET_DOWNLOAD_SERVICE_ARTIFACT_OF_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUUID, componentNormalizedName, artifactUUID);

		} else {
			url = String.format(Urls.GET_DOWNLOAD_RESOURCE_ARTIFACT_OF_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUUID, componentNormalizedName, artifactUUID);
		}
		
		Map<String, String> headersMap = new HashMap<String,String>();
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		
		HttpRequest http = new HttpRequest();

		logger.debug("Send GET request to get Resource Assets: {}",url);
		System.out.println("Send GET request to get Resource Assets: " + url);
		
		logger.debug("Request headers: {}",headersMap);
		System.out.println("Request headers: " + headersMap);

		RestResponse sendGetResourceAssets = http.httpSendGet(url, headersMap);

		return sendGetResourceAssets;

	}
	
	
	//***********  SERVICE ****************
	public static RestResponse getArtifactTypesList() throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_ARTIFACTS, config.getCatalogBeHost(), config.getCatalogBePort());

		return sendGet(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
	}

	public static RestResponse addInformationalArtifactToService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid) throws IOException {
		return addInformationalArtifactToService(artifactDetails, sdncModifierDetails, serviceUid, calculateChecksum(artifactDetails));
	}

	public static RestResponse addInformationalArtifactToService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String checksum) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.ADD_ARTIFACT_TO_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceUid);

		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, checksum, url);
	}

	public static RestResponse downloadServiceArtifact(ServiceReqDetails service, ArtifactReqDetails artifact, User user, Map<String, String> addionalHeaders) throws Exception
	{
	
		return downloadServiceArtifact( service,  artifact,  user,addionalHeaders,true);
	}
	
	public static RestResponse downloadServiceArtifact(ServiceReqDetails service, ArtifactReqDetails artifact, User user, Map<String, String> addionalHeaders,boolean addEcompHeader) throws Exception
	{
		Config config = Utils.getConfig();
		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL, ValidationUtils.convertToSystemName(service.getName()), service.getVersion(),  ValidationUtils.normalizeFileName(artifact.getArtifactName())));
		String fullUrl = String.format(Urls.DOWNLOAD_SERVICE_ARTIFACT_FULL_URL, config.getCatalogBeHost(),config.getCatalogBePort(), relativeUrl);
		
		return downloadArtifact(fullUrl, user, addionalHeaders,addEcompHeader);
	}
	
	public static RestResponse downloadResourceArtifact(ServiceReqDetails service, ResourceReqDetails resource, ArtifactReqDetails artifact, User user, Map<String, String> addionalHeaders) throws Exception
	{	
		return downloadResourceArtifact(service, resource,  artifact,  user,addionalHeaders, true);
	}
	
	public static RestResponse downloadResourceArtifact(ServiceReqDetails service,ResourceReqDetails resource, ArtifactReqDetails artifact, User user, Map<String, String> addionalHeaders,boolean addEcompHeader) throws Exception
	{
		Config config = Utils.getConfig();
		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL, ValidationUtils.convertToSystemName(service.getName()),service.getVersion(),ValidationUtils.convertToSystemName(resource.getName()), resource.getVersion(),  ValidationUtils.normalizeFileName(artifact.getArtifactName())));
		String fullUrl = String.format(Urls.DOWNLOAD_RESOURCE_ARTIFACT_FULL_URL, config.getCatalogBeHost(),config.getCatalogBePort(), relativeUrl);
		
		return downloadArtifact(fullUrl, user, addionalHeaders,addEcompHeader);
	}
	
	
	
	public static RestResponse downloadResourceInstanceArtifact(String serviceUniqueId,String resourceInstanceId, User user, String artifactUniqeId) throws Exception
	{
		Config config = Utils.getConfig();
		String url = String.format(Urls.DOWNLOAD_COMPONENT_INSTANCE_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), serviceUniqueId, resourceInstanceId, artifactUniqeId);
		RestResponse res =  sendGet(url, user.getUserId(), null);
		return res;
	}
	
	////	

	//update
	
	public static RestResponse updateInformationalArtifactOfServiceByMethod(ArtifactReqDetails artifactReqDetails, String serviceUid, String artifactUid, User sdncModifierDetails, String httpMethod) throws IOException {
		return updateInformationalArtifactOfServiceByMethod(artifactReqDetails, serviceUid, artifactUid, sdncModifierDetails, httpMethod, calculateChecksum(artifactReqDetails));
	}
	
	public static RestResponse updateInformationalArtifactOfServiceByMethod(ArtifactReqDetails artifactReqDetails, String serviceUid, User sdncModifierDetails, String httpMethod) throws IOException {
		return updateInformationalArtifactOfServiceByMethod(artifactReqDetails, serviceUid, artifactReqDetails.getUniqueId(), sdncModifierDetails, httpMethod, calculateChecksum(artifactReqDetails));
	}
	
	public static RestResponse downloadResourceArtifactInternalApi(String resourceId, User user, String artifactUniqeId) throws Exception
	{
		return downloadComponentArtifactInternalApi(resourceId, user, artifactUniqeId, Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT);
	}

	public static RestResponse downloadServiceArtifactInternalApi(String componentId, User user, String artifactUniqeId) throws Exception
	{
		return downloadComponentArtifactInternalApi(componentId, user, artifactUniqeId, Urls.UI_DOWNLOAD_SERVICE_ARTIFACT);
	}
	public static RestResponse downloadComponentArtifactInternalApi(String componentId, User user, String artifactUniqeId, String urlTemplate) throws Exception
	{
		Config config = Utils.getConfig();
		String url = String.format(urlTemplate, config.getCatalogBeHost(),config.getCatalogBePort(), componentId, artifactUniqeId);
		RestResponse res =  sendGet(url, user.getUserId(), null);
		return res;
	}
	
//	public static RestResponse downloadServiceArtifactInternalApi(String resourceId, User user, String artifactUniqeId) throws Exception
//	{
//		Config config = Utils.getConfig();
//		String url = String.format(Urls.UI_DOWNLOAD_SERVICE_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), resourceId, artifactUniqeId);
//		RestResponse res =  sendGet(url, user.getUserId(), null);
//		return res;
//	}
	
	/*
	public static RestResponse updateInformationalArtifactPayloadOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid, String checksum) throws IOException
	{
		return updateInformationalArtifactOfService(artifactDetails, sdncModifierDetails, serviceUid, artifactUid, checksum, true);
	}

	public static RestResponse updateInformationalArtifactMetadataOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid) throws IOException
	{
		return updateInformationalArtifactOfService(artifactDetails, sdncModifierDetails, serviceUid, artifactUid, calculateChecksum(artifactDetails), false);
	}

	public static RestResponse updateInformationalArtifactOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid, String checksum, boolean isPayloadUpdate) throws IOException
	{
		Config config = Utils.getConfig();
		Map<String, String> headersMap = getHeadersMap(sdncModifierDetails);

		if (isPayloadUpdate){
			headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
		}

		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);

		HttpRequest http = new HttpRequest();

		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE, config.getCatalogBeHost(),config.getCatalogBePort(), serviceUid, artifactUid);
		RestResponse res =  http.httpSendPost(url, jsonBody, headersMap);
		System.out.println("update artifact was finished with response: "+ res.getErrorCode());
		return res;
	}*/
	
	
	
	public static RestResponse updateInformationalArtifactOfServiceByMethod(ArtifactReqDetails artifactReqDetails, String serviceUid, String artifactUid, User sdncModifierDetails, String httpMethod, String checksum) throws IOException 
	{
		Config config = Utils.getConfig();

		Map<String, String> headersMap = getHeadersMap(sdncModifierDetails);
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(artifactReqDetails);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE, config.getCatalogBeHost(),config.getCatalogBePort(), serviceUid, artifactUid);
		RestResponse updateResourceResponse = http.httpSendByMethod(url, httpMethod, userBodyJson, headersMap);
//		System.out.println("update artifact was finished with response: "+ updateResourceResponse.getErrorCode());

		return updateResourceResponse;
	}
	
	
	public static Map<String, String> getHeadersMap(User sdncModifierDetails) {
		Map<String, String> headersMap = new HashMap<String,String>();	
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptJsonHeader);
		
		try{
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		}
		catch(Exception e)
		{
			
		}
		
		return headersMap;
	}

	//***********  RESOURCE ****************
	//add
	public static RestResponse addInformationalArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid) throws IOException{
		return addInformationalArtifactToResource(artifactDetails, sdncModifierDetails, resourceUid, calculateChecksum(artifactDetails));
	}
	
	public static RestResponse explicitAddInformationalArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid) throws IOException{
		Config config = Utils.getConfig();


		String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(),config.getCatalogBePort(), resourceUid);

		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, calculateChecksum(artifactDetails), url);
	}

	
	public static RestResponse addInformationalArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid, String checksum) throws IOException{
		Config config = Utils.getConfig();
			
			if (artifactDetails.getArtifactGroupType()!=null && artifactDetails.getArtifactGroupType().equals(ArtifactGroupTypeEnum.DEPLOYMENT.getType())){
				//YANG_XML and OTHER deployment artifact should be added through this API, not updated
				String artifactType = artifactDetails.getArtifactType();
				if (!(ArtifactTypeEnum.YANG_XML.getType().equals(artifactType) ||
						ArtifactTypeEnum.OTHER.getType().equals(artifactType) ||
						ArtifactTypeEnum.VNF_CATALOG.getType().equals(artifactType) ||
						ArtifactTypeEnum.VF_LICENSE.getType().equals(artifactType) ||
						ArtifactTypeEnum.VENDOR_LICENSE.getType().equals(artifactType) ||
						ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType().equals(artifactType) ||
						ArtifactTypeEnum.MODEL_QUERY_SPEC.getType().equals(artifactType) ||
						ArtifactTypeEnum.APPC_CONFIG.getType().equals(artifactType))){
					//return updateInformationalArtifactToResource(artifactDetails, sdncModifierDetails, resourceUid);
				}
			}
		String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(),config.getCatalogBePort(), resourceUid);
			
		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, checksum, url);
	}
	//update
	public static RestResponse updateInformationalArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid) throws IOException{
		return updateInformationalArtifactToResource(artifactDetails, sdncModifierDetails, resourceUid, calculateChecksum(artifactDetails));
	}

	public static RestResponse updateInformationalArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid, String checksum) throws IOException {
		Config config = Utils.getConfig();
		if (artifactDetails.getArtifactGroupType()!=null && artifactDetails.getArtifactGroupType().equals("DEPLOYMENT")){
			RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceUid );
			Resource resourceRespJavaObject = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
			Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactDetails.getArtifactLabel());
			artifactDetails.setUniqueId(artifactDefinition.getUniqueId());
			artifactDetails.setArtifactLabel(artifactDefinition.getArtifactLabel());
			
		}
		
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid, artifactDetails.getUniqueId());

		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, calculateChecksum(artifactDetails), url);
	}
	
	public static RestResponse uploadArtifactToPlaceholderOnResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid, String placeHolderLabel) throws IOException {
		Config config = Utils.getConfig();
		if (artifactDetails.getArtifactLabel() != null && !artifactDetails.getArtifactLabel().isEmpty()){
			RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceUid );
			Resource resourceRespJavaObject = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
			Map<String, ArtifactDefinition> deploymentArtifacts = resourceRespJavaObject.getDeploymentArtifacts();
			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactDetails.getArtifactLabel());
			AssertJUnit.assertNotNull(artifactDefinition);
			artifactDetails.setUniqueId(artifactDefinition.getUniqueId());
			artifactDetails.setArtifactLabel(artifactDefinition.getArtifactLabel());
			
		}
		
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid, artifactDetails.getUniqueId());

		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, calculateChecksum(artifactDetails), url);
	}
	
	public static RestResponse updateArtifactToResourceInstance(ArtifactDefinition artifactDefinition, User sdncModifierDetails, String resourceInstanceId, String serviceId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_INSTANCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId, resourceInstanceId, artifactDefinition.getUniqueId());
		return updateDeploymentArtifact(artifactDefinition, sdncModifierDetails, url);
	}
	
	public static RestResponse updateDeploymentArtifactToResource(ArtifactDefinition artifact, User sdncModifierDetails, String resourceUid) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid, artifact.getUniqueId());
	
		return updateDeploymentArtifact(artifact, sdncModifierDetails, url);
	}
	public static RestResponse updateDeploymentArtifactToResource(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceUid) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid, artifactDetails.getUniqueId());
	
		return updateDeploymentArtifact(artifactDetails, sdncModifierDetails, url);
	}


	public static RestResponse updateDeploymentArtifactToRI(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceInstanceId, String serviceId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_INSTANCE_HEAT_ENV_PARAMS, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId, resourceInstanceId, artifactDetails.getUniqueId());
		return updateDeploymentArtifact(artifactDetails, sdncModifierDetails, url);
	}
	public static RestResponse updateDeploymentArtifactToRI(ArtifactDefinition artifactDetails, User sdncModifierDetails, String resourceInstanceId, String serviceId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_INSTANCE_HEAT_ENV_PARAMS, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId, resourceInstanceId, artifactDetails.getUniqueId());
		return updateDeploymentArtifact(artifactDetails, sdncModifierDetails, url);
	}
	
	//delete
	public static RestResponse deleteArtifactFromResourceInstance (ArtifactDefinition artifactDefinition, User sdncModifierDetails, String resourceUid, String serviceId) throws IOException{
		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_RESOURCE_INSTANCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId, resourceUid, artifactDefinition.getUniqueId());
		return sendDelete(url, sdncModifierDetails.getUserId());		
	}
	
	public static RestResponse deleteInformationalArtifactFromResource(String resourceUid, ArtifactReqDetails artifactDetails, User sdncModifierDetails) throws IOException{
		return deleteInformationalArtifactFromResource( resourceUid, artifactDetails.getUniqueId(),  sdncModifierDetails);
	}
	
	public static RestResponse deleteInformationalArtifactFromResource( String resourceUid, String artifactId, User sdncModifierDetails) throws IOException{
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceUid, artifactId);
		return sendDelete(url, sdncModifierDetails.getUserId());
	}
	
	public static RestResponse deleteServiceApiArtifact(ArtifactReqDetails artifactDetails, String serviceUniqueId, User user) throws Exception
	{
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_DELETE_SERVICE_API_ARTIFACT, config.getCatalogBeHost(),config.getCatalogBePort(), serviceUniqueId, artifactDetails.getUniqueId());
		RestResponse res =  sendDelete(url, user.getUserId());
		logger.debug("Deleting api artifact was finished with response: {}",res.getErrorCode());
		logger.debug("Response body: {}",res.getResponseMessage());
		return res;
	}
	
	//*************** RESOURCE INSTANCE **************
	/**
	 * Add DCAE artifacts to resource instance.
	 * @param artifactDetails
	 * @param sdncModifierDetails
	 * @param resourceInstanceId
	 * @param serviceId
	 * @return
	 * @throws IOException
	 */
	public static RestResponse addArtifactToResourceInstance(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String resourceInstanceId, String serviceId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.ADD_RESOURCE_INSTANCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId,resourceInstanceId, artifactDetails.getUniqueId());
		return addArtifactToInstance(artifactDetails, sdncModifierDetails, calculateChecksum(artifactDetails), url);
	}
	
	//*************** COMPONENT **************
	
	public static RestResponse uploadDeploymentArtifact(ArtifactReqDetails artifactDetails, Component component, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();
		Map<String, String> additionalHeaders = null;
		String checksum = ResponseParser.calculateMD5Header(artifactDetails);
		additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
		
		ComponentTypeEnum componentType = component.getComponentType();
		
		String url = null;
				
		switch (componentType){

		case RESOURCE:
		{
			url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE, config.getCatalogBeHost(),config.getCatalogBePort(), component.getUniqueId(), artifactDetails.getUniqueId());
			
			break;
		}
		case SERVICE: {
			
			break;
		}
		
		case PRODUCT: {
			
			break;
		}
		
		default: {//dummy
			assertTrue("failed on enum selection", false);
			
			break;
		}
		}
		
		
		

		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
//		System.out.println("ArtifactDetails: "+ jsonBody);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), acceptHeaderData, additionalHeaders);
		if (res.getErrorCode() == STATUS_CODE_SUCCESS) {
			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
		}
//		System.out.println("Add artifact was finished with response: "+ res.getErrorCode());
		return res;
	}
	
	public static RestResponse uploadArtifact(ArtifactReqDetails artifactDetails, Component component, User sdncModifierDetails) throws IOException {
		Config config = Utils.getConfig();
		List<String> placeHolderlst = Utils.getListOfResPlaceHoldersDepArtTypes();
		Map<String, String> additionalHeaders = null;
		String checksum = null;	
		String url= null;
//
//		
//		if (artifactDetails.getArtifactGroupType() != null
//				&& artifactDetails.getArtifactGroupType().equals("DEPLOYMENT")
//				&& placeHolderlst.contains(artifactDetails.getArtifactType())) {
//			Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
//			ArtifactDefinition artifactDefinition = deploymentArtifacts.get(artifactDetails.getArtifactLabel());
//			artifactDetails.setUniqueId(artifactDefinition.getUniqueId());
//			artifactDetails.setArtifactLabel(artifactDefinition.getArtifactLabel());
//			checksum = ResponseParser.calculateMD5Header(artifactDetails);
//			additionalHeaders = new HashMap<String, String>();
//			additionalHeaders.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
//			url = String.format(Urls.UPDATE_ARTIFACT_OF_COMPONENT, config.getCatalogBeHost(),
//					config.getCatalogBePort(), ComponentTypeEnum.findParamByType(component.getComponentType()),
//					component.getUniqueId(), artifactDetails.getUniqueId());
//		}
//
//		else {
			checksum = ResponseParser.calculateMD5Header(artifactDetails);
			additionalHeaders = new HashMap<String, String>();
			additionalHeaders.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
			url = String.format(Urls.UPLOAD_DELETE_ARTIFACT_OF_COMPONENT, config.getCatalogBeHost(),
					config.getCatalogBePort(), ComponentTypeEnum.findParamByType(component.getComponentType()),
					component.getUniqueId(), artifactDetails.getUniqueId());
//		}
		
		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
//		System.out.println("ArtifactDetails: "+ jsonBody);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), acceptHeaderData, additionalHeaders);
		if (res.getErrorCode() == STATUS_CODE_SUCCESS) {
			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
		}
//		System.out.println("Add artifact was finished with response: "+ res.getErrorCode());
		return res;
	}
	
	

	
	//*************** PRIVATE **************
	private static RestResponse deleteInformationalArtifact(User sdncModifierDetails, String url) throws IOException {
		Map<String, String> additionalHeaders = null;

			additionalHeaders = new HashMap<String, String>();
		
		
		additionalHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		additionalHeaders.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		
		return sendDelete(url, sdncModifierDetails.getUserId(), additionalHeaders);

//		Gson gson = new Gson();
////		System.out.println("ArtifactDetails: "+ jsonBody);
//		String jsonBody = gson.toJson(artifactDetails);
//
//		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), acceptHeaderData, additionalHeaders);
//		if ((res.getErrorCode() == STATUS_CODE_SUCCESS) || (res.getErrorCode() == STATUS_CODE_CREATED)) {
//			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
//		}
////		System.out.println("Add artifact was finished with response: "+ res.getErrorCode());
//		return res;
	}
	
	private static RestResponse updateInformationalArtifact(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String checksum, String url) throws IOException {
		return uploadInformationalArtifact(artifactDetails, sdncModifierDetails, checksum, url);
	}
	
	private static RestResponse uploadInformationalArtifact(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String checksum, String url) throws IOException {
		Map<String, String> additionalHeaders = null;
		if (checksum != null && !checksum.isEmpty()) {
			additionalHeaders = new HashMap<String, String>();
			additionalHeaders.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
		}
		
		additionalHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		additionalHeaders.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		Gson gson = new Gson();
//		System.out.println("ArtifactDetails: "+ jsonBody);
		String jsonBody = gson.toJson(artifactDetails);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), acceptHeaderData, additionalHeaders);
		if ((res.getErrorCode() == STATUS_CODE_SUCCESS) || (res.getErrorCode() == STATUS_CODE_CREATED)) {
			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
		}
//		System.out.println("Add artifact was finished with response: "+ res.getErrorCode());
		return res;
	}
	
	private static RestResponse addArtifactToInstance(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String checksum, String url) throws IOException {
		Map<String, String> additionalHeaders = null;
		additionalHeaders = new HashMap<String, String>();
		if (checksum != null && !checksum.isEmpty()) {
			additionalHeaders = new HashMap<String, String>();
			additionalHeaders.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
		}
		additionalHeaders.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json, text/plain, */*");
		additionalHeaders.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json;charset=UTF-8");

		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), "application/json, text/plain, */*", additionalHeaders);
		if (res.getErrorCode() == STATUS_CODE_SUCCESS) {
			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
		}
		return res;
	}
	
	private static RestResponse updateDeploymentArtifact(ArtifactDefinition artifactDefinition, User sdncModifierDetails, String url) throws IOException {
		Map<String, String> additionalHeaders = null;
		additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json, text/plain, */*");
		additionalHeaders.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json;charset=UTF-8");
		
		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDefinition);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), "application/json, text/plain, */*", additionalHeaders);
		return res;
	}
	
	private static RestResponse updateDeploymentArtifact(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String url) throws IOException {
		Map<String, String> additionalHeaders = null;
		
			additionalHeaders = new HashMap<String, String>();
			additionalHeaders.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json, text/plain, */*");
			additionalHeaders.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json;charset=UTF-8");
//			additionalHeaders.put(HttpHeaderEnum..getValue(), "application/json;charset=UTF-8");
		

		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
//		System.out.println("ArtifactDetails: "+ jsonBody);

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), "application/json, text/plain, */*", additionalHeaders);
		if (res.getErrorCode() == STATUS_CODE_SUCCESS) {
			artifactDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
		}
//		System.out.println("Add artifact was finished with response: "+ res.getErrorCode());
		return res;
	}
	
	private static RestResponse downloadArtifact(String url, User user, Map<String, String> addionalHeaders,boolean addEcompHeader) throws IOException
	{	
		if(addEcompHeader){
			addionalHeaders.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), ecomp);
		}
		return downloadArtifact(url, user, addionalHeaders, acceptOctetStream);
	}
	
	private static RestResponse downloadArtifact(String url, User user, Map<String, String> addionalHeaders, String accept) throws IOException
	{
		addionalHeaders.put(HttpHeaderEnum.ACCEPT.getValue(), accept);
		
		RestResponse res =  sendGet(url, user.getUserId(), addionalHeaders);
//		System.out.println("download artifact was finished with response: "+ res.getErrorCode());
//		System.out.println("response is: " + res.getResponse());
		return res;
	}
	
	private static Map<String,Map<String,Object>> getArtifactsListFromResponse(String jsonResponse, String fieldOfArtifactList){
		JSONObject object = (JSONObject)JSONValue.parse(jsonResponse);
		Map<String,Map<String,Object>> map = (Map<String,Map<String,Object>>)object.get(fieldOfArtifactList);
		return map;
	}

	public static String calculateChecksum(ArtifactReqDetails artifactDetails) {
		String checksum = null;
		if (artifactDetails.getPayload() != null) {
			checksum = ResponseParser.calculateMD5Header(artifactDetails);
		}
		return checksum;
	}
	
	public static String encodeUrlForDownload(String url){

		return url.replaceAll(" ", "%20");
	}
	
	public static String getPartialUrlByArtifactName(ServiceReqDetails serviceDetails,String serviceVersion ,String artifactName){
		return encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL, ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceVersion, artifactName));
	}
	
	public static String getUniqueIdOfArtifact(RestResponse createResponse, String artifactField, String requieredArtifactLabel) throws Exception
	{
		Map<String, Object> artifact = getArtifactFromRestResponse(createResponse, artifactField, requieredArtifactLabel);
		assertNotNull(artifact);
		return artifact.get("uniqueId").toString();
	}
	
	public static Map<String, Object> getArtifactFromRestResponse(RestResponse response, String artifactField, String requieredArtifactLabel)
	{
		Map<String, Map<String, Object>> map = getArtifactsListFromResponse(response.getResponse(), artifactField);
		return map.get(requieredArtifactLabel);
	}
	

	
	public static RestResponse updateInformationalArtifactPayloadOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid) throws IOException
	{
		return updateInformationalArtifactPayloadOfService(artifactDetails, sdncModifierDetails, serviceUid, artifactUid, calculateMD5Header(artifactDetails));
	}
	
	private static RestResponse updateInformationalArtifactPayloadOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid, String checksum) throws IOException
	{
		return updateInformationalArtifactOfService(artifactDetails, sdncModifierDetails, serviceUid, artifactUid, checksum, true);
	}
	
	private static RestResponse updateInformationalArtifactOfService(ArtifactReqDetails artifactDetails, User sdncModifierDetails, String serviceUid, String artifactUid, String checksum, boolean isPayloadUpdate) throws IOException
	{
		Config config = Utils.getConfig();
		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails.getUserId());

		if (isPayloadUpdate){
			headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), checksum);
		}

		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);

		HttpRequest http = new HttpRequest();

		String url = String.format(Urls.UPDATE_OR_DELETE_ARTIFACT_OF_SERVICE, config.getCatalogBeHost(),config.getCatalogBePort(), serviceUid, artifactUid);
		RestResponse res =  http.httpSendPost(url, jsonBody, headersMap);
//		System.out.println("update artifact was finished with response: "+ res.getErrorCode());
		return res;
	}
	
	public static String calculateMD5Header(ArtifactReqDetails artifactDetails)
	{
		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
		// calculate MD5 for json body
		return calculateMD5(jsonBody);

	}
	
	public static String calculateMD5 (String data){
		String calculatedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
		// encode base-64 result
		byte[] encodeBase64 = Base64.encodeBase64(calculatedMd5.getBytes());
		String encodeBase64Str = new String(encodeBase64);
		return encodeBase64Str;

	}

	
	

}
