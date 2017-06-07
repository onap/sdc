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

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDistributionStatus;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;

//
//
//
// import static org.testng.AssertJUnit.assertTrue;
// import static org.testng.AssertJUnit.assertEquals;
// import static org.testng.AssertJUnit.assertNotNull;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;
//
// import org.apache.log4j.Logger;
//
// import org.openecomp.sdc.be.model.ArtifactDefinition;
// import org.openecomp.sdc.ci.tests.api.Urls;
// import org.openecomp.sdc.ci.tests.config.Config;
// import
// org.openecomp.sdc.ci.tests.executeOnUGN.distributionClient.ClientConfiguration;
// import org.openecomp.sdc.ci.tests.http.HttpHeaderEnum;
// import org.openecomp.sdc.ci.tests.http.RestResponse;
// import org.openecomp.sdc.ci.tests.run.StartTest;
// import com.google.gson.Gson;
//
 public class DistributionUtils {
	 
	 final static String serviceDistributionSuffix = "/asdc/v1/catalog/services/";

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
//		format	"/asdc/v1/catalog/services/" + service.getSystemName() + "/" + service.getVersion() + "/artifacts/AAI-" + service.getName() + "-service-1.xml"
		return serviceDistributionSuffix + service.getSystemName() + "/" + service.getVersion() + "/artifacts/" + artifactName;
	}

	public static String buildResourceInstanceDeploymentUrl(Service service, String artifactName, String artifactUUID){
		
//			/asdc/v1/catalog/services/Servicefordistribution/1.0  /resourceInstances/nestedfrommarina2   /artifacts/FEAdd_On_Module_vProbeLauncher.yaml
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
	 
	 
 }
