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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;

public class ArtifactsCorrelationManager {

		private static HashMap<String, LinkedList<HeatMetaFirstLevelDefinition>> vNFArtifactsCorrelationMap = new HashMap<String, LinkedList<HeatMetaFirstLevelDefinition>>();
		private static HashMap<String, Entry<String, LinkedList<HeatMetaFirstLevelDefinition>>> serviceVNFCorrelationMap = new HashMap<String, Entry<String, LinkedList<HeatMetaFirstLevelDefinition>>>();

		public static void addVNFartifactDetails(String vspName,
				LinkedList<HeatMetaFirstLevelDefinition> deploymentArtifacts) {
			
			vNFArtifactsCorrelationMap.put(vspName, deploymentArtifacts);
			
			
		}
		
		public static Entry<String, LinkedList<HeatMetaFirstLevelDefinition>> getVNFartifactDetails(String vnfName){
			
			
			Set<Entry<String, LinkedList<HeatMetaFirstLevelDefinition>>> entrySet = vNFArtifactsCorrelationMap.entrySet();
			for (Entry<String, LinkedList<HeatMetaFirstLevelDefinition>> entry : entrySet) {
				String key = entry.getKey();
				if (key.equals(vnfName)) {
					return entry;
				}
												
			}
			return null;
						
		}
		
		
		public static void addVNFtoServiceArtifactCorrelation(String service, String vnfName){
			
			serviceVNFCorrelationMap.put(service,  getVNFartifactDetails(vnfName));
			
		}
		
		public static Set<Entry<String, Entry<String, LinkedList<HeatMetaFirstLevelDefinition>>>> getServiceArtifactCorrelationMap(String service){
			
			return serviceVNFCorrelationMap.entrySet();
			
		}
		
}

