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

import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.utils.DistributionUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

public class DistributionValidationUtils {

	public static Map<String, String> verifyDistributedArtifactDownloadUsingDB(String distributionID, Map<String, String> expectedArtifactsMapOfDistributedService, List<String> distributionStatusEnumList) throws Exception {
		
		String action = "DStatus";
		int timer = 0;
		int timeWaitPerArtifcat = 3;
		if(expectedArtifactsMapOfDistributedService.size() != 0){
			timer = (expectedArtifactsMapOfDistributedService.size()/10*15 + expectedArtifactsMapOfDistributedService.size() * timeWaitPerArtifcat * distributionStatusEnumList.size() + 30) * 1000 ;
		}
		for (String distributionStatusList : distributionStatusEnumList){
			for (Entry<String, String> url : expectedArtifactsMapOfDistributedService.entrySet()){
				Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
		        body.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID, distributionID);
		        body.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, url.getValue());
		        body.put(AuditingFieldsKeysEnum.AUDIT_STATUS, distributionStatusList);
		        Map<String, String> actualAuditRecord = new HashMap<String, String>();
				actualAuditRecord = AuditValidationUtils.retrieveAuditMessagesByPattern(action, body, true);
				while (timer != 0) {
					if(actualAuditRecord.size() == 0 ){
						Thread.sleep(1000);
						actualAuditRecord = AuditValidationUtils.retrieveAuditMessagesByPattern(action, body, true);
						timer-=1000;
						if(timer == 0 && actualAuditRecord.size() == 0){
							assertNotNull("audit record did not found in DB for artifact url: " + url.getValue(), null);
						}
					}else{
						timer = timer - timeWaitPerArtifcat * 1000;
						break;
					}
					
				}
			}
		}
		return null;
	}

	public static void validateDistributedArtifactsByAudit(Service service, List<String> distributionStatusList) throws Exception, IOException, ParseException {
		String distributionID;
		AtomicOperationUtils.distributeService(service, true);
		distributionID = DistributionUtils.getLatestServiceDistributionObject(service).getDistributionID();
		if(distributionID != null){
			Map<String, String> expectedArtifactsMapOfDistributedService = DistributionUtils.getArtifactsMapOfDistributedService(service);
			DistributionValidationUtils.verifyDistributedArtifactDownloadUsingDB(distributionID, expectedArtifactsMapOfDistributedService, distributionStatusList);
		}
		else{
			assertNotNull("distributionID is null", distributionID);
		}
	}
}
