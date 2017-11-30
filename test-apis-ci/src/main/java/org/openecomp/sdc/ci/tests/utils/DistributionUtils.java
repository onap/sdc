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

package org.openecomp.sdc.ci.tests.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.DistributionMonitorObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDistributionStatus;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
//import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotificationEnum;

import com.clearspring.analytics.util.Pair;
import com.google.gson.Gson;

 public class DistributionUtils extends BaseRestUtils{
	 
	 final static String serviceDistributionSuffix = "/sdc/v1/catalog/services/";

	 public static ServiceDistributionStatus getLatestServiceDistributionObject(Service service) throws IOException, ParseException {
			ServiceDistributionStatus serviceDistributionStatus = null;
			RestResponse distributionServiceList = ServiceRestUtils.getDistributionServiceList(service, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
			Map<Long, ServiceDistributionStatus> serviveDistributionStatusMap = ResponseParser.convertServiceDistributionStatusToObject(distributionServiceList.getResponse());
			if(serviveDistributionStatusMap.size() != 0){
				serviceDistributionStatus = getLatestServiceDistributionObjectFromMap(serviveDistributionStatusMap);
				return serviceDistributionStatus;
			}
			
			return null;
	}

	public static ServiceDistributionStatus getLatestServiceDistributionObjectFromMap(Map<Long, ServiceDistributionStatus> serviceDistributionStatusMap) {
		
		ServiceDistributionStatus serviceDistributionStatus = null;
		if (serviceDistributionStatusMap.size() == 1 ){
			for (Entry<Long, ServiceDistributionStatus> entry : serviceDistributionStatusMap.entrySet()) {
				return entry.getValue();
			}
		}
		else{
			serviceDistributionStatus = getFilteredServiceDistributionObject(serviceDistributionStatusMap);
		}
		
		return serviceDistributionStatus;
	}

	private static ServiceDistributionStatus getFilteredServiceDistributionObject(Map<Long, ServiceDistributionStatus> serviceDistributionStatusMap) {
		
		List<Long> list = new ArrayList<Long>();
		list.addAll(serviceDistributionStatusMap.keySet());
		Collections.sort(list);
		return serviceDistributionStatusMap.get(list.get(list.size() - 1));
	}
	
	public static Map<String, String> getArtifactsMapOfDistributedService(Service service) throws Exception{
		
		Map<String, String> expectedDistributionArtifactMap = new HashMap<String, String>();
		expectedDistributionArtifactMap = addServiceDeploymentArtifactToMap(service, expectedDistributionArtifactMap);
		expectedDistributionArtifactMap = addComponentInstancesDeploymentArtifactToMap(service, expectedDistributionArtifactMap);
		
		return expectedDistributionArtifactMap;
	}
	
	
	public static Map<String, String> addServiceDeploymentArtifactToMap(Service service, Map<String, String> distributionArtifactMap){
		
		Map<String, ArtifactDefinition> deploymentArtifacts = service.getDeploymentArtifacts();
		if (deploymentArtifacts != null && deploymentArtifacts.size() > 0){
			for(Entry<String, ArtifactDefinition> artifact : deploymentArtifacts.entrySet()){
				String url = buildServiceDeploymentUrl(service, artifact.getValue().getArtifactName(), artifact.getValue().getArtifactUUID());
				distributionArtifactMap.put(artifact.getKey(), url);
			}
		}
		
		return distributionArtifactMap;
	}
	
	private static String buildServiceDeploymentUrl(Service service, String artifactName, String artifactUUID) {
//		format	"/sdc/v1/catalog/services/" + service.getSystemName() + "/" + service.getVersion() + "/artifacts/AAI-" + service.getName() + "-service-1.xml"
		return serviceDistributionSuffix + service.getSystemName() + "/" + service.getVersion() + "/artifacts/" + artifactName;
	}

	public static String buildResourceInstanceDeploymentUrl(Service service, String artifactName, String artifactUUID){
		
//			/sdc/v1/catalog/services/Servicefordistribution/1.0  /resourceInstances/nestedfrommarina2   /artifacts/FEAdd_On_Module_vProbeLauncher.yaml
		String resourceInstanceNormalizedName = getResourceInstanceNormalizeName(service, artifactName, artifactUUID );
		return serviceDistributionSuffix + service.getSystemName() + "/" + service.getVersion() + "/resourceInstances/" + resourceInstanceNormalizedName  +"/artifacts/" + artifactName;
	}
	
	public static String getResourceInstanceNormalizeName(Service service, String artifactName, String artifactUUID) {
		for (ComponentInstance componentInstance : service.getComponentInstances()){
			for(String key : componentInstance.getDeploymentArtifacts().keySet()){
				if(componentInstance.getDeploymentArtifacts().get(key).getArtifactUUID().equals(artifactUUID)) {
					return componentInstance.getNormalizedName();
				}
			}
		}
		return null;
	}

	public static Map<String, String> addComponentInstancesDeploymentArtifactToMap(Service service, Map<String, String> distributionArtifactMap){
//			TODO Andrey create correct method to build RI url
		if(service.getComponentInstances() != null && service.getComponentInstances().size() != 0){
		for(ComponentInstance componentInstance : service.getComponentInstances()){
			if (componentInstance.getDeploymentArtifacts() != null && componentInstance.getDeploymentArtifacts().size() != 0){
				for(Entry<String, ArtifactDefinition> artifact : componentInstance.getDeploymentArtifacts().entrySet()){
					String url = buildResourceInstanceDeploymentUrl(service, artifact.getValue().getArtifactName(), artifact.getValue().getArtifactUUID());;
					distributionArtifactMap.put(artifact.getKey(), url);
				}
			}
		}
	}
		
		return distributionArtifactMap;
	}
	 
	
	
	public static RestResponse getDistributionStatus(User sdncModifierDetails, String distributionId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DISTRIBUTION_SERVICE_MONITOR, config.getCatalogBeHost(), config.getCatalogBePort(), distributionId);
		return sendGet(url, sdncModifierDetails.getUserId());
		
	}
	
	
	/**
	 * @param response
	 * @return parsed distribution list of DistributionMonitorObject java objects
	 * @throws JSONException
	 */
	public static Map<String, List<DistributionMonitorObject>> getSortedDistributionStatus(RestResponse response) throws JSONException{
		
		ArrayList<DistributionMonitorObject> distributionStatusList = new ArrayList<DistributionMonitorObject>();
		String responseString = response.getResponse();
		JSONObject jObject;
		JSONArray jsonArray = null;
		jObject = new JSONObject(responseString);
		jsonArray = jObject.getJSONArray("distributionStatusList");
		
		Gson gson = new Gson();
		for(int i=0; i<jsonArray.length(); i++){
			String jsonElement = jsonArray.get(i).toString();
			DistributionMonitorObject distributionStatus = gson.fromJson(jsonElement, DistributionMonitorObject.class);
			distributionStatusList.add(distributionStatus);
		}
			Map<String, List<DistributionMonitorObject>> sortedDistributionMapByConsumer = sortDistributionStatusByConsumer(distributionStatusList);
		
		return sortedDistributionMapByConsumer;
	}

	/**
	 * @param distributionStatusList
	 * @return sorted distribution map where key is consumer name and value contains list of corresponded DistributionMonitorObject java object
	 */
	public static Map<String, List<DistributionMonitorObject>> sortDistributionStatusByConsumer(ArrayList<DistributionMonitorObject> distributionStatusList) {
		//		sort distribution status list per consumer
				Map<String, List<DistributionMonitorObject>> distributionStatusMapByConsumer = new HashMap<String, List<DistributionMonitorObject>>();
				for(DistributionMonitorObject distributionListElement : distributionStatusList){
					String key = distributionListElement.getOmfComponentID();
					List<DistributionMonitorObject> list = new ArrayList<>();
					if(distributionStatusMapByConsumer.get(key) != null){
						list = distributionStatusMapByConsumer.get(key);
						list.add(distributionListElement);
						distributionStatusMapByConsumer.put(key, list);
					}else{
						list.add(distributionListElement);
						distributionStatusMapByConsumer.put(key, list);
					}
					
				}
				return distributionStatusMapByConsumer;
	}
	
	
	/**
	 * @param pair
	 * @return consumer Status map: if map is empty - all consumers successes download and deploy the artifacts,
	 * else - return failed consumer status per url 
	 */
	public static Pair<Boolean, Map<String, List<String>>> verifyDistributionStatus(Map<String, List<DistributionMonitorObject>> map){
		
		Map<String, List<String>> consumerStatusMap = new HashMap<>();
		List<Boolean> flag = new ArrayList<>();
		for (Entry<String, List<DistributionMonitorObject>> distributionMonitor : map.entrySet()){
			int notifiedCount = 0, downloadCount = 0, deployCount = 0;
			List<String> failedList = new ArrayList<>();
			List<DistributionMonitorObject> listValue = distributionMonitor.getValue();
			for(DistributionMonitorObject distributionStatus : listValue){
				String status = distributionStatus.getStatus();
				switch (status) {
				case "NOTIFIED": notifiedCount++;
					break;
				case "NOT_NOTIFIED":
				break;
				case "DOWNLOAD_OK": downloadCount++;
				break;
				case "DEPLOY_OK": deployCount++;
				break;
				default:
					failedList.add("Url " + distributionStatus.getUrl() + " failed with status " + distributionStatus.getStatus());
					break;
				}
			}
			if((notifiedCount != downloadCount || notifiedCount != deployCount) && notifiedCount != 0){
				consumerStatusMap.put(distributionMonitor.getKey(), failedList);
				flag.add(false);
			}
			if(notifiedCount == 0){
				flag.add(true);
			}
		}
	
		if(!flag.contains(false)){
			return Pair.create(true, consumerStatusMap);
		}else{
			return Pair.create(false, consumerStatusMap);
		}

	}
	
	
	
 }
