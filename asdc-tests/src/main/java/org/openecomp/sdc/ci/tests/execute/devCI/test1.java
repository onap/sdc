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

package org.openecomp.sdc.ci.tests.execute.devCI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.DistributionNotificationStatusEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.DistributionValidationUtils;
import org.testng.annotations.Test;

import fj.data.Either;

public class test1 extends ComponentBaseTest{

	@Rule 
	public static TestName name = new TestName();

	public test1() {
		super(name, CRUDExternalAPI.class.getName());

	}
	
	@Test()
	public void uploadArtifactOnServiceViaExternalAPI() throws Exception {
		Config config = Utils.getConfig();	
		
		Service service = AtomicOperationUtils.createServiceByCategory(ServiceCategoriesEnum.MOBILITY, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_QUERY_SPEC, service, UserRoleEnum.DESIGNER, true, true);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		if(config.getIsDistributionClientRunning()){
			List<String> distributionStatusList = Arrays.asList(DistributionNotificationStatusEnum.DOWNLOAD_OK.toString(), DistributionNotificationStatusEnum.DEPLOY_OK.toString(), DistributionNotificationStatusEnum.NOTIFIED.toString());
			DistributionValidationUtils.validateDistributedArtifactsByAudit(service, distributionStatusList);
		}
	}

	
	public static Map<String, String> addVNF_ModuleDeploymentArtifactToMap(Service service, Map<String, String> distributionArtifactMap){
		
		
		return distributionArtifactMap;
	}
	
	public Component getComponentInTargetLifeCycleState(String componentType, UserRoleEnum creatorUser, LifeCycleStatesEnum targetLifeCycleState) throws Exception {
		Component component = null;
		
		if(componentType.toLowerCase().equals("vf")) {
			Either<Resource, RestResponse> createdResource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, creatorUser, true);
			component = createdResource.left().value();
			component = AtomicOperationUtils.changeComponentState(component, creatorUser, targetLifeCycleState, true).getLeft();
		} else {
			Either<Service, RestResponse> createdResource = AtomicOperationUtils.createDefaultService(creatorUser, true);
			component = createdResource.left().value();
			component = AtomicOperationUtils.changeComponentState(component, creatorUser, targetLifeCycleState, true).getLeft();
		}
		
		return component;
	}
}
